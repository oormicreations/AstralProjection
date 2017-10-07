package in.oormi.apguide;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JournalDBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "journalManager";

    private static final String TABLE_JOURNAL = "journal";
    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "date";
    private static final String KEY_DESC = "desc";

    public JournalDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
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
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOURNAL);
        // Create tables again
        onCreate(db);
    }

    // add the new tasks
    void addData(String jdate, String jdesc) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DATE, jdate);
        values.put(KEY_DESC, jdesc);

        // Inserting Row
        long id = db.insert(TABLE_JOURNAL, null, values);
        values.clear();

        db.close();
    }
   /*
    // add the new tasks
    void insertData(int at, GroupInfo task) {
        SQLiteDatabase db = this.getWritableDatabase();

        //update all taskids in tasks
        String selectQuery = "SELECT  * FROM " + TABLE_TASKS + " ORDER BY " + KEY_ID + " ASC";
        Cursor cursor = db.rawQuery(selectQuery, null);
        int idx = 0, ntask = 1;

        if (cursor.moveToFirst()) {
            do {
                idx = Integer.parseInt(cursor.getString(2));
                ntask = Integer.parseInt(cursor.getString(0));
                if (idx>=at){
                    selectQuery = "UPDATE " + TABLE_TASKS + " SET " + KEY_TASKID + " = "
                            + "\"" + String.valueOf(idx + 1) + "\"" + " WHERE " + KEY_ID
                            + " = " + "\"" + String.valueOf(ntask) + "\"";
                    db.execSQL(selectQuery);
                }
                //ntask++;

            } while (cursor.moveToNext());
        }

        //update all taskids in details
        selectQuery = "SELECT  * FROM " + TABLE_DETAILS + " ORDER BY " + KEY_ID + " ASC";
        cursor = db.rawQuery(selectQuery, null);
        idx = 0;
        ntask = 1;

        if (cursor.moveToFirst()) {
            do {
                idx = Integer.parseInt(cursor.getString(3));
                ntask = Integer.parseInt(cursor.getString(0));
                if (idx>=at){
                    selectQuery = "UPDATE " + TABLE_DETAILS + " SET " + KEY_TASKID + " = "
                            + "\"" + String.valueOf(idx + 1) + "\""  + " WHERE " + KEY_ID
                            + " = " + "\"" + String.valueOf(ntask) + "\"";
                    db.execSQL(selectQuery);
                   }
                //ntask++;

            } while (cursor.moveToNext());
        }

        addData(task);
        db.close();
    }
*/
    public String getPage(int idx) {
        String selectQuery = "SELECT  * FROM " + TABLE_JOURNAL
                + " WHERE " + KEY_ID + " = " + String.valueOf(idx);

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
/*
    public int updateTask(GroupInfo task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, task.getTask());
        //return db.update(TABLE_TASKS, values, KEY_ID + " = ?", new String[] { "ID" });
        return db.update(TABLE_TASKS, values,
                KEY_TASKID + String.format(" = %d", task.getId()), null);
    }

    public void insertStep(GroupInfo task, int at) {
        SQLiteDatabase db = this.getWritableDatabase();
        //update all seq in details
        String selectQuery = "SELECT  * FROM " + TABLE_DETAILS + " WHERE "
                + KEY_TASKID + " = " + task.getId();
        Cursor cursor = db.rawQuery(selectQuery, null);
        int seq = 0;
        int nstep = 1;

        //increment all seq after new one
        if (cursor.moveToFirst()) {
            do {
                seq = Integer.parseInt(cursor.getString(4));
                nstep = Integer.parseInt(cursor.getString(0));
                if (seq>=at){
                    selectQuery = "UPDATE " + TABLE_DETAILS + " SET " + KEY_SEQ + " = " + "\""
                            + String.valueOf(seq + 1) + "\""  + " WHERE " + KEY_ID + " = " + "\""
                            + String.valueOf(nstep) + "\"";
                    db.execSQL(selectQuery);
                }
            } while (cursor.moveToNext());
        }

        //add new step
        ContentValues values = new ContentValues();
        values.put(KEY_DET, task.getDetailsList().get(at).getDescription());
        values.put(KEY_DELAY, task.getDetailsList().get(at).getDelay());
        values.put(KEY_SEQ, task.getDetailsList().get(at).getSequence());
        values.put(KEY_TASKID, task.getId());
        db.insert(TABLE_DETAILS, null, values);
    }

    public void deleteStep(GroupInfo task, int at) {
        SQLiteDatabase db = this.getWritableDatabase();
        //remove step
        db.delete(TABLE_DETAILS, KEY_TASKID + " = ? AND " + KEY_SEQ + " = ?",
                new String[] { String.valueOf(task.getId()),
                        String.valueOf(task.getDetailsList().get(at).getSequence()) });

        //update all seq in details
        String selectQuery = "SELECT  * FROM " + TABLE_DETAILS + " WHERE "
                + KEY_TASKID + " = " + task.getId();
        Cursor cursor = db.rawQuery(selectQuery, null);
        int seq = 0;
        int nstep = 1;

        //decrement all seq after deleted one
        if (cursor.moveToFirst()) {
            do {
                seq = Integer.parseInt(cursor.getString(4));
                nstep = Integer.parseInt(cursor.getString(0));
                if (seq>=at){
                    selectQuery = "UPDATE " + TABLE_DETAILS + " SET " + KEY_SEQ + " = " + "\""
                            + String.valueOf(seq - 1) + "\""  + " WHERE " + KEY_ID + " = " + "\""
                            + String.valueOf(nstep) + "\"";
                    db.execSQL(selectQuery);
                }
            } while (cursor.moveToNext());
        }

    }

    public void updateStep(GroupInfo task, int pos) {
        SQLiteDatabase db = this.getWritableDatabase();
        //ContentValues values = new ContentValues();
        String tname = task.getDetailsList().get(pos).getDescription();
        String tdelay = task.getDetailsList().get(pos).getDelay();
        int tid = task.getId();
        int tseq = task.getDetailsList().get(pos).getSequence();

        //db.update(TABLE_DETAILS, values, KEY_TASKID + String.format(" = %d", task.getId()), null);
        String selectQuery = "UPDATE " + TABLE_DETAILS + " SET " + KEY_DET + " = "
                + "\"" + tname + "\""  + " WHERE " + KEY_TASKID + " = " + tid + " AND "
                + KEY_SEQ + " = " + tseq;
        db.execSQL(selectQuery);
        selectQuery = "UPDATE " + TABLE_DETAILS + " SET " + KEY_DELAY + " = " + "\""
                + tdelay + "\""  + " WHERE " + KEY_TASKID + " = " + tid + " AND "
                + KEY_SEQ + " = " + tseq;
        db.execSQL(selectQuery);
    }

    public void deleteTask(int at, GroupInfo task) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DETAILS, KEY_TASKID + " = ?", new String[]{ String.valueOf(task.getId()) });
        db.delete(TABLE_TASKS, KEY_TASKID + " = ?", new String[] { String.valueOf(task.getId()) });

        //update all taskids in tasks
        String selectQuery = "SELECT  * FROM " + TABLE_TASKS + " ORDER BY " + KEY_ID + " ASC";
        Cursor cursor = db.rawQuery(selectQuery, null);
        int idx = 0, ntask = 1;

        if (cursor.moveToFirst()) {
            do {
                idx = Integer.parseInt(cursor.getString(2));
                ntask = Integer.parseInt(cursor.getString(0));
                if (idx>=at){
                    selectQuery = "UPDATE " + TABLE_TASKS + " SET " + KEY_TASKID + " = " + "\""
                            + String.valueOf(idx - 1) + "\"" + " WHERE " + KEY_ID + " = " + "\""
                            + String.valueOf(ntask) + "\"";
                    db.execSQL(selectQuery);
                }

            } while (cursor.moveToNext());
        }

        //update all taskids in details
        selectQuery = "SELECT  * FROM " + TABLE_DETAILS + " ORDER BY " + KEY_ID + " ASC";
        cursor = db.rawQuery(selectQuery, null);
        idx = 0;
        ntask = 1;

        if (cursor.moveToFirst()) {
            do {
                idx = Integer.parseInt(cursor.getString(3));
                ntask = Integer.parseInt(cursor.getString(0));
                if (idx>=at){
                    selectQuery = "UPDATE " + TABLE_DETAILS + " SET " + KEY_TASKID + " = " + "\""
                            + String.valueOf(idx - 1) + "\""  + " WHERE " + KEY_ID + " = " + "\""
                            + String.valueOf(ntask) + "\"";
                    db.execSQL(selectQuery);
                }

            } while (cursor.moveToNext());
        }

        db.close();
    }
*/
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
