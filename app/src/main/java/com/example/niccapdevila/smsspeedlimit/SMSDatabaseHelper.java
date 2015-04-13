package com.example.niccapdevila.smsspeedlimit;

/**
 * Created by niccapdevila on 4/7/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niccapdevila on 4/6/15.
 */
public class SMSDatabaseHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "SpeedLimitSMS.db";

    // Table name
    private static final String TABLE_NAME = "SpeedLimitSMS_table";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_SPEED = "speed";
    private static final String COLUMN_ADDRESS = "address";

    public SMSDatabaseHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = getWritableDatabase();
        db.close();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + "  varchar(255),"
                + COLUMN_DATE + " varchar(255),"
                + COLUMN_SPEED + " varchar(255),"
                + COLUMN_ADDRESS + " varchar(255))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // simple database upgrade operation:
        // 1) drop the old table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // 2) create a new database
        onCreate(db);

    }

    public SMSInfo getSMSInfo(String id, String date, String address) {
        SMSInfo smsInfo = null;
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = COLUMN_ID + " = ? AND " + COLUMN_DATE + " = ? AND " + COLUMN_ADDRESS + " = ?";
        String[] whereArgs = new String[]{id,date, address};

        Cursor cursor = db.query(TABLE_NAME, null, whereClause, whereArgs, null, null, null);
        if (cursor != null) {
            if(cursor.moveToFirst()) {
                // add bookInfos to the list
                smsInfo = new SMSInfo();
                smsInfo.setID(cursor.getString(0));
                smsInfo.setDate(cursor.getString(1));
                smsInfo.setSpeed(cursor.getString(2));
                smsInfo.setAddress(cursor.getString(3));
            }
            cursor.close();
        }
        db.close();

        return smsInfo;
    }


    public boolean updateSMS(SMSInfo smsInfo){
        if (smsInfo != null) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_ID, smsInfo.getID());
            contentValues.put(COLUMN_DATE, smsInfo.getDate());
            contentValues.put(COLUMN_SPEED, smsInfo.getSpeed());
            contentValues.put(COLUMN_ADDRESS, smsInfo.getAddress());

            String whereClause = COLUMN_ID + " = ? AND " + COLUMN_DATE + " = ? AND " + COLUMN_ADDRESS + " = ?";
            String[] whereArgs = new String[]{smsInfo.getID() ,smsInfo.getDate(), smsInfo.getAddress()};

            if (db.update(TABLE_NAME, contentValues, whereClause, whereArgs) == 0) {
                db.close();
                return false;
            }
            db.close();
        }
        return true;

    }

    /**
     * Add a new bookInfo
     */
    private void addSMS(SQLiteDatabase db, SMSInfo smsInfo) {
        // prepare values
        ContentValues values = new ContentValues();


        values.put(COLUMN_ID, smsInfo.getID());
        values.put(COLUMN_DATE, smsInfo.getDate());
        values.put(COLUMN_SPEED, smsInfo.getSpeed());
        values.put(COLUMN_ADDRESS, smsInfo.getAddress());

        // add the row
        db.insert(TABLE_NAME, null, values);
    }

    public void addSMS(SMSInfo smsInfo) {
        if (smsInfo != null) {
            // obtain a readable database
            SQLiteDatabase db = getWritableDatabase();
            addSMS(db, smsInfo);
            // close the database connection
            db.close();
        }
    }


    private void deleteSMS(SQLiteDatabase db, SMSInfo smsInfo){

            if (smsInfo != null) {
                String whereClause = COLUMN_ID + " = ? AND " + COLUMN_DATE + " = ? AND " + COLUMN_ADDRESS + " = ?";
                String[] whereArgs = new String[]{smsInfo.getID() ,smsInfo.getDate(), smsInfo.getAddress()};
                db.delete(TABLE_NAME,whereClause,whereArgs);
            }


    }

    public void deleteSMSs(List<SMSInfo> smsInfos) {
        if (smsInfos != null && smsInfos.size() > 0) {
            // obtain a readable database
            SQLiteDatabase db = getWritableDatabase();

            for (SMSInfo item : smsInfos) {
                deleteSMS(db, item);
            }

            // close the database connection
            db.close();
        }
    }

    public void DelateAllSMSs() {

        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }



    /**
     * retrieve all bookInfos from the database
     */
    public List<SMSInfo> getAllSMSInfos() {
        // initialize the list
        List<SMSInfo> smsInfos = new ArrayList<>();

        // obtain a readable database
        SQLiteDatabase db = getReadableDatabase();


        Cursor cursor = db.query(TABLE_NAME, new String[]{
                        COLUMN_ID,
                        COLUMN_DATE,
                        COLUMN_SPEED,
                        COLUMN_ADDRESS

                },
                null, null, null, null, COLUMN_DATE, null); // get all rows

        if (cursor != null) {
            // add smsInfos to the list
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                SMSInfo smsInfo = new SMSInfo();
                smsInfo.setID(cursor.getString(0));
                smsInfo.setDate(cursor.getString(1));
                smsInfo.setSpeed(cursor.getString(2));
                smsInfo.setAddress(cursor.getString(3));

                smsInfos.add(smsInfo);
            }

            // close the cursor
            cursor.close();
        }

        // close the database connection
        db.close();

        // return the list
        return smsInfos;
    }
}
