package com.abs.inputmethod.pinyin;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.abs.inputmethod.pinyin.utils.PermissionUtil;
import com.tb.inputmethod.pinyin.R;

import java.util.List;

public class HomeActivity extends BaseActivity implements Handler.Callback {

    private static final String THIS_FILE = "HomeActivity";
    private static final int REQUEST_DIALOG_PERMISSION = 100;
    private static final int MSG_CHECK = 1;
    private static final int MSG_ENTER_MAIN_ACTIVITY = 0;

    private InputChangeReceiver recevier;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recevier = new InputChangeReceiver();
        handler = new Handler(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
        registerReceiver(recevier, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DIALOG_PERMISSION) {
            if (Build.VERSION.SDK_INT >= 23 && PermissionUtil.canDrawOverlays(this)) {
                Log.d(THIS_FILE, "testWindow");
            } else {
                Log.d(THIS_FILE, "permission denied.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(recevier);
    }

    private void checkInput() {
        // 判断是否已启用输入法
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> list = imm.getEnabledInputMethodList();
        boolean enable = false;
        for (InputMethodInfo inputMethodInfo : list) {
            Log.i(THIS_FILE, inputMethodInfo.getServiceName());
            if (inputMethodInfo.getPackageName().equals(getPackageName())) {
                Log.i(THIS_FILE, "输入法已启用");
                enable = true;
                break;
            }

        }

        if (!enable) {
            // 输入法未启用
            setContentView(R.layout.activity_home_guide_1);
            ((TextView) findViewById(R.id.title_tv)).setText("启用摩尔斯输入法");
            findViewById(R.id.title_left_btn).setVisibility(View.INVISIBLE);
            return;
        } else {
            // 判断是否为默认输入法
            boolean selected = TextUtils.equals(getDefaultInputMethodPkgName(this), getPackageName());
            Log.i(THIS_FILE, "ABS为默认输入法？：" + selected);
            if (!selected) {
                // ABS输入法不是默认输入法
                setContentView(R.layout.activity_home_guide_2);
                ((TextView) findViewById(R.id.title_tv)).setText("启用摩尔斯输入法");
                findViewById(R.id.title_left_btn).setVisibility(View.INVISIBLE);
                return;
            }
        }

        // 输入法已按照指南设置。loading页
        handler.sendEmptyMessageDelayed(MSG_ENTER_MAIN_ACTIVITY, 1000);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // check有没有显示悬浮框权限
            // 直接获取的话，得到的结果是仍然没有开通权限; 延时0.5秒再获取能获取到正确的值
            handler.sendEmptyMessageDelayed(MSG_CHECK, 500);
        } else {
            checkInput();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void check() {
        if (!PermissionUtil.canDrawOverlays(this)) {
            Toast.makeText(this, "请打开显示悬浮窗开关!", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("package:" + getPackageName());
            Intent intent = new Intent();
            if (PermissionUtil.isVivo()) {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            } else {
                intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            }
            intent.setData(uri);
            startActivityForResult(intent, REQUEST_DIALOG_PERMISSION /* REQUEST CODE */);
        } else {
            checkInput();
        }
    }

    //获取默认输入法包名：
    private String getDefaultInputMethodPkgName(Context context) {
        String mDefaultInputMethodPkg = null;

        String mDefaultInputMethodCls = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
        //输入法类名信息
        Log.d(THIS_FILE, "mDefaultInputMethodCls=" + mDefaultInputMethodCls);
        if (!TextUtils.isEmpty(mDefaultInputMethodCls)) {
            //输入法包名
            mDefaultInputMethodPkg = mDefaultInputMethodCls.split("/")[0];
            Log.d(THIS_FILE, "mDefaultInputMethodPkg=" + mDefaultInputMethodPkg);
        }
        return mDefaultInputMethodPkg;
    }

    /**
     * 启用输入法
     *
     * @param view
     */
    public void openInputMethod(View view) {
        Intent intent = new Intent();
        intent.setAction("android.settings.INPUT_METHOD_SETTINGS");
        startActivityForResult(intent, 0);
    }

    /**
     * 切换输入法
     *
     * @param view
     */
    public void settingsInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showInputMethodPicker();
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_ENTER_MAIN_ACTIVITY:
                Intent startIntent = new Intent(HomeActivity.this, MainActivity.class);
                HomeActivity.this.startActivity(startIntent);
                HomeActivity.this.finish();
                break;
            case MSG_CHECK:
                check();
                break;
        }

        return false;
    }

    class InputChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(THIS_FILE, intent.getAction());
            // 判断是否为默认输入法
            boolean selected = TextUtils.equals(getDefaultInputMethodPkgName(HomeActivity.this), getPackageName());
            Log.i(THIS_FILE, "ABS为默认输入法？：" + selected);
            if (selected) {
                // ABS输入法是默认输入法
                Intent startIntent = new Intent(HomeActivity.this, MainActivity.class);
                HomeActivity.this.startActivity(startIntent);
                HomeActivity.this.finish();
                return;
            }
        }
    }
}
