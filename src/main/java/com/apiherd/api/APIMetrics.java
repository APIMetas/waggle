package com.apiherd.api;

import com.apiherd.waggle.BuffedInvokePool;

import java.util.Date;
import java.util.HashMap;

public class APIMetrics {
    private Date date = new Date();
    private HashMap<String, String> ids = new HashMap<>();
    private HashMap<String, Numbered> max = new HashMap<>();
    private HashMap<String, Numbered> qps = new HashMap<>();
    private HashMap<String, Numbered> mrt = new HashMap<>();

    public void addAPI(String name) {
        this.qps.put(name, new APIQps());
        this.mrt.put(name, new APIMrt());
        this.max.put(name, new APIMax());
        this.ids.put(name, BuffedInvokePool.REFERED_REQUEST_ID);
    }

    public void apiCalling(String name) {
        this.qps.get(name).incrNumber();
    }

    public void apiCalled(String requestId, String name, long diff) {
        this.mrt.get(name).incrBy(diff);
        if(this.max.get(name).maxNumber(diff))
            this.ids.put(name, requestId);
    }

    public String readMetrics(String name, String requestId) {
        Date now = new Date();
        long diff = (now.getTime() - date.getTime())/1000;
        StringBuilder strB = new StringBuilder("{\"ServiceName\":\"");
        strB.append(name).append("\",\"Actions\":[");
        for (String api:qps.keySet()) {
            strB.append("{\"ActionName\":\"").append(api);
            strB.append("\",\"MaxResponse\":\"").append(ids.get(api));
            strB.append("\",\"Metrics\":[");
            strB.append(qps.get(api).readToClear(diff)).append(",");
            strB.append(max.get(api).readToClear(diff)).append(",");
            strB.append(mrt.get(api).readToClear(diff)).append("]}");
            strB.append(",");
        }
        this.date = now;
        if (qps.keySet().size() > 0)
            strB.deleteCharAt(strB.length() - 1);
        strB.append("],\"RequestId\":\"").append(requestId).append("\"}");
        return strB.toString();
    }

    public String getAPIMetrics(RawsRequest request, WriteableChannel channel) {
        APIRequest api = (APIRequest) request;
        String service = api.getBiz().optString("ServiceName");
        if (null == service)
            return api.getRequestIdJson();
        return this.readMetrics(service, api.getMeta().getRequestId());
    }
}
