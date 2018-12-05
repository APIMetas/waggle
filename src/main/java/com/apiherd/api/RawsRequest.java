package com.apiherd.api;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class RawsRequest {
    private String json;
    private byte[] MIME;

    public String getJson() {
        return json;
    }

    public RawsRequest setJson(String json) {
        this.json = json;
        return this;
    }

    public byte[] getMIME() {
        return MIME;
    }

    public void setMIME(byte[] buff, int begin, int length) {
        this.MIME = new byte[length];
        System.arraycopy(buff, begin, this.MIME, 0, length);
    }

    public static RawsRequest parseRawRequest(String request) {
        String strRequest = request;
        int posBegin = strRequest.indexOf("{");
        int posEnd = strRequest.lastIndexOf("}");

        if (posBegin == -1 || posEnd == -1 || posBegin > posEnd)
            return null;

        RawsRequest raw = new RawsRequest();
        raw.setJson(strRequest.substring(posBegin, posEnd + 1));
        if (strRequest.length() - 1 > posEnd) {
            int posLen = strRequest.length() - 1;
            raw.setMIME(strRequest.substring(posEnd + 1).getBytes(), 0, posLen - posEnd);
        }

        return raw;
    }
}
