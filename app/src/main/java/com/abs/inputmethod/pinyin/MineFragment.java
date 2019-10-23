package com.abs.inputmethod.pinyin;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.abs.inputmethod.pinyin.constant.LoginStatus;
import com.abs.inputmethod.pinyin.contacts.AddContactActivity;
import com.abs.inputmethod.pinyin.db.Contacts;
import com.abs.inputmethod.pinyin.db.ContactsUtil;
import com.abs.inputmethod.pinyin.db.SessionKeyUtil;
import com.abs.inputmethod.pinyin.dialog.CustomDialog;
import com.abs.inputmethod.pinyin.dialog.CustomEditDialog;
import com.abs.inputmethod.pinyin.dialog.PickContactsDialog;
import com.abs.inputmethod.pinyin.utils.AESUtil;
import com.abs.inputmethod.pinyin.utils.Md5Util;
import com.abs.inputmethod.pinyin.utils.SystemUtil;
import com.abs.inputmethod.pinyin.utils.Utils;
import com.tb.inputmethod.pinyin.R;

import java.util.ArrayList;

public class MineFragment extends Fragment implements View.OnClickListener {


    private static final String EXTRA_CONTENT = "mine";
    private static final String THIS_FILE = "MineFragment";
    public static final String ACTION_SET_ADDRESS = "action_set_address";

    private View logoutView;
    private View loggedView;
    // 正在通讯地址
    private View addressLayout;
    private View importPublicKeyLayout;
    private TextView addressTv;
    private TextView addressHintTv;
    private String action;

    private CustomEditDialog setAddressDialog;
    private CustomEditDialog setPasswdDialog;
    private CustomDialog logoutDialog;
    private PickContactsDialog pickContactsDialog;

