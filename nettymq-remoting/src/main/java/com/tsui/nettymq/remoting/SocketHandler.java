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

import org.apache.log4j.Logger;

public class SocketHandler implements Runnable {
    public static Logger logger                   = Logger.getLogger(SocketHandler.class);

    final SocketChannel  socket;
    final SelectionKey   sk;

    static final int     READING                  = 0, SENDING = 1;
    int                  state                    = READING;

    ByteBuffer           buffer                   = ByteBuffer.allocate(1024);

    static int           sockethandler_init_count = 0;
    static int           sockethandler_count      = 0;

    public SocketHandler(Selector sel, SocketChannel c) throws IOException {
        System.out.println("SocketHandler 初始化 " + (++sockethandler_init_count) + "次");
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
        System.out.println("SocketHandler 运行 " + (++sockethandler_count) + "次");
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
        //激活线程池 处理这些request 
        //requestHandle(new Request(socket,btt)); 
        WritableByteChannel out = Channels.newChannel(System.out);
        buffer.flip();
        out.write(buffer);
        buffer.rewind();
        state = SENDING;
        readProcess();
        //同时将SelectionKey标记为可读，以便读取。 
        sk.interestOps(SelectionKey.OP_WRITE);
    }

    private void readProcess() throws Exception {
        Thread.sleep(10000);
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
}