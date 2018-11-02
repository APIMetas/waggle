package com.apiherd.waggle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.net.InetSocketAddress;

public class UdpStreamingServer extends StreamingServer implements Runnable {

    public UdpStreamingServer(int transferPort, BuffedInvokePool pool) {
        super(transferPort, pool);
    }

    public UdpStreamingServer(int transferPort, BuffedInvokePool pool, String dir) {
        super(transferPort, pool, dir);
    }

    public void start() {
        try {
            this.selector = this.setPortServer(this.port);
        } catch (Exception exp) {
            this.log.error("", exp);
        }
        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    protected Selector setPortServer(int portNumber) throws IOException {
        Selector selector = Selector.open();
        DatagramChannel socketChannel =  DatagramChannel.open();
        socketChannel.configureBlocking(false);
        InetSocketAddress address = new InetSocketAddress(portNumber);
        socketChannel.socket().bind(address);
        SelectionKey newKey = socketChannel.register(selector, SelectionKey.OP_READ);
        ByteBuffer buffer = ByteBuffer.allocate(this.pool.getBuffSize());
        newKey.attach(buffer);

        return selector;
    }

    @Override
    protected ConsumerHandler readableIO(ByteBuffer buffer, SelectableChannel chnnl) throws IOException {
        DatagramChannel channel = (DatagramChannel)chnnl;
        try {
            buffer.clear();
            InetSocketAddress address = (InetSocketAddress)channel.receive(buffer);
            ConsumerUdpHandler handler = new ConsumerUdpHandler(address, buffer, chnnl, this.pool);
            if (0 != buffer.position())
                pool.executeAPIInvoke(handler);
        } catch (Exception exp) {
            try {
                channel.close();
            } catch (Exception e) {
                log.error("",e);
            }
            log.error("",exp);
        }

        return null;
    }
}
