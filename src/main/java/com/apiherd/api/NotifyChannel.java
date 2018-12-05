package com.apiherd.api;

import org.json.JSONObject;

import java.util.Iterator;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class NotifyChannel{
    private ConcurrentHashMap<String, ConcurrentLinkedDeque<WriteableChannel>> notifyBook;

    public NotifyChannel() {
        this.notifyBook = new ConcurrentHashMap<>();
    }

    private synchronized void registerListener(String key, WriteableChannel channel) {
        String parseKey = key;
        ConcurrentLinkedDeque queue = null;
        for (String sub : this.notifyBook.keySet()) {
            if (MQTTTopic.keyContains(sub, key)) {
                queue = this.notifyBook.get(sub);
                parseKey = sub;
            }
        }
        if (null == queue)
            queue = new ConcurrentLinkedDeque<>();
        queue.add(channel);
        this.notifyBook.put(parseKey, queue);
    }

    public void notifyListener(String key, ByteBuffer buffer) {
        if (null == key)
            return;
        ConcurrentLinkedDeque<WriteableChannel> channels = null;
        for (String sub : this.notifyBook.keySet()) {
            if (MQTTTopic.keyContains(sub, key))
                channels = this.notifyBook.get(sub);
        }
        if (null == channels)
            return;
        Iterator<WriteableChannel> iterator = channels.iterator();
        while (iterator.hasNext()) {
            WriteableChannel channel = iterator.next();
            try {
                channel.writeBuffer(buffer);
            } catch (IOException e) {
                channel.close();
                iterator.remove();
            }
        }
    }

    public JSONObject subscribeTopic(RawsRequest request, WriteableChannel channel) {
        APIRequest api = (APIRequest) request;
        String key = (String) api.getBiz().opt("ResourceKey");
        if (null == key)
            return new JSONObject(api.getRequestIdJson());
        this.registerListener(key, channel);

        return new JSONObject(api.getRequestIdJson());
    }
}
