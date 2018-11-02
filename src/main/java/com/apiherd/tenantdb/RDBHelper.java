package com.apiherd.tenantdb;

import com.apiherd.api.DateHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.Iterator;
import java.util.LinkedHashMap;

public abstract class RDBHelper extends DBHelper {

    protected RDBJsonParser parser;
    protected Connection connection;
    //protected PreparedStatement statement;
    protected static final Logger log = LoggerFactory.getLogger(RDBHelper.class);


    public RDBHelper(String dirPath, String dbName,  RDBJsonParser parser) {
        super(dirPath, dbName);
        this.parser = parser;
    }

    @Override
    public void setupDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbName = super.dbPath + "/" + super.dbName;
            SQLiteConfig config = new SQLiteConfig();
            config.setEncoding(SQLiteConfig.Encoding.UTF8);
            config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName, config.toProperties());
            this.parser.createRDB(connection);
            //this.statement = connection.prepareStatement(parser.getInsertSQL());

        } catch (ClassNotFoundException exp) {
            log.error("", exp);
        } catch (SQLException exp) {
            log.error("", exp);
        }
    }

    @Override
    public void closeDB() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException exp) {
            log.error("", exp);
        }
    }

    @Override
    public void putJsonObject(String key, String value) {
        try {
            String insert = parser.parseResource2Row(key, value);
            this.connection.createStatement().execute(insert);
        } catch (Exception exp){
            log.error("", exp);
        }
    }

    private JSONArray convertJsonArray(ResultSet set) {
        try {
            JSONArray json = new JSONArray();
            ResultSetMetaData rsmd = set.getMetaData();
            while (set.next()) {
                int numColumns = rsmd.getColumnCount();
                JSONObject obj = new JSONObject();
                for (int i = 1; i <= numColumns; i++) {
                    String column_name = rsmd.getColumnName(i);
                    obj.put(column_name, set.getObject(column_name));
                }
                json.put(obj);
            }
            return json;
        } catch (Exception exp) {
            log.error("", exp);
        }
        return null;
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

    private StringBuilder getSelect(String method, LinkedHashMap<String, String> map) {
        StringBuilder strB = new StringBuilder(method);
        strB.append(" from ").append(parser.tbName).append(" where ");
        Iterator<String> ite = map.keySet().iterator();
        String tmp = ite.next();
        strB.append(tmp).append("=").append(map.get(tmp));
        while(ite.hasNext()) {
            tmp = ite.next();
            String strValue = map.get(tmp);
            if ("'+'".equals(strValue))
                continue;
            strB.append(" and ").append(tmp);
            strB.append("=").append(map.get(tmp));
        }
        return strB;
    }

    @Override
    public String getJsonObject(String key) {
        StringBuilder sql = getSelect("select * ", parser.parseKey(key));

        try {
            ResultSet set = connection.createStatement().executeQuery(sql.toString());
            JSONObject obj = convertJson(set);

            return obj.toString();
        } catch(Exception exp) {
            log.error("", exp);
        }

        return "{}";
    }

    public String getJsonArray(String key, String timeBegin, String timeEnd) {
        StringBuilder sql = getSelect("select * ", parser.parseKey(key));
        if (null == timeEnd || "".equals(timeEnd))
            timeEnd = DateHelper.getISO8601Time(null);
        sql.append(" and LastUpdate > \'").append(timeBegin).append("\'");
        sql.append(" and LastUpdate <= \'").append(timeEnd).append("\' limit 10240");

        try {
            ResultSet set = connection.createStatement().executeQuery(sql.toString());
            return convertJsonArray(set).toString();
        } catch(Exception exp) {
            log.error("", exp);
        }

        return "[]";
    }

    public JSONArray getTokenedArray(String key, String token, long limit) {
        LinkedHashMap<String, String> map = null;
        StringBuilder strB = null;
        if ("".equals(key))
            strB = new StringBuilder("select rowid as TokenId, * from ").
                    append(parser.tbName).
                    append(" where rowid >  ").append(token);
        else
            strB = getSelect("select rowid as TokenId, * ", parser.parseKey(key)).
                    append(" and TokenId > ").append(token);
        if (0 == limit)
            strB.append(" limit 10240");
        else
            strB.append(" limit ").append(limit);

        try {
            ResultSet set = connection.createStatement().executeQuery(strB.toString());
            return convertJsonArray(set);
        } catch(Exception exp) {
            log.error("", exp);
        }

        return new JSONArray("[]");
    }

    @Override
    public void deleteObject(String key) {
        StringBuilder sql = getSelect("delete ", parser.parseKey(key));
        try {
            connection.createStatement().execute(sql.toString());
        } catch(Exception exp) {

        }
    }
}
