package com.apiherd.tenantdb;

import org.json.JSONObject;

import java.util.Set;
import java.util.HashMap;
import java.util.Collection;

public abstract class TreedJson {
    private String key;
    private TreedJson father;
    private HashMap<String, TreedJson> childArrays;

    public TreedJson(String key) {
        this.key = key;
        this.childArrays = new HashMap<>();
    }

    public TreedJson(TreedJson father, String key) {
        this(key);
        this.father = father;
        if (null != father)
            this.father.addChild(this);
    }

    public Set<String> getChildArrayNames() {
        return this.childArrays.keySet();
    }

    public Collection<TreedJson> getChilds() {
        return this.childArrays.values();
    }

    public TreedJson findChild(String name) {
        return childArrays.get(name);
    }

    public TreedJson getFather() {
        return this.father;
    }

    public void addChild(TreedJson child) {
        this.childArrays.put(child.getClass().getSimpleName() + "s", child);
    }

    public String getKeyName() {
        return this.key;
    }

    public String getPrimaryKey(String userId, JSONObject request) {
        Object value = request.opt(this.key);
        if (null == value)
            return "+";
        return value.toString();
    }

    public String getKeyFullPath(String userId, JSONObject biz) {
        String prefix = this.father.getKeyFullPath(userId, biz);
        prefix += "/" + this.getClass().getSimpleName() + "s/" + this.getPrimaryKey(userId, biz);
        return  prefix;
    }
}
