package in.oormi.apguide;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.sql.SQLException;

public class JournalDBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "journalManager";

    private static final String TABLE_JOURNAL = "journal";
    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "date";
    private static final String KEY_DESC = "desc";

    public JournalDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_JOURNAL_TABLE = "CREATE TABLE " + TABLE_JOURNAL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATE + " TEXT,"
                + KEY_DESC + " TEXT" +")";
        db.execSQL(CREATE_JOURNAL_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOURNAL);
        // Create tables again
        onCreate(db);
    }

    void addData(String jdate, String jdesc) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DATE, jdate);
        values.put(KEY_DESC, jdesc);

        long id = db.insert(TABLE_JOURNAL, null, values);
        values.clear();

        db.close();
    }

    public String getPage(int pageNum) {
        if (pageNum<1) pageNum=1;
        String selectQuery = "SELECT  * FROM " + TABLE_JOURNAL
                + " ORDER BY " + KEY_DATE + " ASC LIMIT 1 OFFSET " + String.valueOf(pageNum-1);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        String pageDesc = "Nothing to see.";
        if (cursor.moveToFirst()) {
            do {
                pageDesc = cursor.getString(2);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pageDesc;
    }

    public void deletePage(int pageNum) {
        if (pageNum<1) pageNum=1;
        String selectQuery = "DELETE FROM " + TABLE_JOURNAL
                 + " WHERE id in (SELECT id FROM " + TABLE_JOURNAL
                + " ORDER BY " + KEY_DATE + " ASC LIMIT 1 OFFSET " + String.valueOf(pageNum-1) + ")";

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(selectQuery);
        db.close();
    }

    public int getPageCount() {
        String countQuery = "SELECT  * FROM " + TABLE_JOURNAL;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int pagecount = cursor.getCount();
        cursor.close();

        return pagecount;
    }

    public void resetDB () throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase ();
        db.delete(TABLE_JOURNAL, null, null);
        db.close ();
    }
}
