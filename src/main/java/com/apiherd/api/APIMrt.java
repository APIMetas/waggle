package com.apiherd.api;

import org.json.JSONObject;

public class APIMrt extends Numbered {
    private long count = 0;

    public APIMrt() {
        super("Responses", "MS");
    }

    @Override
    public synchronized void incrBy(long num) {
        this.count ++;
        this.number += num;
    }

    @Override
    public JSONObject readToClear(long diff) {
        if (0 != this.count)
            this.json.put("Usage", this.number/this.count);
        this.count = 0;
        return super.readToClear(diff);
    }
}
