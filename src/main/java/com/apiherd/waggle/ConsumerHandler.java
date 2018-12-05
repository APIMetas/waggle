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
    private String requestJson = null;
    protected static final Logger log = LoggerFactory.getLogger(ConsumerHandler.class);

    public ConsumerHandler(ByteBuffer buffer, SelectableChannel channel, BuffedInvokePool pool) {
        this.pool = pool;
        this.channel = channel;
        this.requestJson = new String(buffer.array(), 0, buffer.position(), Charset.forName("UTF-8")).trim();
    }

    @Override
    public String call() {
        JSONObject response = null;
        RawsRequest raw = RawsRequest.parseRawRequest(requestJson);
        if (null == raw)
            return "";
        APIRequest request = APIRequest.parseAPIRequest(raw);
        Date calling = new Date();
        this.pool.apiCalling(request.getAPIName());
        APINotifiable function = this.pool.getAPI(request.getAPIName());
        if (null != function) {
            response = function.invokeAPI(request, this);
            try {
                if (request.getMeta().getNeedResponse())
                    this.writeString(response.toString());
                this.pool.apiCalled(request.getMeta().getRequestId(), request.getAPIName(),
                        (new Date()).getTime() - calling.getTime());
            } catch (IOException exp) {
                log.error("", exp);
            }
            if (function.ifNotifiable()) {
                JSONObject meta = request.getMeta().getMetaJson();
                meta.put("RequestId", BuffedInvokePool.REFERED_REQUEST_ID);
                String pathKey = (String) response.opt("ResourceKey");
                response = request.getBiz().put("APIMetas", meta);
                this.pool.notifyListeners(pathKey,
                        ByteBuffer.wrap(response.toString().getBytes(Charset.forName("UTF-8"))));
            }
        }
        return "";
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
