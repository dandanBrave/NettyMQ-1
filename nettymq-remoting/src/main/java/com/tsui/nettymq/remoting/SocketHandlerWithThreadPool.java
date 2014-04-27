package com.tsui.nettymq.remoting;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class SocketHandlerWithThreadPool implements Runnable {
    public static Logger   logger = Logger.getLogger(SocketHandlerWithThreadPool.class);
    static ExecutorService pool   = Executors.newCachedThreadPool();

    final SocketChannel    socket;
    final SelectionKey     sk;

    static final int       READING = 0, SENDING = 1, PROCESSING = 2;
    int                    state   = READING;

    ByteBuffer             buffer  = ByteBuffer.allocate(1024);

    public SocketHandlerWithThreadPool(Selector sel, SocketChannel c) throws IOException {
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

    @Override
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
        System.out.print("[" + new Time(System.currentTimeMillis()).toString() + "] 服务器接收到的字节数: "
                         + bytesRead + ", 接收到的内容: ");
        WritableByteChannel out = Channels.newChannel(System.out);
        buffer.flip();
        out.write(buffer);
        buffer.rewind();

        //修改状态
        state = SENDING;
        //激活线程池 处理这些request 
        //requestHandle(new Request(socket,btt)); 
        if (bytesRead > 0) {
            state = PROCESSING;
            pool.execute(new Processor());
        }
        //        //同时将SelectionKey标记为可读，以便读取。 
        //        sk.interestOps(SelectionKey.OP_WRITE);
    }

    /**
     * 处理写data
     * 
     * @throws Exception
     */
    private void writeRequest() throws Exception {
        buffer.rewind();
        int bytesWrite = socket.write(buffer);
        if (!buffer.hasRemaining()) {
            sk.interestOps(SelectionKey.OP_READ);
        }
        System.out.print("[" + new Time(System.currentTimeMillis()).toString() + "] 服务器发送的字节数: "
                         + bytesWrite + ", 发送的内容: ");
        WritableByteChannel out = Channels.newChannel(System.out);
        buffer.flip();
        out.write(buffer);
        buffer.rewind();

        buffer.compact();
    }

    private void readProcess() throws Exception {
        Thread.sleep(10000);
        buffer.rewind();
        CharsetDecoder cd = Charset.forName("ASCII").newDecoder();
        CharBuffer charBuffer = cd.decode(buffer);
        String read = charBuffer.toString();
        buffer.rewind();
        buffer.put(read.toUpperCase().getBytes());
        //同时将SelectionKey标记为可写，以便客户端读取。 
        sk.interestOps(SelectionKey.OP_WRITE);
    }

    class Processor implements Runnable {
        @Override
        public void run() {
            try {
                long prev = System.currentTimeMillis();
                readProcess();
                long after = System.currentTimeMillis();
                System.out.println("----处理耗时---->" + (after - prev) + " ms");
            } catch (Exception e) {
                logger.error("", e);
            }
            state = SENDING;
        }
    }
}