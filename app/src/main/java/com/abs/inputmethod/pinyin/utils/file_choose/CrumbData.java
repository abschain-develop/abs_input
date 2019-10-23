package com.abs.inputmethod.pinyin.utils.file_choose;

public class CrumbData {
    private String name;
    private String tag;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public CrumbData() {
    }

    public CrumbData(String name, String tag) {
        this.name = name;
        this.tag = tag;
    }
}
