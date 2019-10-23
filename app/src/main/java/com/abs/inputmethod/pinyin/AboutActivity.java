package com.abs.inputmethod.pinyin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.abs.inputmethod.pinyin.utils.PackageUtils;
import com.tb.inputmethod.pinyin.R;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    private TextView versionTv;
    private boolean showVersionCode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ((TextView) findViewById(R.id.title_tv)).setText("关于我们");
        findViewById(R.id.title_left_btn).setOnClickListener(this);
        versionTv = findViewById(R.id.about_version_tv);
        versionTv.setText(PackageUtils.getAppName(this) + " v" + PackageUtils.getVersionName(this));
    }

    public void doBack() {
        finish();
    }

    public void showVersionCode(View view) {
        if (showVersionCode) {
            versionTv.setText(PackageUtils.getAppName(this) + " v" + PackageUtils.getVersionName(this));
        } else {
            versionTv.setText(PackageUtils.getAppName(this) + " v" + PackageUtils.getVersionName(this) + "." + PackageUtils.getVersionCode(this));
        }
        showVersionCode = !showVersionCode;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_left_btn:
                doBack();
                break;
        }
    }
}
