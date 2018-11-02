package com.apiherd.waggle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.net.InetSocketAddress;

public class NioStreamingServer extends StreamingServer implements Runnable {

    protected ByteChannel channel = null;

    public NioStreamingServer(int transferPort, BuffedInvokePool pool) {
        super(transferPort, pool);
    }

    public NioStreamingServer(int transferPort, BuffedInvokePool pool, String dir) {
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
        ServerSocketChannel socketChannel =  ServerSocketChannel.open();
        socketChannel.configureBlocking(false);
        InetSocketAddress address = new InetSocketAddress(portNumber);
        socketChannel.socket().bind(address);
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);
        //registerMetrics(selector);

        return selector;
    }

    @Override
    protected ConsumerHandler readableIO(ByteBuffer buffer, SelectableChannel chnnl) throws IOException {
        try {
            buffer.clear();
            this.channel = (ByteChannel)chnnl;
            this.channel.read(buffer);
            ConsumerHandler handler = new ConsumerHandler(buffer, chnnl, this.pool);
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
