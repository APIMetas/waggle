package com.apiherd.waggle;

import com.etsy.net.JUDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

import com.etsy.net.UnixDomainSocketServer;

public abstract class StreamingServer implements Runnable {
    protected int port;
    protected Thread thread;
    protected boolean ifExit;
    protected JUDSServer juds;
    protected Selector selector;
    protected BuffedInvokePool pool;
    protected static final Logger log = LoggerFactory.getLogger(StreamingServer.class);

    public StreamingServer(int transferPort, BuffedInvokePool pool) {
        this.pool = pool;
        this.ifExit = false;
        this.port = transferPort;
        System.setProperty("java.net.preferIPv4Stack" , "true");
    }

    public StreamingServer(int transferPort, BuffedInvokePool pool, String dir) {
        this.pool = pool;
        this.ifExit = false;
        this.port = transferPort;
        this.registerMetrics(dir);
        System.setProperty("java.net.preferIPv4Stack" , "true");
    }

    protected void registerMetrics(String dir) {
        this.juds = new JUDSServer(pool.getAPI("GetAPIMetrics"), dir, JUDS.SOCK_STREAM);
        this.juds.runServer();
    }

    protected abstract Selector setPortServer(int portNumber) throws IOException;

    protected abstract ConsumerHandler readableIO(ByteBuffer buffer, SelectableChannel chnnl) throws IOException;

    public void interrupt() {
        try {
            this.thread.interrupt();
            this.thread.join();
        } catch (Exception exp) {
            log.error("", exp);
        }
    }

    protected SocketChannel setPortAccept(SelectionKey selectedKey, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) selectedKey.channel();
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        SelectionKey newKey= socketChannel.register(selector, SelectionKey.OP_READ);
        if (null != this.pool) {
            ByteBuffer buffer = ByteBuffer.allocate(this.pool.getBuffSize());
            newKey.attach(buffer);
        }

        return socketChannel;
    }

    public synchronized void toStop() {
        this.ifExit = true;
    }

    public void run() {
        this.runServer(this.selector);
    }

    public void runServer(Selector selector) {
        try {
            while (!ifExit) {
                selector.select();
                Iterator iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey selectedKey = (SelectionKey) iter.next();
                    iter.remove();
                    if (selectedKey.isAcceptable()) {
                        SocketChannel socketChannel = null;
                        try {
                            socketChannel = this.setPortAccept(selectedKey, selector);
                        } catch (Exception exp) {
                            log.error("", exp);
                            socketChannel.close();
                        }
                    } else if (selectedKey.isReadable()) {
                        ByteBuffer buffer = (ByteBuffer) selectedKey.attachment();
                        this.readableIO(buffer, selectedKey.channel());
                        //selectedKey.cancel();
                    } else {
                        selectedKey.cancel();
                        try {
                            selectedKey.channel().close();
                        } catch (Exception exp) {
                            log.error("", exp);
                        }
                    }
                }
            }
        } catch (Exception exp) {
            log.error("", exp);
            return;
        }
    }
}
