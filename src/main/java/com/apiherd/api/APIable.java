package com.apiherd.api;

public interface APIable {
    String invokeAPI(RawsRequest request, WriteableChannel channel);
}
