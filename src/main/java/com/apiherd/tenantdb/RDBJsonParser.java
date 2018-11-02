package com.apiherd.tenantdb;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.*;

public abstract class RDBJsonParser {

    protected String tbName;
    protected TenantUser keys;
    protected HashMap<String, String> items;
    protected static final Logger log = LoggerFactory.getLogger(RDBJsonParser.class);

    public RDBJsonParser(TenantUser keyPath, String tbName) {
        this.keys = keyPath;
        this.tbName = tbName;
        this.items = new HashMap<>();
    }

    public String getTbName() {
        return tbName;
    }

    public void addItem(String key, String type) {
        this.items.put(key, type);
    }

    public abstract void getItems();

    public void createRDB(Connection connect) {
        this.getItems();
        Iterator<TreedJson> ite = keys.getChilds().iterator();
        StringBuilder strB = new StringBuilder("create table if not exists ");
        StringBuilder strKey = new StringBuilder("primary key (UserId");

        strB.append(this.tbName).append(" (UserId nvarchar(32)");
        while(ite.hasNext()) {
            TreedJson json = ite.next();
            strB.append(",").append(json.getKeyName()).append(" nvarchar(64)");
            strKey.append(",").append(json.getKeyName());
            ite = json.getChilds().iterator();
        }
        for(String item:items.keySet()) {
            strB.append(",").append(item).append(" ").append(items.get(item));
        }
        strB.append(", ").append(strKey).append("));");
        try {
            connect.createStatement().execute(strB.toString());
            connect.createStatement().execute(
                    "create index if not exists index_datetime ON " + tbName + "(LastUpdate)");
        } catch (Exception exp) {
            log.error("", exp);
        }
    }


    public String parseResource2Row(String key, String json) {
        LinkedHashMap<String, String> keys = this.parseKey(key);
        LinkedHashMap<String, String> maps = this.parseJson(json);
        StringBuilder strV = new StringBuilder(" values (");
        StringBuilder strB = new StringBuilder("insert or replace into ").append(this.tbName);
        strB.append(" (");
        for (String tmp:keys.keySet()) {
            strB.append(tmp).append(",");
            strV.append(keys.get(tmp)).append(",");
        }
        for (String tmp:maps.keySet()) {
            strB.append(tmp).append(",");
            strV.append(maps.get(tmp)).append(",");
        }
        strB.deleteCharAt(strB.length() - 1);
        strV.deleteCharAt(strV.length() - 1);
        strB.append(")").append(strV).append(")");

        return strB.toString();
    }

    public LinkedHashMap<String, String> parseKey(String resourcePath) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        String[] parts = resourcePath.split("/");

        TreedJson father = keys;
        map.put(keys.getKeyName(), "\'"+parts[0]+"\'");
        for (int i = 1; i < parts.length; i+=2) {
            father = father.findChild(parts[i]);
            map.put(father.getKeyName(), "\'"+parts[i + 1]+"\'");
        }

        return map;
    }

    public LinkedHashMap<String, String> parseJson(String json) {
        JSONObject father = new JSONObject(json);
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for(String key:items.keySet()) {
            Object tmp = father.opt(key);
            if (null == tmp)
                continue;
            if (tmp instanceof String)
                map.put(key, "\'"+tmp+"\'");
            else
                map.put(key, String.valueOf(tmp));
        }
        return map;
    }


}
