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

        //��SelectionKey��Ϊ��Handler ��һ�����¼�����ʱ�������ñ����run������ 
        //�ο�dispatch(SelectionKey k) 
        sk.attach(this);

        //ͬʱ��SelectionKey���Ϊ�ɶ����Ա��ȡ�� 
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
     * �����ȡdata 
     * @throws Exception 
     */
    private void readRequest() throws Exception {
        buffer.clear();
        int bytesRead = socket.read(buffer);
        WritableByteChannel out = Channels.newChannel(System.out);
        System.out.println(bytesRead);
        buffer.flip();
        out.write(buffer);
        buffer.rewind();
        state = SENDING;
        //�����̳߳� ������Щrequest 
        //requestHandle(new Request(socket,btt)); 
        if (bytesRead > 0) {
            state = PROCESSING;
            pool.execute(new Processor());
        }
        //ͬʱ��SelectionKey���Ϊ�ɶ����Ա��ȡ�� 
        sk.interestOps(SelectionKey.OP_WRITE);
    }

    /**
     * ����дdata
     * 
     * @throws Exception
     */
    private void writeRequest() throws Exception {
        buffer.rewind();
        socket.write(buffer);
        if (!buffer.hasRemaining()) {
            sk.interestOps(SelectionKey.OP_READ);
        }
        buffer.compact();
        System.out.println(new Time(System.currentTimeMillis()).toString());
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

    class Processor implements Runnable {
        public void run() {
            try {
                readProcess();
            } catch (Exception e) {
                logger.error("", e);
            }
            state = SENDING;
        }

    }
}