package com.apiherd.tenantdb;

import com.apiherd.api.APIRequest;
import com.apiherd.api.APIable;
import com.apiherd.api.DateHelper;
import com.apiherd.api.WriteableChannel;
import com.apiherd.waggle.BuffedInvokePool;
import com.apiherd.api.RawsRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

public class RDBTree extends RDBHelper {

    public RDBTree(String dirPath, String dbName, RDBJsonParser parser) {
        super(dirPath, dbName, parser);
    }

    private String findLastOwner(String key) {
        int pos = key.lastIndexOf("/");
        int beg = key.lastIndexOf("/", pos - 1);
        if (-1 == beg)
            beg = 0;

        return key.substring(beg + 1, pos);
    }

    public String putJson(TreedJson tree, APIRequest request) {
        String userId = request.getMeta().getUserId();
        String key = tree.getKeyFullPath(userId, request);

        KeyHelper.putLastUpdate(request);
        this.putJsonObject(key, request.getBiz().toString());

        return request.getShortResponse(key);
    }

    public String getJson(TreedJson tree, APIRequest request) {
        String userId = request.getMeta().getUserId();
        String key = tree.getKeyFullPath(userId, request);

        String strObj = this.getJsonObject(key);
        int intPos = strObj.lastIndexOf("}");

        return strObj.substring(0, intPos) +
                ", \"RequestId\":\""+request.getMeta().getRequestId() +"\"}";
    }

    public String deleteJson(TreedJson tree, APIRequest request) {
        String userId = request.getMeta().getUserId();
        String key = tree.getKeyFullPath(userId, request);
        this.deleteObject(key);

        return request.getShortResponse(key);
    }

    public String getTokenedJsons(TreedJson tree, APIRequest request) {
        long nextToken = 0;
        String userId = request.getMeta().getUserId();
        String key = tree.getKeyFullPath(userId, request);
        String token = (String)request.getBiz().opt("NextToken");
        String limit = (String)request.getBiz().opt("LimitRows");

        if (null == limit)
            limit = "0";
        JSONArray array = this.getTokenedArray(key, token, Long.valueOf(limit));
        if (array.length() > 0) {
            JSONObject obj = (JSONObject) array.get(array.length() - 1);
            nextToken = obj.getLong("TokenId");
        }

        String strName = tree.getClass().getSimpleName();
        if (tree.getChilds().size() > 0)
            strName = tree.getChilds().iterator().next().getClass().getSimpleName();
        StringBuilder strB = new StringBuilder("{\"RequestId\":\"").append(request.getMeta().getRequestId()).
                append("\",\"NextToken\":\"").append(nextToken).append("\", \"").
                append("RowsNumber\":").append(array.length()).append(",\"").
                append(strName).append("s\":").append(array.toString()).append("}");

        return strB.toString();
    }

    public String getJsons(TreedJson tree, APIRequest request) {
        String userId = request.getMeta().getUserId();
        String key = tree.getKeyFullPath(userId, request);
        String timeEnd = (String) request.getBiz().opt("TimeEnd");
        String timeBegin = (String) request.getBiz().opt("TimeBegin");

        if (null == timeBegin)
            return "{}";
        if (null == timeEnd)
            timeEnd = DateHelper.getISO8601Time(null);
        if ("".equals(timeBegin)) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, -24);
            timeBegin = DateHelper.getISO8601Time(cal.getTime());
        }
        String ownerKey = findLastOwner(key);
        String father = this.getJsonArray(key, timeBegin, timeEnd);
        int intLast = father.lastIndexOf("LastUpdate");
        intLast = father.indexOf("\"",intLast + 1);
        intLast = father.indexOf("\"",intLast + 1);
        String next = father.substring(intLast + 1, father.indexOf("\"", intLast + 1));

        return "{\""+ownerKey+"\":"+father+
                ",\"RequestId\":\""+request.getMeta().getRequestId()+
                "\",\"NextToken\":\""+next+"\"}";
    }

    public void registerResourceAPIs(String name, TreedJson base, BuffedInvokePool pool) {
        pool.registerAPI("Put" + name, new APIable() {
            @Override
            public String invokeAPI(RawsRequest request, WriteableChannel channel) {
                return putJson(base, (APIRequest) request); }});

        pool.registerAPI("Get" + name, new APIable() {
            @Override
            public String invokeAPI(RawsRequest request, WriteableChannel channel) {
                return getJson(base, (APIRequest) request); }});

        pool.registerAPI("Delete" + name, new APIable() {
            @Override
            public String invokeAPI(RawsRequest request, WriteableChannel channel) {
                return deleteJson(base, (APIRequest) request); }});

        pool.registerAPI("Search" + name + "s", new APIable() {
            @Override
            public String invokeAPI(RawsRequest request, WriteableChannel channel) {
                return getJsons(base, (APIRequest) request); }});
    }
}
