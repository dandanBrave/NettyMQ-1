package com.tsui.nettymq.remoting;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.sql.Time;
import java.util.Iterator;

public class NIOEchoServer {
    private static final int TIMEOUT = 300;
    private static final int PORT    = 8888;

    public static void main(String[] args) {
        try {
            //����һ��ѡ����  �������Ǽ��������¼�
            Selector selector = Selector.open();
            //����һ��ServerSocketChannel  ���� server
            ServerSocketChannel listenChannel = ServerSocketChannel.open();
            //����Ϊ��������ʽ
            listenChannel.configureBlocking(false);
            // �������������ѷ������󶨵������˿� 8888
            listenChannel.socket().bind(new InetSocketAddress(PORT));
            //ע�ᵽѡ����selector��   ע��ע����¼�����Ϊ OP_ACCEPT �ȸ���
            //ע���� Seclector ����Ҫ���� ָ���˿ڵ�On_ACCEPT�¼� ��ע������ע����ֻ�������һ���¼� ������ָ���˿ڣ� 
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                //�����¼�
                if (selector.select(TIMEOUT) == 0) {
                    System.out.print(".");
                    continue;
                }
                //�¼���Դ�б�
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    //��ȡһ���¼�  ��ʼ��������¼�
                    SelectionKey key = iter.next();
                    iter.remove(); //ɾ����ǰ�¼�

                    //��ÿһ���¼���Դkey�ֱ���д���
                    if (key.isAcceptable()) {
                        //ǰ��˵ע����SeclectorҪ����ON_ACCEPT�¼� �����¼�����  ��Ҫ���������Ӧ������
                        //����Ĵ���ʽ�� ����һ�� SocketChannel����  ������ͻ��˵��������  ����Ĵ���
                        //Ҳ������˼  �Ǹ���ǰ������¼�key��һ��SocketChannel���� ������Seltor ����ON_READ�¼�
                        //���� ���� �� SocketChannl������  
                        //������������һ��channel�����ĳ���¼����д������channel�������ͻ��˵�����
                        // ����������socket
                        SocketChannel channel = listenChannel.accept();
                        channel.configureBlocking(false);
                        //�Ѵ�channel �ͻ��˶�����Ϊһ���¼�ע�ᵽ ѡ���� selector��
                        SelectionKey connkey = channel.register(selector, SelectionKey.OP_READ);
                        NIOServerConnection conn = new NIOServerConnection(connkey);
                        connkey.attach(conn);
                    }

                    if (key.isReadable()) {
                        NIOServerConnection conn = (NIOServerConnection) key.attachment();
                        conn.handleRead();
                    }

                    if (key.isValid() && key.isWritable()) {
                        NIOServerConnection conn = (NIOServerConnection) key.attachment();
                        conn.handleWrite();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class NIOServerConnection {
    private static final int BUFFSIZE = 1024;

    SelectionKey             key;
    SocketChannel            channel;
    ByteBuffer               buffer;

    public NIOServerConnection(SelectionKey key) {
        this.key = key;
        this.channel = (SocketChannel) key.channel();
        buffer = ByteBuffer.allocate(BUFFSIZE);
    }

    public void handleRead() throws IOException {
        // System.out.println("now server read");
        long bytesRead = channel.read(buffer);
        WritableByteChannel out = Channels.newChannel(System.out);
        System.out.print("from client: read num -> " + bytesRead + ", content -> ");
        buffer.flip();
        //        buffer.rewind();
        out.write(buffer);
        buffer.rewind();

        if (bytesRead == -1) {
            channel.close();
        } else {
            key.interestOps(SelectionKey.OP_WRITE);
        }
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

    public void handleWrite() throws Exception {
        readProcess();
        buffer.rewind();
        channel.write(buffer);
        if (!buffer.hasRemaining()) {
            key.interestOps(SelectionKey.OP_READ);
        }
        buffer.compact();
    }
}