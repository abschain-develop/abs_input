package com.abs.inputmethod.pinyin.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class SystemUtil {
    public static void copyToClipboard(Context context, String str) {
        ClipboardManager myClipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData myClip = ClipData.newPlainText("abs_input_copy_data", str);
        myClipboard.setPrimaryClip(myClip);
    }
}
