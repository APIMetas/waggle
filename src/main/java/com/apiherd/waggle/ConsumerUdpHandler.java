package com.apiherd.waggle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;

public class ConsumerUdpHandler extends ConsumerHandler {
    private InetSocketAddress socketAddress;

    public ConsumerUdpHandler(InetSocketAddress socketAddress, ByteBuffer buffer,
                              SelectableChannel channel, BuffedInvokePool pool) {
        super(buffer, channel, pool);
        this.socketAddress = socketAddress;
    }

    @Override
    public void writeBuffer(ByteBuffer buffer) throws IOException {
        DatagramChannel chnnl = (DatagramChannel) this.channel;
        chnnl.send(buffer, this.socketAddress);
    }
}
