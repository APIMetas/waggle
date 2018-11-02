package com.apiherd.tenantdb;

import com.apiherd.api.APIRequest;

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
        this.key = key;
        this.father = father;
        this.childArrays = new HashMap<>();

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

    public void addChild(TreedJson child) {
        this.childArrays.put(child.getClass().getSimpleName() + "s", child);
    }

    public String getKeyName() {
        return this.key;
    }

    public String getPrimaryKey(APIRequest request) {
        String value = (String)request.getBiz().opt(this.key);
        if (null == value)
            return "+";
        return value;
    }

    public String getKeyFullPath(String userId, APIRequest request) {

        String prefix = this.father.getKeyFullPath(userId, request);

        prefix += "/" + this.getClass().getSimpleName() + "s/" + this.getPrimaryKey(request);

        return  prefix;
    }
}
