package com.abs.inputmethod.pinyin.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.abs.inputmethod.pinyin.InputMethodApplication;
import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;

public class ContactsUtil {

    private static InputOpenHelper inputOpenHelper;

    public static void logout() {
        inputOpenHelper = null;
    }

    public static long insertContacts(String nick, String address, String pubKey) {
        if (inputOpenHelper == null) {
            inputOpenHelper = new InputOpenHelper(InputMethodApplication.getInstance(), InputMethodApplication.getInstance().getWalletAddress());
        }

        SQLiteDatabase bd = inputOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nick", nick);
        values.put("address", address);
        values.put("pub_key", pubKey);
        values.put("pinyin", Pinyin.toPinyin(nick.trim(), ""));
        long insert = bd.insert("contacts", null, values);

        return insert;
    }

    public static long updateContactById(String id, String nick, String address, String pubKey) {
        if (inputOpenHelper == null) {
            inputOpenHelper = new InputOpenHelper(InputMethodApplication.getInstance(), InputMethodApplication.getInstance().getWalletAddress());
        }

        SQLiteDatabase db = inputOpenHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("nick", nick);
        if (!TextUtils.isEmpty(address)) {
            values.put("address", address);
        }
        if (!TextUtils.isEmpty(pubKey)) {
            values.put("pub_key", pubKey);
        }
        values.put("pinyin", Pinyin.toPinyin(nick.trim(), ""));
        long update = db.update("contacts", values, "_id=?", new String[]{id});

        return update;
    }

    public static long updateContact(String nick, String address, String pubKey) {
        if (inputOpenHelper == null) {
            inputOpenHelper = new InputOpenHelper(InputMethodApplication.getInstance(), InputMethodApplication.getInstance().getWalletAddress());
        }

        SQLiteDatabase db = inputOpenHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("nick", nick);
        values.put("address", address);
        values.put("pinyin", Pinyin.toPinyin(nick.trim(), ""));
        long update = db.update("contacts", values, "pub_key=?", new String[]{pubKey});

        return update;
    }

    public static ArrayList<Contacts> getContactsByPubKey(String s_pubKey) {
        if (inputOpenHelper == null) {
            inputOpenHelper = new InputOpenHelper(InputMethodApplication.getInstance(), InputMethodApplication.getInstance().getWalletAddress());
        }

        SQLiteDatabase db = inputOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from contacts where pub_key=?", new String[]{s_pubKey});
        ArrayList<Contacts> contacts = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex("_id"));
                String nick = cursor.getString(cursor.getColumnIndex("nick"));
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String pubKey = cursor.getString(cursor.getColumnIndex("pub_key"));
                String pinyin = cursor.getString(cursor.getColumnIndex("pinyin"));
                contacts.add(new Contacts(id, nick, address, pubKey, pinyin));
            }
        }

        return contacts;
    }

    public static ArrayList<Contacts> getContacts() {
        if (inputOpenHelper == null) {
            inputOpenHelper = new InputOpenHelper(InputMethodApplication.getInstance(), InputMethodApplication.getInstance().getWalletAddress());
        }

        SQLiteDatabase db = inputOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from contacts order by pinyin asc", null);
        ArrayList<Contacts> contacts = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex("_id"));
                String nick = cursor.getString(cursor.getColumnIndex("nick"));
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String pubKey = cursor.getString(cursor.getColumnIndex("pub_key"));
                String pinyin = cursor.getString(cursor.getColumnIndex("pinyin"));
                contacts.add(new Contacts(id, nick, address, pubKey, pinyin));
            }
        }

        return contacts;
    }


    public static Contacts getContactById(String s_id) {
        if (inputOpenHelper == null) {
            inputOpenHelper = new InputOpenHelper(InputMethodApplication.getInstance(), InputMethodApplication.getInstance().getWalletAddress());
        }

        SQLiteDatabase db = inputOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from contacts where _id=?", new String[]{s_id});
        ArrayList<Contacts> contacts = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex("_id"));
                String nick = cursor.getString(cursor.getColumnIndex("nick"));
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String pubKey = cursor.getString(cursor.getColumnIndex("pub_key"));
                String pinyin = cursor.getString(cursor.getColumnIndex("pinyin"));
                return new Contacts(id, nick, address, pubKey, pinyin);
            }
        }

        return null;
    }

    public static int deleteById(String s_id) {
        if (inputOpenHelper == null) {
            inputOpenHelper = new InputOpenHelper(InputMethodApplication.getInstance(), InputMethodApplication.getInstance().getWalletAddress());
        }

        SQLiteDatabase db = inputOpenHelper.getWritableDatabase();
        return db.delete("contacts", "_id=?", new String[]{s_id});
    }
}