    public static MineFragment newInstance(String content) {
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_CONTENT, content);
        MineFragment tabContentFragment = new MineFragment();
        tabContentFragment.setArguments(arguments);
        return tabContentFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_mine, null);
        ((TextView) contentView.findViewById(R.id.title_tv)).setText("我的");
        contentView.findViewById(R.id.title_left_btn).setVisibility(View.INVISIBLE);

        logoutView = contentView.findViewById(R.id.mine_logout_layout);
        loggedView = contentView.findViewById(R.id.mine_logged_layout);
        addressLayout = contentView.findViewById(R.id.mine_address_layout);
        importPublicKeyLayout = contentView.findViewById(R.id.mine_import_pub_layout);
        addressTv = contentView.findViewById(R.id.mine_logged_text);
        addressHintTv = contentView.findViewById(R.id.mine_address_hint_text);

        contentView.findViewById(R.id.mine_address_layout).setOnClickListener(this);
        contentView.findViewById(R.id.mine_settings_layout).setOnClickListener(this);
        contentView.findViewById(R.id.mine_about_layout).setOnClickListener(this);
        importPublicKeyLayout.setOnClickListener(this);
        logoutView.setOnClickListener(this);
        loggedView.setOnClickListener(this);

        Bundle dataBundle = getActivity().getIntent().getExtras();//从当前<span style="font-family: Arial, Helvetica, sans-serif;">Activity中获得Intent，并获得数据Bundle</span>
        if (dataBundle != null) {
            action = dataBundle.getString("action");
        }
        Log.d(THIS_FILE, "onCreateView:" + action);

        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 判断当前是否已登录
        boolean logged = InputMethodApplication.getInstance().getLoginStatus() == LoginStatus.LOGGED;
        if (logged) {
            logoutView.setVisibility(View.GONE);
            loggedView.setVisibility(View.VISIBLE);
            addressLayout.setVisibility(View.VISIBLE);
            importPublicKeyLayout.setVisibility(View.VISIBLE);
            addressTv.setText(InputMethodApplication.getInstance().getWalletAddress());
            addressHintTv.setText(InputMethodApplication.getInstance().getCurrentAddress());
        } else {
            logoutView.setVisibility(View.VISIBLE);
            loggedView.setVisibility(View.GONE);
            addressLayout.setVisibility(View.GONE);
            importPublicKeyLayout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(action) && action.equals(ACTION_SET_ADDRESS)) {
            if (IpksManager.getInstance().isRunning()) {
                setAddress();
            } else {
                // start ipks manager
                startIpksManager();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (setAddressDialog != null && setAddressDialog.isShowing()) {
            setAddressDialog.dismiss();
        }

        if (setPasswdDialog != null && setPasswdDialog.isShowing()) {
            setPasswdDialog.dismiss();
        }
        if (logoutDialog != null && logoutDialog.isShowing()) {
            logoutDialog.dismiss();
        }
    }

    public void enterLogin() {
        startActivity(new Intent(getActivity(), ImportKeystoreActivity.class));
    }

    public void enterSettings() {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
    }

    public void enterAbout() {
        startActivity(new Intent(getActivity(), AboutActivity.class));
    }

    /**
     * 登出
     */
    public void logout() {
        if (logoutDialog == null) {
            logoutDialog = new CustomDialog(getActivity(), R.style.noTitleDialog);
            logoutDialog.setMessage("是否注销当前账号，注销后将无法继续享受加解密功能？");
            logoutDialog.setYesOnclickListener("注销", new CustomDialog.OnConfirmOnClickListener() {
                @Override
                public void onConfirmClick() {
                    logoutDialog.dismiss();
                    InputMethodApplication.getInstance().clearLoggedInfo();
                    IpksManager.getInstance().stopService();
                    ContactsUtil.logout();
                    SessionKeyUtil.logout();
                    getActivity().recreate();
                }
            });
            logoutDialog.setCancelOnClickListener(getString(R.string.cancel), new CustomDialog.OnCancelOnClickListener() {
                @Override
                public void onCancelClick() {
                    logoutDialog.dismiss();
                }
            });
        }
        logoutDialog.show();
    }

    /**
     * 配置对方公钥
     */
    public void enterSetAddress() {
        if (IpksManager.getInstance().isRunning()) {
            setAddress();
        } else {
            // start ipks manager
            startIpksManager();
        }
    }

    private void setAddress() {
        // 查看是否有联系人
        ArrayList<Contacts> contacts = ContactsUtil.getContacts();
        if (contacts != null && contacts.size() > 0) {
            // 选择正在通讯联系人
            if (pickContactsDialog == null) {
                pickContactsDialog = new PickContactsDialog(getActivity(), R.style.noTitleDialog);
                pickContactsDialog.setItemClickListener(new PickContactsDialog.ListItemClickListener() {
                    @Override
                    public void onItemClick(Contacts contact) {
                        Log.d(THIS_FILE, "onItemClick:" + contact);
                        // 设置正在通讯地址
                        Utils.setIpksContact(contact.getAddress(), contact.getPubKey());
                        pickContactsDialog.dismiss();
                        if (ACTION_SET_ADDRESS.equals(action)) {
                            ((BaseActivity) getActivity()).finishAll();
                        } else {
                            getActivity().recreate();
                        }
                    }
                });
            }
            if (!pickContactsDialog.isShowing()) {
                pickContactsDialog.show();
            }
        } else {
            // 添加联系人并设置为正在通讯联系人
            Intent intent = new Intent(getActivity(), AddContactActivity.class);
            intent.setAction(ACTION_SET_ADDRESS.equals(action) ?
                    "com.abs.inputmethod.add.contact.ADD_AND_SET_FROM_OTHER" : "com.abs.inputmethod.add.contact.ADD_AND_SET");
            getActivity().startActivity(intent);
        }
//        Log.d(THIS_FILE, "setAddress");
//        if (setAddressDialog == null) {
//            setAddressDialog = new CustomEditDialog(getActivity(), R.style.noTitleDialog);
//            setAddressDialog.setMessage("请输入对方公钥");
//            setAddressDialog.setContentHint("请输入对方公钥");
//            setAddressDialog.setCancelOnClickListener("取消", new CustomEditDialog.OnCancelOnClickListener() {
//                @Override
//                public void onCancelClick() {
//                    Log.d(THIS_FILE, "取消登录");
//                    setAddressDialog.dismiss();
//                    if (!TextUtils.isEmpty(action) && action.equals(ACTION_SET_ADDRESS)) {
//                        ((BaseActivity) getActivity()).finishAll();
//                    }
//                }
//            });
//            setAddressDialog.setYesOnclickListener("确定", new CustomEditDialog.OnConfirmOnClickListener() {
//                @Override
//                public void onConfirmClick(String content) {
//                    Log.d(THIS_FILE, "确定:" + content);
//                    content = content.trim();
//                    // 检查公钥格式
//                    if (!FormatUtil.isPubKey(content)) {
//                        Toast.makeText(getActivity(), "公钥格式错误", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    // 解析公钥
//                    String address = Keys.toChecksumAddress(Numeric.prependHexPrefix(Keys.getAddress(content)));
//                    Log.e(THIS_FILE, "address3:" + address);
//                    ArrayList<SessionKey> keys = SessionKeyUtil.getSessionKey(address);
//                    if (keys == null || keys.size() < 1) {
//                        Log.d(THIS_FILE, "generate new session key");
//                        String sessionKey = Base64.encodeToString(AlgorithmUtil.generateSessionKey(128), Base64.NO_WRAP);
//                        long version = System.currentTimeMillis();
//                        SessionKeyUtil.insertSessionKey(address, version, sessionKey);
//                        IpksSessionKey ipksSessionKey = new IpksSessionKey(version, sessionKey);
//                        IpksManager.getInstance().sendMsg(content, ipksSessionKey.toString().getBytes());
//                        InputMethodApplication.getInstance().setCurrentAddress(address);
//                        InputMethodApplication.getInstance().setSessionKey(address, new SessionKey(null, address, sessionKey, version));
//                    } else {
//                        IpksSessionKey ipksSessionKey = new IpksSessionKey(keys.get(0).getVersion(), keys.get(0).getKey());
//                        IpksManager.getInstance().sendMsg(content, ipksSessionKey.toString().getBytes());
//                        InputMethodApplication.getInstance().setCurrentAddress(address);
//                        InputMethodApplication.getInstance().setSessionKey(address, keys.get(0));
//                    }
//                    addressHintTv.setText(InputMethodApplication.getInstance().getCurrentAddress());
//                    setAddressDialog.dismiss();
//                    if (!TextUtils.isEmpty(action) && action.equals(ACTION_SET_ADDRESS)) {
//                        getActivity().setResult(getActivity().RESULT_OK);
//                        ((BaseActivity) getActivity()).finishAll();
//                    }
//                }
//            });
//        } else {
//            setAddressDialog.setContent();
//        }
//
//        setAddressDialog.show();
    }

    public void exportPublicKey() {
        SystemUtil.copyToClipboard(getActivity(), InputMethodApplication.getInstance().getPublicKey());
        Toast.makeText(getActivity(), "公钥已经复制到粘贴板", Toast.LENGTH_SHORT).show();
    }

    private void startIpksManager() {
        // 如果私钥不为空则直接启动IPKS。
        if (!TextUtils.isEmpty(InputMethodApplication.getInstance().getPrivateKey())) {
            IpksManager.getInstance().startService();
            if (TextUtils.isEmpty(InputMethodApplication.getInstance().getCurrentAddress())) {
                setAddress();
            }
            return;
        }
        if (setPasswdDialog == null) {
            Log.d(THIS_FILE, "startIpksManager");
            setPasswdDialog = new CustomEditDialog(getActivity(), R.style.noTitleDialog);
            setPasswdDialog.setMessage("需要登录摩尔斯输入法才能使用加解密功能，请输入密码登录");
            setPasswdDialog.setContentHint("请输入密码");
            setPasswdDialog.setContentInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            setPasswdDialog.setCancelOnClickListener(getString(R.string.cancel), new CustomEditDialog.OnCancelOnClickListener() {
                @Override
                public void onCancelClick() {
                    Log.d(THIS_FILE, "取消登录");
                    setPasswdDialog.dismiss();
                    if (!TextUtils.isEmpty(action) && action.equals(ACTION_SET_ADDRESS)) {
                        ((BaseActivity) getActivity()).finishAll();
                    }

                }
            });
            setPasswdDialog.setYesOnclickListener(getString(R.string.confirm), new CustomEditDialog.OnConfirmOnClickListener() {
                @Override
                public void onConfirmClick(String content) {
                    // 检查密码是否正确
                    if (TextUtils.isEmpty(content) || !TextUtils.equals(Md5Util.sha256(content), InputMethodApplication.getInstance().getPasswordHash())) {
                        Log.d(THIS_FILE, "content error");
                        Toast.makeText(getActivity(), "密码错误", Toast.LENGTH_SHORT).show();
                        setPasswdDialog.setContent();
                        return;
                    }
                    // 解密私钥
                    String privateKey = AESUtil.decrypt(InputMethodApplication.getInstance().getPrivateCipher(), AESUtil.createAESKey(content, InputMethodApplication.getInstance().getWalletAddress()));
                    if (TextUtils.isEmpty(privateKey)) {
                        Log.d(THIS_FILE, "decrypt error");
                        Toast.makeText(getActivity(), "密码错误", Toast.LENGTH_SHORT).show();
                        setPasswdDialog.setContent();
                        return;
                    }
                    Log.e(THIS_FILE, "keyStoreImportPrivateKey:" + privateKey);
                    // 登录成功
                    InputMethodApplication.getInstance().setPrivateKey(privateKey);
                    IpksManager.getInstance().startService();
                    setPasswdDialog.dismiss();
                    setAddress();
                    return;
                }
            });
        } else {
            setPasswdDialog.setContent();
        }
        setPasswdDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mine_address_layout:
                enterSetAddress();
                break;
            case R.id.mine_import_pub_layout:
                // 导出公钥
                exportPublicKey();
                break;
            case R.id.mine_settings_layout:
                enterSettings();
                break;
            case R.id.mine_about_layout:
                enterAbout();
                break;
            case R.id.mine_logout_layout:
                enterLogin();
                break;
            case R.id.mine_logged_layout:
                logout();
                break;
        }
    }
}
