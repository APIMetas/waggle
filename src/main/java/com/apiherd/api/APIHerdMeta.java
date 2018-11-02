package com.apiherd.api;

import org.json.JSONObject;

/**
 * Created by leesogo on 17/6/9.
 */
public class APIHerdMeta {
    private String requestId;
    private String userId;
    private String serviceName;
    private String serviceVersion;
    private String serviceStage;
    private String actionName;
    private String methodName;
    private String requestTime;
    private String resourcePath;
    private JSONObject metaJson;
    private boolean needResponse;

    public String getMethodName() {
        return methodName;
    }

    public JSONObject getMetaJson() {
        return metaJson;
    }

    public void setMetaJson(JSONObject metaJson) {
        this.metaJson = metaJson;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String productName) {
        this.serviceName = productName;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getServiceStage() {
        return serviceStage;
    }

    public void setServiceStage(String serviceStage) {
        this.serviceStage = serviceStage;
    }

    public boolean getNeedResponse() {
        return needResponse;
    }

    private static String getOptString(JSONObject obj, String key) {
        if (null == obj.opt(key))
            return new String("");
        return obj.getString(key);
    }

    public static APIHerdMeta parseAPIMeta(JSONObject json) {
        APIHerdMeta meta = new APIHerdMeta();
        if (null == json.opt("APIMetas"))
            return meta;

        JSONObject herd = json.getJSONObject("APIMetas");
        meta.setRequestId(getOptString(herd, "RequestId"));
        meta.setUserId(getOptString(herd, "UserId"));
        meta.setServiceName(getOptString(herd, "ServiceName"));
        meta.setServiceVersion(getOptString(herd, "ServiceVersion"));
        meta.setActionName(getOptString(herd, "ActionName"));
        meta.setResourcePath(getOptString(herd, "ResourcePath"));
        meta.setRequestTime(getOptString(herd, "RequestTime"));
        meta.setServiceStage(getOptString(herd, "ServiceStage"));
        meta.setMethodName(getOptString(herd, "MethodName"));
        String needResponse = herd.optString("NeedResponse");
        if (null == needResponse)
            meta.needResponse = true;
        else if (needResponse.startsWith("f"))
            meta.needResponse = false;
        else
            meta.needResponse = true;
        meta.setMetaJson(herd);

        return meta;
    }
}
