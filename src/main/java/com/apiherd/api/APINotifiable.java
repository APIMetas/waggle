package com.apiherd.api;

public class APINotifiable implements APIable {
    protected APIable api;
    protected boolean notifiable;

    public APINotifiable(APIable api) {
        this.api = api;
        this.notifiable = false;
    }

    public APINotifiable(APIable api, boolean notifiable) {
        this.api = api;
        this.notifiable = notifiable;
    }

    public boolean ifNotifiable() {
        return this.notifiable;
    }

    @Override
    public String invokeAPI(RawsRequest request, WriteableChannel channel) {
        return this.api.invokeAPI(request, channel);
    }
}
