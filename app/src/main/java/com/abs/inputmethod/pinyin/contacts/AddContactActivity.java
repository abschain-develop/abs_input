package com.abs.inputmethod.pinyin.contacts;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.abs.inputmethod.pinyin.BaseActivity;
import com.abs.inputmethod.pinyin.db.Contacts;
import com.abs.inputmethod.pinyin.db.ContactsUtil;
import com.abs.inputmethod.pinyin.dialog.CustomDialog;
import com.abs.inputmethod.pinyin.utils.FormatUtil;
import com.abs.inputmethod.pinyin.utils.Utils;
import com.tb.inputmethod.pinyin.R;

import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.util.ArrayList;

public class AddContactActivity extends BaseActivity implements View.OnClickListener {

    private static final String THIS_FILE = "AddContactActivity";
    private EditText nickEt;
    private EditText pubKeyEt;
    private CustomDialog customDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        ((TextView) findViewById(R.id.title_tv)).setText(R.string.add_contact);
        findViewById(R.id.title_left_btn).setOnClickListener(this);

        nickEt = findViewById(R.id.add_contact_nick_et);
        pubKeyEt = findViewById(R.id.add_contact_pub_key_et);

        if ("com.abs.inputmethod.add.contact.ADD_AND_SET".equals(getIntent().getAction())
                || "com.abs.inputmethod.add.contact.ADD_AND_SET_FROM_OTHER".equals(getIntent().getAction())) {
            ((Button) findViewById(R.id.add_contact_save_btn)).setText("保存并设置");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_left_btn:
                if ("com.abs.inputmethod.add.contact.ADD_AND_SET_FROM_OTHER".equals(getIntent().getAction())) {
                    finishAll();
                } else {
                    finish();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if ("com.abs.inputmethod.add.contact.ADD_AND_SET_FROM_OTHER".equals(getIntent().getAction())) {
            finishAll();
        }
    }

    public void saveContact(View view) {
        final String nick = nickEt.getText().toString();
        final String pubKey = pubKeyEt.getText().toString().trim();

        if (TextUtils.isEmpty(nick) || TextUtils.isEmpty(nick.trim())) {
            Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(pubKey)) {
            Toast.makeText(this, "公钥不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查公钥格式
        if (!FormatUtil.isPubKey(pubKey)) {
            Toast.makeText(this, "请输入合法公钥", Toast.LENGTH_SHORT).show();
            return;
        }

        // 解析公钥
        final String address = Keys.toChecksumAddress(Numeric.prependHexPrefix(Keys.getAddress(pubKey)));
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "请输入合法公钥", Toast.LENGTH_SHORT).show();
            return;
        }

        // 保存联系人
        ArrayList<Contacts> contacts = ContactsUtil.getContactsByPubKey(pubKey);
        if (contacts.size() > 0) {
            // update
            // 联系人已存在，（公钥相同）
            if (contacts.get(0).getNick().equals(nick)) {
                // 公钥相同，昵称相同
                Log.d(THIS_FILE, "insert exists contact");
                if ("com.abs.inputmethod.add.contact.ADD_AND_SET".equals(getIntent().getAction())) {
                    Utils.setIpksContact(address, pubKey);
                }
                Toast.makeText(this, "新建成功", Toast.LENGTH_SHORT).show();
                if ("com.abs.inputmethod.add.contact.ADD_AND_SET_FROM_OTHER".equals(getIntent().getAction())) {
                    finishAll();
                } else {
                    finish();
                }
            } else {
                // 公钥相同，昵称不同。弹框提醒是否覆盖
                if (customDialog == null) {
                    customDialog = new CustomDialog(this, R.style.noTitleDialog);
                    customDialog.setMessage("当前联系人已存在，是否覆盖？");
                    customDialog.setYesOnclickListener("覆盖", new CustomDialog.OnConfirmOnClickListener() {
                        @Override
                        public void onConfirmClick() {
                            customDialog.dismiss();
                            // update
                            Log.d(THIS_FILE, "update result:" + ContactsUtil.updateContact(nick, address, pubKey));
                            if ("com.abs.inputmethod.add.contact.ADD_AND_SET".equals(getIntent().getAction())) {
                                Utils.setIpksContact(address, pubKey);
                            }
                            Toast.makeText(AddContactActivity.this, "新建成功", Toast.LENGTH_SHORT).show();
                            if ("com.abs.inputmethod.add.contact.ADD_AND_SET_FROM_OTHER".equals(getIntent().getAction())) {
                                finishAll();
                            } else {
                                finish();
                            }
                        }
                    });
                    customDialog.setCancelOnClickListener(getString(R.string.cancel), new CustomDialog.OnCancelOnClickListener() {
                        @Override
                        public void onCancelClick() {
                            customDialog.dismiss();
                        }
                    });
                }

                customDialog.show();
            }
        } else {
            // insert new contact
            Log.d(THIS_FILE, "insert result:" + ContactsUtil.insertContacts(nick, address, pubKey));
            if ("com.abs.inputmethod.add.contact.ADD_AND_SET".equals(getIntent().getAction())) {
                Utils.setIpksContact(address, pubKey);
            }
            Toast.makeText(this, "新建成功", Toast.LENGTH_SHORT).show();
            if ("com.abs.inputmethod.add.contact.ADD_AND_SET_FROM_OTHER".equals(getIntent().getAction())) {
                finishAll();
            } else {
                finish();
            }
        }
    }
}
