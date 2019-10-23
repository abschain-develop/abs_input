package com.abs.inputmethod.pinyin.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InputOpenHelper extends SQLiteOpenHelper {

    public InputOpenHelper(Context context, String dbName) {
        super(context, dbName + ".db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table if not exists sessionKey(_id integer primary key autoincrement, " +
                "key_version integer, address varchar(50),_key varchar(128))");
        sqLiteDatabase.execSQL("create table if not exists contacts(_id integer primary key autoincrement, nick varchar(256) not null, " +
                "address varchar(50) not null,pub_key varchar(128) not null, pinyin varchar(256))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
