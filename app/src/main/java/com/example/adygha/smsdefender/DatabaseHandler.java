package com.example.adygha.smsdefender;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by adygha on 30.04.2017.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "melignantSmsBase";
    public static final String TABLE_SMS = "melignantSmsTable";
    public static final String KEY_ID = "_id";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_SMSBODY = "smsBody";

    public DatabaseHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //creating tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SMS_TABLE = "CREATE TABLE " + TABLE_SMS + " ("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ADDRESS + " TEXT,"
                + KEY_SMSBODY + " TEXT" + ")";
        db.execSQL(CREATE_SMS_TABLE);
    }

    //updating database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS);
        //create table again
        onCreate(db);
    }

    //code to add new melignant sms
    public void addSMS(String address, String smsBody){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values =  new ContentValues();
        values.put(KEY_ADDRESS, address);
        values.put(KEY_SMSBODY, smsBody);
        //inserting row
        db.insert(TABLE_SMS, null, values);
        db.close();
    }

    //code to get the cursor
    public Cursor getCursor(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + KEY_ID + ", " + KEY_ADDRESS + ", " + KEY_SMSBODY + " FROM " + TABLE_SMS, null);
        return cursor;
    }


    public void deleteDB (Context context){
        context.deleteDatabase(DATABASE_NAME);
        Log.d("MY_TAG", "datebase deleted");
    }
}
