package com.apiherd.tenantdb;

import com.apiherd.api.APIRequest;

public class TenantUser extends TreedJson{
    public TenantUser() {
        super("UserId");
    }

    @Override
    public String getPrimaryKey(APIRequest request) {
        return request.getMeta().getUserId();
    }

    @Override
    public String getKeyFullPath(String userId, APIRequest request) {
        return userId;
    }
}
