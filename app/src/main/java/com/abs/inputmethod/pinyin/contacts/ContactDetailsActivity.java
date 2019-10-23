package com.abs.inputmethod.pinyin.contacts;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.abs.inputmethod.pinyin.BaseActivity;
import com.abs.inputmethod.pinyin.db.Contacts;
import com.abs.inputmethod.pinyin.db.ContactsUtil;
import com.abs.inputmethod.pinyin.dialog.CustomDialog;
import com.abs.inputmethod.pinyin.utils.SystemUtil;
import com.tb.inputmethod.pinyin.R;

public class ContactDetailsActivity extends BaseActivity implements View.OnClickListener {

    public static final String CONTACT_ID = "key_contact_id";
    private static final int REQ_MODIFY_ACTIVITY = 0;
    private CustomDialog deleteDialog;
    private String id;
    private Contacts contact;

    private TextView nickTv;
    private TextView addressTv;
    private TextView pubKeyTv;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        ((TextView) findViewById(R.id.title_tv)).setText("联系人详情");
        findViewById(R.id.title_right_text_btn).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.title_right_text_btn)).setText("编辑");
        findViewById(R.id.title_left_btn).setOnClickListener(this);
        findViewById(R.id.title_right_text_btn).setOnClickListener(this);

        nickTv = findViewById(R.id.contact_details_nick);
        addressTv = findViewById(R.id.contact_details_address);
        addressTv.setOnClickListener(this);
        pubKeyTv = findViewById(R.id.contact_details_pub_key);
        pubKeyTv.setOnClickListener(this);

        id = getIntent().getStringExtra(CONTACT_ID);
        if (TextUtils.isEmpty(id)) {
            Toast.makeText(this, "联系人信息不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadContact();
    }

    private void loadContact() {
        contact = ContactsUtil.getContactById(id);
        if (contact == null) {
            Toast.makeText(this, "联系人信息不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nickTv.setText(contact.getNick());

        if (!TextUtils.isEmpty(contact.getAddress())) {
            addressTv.setText(contact.getAddress());
            addressTv.append(Html.fromHtml("<font color=\"#5a4ccb\">&#xe772;</font>"));
        }

        if (!TextUtils.isEmpty(contact.getPubKey())) {
            pubKeyTv.setText(contact.getPubKey());
            pubKeyTv.append(Html.fromHtml("<font color=\"#5a4ccb\">&#xe772;</font>"));
        }
    }

    /**
     * 删除联系人
     *
     * @param view
     */
    public void doDelete(View view) {
        if (deleteDialog == null) {
            deleteDialog = new CustomDialog(this, R.style.noTitleDialog);
            deleteDialog.setMessage("确认删除联系人？");
            deleteDialog.setYesOnclickListener("确认", new CustomDialog.OnConfirmOnClickListener() {
                @Override
                public void onConfirmClick() {
                    deleteDialog.dismiss();
                    ContactsUtil.deleteById(id);
                    Toast.makeText(ContactDetailsActivity.this, "删除联系人成功", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            deleteDialog.setCancelOnClickListener(getString(R.string.cancel), new CustomDialog.OnCancelOnClickListener() {
                @Override
                public void onCancelClick() {
                    deleteDialog.dismiss();
                }
            });
        }
        if (!deleteDialog.isShowing()) {
            deleteDialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_MODIFY_ACTIVITY && resultCode == RESULT_OK) {
            // 刷新联系人显示
            loadContact();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_right_text_btn:
                // 编辑联系人
                if (contact != null) {
                    Intent intent = new Intent(this, ModifyContactActivity.class);
                    intent.putExtra(CONTACT_ID, id);
                    startActivityForResult(intent, REQ_MODIFY_ACTIVITY);
                }
                break;
            case R.id.title_left_btn:
                finish();
                break;
            case R.id.contact_details_address:
                // 复制钱包地址到粘贴板
                if (contact != null && !TextUtils.isEmpty(contact.getAddress())) {
                    SystemUtil.copyToClipboard(this, contact.getAddress());
                    Toast.makeText(this, "钱包地址已经复制到粘贴板", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.contact_details_pub_key:
                // 复制公钥到粘贴板
                if (contact != null && !TextUtils.isEmpty(contact.getPubKey())) {
                    SystemUtil.copyToClipboard(this, contact.getPubKey());
                    Toast.makeText(this, "公钥已经复制到粘贴板", Toast.LENGTH_SHORT).show();
                }
        }
    }
}
