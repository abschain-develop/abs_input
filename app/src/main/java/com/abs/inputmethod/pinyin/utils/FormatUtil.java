package com.abs.inputmethod.pinyin.utils;

import android.text.TextUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class FormatUtil {

    private static final String THIS_FILE = "FormatUtil";

    /**
     * 是否经过Base64编码
     *
     * @param str
     * @return
     */
    public static boolean isBase64(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        return Pattern.matches(base64Pattern, str);
    }

    /**
     * 判断字符串是不是十六进制
     *
     * @param str
     * @return
     */
    public static boolean isHexString(String str) {
        String regex = "^[A-Fa-f0-9]+$";
        return Pattern.matches(regex, str);
    }

    /**
     * 判断是不是以太坊公钥格式
     *
     * @param str
     * @return
     */
    public static boolean isPubKey(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        boolean isHexString = isHexString(str);
        if (isHexString) {
            // 检查字符串长度
            return str.length() == 128;
        }

        return false;
    }

    //byte 数组与 long 的相互转换
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        Log.d(THIS_FILE, " bytes.length:" + bytes.length + "," + buffer.limit());
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    /**
     * 判断一个字符串的首字符是否为字母
     *
     * @param s
     * @return
     */
    public static boolean isLetter(String s) {
        char c = s.charAt(0);
        int i = (int) c;
        if ((i >= 65 && i <= 90) || (i >= 97 && i <= 122)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getTime() {
        String format = "yyyy-MM-dd-HH-mm-ss";
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 将long 转换成 yyyy-MM-dd HH:mm:ss
     *
     * @param timestamp
     * @return
     */
    public static String getDateAndTime(long timestamp) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        if (timestamp != 0)
            date.setTime(timestamp);
        return dateFormat.format(date);
    }


}
