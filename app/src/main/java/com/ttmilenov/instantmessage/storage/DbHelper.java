package com.ttmilenov.instantmessage.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Teodor Milenov on 4/9/2016.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "instantmessage.db";
    private static final int DATABASE_VERSION = 1;
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table messages " +
                    "("
                    + "_id"                            +" integer primary key autoincrement, "
                    + DataProvider.COL_TYPE        	   +" integer, "
                    + DataProvider.COL_MESSAGE         +" text, "
                    + DataProvider.COL_SENDER_EMAIL    +" text, "
                    + DataProvider.COL_RECEIVER_EMAIL  +" text, "
                    + DataProvider.COL_TIME 		   +" datetime default current_timestamp" +
                    ");");

        db.execSQL("create table profile " +
                    "("
                    + "_id"                            +" integer primary key autoincrement, "
                    + DataProvider.COL_NAME            +" text, "
                    + DataProvider.COL_EMAIL           +" text unique, "
                    + DataProvider.COL_COUNT           +" integer default 0" +
                    ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
