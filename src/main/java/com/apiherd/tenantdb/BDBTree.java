package com.apiherd.tenantdb;

import java.util.Set;

import com.apiherd.api.APIRequest;
import com.apiherd.api.APIable;
import com.apiherd.api.WriteableChannel;
import com.apiherd.waggle.BuffedInvokePool;
import com.apiherd.api.RawsRequest;

public class BDBTree extends BDBHelper {

    public BDBTree(String dirPath, String dbName) {
        super(dirPath, dbName);
    }

    public String putJson(TreedJson tree, APIRequest request) {
        String userId = request.getMeta().getUserId();
        String key = tree.getKeyFullPath(userId, request);

        KeyHelper.putLastUpdate(request);

        this.putJsonObject(key, request.getBiz().toString());

        return request.getShortResponse(key);
    }

    public String deleteJson(TreedJson tree, APIRequest request) {
        String userId = request.getMeta().getUserId();
        String key = tree.getKeyFullPath(userId, request);
        this.deleteObject(key);

        return request.getShortResponse(key);
    }

    public String searchJsons(TreedJson tree, APIRequest request) {
        String name = tree.getClass().getSimpleName() + "s";
        String key = request.getBiz().getString("SearchKey");

        StringBuilder sb = new StringBuilder("{\"RequestId\":\"");
        sb.append(request.getMeta().getRequestId()).append("\"}");
        KeyHelper.concatDescends(sb, name, this.searchJsons(key));

        return sb.toString();
    }

    public String getJson(TreedJson tree, APIRequest request) {
        String userId = request.getMeta().getUserId();
        String key = tree.getKeyFullPath(userId, request);
        String father = this.getJsonObject(key);

        StringBuilder sb = new StringBuilder("{\"RequestId\":\"");
        sb.append(request.getMeta().getRequestId());
        if (null == father || father.equals(""))
            return sb.append("}").toString();

        if (father.trim().length() > 2)
            sb.append("\",");
        else
            sb.append("\"");
        sb.append(father.substring(1));
        Set<String> childs = tree.getChildArrayNames();
        for (String child:childs) {
            String[] subArray = this.getDescendObjects(key + "/" + child);
            KeyHelper.concatDescends(sb, child, subArray);
        }

        return sb.toString();
    }

    public void registerResource(TreedJson base, BuffedInvokePool pool) {
        String strName = base.getClass().getSimpleName();
        pool.registerAPI("Put" + strName, new APIable() {
            @Override
            public String invokeAPI(RawsRequest request, WriteableChannel channel) {
                return putJson(base, (APIRequest) request); }});
        pool.registerAPI("Get" + strName, new APIable() {
            @Override
            public String invokeAPI(RawsRequest request, WriteableChannel channel) {
                return getJson(base, (APIRequest) request); }});
        pool.registerAPI("Delete" + strName, new APIable() {
            @Override
            public String invokeAPI(RawsRequest request, WriteableChannel channel) {
                return deleteJson(base, (APIRequest) request); }});
        pool.registerAPI("Search" + strName +"s", new APIable() {
            @Override
            public String invokeAPI(RawsRequest request, WriteableChannel channel) {
                return searchJsons(base, (APIRequest) request); }});
    }

}
