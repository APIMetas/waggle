package com.apiherd.tenantdb;

import com.apiherd.api.DateHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;

import org.sqlite.SQLiteConfig;

public class RDBHelper extends DBHelper {
    protected RDBParser parser;
    protected Connection connection;
    protected SQLiteConfig config = new SQLiteConfig();
    protected static final Logger log = LoggerFactory.getLogger(RDBHelper.class);

    public RDBHelper(String dirPath,
                     String dbName) {
        super(dirPath, dbName);
        if (null != this.dbPath)
            this.dbName = super.dbPath + "/" + super.dbName;
        this.parser = new RDBParser();
    }

    public RDBHelper(String dirPath,
                     String dbName,
                     RDBParser parser) {
        super(dirPath, dbName);
        if (null != this.dbPath)
            this.dbName = super.dbPath + "/" + super.dbName;
        this.parser = parser;
    }

    @Override
    public void setupDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            config.setEncoding(SQLiteConfig.Encoding.UTF8);
            config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
            config.setJournalMode(SQLiteConfig.JournalMode.OFF);
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName, config.toProperties());
        } catch (ClassNotFoundException exp) {
            log.error("", exp);
        } catch (SQLException exp) {
            log.error("", exp);
        }
    }

    public Connection getNewConnection() {
        try {
            Connection cnnt = DriverManager.getConnection("jdbc:sqlite:" + dbName, config.toProperties());
            return cnnt;
        } catch (Exception exp) {
            log.error("", exp);
        }
        return  null;
    }

    @Override
    public void closeDB() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException exp) {
            log.error("", exp);
        }
    }

    private JSONObject convertJson(ResultSet set) {
        try {
            ResultSetMetaData rsmd = set.getMetaData();
            if (set.next()) {
                int numColumns = rsmd.getColumnCount();
                JSONObject obj = new JSONObject();
                for (int i = 1; i <= numColumns; i++) {
                    String column_name = rsmd.getColumnName(i);
                    obj.put(column_name, set.getObject(column_name));
                }
                return obj;
            }
        } catch (Exception exp) {
            log.error("", exp);
        }
        return null;
    }

    private JSONObject convertJsonArray(String arrayName, String keyName, ResultSet set) {
        try {
            String tokenId = "0";
            JSONObject father = new JSONObject();
            JSONArray array = new JSONArray();
            JSONObject conv = convertJson(set);
            while (null != conv) {
                array.put(conv);
                Object keyValue = conv.opt(keyName);
                tokenId = keyValue == null ? "0":keyValue.toString();
                conv = convertJson(set);
            }

            father.put("CurrentSize", array.length());
            father.put("NextToken", tokenId);
            father.put(arrayName, array);
            return father;
        } catch (Exception exp) {
            log.error("", exp);
        }
        return null;
    }

    private StringBuilder getSelect(String method,
                                    String ownerId,
                                    String tableName,
                                    JSONObject value) {
        StringBuilder sql = new StringBuilder(method).append(" from ");
        sql.append(tableName).append(" where OwnerId=").append(ownerId);
        HashMap<String, String> keys = parser.created(tableName);
        for (String key : value.keySet()) {
            String append = keys.get(key);
            String item = value.getString(key);
            sql.append(" and ").append(key).append("=").append(append).append(item).append(append);
        }
        return sql;
    }

    public String parseResource2Row(String ownerId,
                                    String tbName,
                                    JSONObject json,
                                    HashMap<String, String> append) {
        StringBuilder strV = new StringBuilder(" values (").append(ownerId).append(",");
        StringBuilder strB = new StringBuilder("insert or replace into ").append(tbName).append(" (OwnerId,");
        for (String tmp:append.keySet()) {
            String end = append.get(tmp);
            Object value = json.opt(tmp);
            if (null == value) {
                log.error("Can not find "+ tmp);
                continue;
            }
            strB.append(tmp).append(",");
            strV.append(end).append(value).append(end).append(",");
        }
        strB.deleteCharAt(strB.length() - 1);
        strV.deleteCharAt(strV.length() - 1);
        strB.append(")").append(strV).append(")");

        return strB.toString();
    }

    public JSONObject searchJsonArray(String ownerId, TreedJson tree, String sql) {
        ResultSet set = null;
        Connection cnnt = null;
        try {
            cnnt = this.getNewConnection();
            set = cnnt.createStatement().executeQuery(sql);
            JSONObject obj = convertJsonArray(tree.getClass().getSimpleName()+"s", tree.getKeyName(), set);
            set.close();
            cnnt.close();
            return obj;
        } catch(Exception exp) {
            log.error("", exp);
        } finally {
            try {
                if (set != null && !set.isClosed())
                    set.close();
            } catch (Exception exp) {
                ;
            }
        }

        return null;
    }

    @Override
    public JSONObject getJsonArray(String ownerId, TreedJson tree, JSONObject value) {
        boolean isAsc = false;
        String tableName = tree.getClass().getSimpleName();
        String tokenId = value.optString("NextToken");

        if (!"".equals(tokenId)) {
            String[] parts = tokenId.split("/");
            tokenId = parts[1];
            isAsc = parts[0].equals("asc");
        } else
            tokenId = "0";
        int limit = value.optInt("Limit", 10);
        HashMap<String, String> keys = (HashMap<String, String>)parser.created(tableName).clone();
        String append = keys.get(tree.getKeyName());
        keys.remove(tree.getKeyName());
        StringBuilder sql = new StringBuilder("select * from ").append(tableName);
        sql.append(" where OwnerId=").append(ownerId);
        TreedJson father = tree.getFather();
        while(null != father.getFather()) {
            String keyAppend = keys.get(father.getKeyName());
            sql.append(" and ").append(father.getKeyName()).append("=")
                    .append(keyAppend).append(value.optString(father.getKeyName())).append(keyAppend);
            father = father.getFather();
        }
        sql.append(" and ").append(tree.getKeyName());
        if (isAsc) {
            sql.append(" > ").append(append).append(tokenId).append(append);
            sql.append(" order by ").append(tree.getKeyName()).append(" asc");
        } else {
            sql.append(" > ").append(append).append(tokenId).append(append);
            sql.append(" order by ").append(tree.getKeyName()).append(" desc");
        }
        sql.append(" limit ").append(limit);

        String sqlStr = sql.toString();
        JSONObject getted = this.searchJsonArray(ownerId, tree, sql.toString());
        String token = getted.get("NextToken").toString();
        if (sqlStr.contains("desc"))
            getted.put("NextToken","desc/"+token);
        else
            getted.put("NextToken","asc/"+token);

        return getted;
    }

    @Override
    public void putJsonObject(String ownerId, TreedJson tree, JSONObject value) {
        super.putJsonObject(ownerId, tree, value);
        String tableName = tree.getClass().getSimpleName();
        HashMap<String, String> keys = parser.createTable(tableName, tree, this.connection, value);
        try {
            this.connection.createStatement().execute(this.parseResource2Row(ownerId, tableName, value, keys));
        } catch (Exception exp) {
            log.error("", exp);
        }
    }

    @Override
    public void putJsonArray(String ownerId, TreedJson tree, JSONArray array) {
        for (Iterator<Object> it = array.iterator(); it.hasNext(); ) {
            JSONObject obj = (JSONObject) it.next();
            obj.put("LastUpdate", DateHelper.getISO8601Time(null));
            this.putJsonObject(ownerId, tree, obj);
        }
    }

    @Override
    public JSONObject getJsonObject(String ownerId, TreedJson tree, JSONObject value) {
        ResultSet set = null;
        Connection cnnt = null;
        String tableName = tree.getClass().getSimpleName();
        StringBuilder sql = getSelect("select *", ownerId, tableName, value);
        try {
            cnnt = this.getNewConnection();
            set = cnnt.createStatement().executeQuery(sql.toString());
            JSONObject obj = convertJson(set);
            return obj;
        } catch(Exception exp) {
            log.error("", exp);
        } finally {
            try {
                if (set != null && !set.isClosed())
                    set.close();
            } catch (Exception exp) {
                ;
            }
        }

        return new JSONObject("{}");
    }

    @Override
    public void deleteObject(String ownerId, TreedJson tree, JSONObject value) {
        String tableName = tree.getClass().getSimpleName();
        StringBuilder sql = getSelect("delete", ownerId, tableName, value);
        try {
            connection.createStatement().execute(sql.toString());
        } catch(Exception exp) {

        }
    }
}
