package com.tsui.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.sql.Time;

import org.apache.log4j.Logger;

public class SocketHandler implements Runnable {
    public static Logger logger  = Logger.getLogger(SocketHandler.class);

    final SocketChannel  socket;
    final SelectionKey   sk;

    static final int     READING = 0, SENDING = 1;
    int                  state   = READING;

    ByteBuffer           buffer  = ByteBuffer.allocate(1024);

    public SocketHandler(Selector sel, SocketChannel c) throws IOException {
        socket = c;
        socket.configureBlocking(false);
        sk = socket.register(sel, 0);

        //将SelectionKey绑定为本Handler 下一步有事件触发时，将调用本类的run方法。 
        //参看dispatch(SelectionKey k) 
        sk.attach(this);

        //同时将SelectionKey标记为可读，以便读取。 
        sk.interestOps(SelectionKey.OP_READ);
        sel.wakeup();
    }

    public void run() {
        try {
            if (state == READING) {
                readRequest();
            } else if (state == SENDING) {
                writeRequest();
            }
        } catch (Exception ex) {
            logger.debug("readRequest error" + ex);
        }
    }

    /** 
    * 处理读取data 
    * @throws Exception 
    */
    private void readRequest() throws Exception {
        buffer.clear();
        int bytesRead = socket.read(buffer);
        //激活线程池 处理这些request 
        //requestHandle(new Request(socket,btt)); 
        WritableByteChannel out = Channels.newChannel(System.out);
        System.out.println(bytesRead);
        buffer.flip();
        out.write(buffer);
        buffer.rewind();
        state = SENDING;
        //同时将SelectionKey标记为可读，以便读取。 
        sk.interestOps(SelectionKey.OP_WRITE);
    }

    private void readProcess() throws Exception {
        Thread.sleep(5000);
        buffer.rewind();
        CharsetDecoder cd = Charset.forName("ASCII").newDecoder();
        CharBuffer charBuffer = cd.decode(buffer);
        String read = charBuffer.toString();
        buffer.rewind();
        buffer.put(read.toUpperCase().getBytes());
    }

    /**
     * 处理写data
     * 
     * @throws Exception
     */
    private void writeRequest() throws Exception {
        readProcess();
        buffer.rewind();
        socket.write(buffer);
        if (!buffer.hasRemaining()) {
            sk.interestOps(SelectionKey.OP_READ);
        }
        buffer.compact();
        System.out.println(new Time(System.currentTimeMillis()).toString());
    }
}