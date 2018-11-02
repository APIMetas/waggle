package com.apiherd.api;

import org.json.JSONObject;

public class APIRequest extends RawsRequest {

    private JSONObject biz;
    private APIHerdMeta meta;

    public JSONObject getBiz() {
        return biz;
    }

    public void setBiz(JSONObject biz) {
        this.biz = biz;
    }

    public APIHerdMeta getMeta() {
        return meta;
    }

    public void setMeta(APIHerdMeta meta) {
        this.meta = meta;
    }

    public String getAPIName() {
        return this.meta.getActionName();
    }

    public String getAPIRN() {
        StringBuilder builder = new StringBuilder("/");
        builder.append(meta.getUserId()).append("/");
        builder.append(meta.getServiceName()).append("/");
        builder.append(meta.getServiceVersion()).append("/");
        builder.append(meta.getServiceStage());

        return builder.toString();
    }

    public String getRNKey() {
        return meta.getResourcePath();
    }

    public static APIRequest parseAPIRequest(RawsRequest request) {
        APIRequest api = new APIRequest();
        JSONObject json = new JSONObject(request.getJson());
        api.setMeta(APIHerdMeta.parseAPIMeta(json));
        json.remove("APIMetas");
        api.setBiz(json);

        return api;
    }

    public String getRequestIdJson() {
        return "{\"RequestId\":\"" + this.getMeta().getRequestId() + "\"}";
    }

    public String getShortResponse(String fullKey) {
        return "{\"RequestId\":\"" + this.getMeta().getRequestId() +
                "\", \"ResourceKey\":"+JSONObject.quote(fullKey)+"}";
    }

}
