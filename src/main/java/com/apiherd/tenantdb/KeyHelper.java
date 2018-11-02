package com.apiherd.tenantdb;

import com.apiherd.api.APIRequest;
import com.apiherd.api.DateHelper;

import org.json.JSONObject;

public class KeyHelper {

    public static  String getFatherKey(String primKey) {
        String strPath = primKey;
        int lastPos = strPath.lastIndexOf('/');
        if (0 >= lastPos)
            return "";

        return strPath.substring(0, lastPos);
    }

    public static void putLastUpdate(APIRequest request) {
        JSONObject obj = request.getBiz();
        if (null != obj)
            if (null == obj.opt("LastUpdate"))
                obj.put("LastUpdate", DateHelper.getISO8601Time(null));

    }

    public static StringBuilder concatDescends(StringBuilder sb, String key, String[] array) {
        if (null == array || 0 == array.length) {
            return sb;
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(",\"").append(key).append("\":[");
        for(String strStack:array)
            sb.append(strStack).append(",");
        sb.deleteCharAt(sb.length() - 1).append("]}");
        return sb;
    }
}
