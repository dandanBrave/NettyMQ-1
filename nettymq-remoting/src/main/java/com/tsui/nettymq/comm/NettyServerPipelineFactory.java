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
 * ͨѶ�� ����˳�ʼ��������ֵ
 * @author jin.qian
 * @version $Id:NettyServerPipelineFactory.java,v 0.1 2011-4-6 ����10:56:28 jin.qian Exp $
 */
public class NettyServerPipelineFactory implements ChannelPipelineFactory {

    /**��־*/
    public static Logger                      logger = Logger
                                                         .getLogger(NettyServerPipelineFactory.class);

    /**�������Ϣ������*/
    private NettyServerChannelUpStreamHandler nettyServerHandler;

    /** 
     * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
     */
    @Override
    public ChannelPipeline getPipeline() {
        //�����������ʵ���˹�������ģʽChannelPipeline
        ChannelPipeline pipeline = new DefaultChannelPipeline();
        //��ӽ�����
        pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        //        pipeline.addLast("protobufDecoder",
        //            new ProtobufDecoder(ZBridgeMessage.ZBridgeMessageinfo.getDefaultInstance()));
        //��ӱ�����
        pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast("protobufEncoder", new ProtobufEncoder());
        //��� �¼�������
        pipeline.addLast("handler", new NettyServerChannelUpStreamHandler());
        if (logger.isInfoEnabled()) {
            logger.info("���� ->ZbridgeMessageDecoder,ZbridgeMessageEncoder, ZbridgeServerHandler");
        }
        return pipeline;
    }

    //Spring ע��
    public void setNettyServerHandler(NettyServerChannelUpStreamHandler nettyServerHandler) {
        this.nettyServerHandler = nettyServerHandler;
    }

}
