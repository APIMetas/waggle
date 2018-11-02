package com.apiherd.api;

import org.json.JSONObject;

public class APIQps extends Numbered {
    public APIQps() {
        super("Requests", "QPS");
    }

    @Override
    public JSONObject readToClear(long diff) {
        this.json.put("Usage", this.number/diff);
        return super.readToClear(diff);
    }
}
