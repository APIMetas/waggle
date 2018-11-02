package com.apiherd.waggle;

import java.nio.charset.Charset;
import java.util.Date;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;

import com.apiherd.api.*;
import org.slf4j.Logger;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

public class ConsumerHandler extends WriteableChannel implements Callable<String> {

    private BuffedInvokePool pool;
    private ByteBuffer buffer = null;
    protected static final Logger log = LoggerFactory.getLogger(ConsumerHandler.class);

    public ConsumerHandler(ByteBuffer buffer, SelectableChannel channel, BuffedInvokePool pool) {
        this.pool = pool;
        this.buffer = buffer;
        this.channel = channel;
    }

    @Override
    public String call() {
        String response = null;
        RawsRequest raw = RawsRequest.parseRawRequest(buffer);
        APIRequest request = APIRequest.parseAPIRequest(raw);
        if (null == request.getMeta())
            return "";
        Date calling = new Date();
        this.pool.apiCalling(request.getAPIName());
        APINotifiable function = this.pool.getAPI(request.getAPIName());
        if (null != function) {
            response = function.invokeAPI(request, this);
            try {
                if (request.getMeta().getNeedResponse())
                    this.writeString(response);
                this.pool.apiCalled(request.getMeta().getRequestId(), request.getAPIName(),
                        (new Date()).getTime() - calling.getTime());
            } catch (IOException exp) {
                log.error("", exp);
            }
            if (function.ifNotifiable()) {
                JSONObject meta = request.getMeta().getMetaJson();
                meta.put("RequestId", BuffedInvokePool.REFERED_REQUEST_ID);
                JSONObject json = new JSONObject(response);
                String pathKey = (String) json.opt("ResourceKey");
                json = request.getBiz().put("APIMetas", meta);
                this.pool.notifyListeners(pathKey,
                        ByteBuffer.wrap(json.toString().getBytes(Charset.forName("UTF-8"))));
            }
        }

        return response;
    }

    @Override
    public void writeBuffer(ByteBuffer buffer) throws IOException {
        ByteChannel chnnl = (ByteChannel) this.channel;
        if (chnnl.isOpen())
            chnnl.write(buffer);
        else
            throw new IOException();
    }
}
