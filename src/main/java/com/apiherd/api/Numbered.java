package com.apiherd.api;

import org.json.JSONObject;

public class Numbered {

    private String name;
    private String unit;
    protected JSONObject json;
    protected long number = 0;

    public Numbered(String name, String unit) {
        this.name = name;
        this.unit = unit;
        this.json = new JSONObject(
                "{\"MetricName\":\"\", \"Usage\":0, \"Unit\":\"\", \"Capacity\":0}");
        this.json.put("Unit", unit);
        this.json.put("MetricName", name);
    }

    public synchronized boolean maxNumber(long num) {
        if (num > this.number) {
            this.number = num;
            return true;
        }
        return false;
    }

    public synchronized void incrNumber() {
        this.number ++;
    }

    public synchronized void incrBy(long num) {
        this.number += num;
    }

    public JSONObject readToClear(long diff) {
        this.number = 0;
        return this.json;
    }
}
