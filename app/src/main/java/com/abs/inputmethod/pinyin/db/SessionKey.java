package com.abs.inputmethod.pinyin.db;

import com.google.gson.Gson;

public class SessionKey {
    private String ID;
    private String address;
    private String key;
    private long version;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public SessionKey(String ID, String address, String key, long version) {
        this.ID = ID;
        this.address = address;
        this.key = key;
        this.version = version;
    }

    public SessionKey() {
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
