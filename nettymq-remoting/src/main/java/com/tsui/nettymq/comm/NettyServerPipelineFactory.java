/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.tsui.nettymq.comm;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * 通讯层 服务端初始化工厂赋值
 * @author jin.qian
 * @version $Id:NettyServerPipelineFactory.java,v 0.1 2011-4-6 上午10:56:28 jin.qian Exp $
 */
public class NettyServerPipelineFactory implements ChannelPipelineFactory {

    /**日志*/
    public static Logger                      logger = Logger
                                                         .getLogger(NettyServerPipelineFactory.class);

    /**服务端消息处理器*/
    private NettyServerChannelUpStreamHandler nettyServerHandler;

    /** 
     * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
     */
    @Override
    public ChannelPipeline getPipeline() {
        //对请求处理过程实现了过滤器链模式ChannelPipeline
        ChannelPipeline pipeline = new DefaultChannelPipeline();
        //添加解码器
        pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        //        pipeline.addLast("protobufDecoder",
        //            new ProtobufDecoder(ZBridgeMessage.ZBridgeMessageinfo.getDefaultInstance()));
        //添加编码器
        pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast("protobufEncoder", new ProtobufEncoder());
        //添加 事件处理器
        pipeline.addLast("handler", new NettyServerChannelUpStreamHandler());
        if (logger.isInfoEnabled()) {
            logger.info("加载 ->ZbridgeMessageDecoder,ZbridgeMessageEncoder, ZbridgeServerHandler");
        }
        return pipeline;
    }

    //Spring 注入
    public void setNettyServerHandler(NettyServerChannelUpStreamHandler nettyServerHandler) {
        this.nettyServerHandler = nettyServerHandler;
    }

}
