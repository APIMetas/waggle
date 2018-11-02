package com.apiherd.tenantdb;

import java.util.Set;
import redis.clients.jedis.Jedis;

public class RedisHelper extends DBHelper {
    protected String host;
    private Jedis redis = null;

    public RedisHelper(String host) {
        super(host, "Redis");
    }

    @Override
    public void setupDB() {
        redis = new Jedis(this.host);
    }

    @Override
    public void closeDB() {
        this.redis.close();
    }

    @Override
    public void putJsonObject(String key, String value) {
        if (!this.redis.isConnected())
            this.redis.connect();

        int lastPos = key.lastIndexOf('/');
        if (0 >= lastPos)
            this.redis.set(key, value);
        else {
            String fatherKey = key.substring(0, lastPos);
            String primaryKey = key.substring(lastPos + 1);
            this.redis.zadd(fatherKey, 0, primaryKey + ":" + value);
        }
    }

    @Override
    public String getJsonObject(String key) {
        if (!this.redis.isConnected())
            this.redis.connect();
        int lastPos = key.lastIndexOf('/');
        if (0 >= lastPos)
            return redis.get(key);
        else {
            String fatherKey = key.substring(0, lastPos);
            String primaryKey = key.substring(lastPos + 1);
            Set<String> values = redis.zrangeByLex(fatherKey, "["+primaryKey, "+");
            if (values.size() > 0) {
                String value = values.iterator().next();
                return value.substring(primaryKey.length() + 2);
            } else
                return "{}";
        }
    }

    public String[] getDescendObjects(String key) {
        if (!this.redis.isConnected())
            this.redis.connect();

        Set<String> objs = this.redis.zrange(key, 0, -1);
        String[] arrays = objs.toArray(new String[1]);
        for (int i = 0; i < arrays.length; i++)
            arrays[i] = arrays[i].substring(arrays[i].indexOf(":") + 1);

        return arrays;
    }

    @Override
    public void deleteObject(String key) {
        if (!this.redis.isConnected())
            this.redis.connect();
        int lastPos = key.lastIndexOf('/');
        if (0 >= lastPos)
            redis.del(key);
        else {
            String fatherKey = key.substring(0, lastPos);
            String primaryKey = key.substring(lastPos + 1);
            redis.zremrangeByLex(fatherKey, "["+primaryKey, "+");
        }
    }
}
