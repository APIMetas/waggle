package com.apiherd.tenantdb;

import com.apiherd.api.APIRequest;
import com.apiherd.api.APIable;
import com.apiherd.api.WriteableChannel;
import com.apiherd.waggle.BuffedInvokePool;
import com.apiherd.api.RawsRequest;
import org.json.JSONArray;
import org.json.JSONObject;

public class RDBTree extends RDBHelper {

    public RDBTree(String dirPath, String dbName) {
        super(dirPath, dbName);
    }

    public RDBTree(String dirPath, String dbName, RDBParser parser) {
        super(dirPath, dbName, parser);
    }

    public JSONObject putJson(TreedJson tree, APIRequest request) {
        String ownerId = request.getMeta().getOwnerId();
        KeyHelper.putLastUpdate(request);
        this.putJsonObject(ownerId, tree, request.getBiz());
        return new JSONObject(request.getShortResponse(tree.getKeyFullPath(ownerId, request.getBiz())));
    }

    public JSONObject putJsons(TreedJson tree, APIRequest request) {
        //KeyHelper.putLastUpdate(request);
        String ownerId = request.getMeta().getOwnerId();
        JSONArray array = request.getBiz().optJSONArray(tree.getClass().getSimpleName()+"s");
        if (null == array)
            return new JSONObject(request.getShortResponse(tree.getKeyFullPath(ownerId, request.getBiz())));
        this.putJsonArray(ownerId, tree, array);
        return new JSONObject(request.getShortResponse(tree.getKeyFullPath(ownerId, request.getBiz())));
    }

    public JSONObject getJson(TreedJson tree, APIRequest request) {
        String ownerId = request.getMeta().getUserId();
        JSONObject obj = this.getJsonObject(ownerId, tree, request.getBiz());
        obj.put("RequestId", request.getMeta().getRequestId());
        return obj;
    }

    public JSONObject deleteJson(TreedJson tree, APIRequest request) {
        String ownerId = request.getMeta().getUserId();
        this.deleteObject(ownerId, tree, request.getBiz());
        return new JSONObject(request.getShortResponse(tree.getKeyFullPath(ownerId, request.getBiz())));
    }

    public JSONObject selectJsons(TreedJson tree, APIRequest request, String sql) {
        String ownerId = request.getMeta().getUserId();
        JSONObject obj = this.searchJsonArray(ownerId, tree, sql);
        obj.put("RequestId", request.getMeta().getRequestId());

        return obj;
    }

    public JSONObject getJsons(TreedJson tree, APIRequest request) {
        String ownerId = request.getMeta().getUserId();
        JSONObject obj = this.getJsonArray(ownerId, tree, request.getBiz());
        obj.put("RequestId", request.getMeta().getRequestId());

        return obj;
    }

    public void registerResourceAPIs(String name, TreedJson base, BuffedInvokePool pool) {
        pool.registerAPI("Put" + name, new APIable() {
            @Override
            public JSONObject invokeAPI(RawsRequest request, WriteableChannel channel) {
                return putJson(base, (APIRequest) request); }}, true);

        pool.registerAPI("Put" + name + "s", new APIable() {
            @Override
            public JSONObject invokeAPI(RawsRequest request, WriteableChannel channel) {
                return putJsons(base, (APIRequest) request); }});

        pool.registerAPI("Get" + name, new APIable() {
            @Override
            public JSONObject invokeAPI(RawsRequest request, WriteableChannel channel) {
                return getJson(base, (APIRequest) request); }});

        pool.registerAPI("Delete" + name, new APIable() {
            @Override
            public JSONObject invokeAPI(RawsRequest request, WriteableChannel channel) {
                return deleteJson(base, (APIRequest) request); }}, true);

        pool.registerAPI("Get" + name + "s", new APIable() {
            @Override
            public JSONObject invokeAPI(RawsRequest request, WriteableChannel channel) {
                return getJsons(base, (APIRequest) request); }});
    }
}
