package com.abs.inputmethod.pinyin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.tb.inputmethod.pinyin.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseActivity extends AppCompatActivity {

    private static final String THIS_FILE = "BaseActivity";

    private static HashMap<String, Activity> activitys = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(THIS_FILE, "className:" + getLocalClassName());
        activitys.put(getLocalClassName(), this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activitys.remove(getLocalClassName());
    }

    protected void finishAll() {
        for (Map.Entry<String, Activity> entry : activitys.entrySet()) {
            Log.d(THIS_FILE, "finish:" + entry.getValue());
            entry.getValue().finish();
        }
    }
}
