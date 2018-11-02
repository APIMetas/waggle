package com.apiherd.waggle;

import com.apiherd.api.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BuffedInvokePool {
    private int size;
    private int buffSize;
    private APIMetrics metrics;
    private NotifyChannel notes;
    private ExecutorService pool;
    private HashMap<String, APINotifiable> apiBook;
    public static String REFERED_REQUEST_ID = "000000000000";

    public BuffedInvokePool(int poolSize, int buffSize) {
        this.size = poolSize;
        this.buffSize = buffSize;
        this.apiBook = new HashMap<>();
        this.metrics = new APIMetrics();
        this.notes = new NotifyChannel();
        this.pool = Executors.newFixedThreadPool(size);
        this.initPoolFixed();
    }

    private void initPoolFixed() {
        this.registerAPI("SubscribeResource", new APIable() {
            @Override
            public String invokeAPI(RawsRequest request, WriteableChannel channel) {
                return notes.subscribeTopic(request, channel);
            }
        });
        this.registerAPI("GetAPIMetrics", new APIable() {
            @Override
            public String invokeAPI(RawsRequest request, WriteableChannel channel) {
                return metrics.getAPIMetrics(request, channel);
            }
        });
    }

    public int getSize() {
        return size;
    }

    public int getBuffSize() {
        return buffSize;
    }

    public void notifyListeners(String key, ByteBuffer buff) {
        this.notes.notifyListener(key, buff);
    }

    public void registerAPI(String methodName, APIable func) {
        this.apiBook.put(methodName, new APINotifiable(func));
        this.metrics.addAPI(methodName);
    }

    public void registerAPI(String methodName, APIable func, boolean notifiable) {
        this.apiBook.put(methodName, new APINotifiable(func, notifiable));
        this.metrics.addAPI(methodName);
    }

    public APINotifiable getAPI(String name) {
        return this.apiBook.get(name);
    }

    public void apiCalling(String name) {
        this.metrics.apiCalling(name);
    }

    public void apiCalled(String requestId, String name,long diff) {
        this.metrics.apiCalled(requestId, name, diff);
    }

    public Future<String> executeAPIInvoke(ConsumerHandler run) {
        return  this.pool.submit(run);
    }
}
