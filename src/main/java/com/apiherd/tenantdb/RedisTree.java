package com.apiherd.tenantdb;

import com.apiherd.api.APIRequest;
import com.apiherd.api.APIable;
import com.apiherd.api.RawsRequest;
import com.apiherd.api.WriteableChannel;
import com.apiherd.waggle.BuffedInvokePool;

import java.nio.channels.ByteChannel;
import java.util.Set;

public class RedisTree extends RedisHelper {

    public RedisTree(String host) {
        super(host);
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

    public String deleteJson(TreedJson tree, APIRequest request) {
        String userId = request.getMeta().getUserId();
        String key = tree.getKeyFullPath(userId, request);
        this.deleteObject(key);

        return request.getShortResponse(key);
    }

    private void registerResourceAPIs(String name, TreedJson base, BuffedInvokePool pool) {
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
    }

    public void registerResource(TreedJson base, BuffedInvokePool pool) {
        String strName = base.getClass().getSimpleName();
        this.registerResourceAPIs(strName, base, pool);

    }
}
