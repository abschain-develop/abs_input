package com.abs.inputmethod.pinyin;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.abs.inputmethod.pinyin.constant.LoginStatus;
import com.abs.inputmethod.pinyin.db.SessionKey;
import com.abs.inputmethod.pinyin.db.SessionKeyUtil;
import com.abs.inputmethod.pinyin.model.Wallet;
import com.abs.inputmethod.pinyin.utils.AESUtil;
import com.abs.inputmethod.pinyin.utils.Md5Util;
import com.github.promeg.pinyinhelper.Pinyin;
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict;

import java.util.ArrayList;
import java.util.HashMap;

public class InputMethodApplication extends MultiDexApplication {

    private static InputMethodApplication application;

    private final String FILE_NAME = "abs.inputmethod.config";
    private final String KEY_LOGIN_STATUS = "login_status";
    private final String KEY_WALLET_KEYSTORE = "wallet_keystore";
    private final String KEY_WALLET_PUBLIC_KEY = "wallet_public_key";
    private final String KEY_WALLET_PRIVATE_KEY = "wallet_private_key";
    private final String KEY_WALLET_PASSWORD_HASH = "wallet_password_hash";
    private final String KEY_WALLET_ADDRESS = "wallet_address";

    private final String THIS_FILE = "InputMethodApplication";
    private String privateKey = "";
    private String address = "";
    private HashMap<String, SessionKey> keys = new HashMap<>();

    public static InputMethodApplication getInstance() {
        if (application == null) {
            application = new InputMethodApplication();
        }

        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        // 添加中文城市词典
        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(this)));
    }

    public void saveLoginStatus(LoginStatus status) {
        SharedPreferences sp = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(KEY_LOGIN_STATUS, status.ordinal()).commit();
    }

    public LoginStatus getLoginStatus() {
        SharedPreferences sp = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return LoginStatus.values()[sp.getInt(KEY_LOGIN_STATUS, LoginStatus.LOGOUT.ordinal())];
    }

    public void saveWallet(String password, Wallet wallet) {
        SharedPreferences sp = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_WALLET_KEYSTORE, wallet.getKeystore());
        editor.putString(KEY_WALLET_PUBLIC_KEY, wallet.getPublicKey());
        editor.putString(KEY_WALLET_ADDRESS, wallet.getAddress());
        String privateKeyCipher = AESUtil.encrypt(wallet.getPrivateKey(), AESUtil.createAESKey(password, wallet.getAddress()));
        Log.d(THIS_FILE, "privateKeyCipher:" + privateKeyCipher);
        editor.putString(KEY_WALLET_PRIVATE_KEY, privateKeyCipher);
        editor.putString(KEY_WALLET_PASSWORD_HASH, Md5Util.sha256(password));
        privateKey = wallet.getPrivateKey();
        editor.commit();
    }

    public void clearLoggedInfo() {
        SharedPreferences sp = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(KEY_LOGIN_STATUS);
        editor.remove(KEY_WALLET_KEYSTORE);
        editor.remove(KEY_WALLET_PUBLIC_KEY);
        editor.remove(KEY_WALLET_ADDRESS);
        editor.remove(KEY_WALLET_PRIVATE_KEY);
        editor.remove(KEY_WALLET_PASSWORD_HASH);
        privateKey = "";
        address = "";
        keys.clear();
        editor.commit();
    }

    public String getKeystore() {
        SharedPreferences sp = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_WALLET_KEYSTORE, "");
    }

    public String getWalletAddress() {
        SharedPreferences sp = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_WALLET_ADDRESS, "");
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPrivateCipher() {
        SharedPreferences sp = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_WALLET_PRIVATE_KEY, "");
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        SharedPreferences sp = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_WALLET_PUBLIC_KEY, "");
    }

    public void setCurrentAddress(String address) {
        this.address = address;
    }

    public void setSessionKey(String address, SessionKey sessionKey) {
        if (this.keys.containsKey(address) && this.keys.get(address).getVersion() > sessionKey.getVersion()) {
            return;
        }
        Log.d(THIS_FILE, "put keys");
        this.keys.put(address, sessionKey);
    }

    public SessionKey getSessionKey(String address) {
        if (this.keys.containsKey(address)) {
            return this.keys.get(address);
        }

        ArrayList<SessionKey> sessionKeys = SessionKeyUtil.getSessionKey(address);
        if (sessionKeys != null && sessionKeys.size() > 0) {
            this.keys.put(address, sessionKeys.get(0));
            return sessionKeys.get(0);
        }

        return null;
    }

    public String getCurrentAddress() {
        return this.address;
//        return "0x64fC2959E5ae9CC30a6363B37C3C6AC11c65Da4e";
//        return "0x605B29c72bc0823c9d61eF910F364CFeD693a38B";
    }


    public String getPasswordHash() {
        SharedPreferences sp = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_WALLET_PASSWORD_HASH, "");
    }
}
