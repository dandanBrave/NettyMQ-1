package com.tsui.nettymq.comm;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * Netty通信服务器
 * 
 * @author xmtsui
 * @version $Id: NettyServer.java, v 0.1 2014年5月16日 下午5:01:16 xmtsui Exp $
 */
public class NettyServer {
    /**日志*/
    private static final Logger        LOGGER = Logger.getLogger(NettyServer.class);
    private ServerBootstrap            bootstrap;
    /**侦听连接线程池  */
    private Executor                   bossExecutor;
    /**通讯工作线程池*/
    private Executor                   workerExecutor;
    /**过滤器链  */
    private NettyServerPipelineFactory nettyServerPipelineFactory;

    /**
     * 启动服务端侦听指定端口
     * @param port                 服务端的侦听端口
     */
    private void startNewBootstrap(int port) {
        /**通讯连接启动器  */
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor,
            workerExecutor));
        bootstrap.setPipelineFactory(new NettyServerPipelineFactory());
        //设置tpcip长连接 心跳功能开启
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.bind(new InetSocketAddress(port));
    }

}
