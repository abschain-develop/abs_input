package com.abs.inputmethod.pinyin;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tb.inputmethod.pinyin.R;
import com.tb.inputmethod.pinyin.Settings;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    private CheckBox voiceCb;
    private CheckBox vibrateCb;
    private CheckBox predictionCb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ((TextView) findViewById(R.id.title_tv)).setText("设置");
        findViewById(R.id.title_left_btn).setOnClickListener(this);

        voiceCb = findViewById(R.id.settings_key_voice_cb);
        vibrateCb = findViewById(R.id.settings_key_vibration_cb);
        predictionCb = findViewById(R.id.settings_key_prediction_cb);

        Settings.getInstance(PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()));
    }

    public void doBack() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateWidgets();
    }

    private void updateWidgets() {
        voiceCb.setChecked(Settings.getKeySound());
        vibrateCb.setChecked(Settings.getVibrate());
        predictionCb.setChecked(Settings.getPrediction());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Settings.setKeySound(voiceCb.isChecked());
        Settings.setVibrate(vibrateCb.isChecked());
        Settings.setPrediction(predictionCb.isChecked());

        Settings.writeBack();
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
