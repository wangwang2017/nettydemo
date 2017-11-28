package com.yuyuehao.nettydemo;

import android.util.Log;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by Wang
 * on 2017-11-24
 */

public class NettyClient {

    private static NettyClient nettyClient = new NettyClient();
    private EventLoopGroup mEventLoopGroup;
    private NettyListener mNettyListener;
    private Channel mChannel;
    private boolean isConnect = false;
    private int reconnectNum = Integer.MAX_VALUE;
    private long reconnectIntervalTime = 5000;


    public static NettyClient getInstance(){
        return nettyClient;
    }

    public synchronized NettyClient connect(){
        if (!isConnect){
            mEventLoopGroup = new NioEventLoopGroup();
            Bootstrap mBootstrap = new Bootstrap().group(mEventLoopGroup)
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .channel(NioSocketChannel.class)
                    .handler(new NettyClientInitializer(mNettyListener));
            try{
                mBootstrap.connect(Const.HOST,Const.PORT).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()){
                            isConnect = true;
                            mChannel = future.channel();
                        }else{
                            isConnect = false;
                        }
                    }
                }).sync();
            }catch (Exception e){
                Log.e(Const.TAG, e.getMessage());
                mNettyListener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_ERROR);
                reconnect();
            }
        }
        return this;
    }

    public void disconnect() {
        mEventLoopGroup.shutdownGracefully();
    }

    public void reconnect(){
        if (reconnectNum >0 && !isConnect){
            reconnectNum--;
            try{
                Thread.sleep(reconnectIntervalTime);
            }catch (InterruptedException e){}
            Log.e(Const.TAG,"重新连接");
            disconnect();
            connect();
        }else{
            disconnect();
        }
    }

    public boolean sendMsgToServer(byte[] data,ChannelFutureListener listener){
        boolean flag = mChannel != null && isConnect;
        if (flag){
            ByteBuf buf = Unpooled.copiedBuffer(data);
            mChannel.writeAndFlush(buf).addListener(listener);
        }
        return flag;
    }

    public void setReconnectNum(int reconnectNum){
        this.reconnectNum = reconnectNum;
    }

    public void setReconnectIntervalTime(long reconnectIntervalTime){
        this.reconnectIntervalTime = reconnectIntervalTime;
    }

    public boolean getConnectStatus(){
        return isConnect;
    }

    public void setConnectStatus(boolean status){
        this.isConnect = status;
    }

    public void setListener(NettyListener listener){
        this.mNettyListener = listener;
    }

}
