package com.abs.inputmethod.pinyin.utils;

import android.util.Log;

import com.tb.inputmethod.pinyin.R;

public class RandomUtil {
    private static final String THIS_FILE = "RandomUtil";

    public static int randomContactLogo() {
        int random = (int) (Math.random() * 5 + 1);
        Log.d(THIS_FILE, "randomContactLogo random:" + random);
        switch (random) {
            case 1:
                return R.drawable.fragment_contacts_list_item_logo_bg_1;
            case 2:
                return R.drawable.fragment_contacts_list_item_logo_bg_2;
            case 3:
                return R.drawable.fragment_contacts_list_item_logo_bg_3;
            case 4:
                return R.drawable.fragment_contacts_list_item_logo_bg_4;
            case 5:
                return R.drawable.fragment_contacts_list_item_logo_bg_5;
            default:
                return R.drawable.fragment_contacts_list_item_logo_bg_1;
        }
    }
}
