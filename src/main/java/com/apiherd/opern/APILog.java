package com.apiherd.opern;

import com.apiherd.api.APIRequest;
import com.apiherd.api.DateHelper;
import com.apiherd.api.RawsRequest;

import java.util.Date;

public class APILog {
    private String apiRN;
    private String apiName;
    private String dateTime;
    private String request;
    private String response;
    private String statusCode;
    private String requestId;

    private Date dateFrom;
    private Date dateTo;
    private Assertion lateAssert;

    public APILog(String json) {
        this.request = json;
        this.dateFrom = new Date();
        this.dateTime = DateHelper.getISO8601Time(dateFrom);

        RawsRequest raw = new RawsRequest().setJson(json);
        APIRequest request = APIRequest.parseAPIRequest(raw);
        this.apiName = request.getAPIName();
        this.apiRN = request.getAPIRN();
        this.requestId = request.getMeta().getRequestId();
        this.statusCode = "true";
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.dateTo = new Date();
        this.response = response;
    }

    public String getApiRN() {
        return apiRN;
    }

    public void setApiRN(String apiRN) {
        this.apiRN = apiRN;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public Assertion getLateAssert() {
        return lateAssert;
    }

    public void setLateAssert(Assertion lateAssert) {
        this.lateAssert = lateAssert;
    }

    public void makeLateAssert(String response) {
        if (null == this.lateAssert)
            return;
        this.setResponse(response);
        this.statusCode = String.valueOf(this.lateAssert.makeAssert(response));
    }

    public long getMRTinMS() {
        try {
            return this.dateTo.getTime() - this.dateFrom.getTime();
        } catch (Exception exp) {
        }
        return 0;
    }
}
