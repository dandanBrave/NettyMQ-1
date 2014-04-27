package com.tsui.nettymq.remoting;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 
 * 
 * @author xmtsui
 * @version $Id: Reactor.java, v 0.1 2014��4��17�� ����10:19:47 xmtsui Exp $
 */
public class Reactor implements Runnable {
    public static Logger      logger = Logger.getLogger(Reactor.class);
    final Selector            selector;
    final ServerSocketChannel serverSocket;
    final boolean             isWithThreadPool;

    public static void main(String[] args) {
        try {
            new Thread(new Reactor(8888, true)).start();
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    Reactor(int port, boolean isWithThreadPool) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        this.isWithThreadPool = isWithThreadPool;
        InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), port);
        serverSocket.socket().bind(address);

        serverSocket.configureBlocking(false);
        //��selectorע���channel
        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        logger.debug("-->Start serverSocket.register!");

        //����sk��attache���ܰ�Acceptor ��������飬����Acceptor
        sk.attach(new Acceptor());
        logger.debug("-->attach(new Acceptor()!");
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set<SelectionKey> selected = selector.selectedKeys();
                Iterator<SelectionKey> it = selected.iterator();
                //Selector�������channel��OP_ACCEPT��READ�¼����������б����ͻ���С�
                while (it.hasNext()) {
                    //��һ���¼� ��һ�δ���һ��accepter�߳�
                    //�Ժ󴥷�SocketReadHandler
                    SelectionKey key = it.next();
                    dispatch(key);
                    //                    it.remove();
                }
                selected.clear();
            }
        } catch (IOException ex) {
            logger.debug("reactor stop!" + ex);
        }
    }

    static int dispatch_count = 0;

    //����Acceptor��SocketReadHandler
    void dispatch(SelectionKey k) {
        Runnable r = (Runnable) (k.attachment());
        if (r != null) {
            System.out.println("dispatch ���� " + (++dispatch_count) + "��");
            r.run();
        }
    }

    static int acceptor_count      = 0;
    static int acceptor_init_count = 0;

    class Acceptor implements Runnable {
        public Acceptor() {
            System.out.println("acceptor ��ʼ�� " + (++acceptor_init_count) + "��");
        }

        @Override
        public void run() {
            try {
                System.out.println("acceptor ���� " + (++acceptor_count) + "��");
                logger.debug("-->ready for accept!");
                SocketChannel socketChannel = serverSocket.accept();
                if (socketChannel != null) {
                    if (isWithThreadPool)
                        new SocketHandlerWithThreadPool(selector, socketChannel);
                    else
                        new SocketHandler(selector, socketChannel);
                }
                logger.debug("-->connection Accepted by Reactor!");
            } catch (IOException ex) {
                logger.debug("accept stop!" + ex);
            }
        }
    }
}