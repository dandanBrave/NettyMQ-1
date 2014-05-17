/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.tsui.nettymq.comm;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * 覆写SimpleChannelUpstreamHandler，实现基于nettyserver端接受消息后的处理。
 * @author jin.qian
 * @version $Id: NettyServerChannelUpStreamHandler.java,v 0.1 2012-2-6 下午05:10:46 jin.qian Exp $
 */
public class NettyServerChannelUpStreamHandler extends SimpleChannelUpstreamHandler {
    /**日志*/
    public static Logger logger = Logger.getLogger(NettyServerChannelUpStreamHandler.class);

    //    private HandlerDispatcher dispatcher;                                                        //处理器的分发器

    /** 
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        logger.warn("创建连接: " + e.getChannel().getLocalAddress() + "-->"
                    + e.getChannel().getRemoteAddress());
        ctx.sendUpstream(e);
    }

    /** 
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        //        dispatcher.dispathceRequest((ZBridgeMessageinfo) e.getMessage(), e.getChannel(), true);
        ctx.sendUpstream(e);
    }

    /** 
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        //        logger.error(getClass(), "服务端通信异常" + ctx.getChannel().getRemoteAddress(), e.getCause());
        ctx.sendUpstream(e);
    }

    /** 
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelDisconnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        logger.warn("断开连接: " + e.getChannel().getLocalAddress() + "-->"
                    + e.getChannel().getRemoteAddress());
        //断开连接
        e.getChannel().close();
        //事件上传
        ctx.sendUpstream(e);
    }

}
