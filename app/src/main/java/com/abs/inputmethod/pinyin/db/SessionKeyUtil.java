package com.abs.inputmethod.pinyin.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.abs.inputmethod.pinyin.InputMethodApplication;

import java.util.ArrayList;

public class SessionKeyUtil {

    private static InputOpenHelper inputOpenHelper;

    public static void logout() {
        inputOpenHelper = null;
    }

    public static long insertSessionKey(String address, long version, String key) {
        if (inputOpenHelper == null) {
            inputOpenHelper = new InputOpenHelper(InputMethodApplication.getInstance(), InputMethodApplication.getInstance().getWalletAddress());
        }

        SQLiteDatabase bd = inputOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("address", address);
        values.put("key_version", version);
        values.put("_key", key);
        long insert = bd.insert("sessionKey", null, values);

        return insert;
    }

    public static long insertOrUpdateSessionKey(String address, long version, String key) {
        if (inputOpenHelper == null) {
            inputOpenHelper = new InputOpenHelper(InputMethodApplication.getInstance(), InputMethodApplication.getInstance().getWalletAddress());
        }

        SQLiteDatabase db = inputOpenHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from sessionKey where address=? AND _key=?", new String[]{address, key});
        if (cursor != null && cursor.getCount() > 0) {
            // 数据库中已有数据
            return -1;
        } else {
            ContentValues values = new ContentValues();
            values.put("address", address);
            values.put("key_version", version);
            values.put("_key", key);
            long insert = db.insert("sessionKey", null, values);

            return insert;
        }
    }

    public static ArrayList<SessionKey> getSessionKey(String s_address) {
        if (inputOpenHelper == null) {
            inputOpenHelper = new InputOpenHelper(InputMethodApplication.getInstance(), InputMethodApplication.getInstance().getWalletAddress());
        }

        SQLiteDatabase db = inputOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from sessionKey where address=? order by key_version desc", new String[]{s_address});
        ArrayList<SessionKey> sessionKeys = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex("_id"));
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String key = cursor.getString(cursor.getColumnIndex("_key"));
                long version = cursor.getLong(cursor.getColumnIndex("key_version"));
                sessionKeys.add(new SessionKey(id, address, key, version));
            }
        }

        return sessionKeys;
    }

    public static ArrayList<SessionKey> getSessionKeyByVersion(long s_version) {
        if (inputOpenHelper == null) {
            inputOpenHelper = new InputOpenHelper(InputMethodApplication.getInstance(), InputMethodApplication.getInstance().getWalletAddress());
        }

        SQLiteDatabase db = inputOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from sessionKey where key_version=?", new String[]{Long.toString(s_version)});
        ArrayList<SessionKey> sessionKeys = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex("_id"));
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String key = cursor.getString(cursor.getColumnIndex("_key"));
                long version = cursor.getLong(cursor.getColumnIndex("key_version"));
                sessionKeys.add(new SessionKey(id, address, key, version));
            }
        }

        return sessionKeys;
    }
}
