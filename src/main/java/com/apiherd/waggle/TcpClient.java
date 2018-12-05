package com.apiherd.waggle;

import java.net.Socket;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class TcpClient extends SocketClient {
    private byte[] data;
    private Socket clientSocket;
    private int maxBuff = 512*1024;
    private InputStream inFromServer;
    private OutputStreamWriter writer;
    //private DataOutputStream outToServer;

    public TcpClient(int num, int maxBuff) {
        super(num);
        this.maxBuff = maxBuff;
        this.data = new byte[maxBuff];
    }

    public TcpClient(int num, int maxBuff, String host) {
        super(host,num);
        this.maxBuff = maxBuff;
        this.data = new byte[maxBuff];
    }

    @Override
    public boolean connect() {
        SocketAddress socketAddress = new InetSocketAddress(this.host, this.port);
        try {
            this.clientSocket.connect(socketAddress);
            return true;
        } catch (Exception exp) {
            log.error("", exp);
            return false;
        }
    }

    public boolean available() {
        if (null != clientSocket && !clientSocket.isClosed())
            return true;
        return false;
    }

    @Override
    public SocketClient setUp() {
        if (null != clientSocket && !clientSocket.isClosed())
            return this;
        try {
            clientSocket = new Socket(this.host, this.port);
            inFromServer = clientSocket.getInputStream();
            //outToServer = new DataOutputStream(clientSocket.getOutputStream());
            this.writer = new OutputStreamWriter(clientSocket.getOutputStream(), Charset.forName("UTF-8"));
        } catch (Exception exp) {
            log.error("", exp);
        }
        return this;
    }

    @Override
    public boolean writeString(String str) {
        try {
            this.writer.write(str);
            this.writer.flush();
        } catch (Exception exp) {
            log.error("", exp);
            return false;
        }
        return true;
    }

    @Override
    public String readString() {
        try {
            int pos = 0;
            pos = this.inFromServer.read(this.data);
            return new String(this.data, 0, pos, Charset.forName("UTF-8"));
        } catch (Exception exp) {
            log.error("", exp);
        }
        return "";
    }

    @Override
    public void close() {
        try {
            this.clientSocket.close();
        } catch (Exception exp) {
            log.error("", exp);
        }
    }

}
