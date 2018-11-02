package com.apiherd.opern;

import java.util.ArrayList;

public class JsonKey {
    private String key;
    private int index;

    public JsonKey() {
        this.index = -1;
    }

    public JsonKey(String key) {
        this.key = key;
        this.index = -1;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public static JsonKey parseKey(String key) {
        String trimKey = key.trim();
        int len = trimKey.length();

        if (trimKey.charAt(len - 1) != (']'))
            return new JsonKey(trimKey);

        int arrayIndex = trimKey.indexOf('[');
        if (-1 == arrayIndex)
            return new JsonKey(trimKey);
        JsonKey jsKey = new JsonKey(trimKey.substring(0, arrayIndex));
        jsKey.setIndex(Integer.valueOf(trimKey.substring(arrayIndex + 1, len - 1)));

        return jsKey;
    }

    public static ArrayList<JsonKey> parseKeys(String key) {
        ArrayList<JsonKey> array = new ArrayList<>();
        String[] keys = key.split("\\.");

        for(String jsKey:keys) {
            array.add(JsonKey.parseKey(jsKey));
        }

        return array;
    }
}
