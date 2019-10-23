package com.abs.inputmethod.pinyin.db;

import com.google.gson.Gson;

import java.util.ArrayList;

public class ContactsList {
    private ArrayList<Contacts> contacts;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public ContactsList() {
    }

    public ContactsList(ArrayList<Contacts> contacts) {
        this.contacts = contacts;
    }

    public ArrayList<Contacts> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<Contacts> contacts) {
        this.contacts = contacts;
    }
}
