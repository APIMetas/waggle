package com.apiherd.tenantdb;

import java.util.Set;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.apiherd.api.APIable;
import com.apiherd.api.APIRequest;
import com.apiherd.api.RawsRequest;
import com.apiherd.api.WriteableChannel;
import com.apiherd.waggle.BuffedInvokePool;

public class BDBTree extends BDBHelper {

    public BDBTree(String dirPath, String dbName) {
        super(dirPath, dbName);
    }

    public JSONObject putJson(TreedJson tree, APIRequest request) {
        String userId = request.getMeta().getUserId();
        KeyHelper.putLastUpdate(request);
        this.putJsonObject(userId, tree, request.getBiz());

        String key = tree.getKeyFullPath(userId, request.getBiz());
        return new JSONObject(request.getShortResponse(key));
    }

    public JSONObject deleteJson(TreedJson tree, APIRequest request) {
        String userId = request.getMeta().getUserId();
        this.deleteObject(userId, tree, request.getBiz());

        String key = tree.getKeyFullPath(userId, request.getBiz());
        return new JSONObject(request.getShortResponse(key));
    }

    public JSONObject getJsons(TreedJson tree, APIRequest request) {
        String userId = request.getMeta().getUserId();
        JSONObject object = this.getJsonArray(userId, tree, request.getBiz());
        object.put("RequestId", request.getMeta().getRequestId());

        return object;
    }

    public JSONObject getJson(TreedJson tree, APIRequest request) {
        String userId = request.getMeta().getUserId();
        String key = tree.getKeyFullPath(userId, request.getBiz());

        Set<String> childs = tree.getChildArrayNames();
        JSONObject object = this.getJsonObject(userId, tree, request.getBiz());
        for (String child:childs) {
            List<String> founds = this.getDescendObjects(key + "/" + child);
            JSONArray array = new JSONArray();
            for(String str:founds)
                array.put(new JSONObject(str));
            object.put(child, array);
        }
        object.put("RequestId", request.getMeta().getRequestId());

        return object;
    }

    public void registerResource(TreedJson base, BuffedInvokePool pool) {
        String strName = base.getClass().getSimpleName();
        pool.registerAPI("Put" + strName, new APIable() {
            @Override
            public JSONObject invokeAPI(RawsRequest request, WriteableChannel channel) {
                return putJson(base, (APIRequest) request); }}, true);
        pool.registerAPI("Get" + strName, new APIable() {
            @Override
            public JSONObject invokeAPI(RawsRequest request, WriteableChannel channel) {
                return getJson(base, (APIRequest) request); }});
        pool.registerAPI("Delete" + strName, new APIable() {
            @Override
            public JSONObject invokeAPI(RawsRequest request, WriteableChannel channel) {
                return deleteJson(base, (APIRequest) request); }}, true);
        pool.registerAPI("Get" + strName +"s", new APIable() {
            @Override
            public JSONObject invokeAPI(RawsRequest request, WriteableChannel channel) {
                return getJsons(base, (APIRequest) request); }});
    }

}
