package com.apiherd.tenantdb;

import org.json.JSONObject;

import java.sql.Connection;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class TokendJsonParser extends RDBJsonParser {

    public TokendJsonParser(TenantUser keyPath, String tbName) {
        super(keyPath, tbName);
    }

    @Override
    public void createRDB(Connection connect) {
        this.getItems();
        Iterator<TreedJson> ite = keys.getChilds().iterator();
        StringBuilder strB = new StringBuilder("create table if not exists ");
        strB.append(this.tbName).append(" (TokenId INTEGER PRIMARY KEY, UserId nvarchar(32)");
        while(ite.hasNext()) {
            TreedJson json = ite.next();
            strB.append(",").append(json.getKeyName()).append(" nvarchar(64)");
            ite = json.getChilds().iterator();
        }
        for(String item:items.keySet())
            strB.append(",").append(item).append(" ").append(items.get(item));
        strB.append(")");
        try {
            connect.createStatement().execute(strB.toString());
            connect.createStatement().execute(
                    "create index if not exists index_datetime ON " + tbName + "(LastUpdate)");
            ite = keys.getChilds().iterator();
            while(ite.hasNext()) {
                TreedJson json = ite.next();
                connect.createStatement().execute(
                        "create index if not exists index_"+json.getKeyName().toLowerCase()
                                + " ON " + tbName + " ("+json.getKeyName()+")");
                ite = json.getChilds().iterator();
            }
        } catch (Exception exp) {
            log.error("", exp);
        }
    }

    @Override
    public void getItems() {
        if (super.items.size() == 0) {
            super.addItem("JSONText", "text");
            super.addItem("LastUpdate", "nvarchar(24)");
        }
    }

    public static JSONObject getJsonObject(JSONObject row) {
        String str = row.getString("JSONText");
        return new JSONObject(str);
    }

    @Override
    public LinkedHashMap<String, String> parseJson(String json) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("JSONText", "'"+json+"'");
        return map;
    }
}
