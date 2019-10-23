package com.abs.inputmethod.pinyin.utils;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.abs.inputmethod.pinyin.db.SessionKey;
import com.abs.inputmethod.pinyin.db.SessionKeyUtil;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import config.CryptoClass;

public class AlgorithmUtil {
    private static final String THIS_FILE = "AlgorithmUtil";

    public static byte[] generateSessionKey(int length) {
        //实例化
        KeyGenerator kgen = null;
        try {
            kgen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //设置密钥长度
        kgen.init(length);
        //生成密钥
        SecretKey skey = kgen.generateKey();
        //返回密钥的二进制编码
        return skey.getEncoded();
    }

    private static byte[] iv = new byte[]{
            -12, 35, -25, 65, 45, -87, 95, -22, -15, 45, 55, -66, 32, 5 - 4, 84, 55
    };

    /**
     * 获取字符串形式的AES密钥
     *
     * @param password 指定随机源的种子
     * @return 字符串密钥
     */
    public static String createKey(String password) {
        try {
            //指定加密算法的名称为AES
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            //初始化密钥生成器，指定密钥的长度为128(单位:bit),
            SecureRandom secureRandom = new SecureRandom(password.getBytes());
            keyGenerator.init(128, secureRandom);
            //生成原始对称密钥
            SecretKey secretKey = keyGenerator.generateKey();
            //返回编码格式的密钥
            byte[] enCodeFormat = secretKey.getEncoded();
            //将编码格式的密钥进行Base64编码，方便进行保存
            return Base64.encodeToString(enCodeFormat, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encrypt(long version, byte[] key, String srcStr) {
        String destStr = "";

        CryptoClass cryptoClass = new CryptoClass();
        byte[] encrypted = cryptoClass.aeS_CBC_Encrypt(srcStr.getBytes(), key, null);
        byte[] versionBytes = FormatUtil.longToBytes(version);
        byte[] destByte = new byte[encrypted.length + versionBytes.length];
        Log.d(THIS_FILE, "encrypted.length:" + encrypted.length);
        Log.d(THIS_FILE, "versionBytes.length:" + versionBytes.length);
        Log.d(THIS_FILE, "version:" + version);
        Log.d(THIS_FILE, "key:" + Base64.encodeToString(key, Base64.NO_WRAP));
        System.arraycopy(versionBytes, 0, destByte, 0, versionBytes.length);
        System.arraycopy(encrypted, 0, destByte, versionBytes.length, encrypted.length);
        destStr = Base64.encodeToString(destByte, Base64.NO_WRAP);
        return destStr;
    }

    public static String decrypt(String srcStr) {
        String destStr = "";

        CryptoClass cryptoClass = new CryptoClass();
        try {
            byte[] srcByte = Base64.decode(srcStr, Base64.NO_WRAP);
            Log.d(THIS_FILE, "srcByte.length:" + srcByte.length);
            byte[] versionBytes = new byte[8];
            System.arraycopy(srcByte, 0, versionBytes, 0, 8);
            byte[] encrypted = new byte[srcByte.length - 8];
            System.arraycopy(srcByte, 8, encrypted, 0, srcByte.length - 8);
            Log.d(THIS_FILE, "encrypted.length:" + encrypted.length);
            Log.d(THIS_FILE, "versionBytes.length:" + versionBytes.length);

            long version = FormatUtil.bytesToLong(versionBytes);
            ArrayList<SessionKey> sessionKeys = SessionKeyUtil.getSessionKeyByVersion(version);
            Log.d(THIS_FILE, "version:" + version);
            if (sessionKeys != null && sessionKeys.size() > 0) {
                for (SessionKey sessionKey : sessionKeys) {
                    // 循环key
                    Log.d(THIS_FILE, "version:" + sessionKey.getVersion());
                    Log.d(THIS_FILE, "key:" + sessionKey.getKey());
                    byte[] destByte = cryptoClass.aeS_CBC_Decrypt(encrypted, Base64.decode(sessionKey.getKey(), Base64.NO_WRAP), null);
                    destStr = new String(destByte);
                    if (!TextUtils.isEmpty(destStr)) {
                        break;
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return destStr;
    }
}
