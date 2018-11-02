package com.apiherd.waggle;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;

public class UDPClient extends SocketClient {
    private DatagramChannel channel;
    private int maxLen = 32 * 1024 * 1024;
    private ByteBuffer reci = ByteBuffer.allocate(maxLen);
    private ByteBuffer send = ByteBuffer.allocate(32*1024);

    public UDPClient(String host, int port) {
        super(host, port);
    }

    @Override
    public boolean connect() {
        return false;
    }

    public UDPClient(String host, int port, int buffSize) {
        super(host, port);
        this.maxLen = buffSize;
        this.reci = ByteBuffer.allocate(buffSize);
    }

    public String readFileEnd(String name) {
        int pos = 0;
        InputStream stream = null;
        byte[] data = new byte[32*1024];

        stream = this.getClass().getClassLoader().getResourceAsStream(name);
        try {
            pos = stream.read(data);
        } catch (Exception exp) {
            log.error("", exp);
        }
        return new String(data, 0, pos, Charset.forName("UTF-8")).trim();
    }

    @Override
    public SocketClient setUp() {
        try {
            DatagramChannel channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(0));
            channel.connect(new InetSocketAddress(this.host, this.port));
            this.channel = channel;
        } catch (Exception exp) {
            log.error("", exp);
        }
        return this;
    }

    @Override
    public void writeString(String msg) {
        try {
            this.send.clear();
            if (!this.channel.isConnected())
                channel.connect(new InetSocketAddress(this.host, this.port));
            this.send.put(msg.getBytes(Charset.forName("UTF-8"))).flip();
            this.channel.write(this.send);
        } catch (Exception exp){
            log.error("", exp);
        }
    }

    @Override
    public String readString() {
        ByteBuffer bb = this.readMessage();
        return new String(bb.array(), 0, bb.position(), Charset.forName("UTF-8")).trim();
    }

    @Override
    public void close() {
        try {
            if (this.channel.isConnected())
                this.channel.close();
        } catch (Exception exp) {
            log.error("", exp);
        }
    }

    public ByteBuffer readMessage() {
        try {
            this.reci.clear();
            if (!this.channel.isConnected())
                channel.connect(new InetSocketAddress(this.host, this.port));
            int len = this.channel.read(this.reci);
        } catch (Exception exp){
            log.error("", exp);
        }
        return this.reci;
    }
}
