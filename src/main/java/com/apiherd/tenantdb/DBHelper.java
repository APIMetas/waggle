package com.apiherd.tenantdb;

import com.apiherd.api.APIRequest;
import com.apiherd.api.APIable;
import com.apiherd.api.RawsRequest;
import com.apiherd.api.WriteableChannel;
import com.apiherd.waggle.BuffedInvokePool;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class DBHelper {
    protected String dbPath;
    protected String dbName;

    public DBHelper(String dirPath, String dbName) {
        this.dbPath = dirPath;
        this.dbName = dbName;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public abstract void setupDB();

    public abstract void closeDB();

    public abstract JSONObject getJsonObject(String ownerId, TreedJson tree, JSONObject value);

    public abstract JSONObject getJsonArray(String ownerId, TreedJson tree, JSONObject value);

    public void putJsonObject(String ownerId, TreedJson tree, JSONObject value) {
        if (tree instanceof TreedIdedJson && tree.getPrimaryKey(ownerId, value).equals("+"))
            value.put(tree.getKeyName(), ((TreedIdedJson)tree).getUniqueId());
    }

    public abstract void putJsonArray(String ownerId, TreedJson tree, JSONArray array);

    public abstract void deleteObject(String ownerId, TreedJson tree, JSONObject value);
}
