package com.yuyuehao.nettydemo;

import java.nio.charset.Charset;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by Wang
 * on 2017-11-24
 */

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    private NettyListener listener;

    private int WRITE_WAIT_SECOND = 10;

    private int READ_WAIT_SECOND = 13;

    public NettyClientInitializer(NettyListener listener){
        this.listener = listener;
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
//        SslContext sslCtx = SslContextBuilder.forClient()
//                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

        ChannelPipeline pipeline = ch.pipeline();
        //pipeline.addLast(sslCtx.newHandler(ch.alloc()));//开启SSL
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));//开启日志，可以设置日志等级
//        pipeline.addLast(new IdleStateHandler(30, 60, 100));
        pipeline.addLast(new NettyClientHandler(listener));
        //解码用
        pipeline.addLast(new ProtobufVarint32FrameDecoder());// 解码(处理半包)
        //构造函数传递要解码成的类型
        //编码用
//        pipeline.addLast(new ProtobufDecoder());
//        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());//加长度
        pipeline.addLast(new StringDecoder(Charset.forName("UTF-8")));
        pipeline.addLast(new StringEncoder(Charset.forName("UTF-8")));

    }

    /*@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE){
                ctx.close();
            }else if (event.state() == IdleState.WRITER_IDLE){
                try{
                    ctx.channel().writeAndFlush("Chilent-Ping\r\n");
                } catch (Exception e){
                    Timber.e(e.getMessage());
                }
            }
        }
        super.userEventTriggered(ctx, evt);
    }*/
}
