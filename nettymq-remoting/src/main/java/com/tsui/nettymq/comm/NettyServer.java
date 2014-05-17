package com.tsui.nettymq.comm;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * Nettyͨ�ŷ�����
 * 
 * @author xmtsui
 * @version $Id: NettyServer.java, v 0.1 2014��5��16�� ����5:01:16 xmtsui Exp $
 */
public class NettyServer {
    /**��־*/
    private static final Logger        LOGGER = Logger.getLogger(NettyServer.class);
    private ServerBootstrap            bootstrap;
    /**���������̳߳�  */
    private Executor                   bossExecutor;
    /**ͨѶ�����̳߳�*/
    private Executor                   workerExecutor;
    /**��������  */
    private NettyServerPipelineFactory nettyServerPipelineFactory;

    /**
     * �������������ָ���˿�
     * @param port                 ����˵������˿�
     */
    private void startNewBootstrap(int port) {
        /**ͨѶ����������  */
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor,
            workerExecutor));
        bootstrap.setPipelineFactory(new NettyServerPipelineFactory());
        //����tpcip������ �������ܿ���
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.bind(new InetSocketAddress(port));
    }

}
