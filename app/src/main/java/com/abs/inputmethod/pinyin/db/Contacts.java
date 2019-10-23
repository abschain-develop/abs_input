package com.abs.inputmethod.pinyin.db;

import com.google.gson.Gson;

public class Contacts {
    private String ID;
    private String nick;
    private String address;
    private String pubKey;
    private String pinyin;
    private boolean isIndex = false;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public boolean isIndex() {
        return isIndex;
    }

    public void setIndex(boolean index) {
        isIndex = index;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public Contacts(String ID, String nick, String address, String pubKey) {
        this.ID = ID;
        this.nick = nick;
        this.address = address;
        this.pubKey = pubKey;
    }

    public Contacts(String ID, String nick, String address, String pubKey, String pinyin) {
        this.ID = ID;
        this.nick = nick;
        this.address = address;
        this.pubKey = pubKey;
        this.pinyin = pinyin;
    }

    public Contacts(String nick, String address, String pubKey) {
        this.nick = nick;
        this.address = address;
        this.pubKey = pubKey;
    }

    public Contacts(String nick, boolean isIndex) {
        this.nick = nick;
        this.isIndex = isIndex;
    }

    public Contacts() {
    }

}
