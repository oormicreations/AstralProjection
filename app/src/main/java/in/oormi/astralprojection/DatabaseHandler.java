package in.oormi.astralprojection;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "tasksManager";
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_DETAILS = "details";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DET = "detail";
    private static final String KEY_DELAY = "delay";
    private static final String KEY_TASKID = "taskid";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT" + ")";
        db.execSQL(CREATE_TASKS_TABLE);

        String CREATE_DETAILS_TABLE = "CREATE TABLE " + TABLE_DETAILS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DET + " TEXT,"
                + KEY_DELAY + " TEXT," + KEY_TASKID + " INTEGER" +")";
        db.execSQL(CREATE_DETAILS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETAILS);

        // Create tables again
        onCreate(db);
    }

    // code to add the new tasks
    void addData(GroupInfo tasks) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, tasks.getTask());

        // Inserting Row
        long id = db.insert(TABLE_TASKS, null, values);
        values.clear();

        ArrayList<ChildInfo> cinfolist = tasks.getDetailsList();

        for (int n = 0; n < cinfolist.size(); n ++) {
            ChildInfo cinfo = cinfolist.get(n);
            String cname = cinfo.getDescription();
            String cdelay = cinfo.getDelay();
            values.put(KEY_DET, cname);
            //db.insert(TABLE_DETAILS, null, values);
            values.put(KEY_DELAY, cdelay);
            //db.insert(TABLE_DETAILS, null, values);
            values.put(KEY_TASKID, (int)id);
            db.insert(TABLE_DETAILS, null, values);
        }

        db.close();
    }

    // code to get the single contact
    GroupInfo getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS, new String[] { KEY_ID,
                        KEY_NAME }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        GroupInfo contact = new GroupInfo();
        return contact;
    }

    public List<GroupInfo> getAllTasks() {
        List<GroupInfo> taskList = new ArrayList<GroupInfo>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TASKS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                GroupInfo task = new GroupInfo();
                task.setTask(cursor.getString(1));
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        for (int ntask = 0; ntask < taskList.size(); ntask++) {
            selectQuery = "SELECT  * FROM " + TABLE_DETAILS + " WHERE "
                    + KEY_TASKID + " = " + String.valueOf(ntask+1);
            cursor = db.rawQuery(selectQuery, null);
            ArrayList<ChildInfo> childInfoArrayList = new ArrayList<ChildInfo>();

            if (cursor.moveToFirst()) {
                do {
                    ChildInfo cinfo = new ChildInfo();
                    cinfo.setDescription(cursor.getString(1));
                    cinfo.setDelay(cursor.getString(2));
                    childInfoArrayList.add(cinfo);
                } while (cursor.moveToNext());
            }
            taskList.get(ntask).setDetailsList(childInfoArrayList);
        }
        cursor.close();
        return taskList;
    }

    // code to update the single contact
    public int updateContact(GroupInfo contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getTask());
       // values.put(KEY_PH_NO, contact.getDescription());

        // updating row
        return db.update(TABLE_TASKS, values, KEY_ID + " = ?",
                new String[] { "ID" });
    }

    // Deleting single contact
    public void deleteContact(GroupInfo contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, KEY_ID + " = ?",
                new String[] { "ID" });
        db.close();
    }

    public int getTaskCount() {
        String countQuery = "SELECT  * FROM " + TABLE_TASKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int tcount = cursor.getCount();
        cursor.close();

        return tcount;
    }

    public void resetDB () throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase ();
       // db.execSQL("DELETE FROM " + TABLE_TASKS);
        //db.execSQL("DELETE FROM " + TABLE_DETAILS);
        db.delete(TABLE_TASKS, null, null);
        db.delete(TABLE_DETAILS, null, null);
        db.close ();
        //this.onCreate (db);
    }
}
