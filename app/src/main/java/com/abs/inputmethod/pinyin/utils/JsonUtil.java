package com.abs.inputmethod.pinyin.utils;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtil {

    private static final String THIS_FILE = "JsonUtil";

    /**
     * < 写这个主要是因为修改<T> T转成的的String后要反转成<T> T存进包装类里，所以可以看作是objectToJsonString的反操作 > <br>
     *
     * @param content   < 这里希望String是objectToJsonString直接转来的,或者手动造的有合理结构的String也阔以 >
     * @param valueType < 待反转的T的class >
     * @return < >
     * @auther: tang
     */
    public static <T> T StringToObject(String content, Class<T> valueType) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return (T) objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            Log.i(THIS_FILE, "StringToObject failed!");
            return null;
        }
    }
}
