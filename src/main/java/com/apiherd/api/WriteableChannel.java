package com.apiherd.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.charset.Charset;

public abstract class WriteableChannel {

    protected SelectableChannel channel;

    public void writeString(String response) throws IOException {
        byte[] buff = response.getBytes(Charset.forName("UTF-8"));
        ByteBuffer byteBuffer = ByteBuffer.wrap(buff);
        this.writeBuffer(byteBuffer);
    }

    public abstract void writeBuffer(ByteBuffer buffer) throws IOException;

    public void close() {
        try {
            this.channel.close();
        } catch (IOException exp) {
            ;
        }
    }
}
