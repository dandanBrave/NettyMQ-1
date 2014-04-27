package com.tsui.nettymq.remoting;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public class NIOEchoClient {
    public NIOEchoClient() throws Exception {
        SocketAddress rama = new InetSocketAddress("192.168.1.100", 8888);
        SocketChannel client = SocketChannel.open(rama);
        ByteBuffer buffer = ByteBuffer.allocate(74);
        //        ReadableByteChannel in = Channels.newChannel(System.in);
        //        in.read(buffer);
        buffer.put("nihao\n".getBytes());
        buffer.flip();
        client.write(buffer);
        buffer.clear();

        WritableByteChannel out = Channels.newChannel(System.out);
        int num = 0;
        while ((num = client.read(buffer)) != -1) {
            System.out.print("from server: read num -> " + num + ", upper case content -> ");
            buffer.flip();
            out.write(buffer);
            buffer.clear();
        }

    }

    public static void main(String[] args) {
        try {
            new NIOEchoClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}