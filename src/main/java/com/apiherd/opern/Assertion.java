package com.apiherd.opern;

import com.apiherd.api.DateHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

public class Assertion {
    private String method;
    private String key;
    private String value;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static Assertion parseAssertion(LinkedHashMap<String, Object> objs) {
        Assertion ass = new Assertion();
        ass.setKey(objs.get("Key").toString());
        ass.setMethod(objs.get("Method").toString());
        if (objs.get("Value") instanceof Date)
            ass.setValue(DateHelper.getISO8601Time((Date)objs.get("Value")));
        else
            ass.setValue(objs.get("Value").toString());
        return ass;
    }

    public boolean makeAssert(String response) {
        String jsValue = "";
        JSONObject obj = new JSONObject(response);
        ArrayList<JsonKey> list = JsonKey.parseKeys(this.key);

        JSONObject fatherObj = obj;
        for(int i = 0; i < list.size() - 1; i ++) {
            JsonKey jsKey = list.get(i);
            if (jsKey.getIndex() >= 0)
                fatherObj = fatherObj.getJSONArray(jsKey.getKey()).getJSONObject(jsKey.getIndex());
            else
                fatherObj = fatherObj.getJSONObject(jsKey.getKey());
        }

        JsonKey jsKey = list.get(list.size() - 1);
        if (jsKey.getIndex() > 0) {
            JSONArray jArrays = fatherObj.getJSONArray(jsKey.getKey());
            jsValue = jArrays.getString(jsKey.getIndex());
        } else
            jsValue = fatherObj.getString(jsKey.getKey());

        if (this.method.equals("Equal"))
            return jsValue.equals(this.value);
        else if (this.method.equals("Contains"))
            return jsValue.contains(this.value);
        else
            return true;
    }
}
