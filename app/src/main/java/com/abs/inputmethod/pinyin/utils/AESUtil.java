package com.abs.inputmethod.pinyin.utils;

import android.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    private static SecretKeySpec secretKeySpec;

    public static SecretKeySpec createAESKey(String password, String address) {
        if (secretKeySpec != null)
            return secretKeySpec;
        try {
            byte[] key = (password + address).getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            byte[] data = new byte[16];
            System.arraycopy(key, 0, data, 0, 16);

            secretKeySpec = new SecretKeySpec(data, "AES");

            return secretKeySpec;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static String encrypt(String srcStr, SecretKeySpec secretKeySpec) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            // 加密
            return Base64.encodeToString(cipher.doFinal(srcStr.getBytes("UTF-8")), Base64.NO_WRAP);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String decrypt(String encrypted, SecretKeySpec secretKeySpec) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            // 初始化解密器
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            // 解密
            return new String(cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return "";
    }
}
