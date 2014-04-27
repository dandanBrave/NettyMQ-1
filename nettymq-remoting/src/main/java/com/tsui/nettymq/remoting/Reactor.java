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
 * @version $Id: Reactor.java, v 0.1 2014年4月17日 上午10:19:47 xmtsui Exp $
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
        //向selector注册该channel
        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        logger.debug("-->Start serverSocket.register!");

        //利用sk的attache功能绑定Acceptor 如果有事情，触发Acceptor
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
                //Selector如果发现channel有OP_ACCEPT或READ事件发生，下列遍历就会进行。
                while (it.hasNext()) {
                    //来一个事件 第一次触发一个accepter线程
                    //以后触发SocketReadHandler
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

    //运行Acceptor或SocketReadHandler
    void dispatch(SelectionKey k) {
        Runnable r = (Runnable) (k.attachment());
        if (r != null) {
            System.out.println("dispatch 运行 " + (++dispatch_count) + "次");
            r.run();
        }
    }

    static int acceptor_count      = 0;
    static int acceptor_init_count = 0;

    class Acceptor implements Runnable {
        public Acceptor() {
            System.out.println("acceptor 初始化 " + (++acceptor_init_count) + "次");
        }

        @Override
        public void run() {
            try {
                System.out.println("acceptor 运行 " + (++acceptor_count) + "次");
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