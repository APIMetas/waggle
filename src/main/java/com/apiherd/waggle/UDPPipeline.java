package com.apiherd.waggle;

import java.util.concurrent.ConcurrentLinkedQueue;

public class UDPPipeline extends Thread{
    private UDPClient client;
    private ConcurrentLinkedQueue<String> queue;

    public UDPPipeline(String host, int port) {
        this.client = new UDPClient(host, port);
    }

    public UDPPipeline(String host, int port, int maxBuff) {
        this.client = new UDPClient(host, port, maxBuff);
    }

    public void addPipe(String msg) {
        this.queue.add(msg);
    }

    public UDPClient getClient() {
        return client;
    }

    public void syncMessages() {
        String msg = this.queue.poll();
        while(msg != null) {
            this.client.writeString(msg);
            msg = this.queue.poll();
        }
    }
}
