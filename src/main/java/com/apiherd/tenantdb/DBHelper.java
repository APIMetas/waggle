package com.apiherd.tenantdb;

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

    public abstract void putJsonObject(String key, String value);

    public abstract String getJsonObject(String key);

    public abstract void deleteObject(String key);

}
