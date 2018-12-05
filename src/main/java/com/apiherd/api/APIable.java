package com.apiherd.api;

import org.json.JSONObject;

public interface APIable {
    JSONObject invokeAPI(RawsRequest request, WriteableChannel channel);
}
