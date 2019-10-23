package com.abs.inputmethod.pinyin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.abs.inputmethod.pinyin.constant.LoginStatus;
import com.abs.inputmethod.pinyin.db.SessionKey;
import com.abs.inputmethod.pinyin.db.SessionKeyUtil;
import com.abs.inputmethod.pinyin.dialog.CustomEditDialog;
import com.abs.inputmethod.pinyin.model.IpksSessionKey;
import com.abs.inputmethod.pinyin.utils.AlgorithmUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kenai.jffi.Main;
import com.tb.inputmethod.pinyin.R;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportKeystoreActivity extends BaseActivity implements Handler.Callback, View.OnClickListener {

    private static final String THIS_FILE = "ImportKeystoreActivity";
    private EditText keystoreEt;
    private EditText passwdEt;

    private String action;
    public static final String ACTION_IMPORT_AND_SET_ADDRESS = "ACTION_IMPORT_AND_SET_ADDRESS";

    private Handler handler;
    private ExecutorService loginES;
    private ProgressDialog loginPd;
    private final int MSG_LOGIN_FAILED = 0;
    private final int MSG_LOGIN_FAILED_PASSWORD_ERROR = -1;
    private final int MSG_LOGIN_SUCCESS_NORMAL_MODEL = 1;
    private final int MSG_LOGIN_SUCCESS_IMPORT_AND_SET_ADDRESS_MODEL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_keystore);

        action = getIntent().getStringExtra("action");
        handler = new Handler(this);

        ((TextView) findViewById(R.id.title_tv)).setText("登录");
        findViewById(R.id.title_left_btn).setOnClickListener(this);
        keystoreEt = findViewById(R.id.import_keystore_et);
        passwdEt = findViewById(R.id.import_password_et);
    }


    public void login(View view) {
        final String keystore = keystoreEt.getText().toString();
        final String passwd = passwdEt.getText().toString();
        /**
         * address：0x605B29c72bc0823c9d61eF910F364CFeD693a38B
         * pubkey：0x027703cb8c38e8f72a318179241c456372e9a22c329c9579997eed3e9cccbc72c6
         * prikey：0xa31b1f69ae0fbf5974754c0527a55020d4290ae195e68e2fb5a87e43415e9de0
         */
//        final String keystore = "{\"version\":3,\"id\":\"ffc425e6-390c-4026-a552-f3cbc9f0501e\",\"address\":\"605b29c72bc0823c9d61ef910f364cfed693a38b\",\"crypto\":{\"ciphertext\":\"1e6534e3bcb0e731de98d3427456e6c3692d31f527cad5d68ca0199bbcd54479\",\"cipherparams\":{\"iv\":\"67b225ba3ef2145065fe68fc6f462716\"},\"cipher\":\"aes-128-ctr\",\"kdf\":\"pbkdf2\",\"kdfparams\":{\"dklen\":32,\"salt\":\"174ac57d6c9944eb91b5f953bbad06a5dd91398402b1d7d6368b391813335c2c\",\"c\":10240,\"prf\":\"hmac-sha256\"},\"mac\":\"ad6c5a2eabec7a7f9c0b9a662ca825dc2f48ee52c98a8a439058b21525c0feb0\"}}";
//        final String passwd = "12345678";
//        final String keystore = "{\"version\":3,\"id\":\"011a2b96-7010-4bd0-8939-4fc61a9261aa\",\"address\":\"64fc2959e5ae9cc30a6363b37c3c6ac11c65da4e\",\"crypto\":{\"ciphertext\":\"42097d60972f295ef83a3c06b9d23e8c44f9fec073cfe31a6e36273732864040\",\"cipherparams\":{\"iv\":\"282c03638d440f2966209c77f794f66c\"},\"cipher\":\"aes-128-ctr\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"salt\":\"81ea3d41b6b898ecdba60cb71a3ab300c531ac032a8313ba62c483fc7f8f98ec\",\"n\":131072,\"r\":8,\"p\":1},\"mac\":\"060dcacd9e54ce489c80f737445997edd4fb99d0788ea92dcece9eed66bbfc70\"}}";
//        final String passwd = "zxc,qwe01";

        if (TextUtils.isEmpty(keystore) || TextUtils.isEmpty(passwd)) {
            Toast.makeText(this, "Keystore或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (loginES == null) {
            loginES = Executors.newSingleThreadExecutor();
        }

        if (loginPd == null) {
            loginPd = new ProgressDialog(this);
            loginPd.setMessage("正在登录...");
            loginPd.setCancelable(false);
        }
        loginPd.show();

        loginES.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    WalletFile walletFile = objectMapper.readValue(keystore, WalletFile.class);
                    ECKeyPair keyPair = Wallet.decrypt(passwd, walletFile);
                    WalletFile generateWalletFile = Wallet.createLight(passwd, keyPair);
                    String address = Keys.toChecksumAddress(Numeric.prependHexPrefix(generateWalletFile.getAddress()));
                    String privateKey = Numeric.toHexStringNoPrefixZeroPadded(keyPair.getPrivateKey(), 64);
                    String publicKey = Numeric.toHexStringNoPrefixZeroPadded(keyPair.getPublicKey(), 128);
                    Log.d(THIS_FILE, "keyStoreImportAddress:" + address);
                    Log.d(THIS_FILE, "keyStoreImportPrivateKey:" + privateKey);
                    Log.d(THIS_FILE, "keyStoreImportPublicKey:" + publicKey);
                    Log.i(THIS_FILE, "login_status:" + InputMethodApplication.getInstance().getLoginStatus());
                    // 登录成功
                    InputMethodApplication.getInstance().saveWallet(passwd, new com.abs.inputmethod.pinyin.model.Wallet(keystore, address, publicKey, privateKey));
                    InputMethodApplication.getInstance().saveLoginStatus(LoginStatus.LOGGED);
                    IpksManager.getInstance().startService();
//                    if (action != null && action.equals(ACTION_IMPORT_AND_SET_ADDRESS)) {
//                        handler.sendEmptyMessage(MSG_LOGIN_SUCCESS_IMPORT_AND_SET_ADDRESS_MODEL);
//                        Log.d(THIS_FILE, "import success. ACTION_IMPORT_AND_SET_ADDRESS");
//                        return;
//                    }
                    handler.sendEmptyMessage(MSG_LOGIN_SUCCESS_NORMAL_MODEL);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CipherException e) {
                    e.printStackTrace();
                    if (e.getMessage().equals("Invalid password provided")) {
                        handler.sendEmptyMessage(MSG_LOGIN_FAILED_PASSWORD_ERROR);
                        return;
                    }
                }

                handler.sendEmptyMessage(MSG_LOGIN_FAILED);
            }
        });
    }

    public void doBack() {
        if (action != null && action.equals(ACTION_IMPORT_AND_SET_ADDRESS)) {
            // finish all
            Log.d(THIS_FILE, "ACTION_IMPORT_AND_SET_ADDRESS");
            finishAll();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loginPd != null && loginPd.isShowing())
            loginPd.dismiss();
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_LOGIN_FAILED:
                if (loginPd != null)
                    loginPd.dismiss();
                Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show();
                break;
            case MSG_LOGIN_FAILED_PASSWORD_ERROR:
                if (loginPd != null)
                    loginPd.dismiss();
                Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
                break;
            case MSG_LOGIN_SUCCESS_NORMAL_MODEL:
                if (loginPd != null)
                    loginPd.dismiss();
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case MSG_LOGIN_SUCCESS_IMPORT_AND_SET_ADDRESS_MODEL:
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("action", MineFragment.ACTION_SET_ADDRESS);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
        return false;
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
