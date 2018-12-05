package com.apiherd.tenantdb;

import org.json.JSONObject;

public class TenantUser extends TreedIdedJson{
    public TenantUser() {
        super(null,"OwnerId", 100, 10);
    }

    @Override
    public String getPrimaryKey(String userId, JSONObject request) {
        return userId;
    }

    @Override
    public String getKeyFullPath(String userId, JSONObject request) {
        return userId;
    }
}
