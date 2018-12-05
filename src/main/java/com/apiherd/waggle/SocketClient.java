package com.apiherd.waggle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SocketClient {
    protected int port = 5409;
    protected String host = "localhost";
    protected static final Logger log = LoggerFactory.getLogger(SocketClient.class);

    public SocketClient() {

    }

    public SocketClient(int port) {
        this.port = port;
    }

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public abstract boolean connect();

    public abstract SocketClient setUp();

    public boolean writeString(String str) {
        return true;
    }

    public abstract String readString();

    public abstract void close();

}
