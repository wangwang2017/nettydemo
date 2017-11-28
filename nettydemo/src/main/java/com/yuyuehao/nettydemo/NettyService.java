package com.yuyuehao.nettydemo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledExecutorService;

import io.netty.buffer.ByteBuf;

/**
 * Created by Wang
 * on 2017-11-24
 */

public class NettyService extends Service implements NettyListener{

    private static final String BROADCAST_ACTION = "com.yuyuehao.nettydemo.broadcast";
    private static final String MESSAGE = "message";
    private NetworkReceiver receiver;
    private static String sessionId = null;
    private WeakReference<Context> mContext;

    private ScheduledExecutorService mScheduledExecutorService;
    private void shutdown() {
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdown();
            mScheduledExecutorService = null;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = new WeakReference<Context>(getApplication());
        receiver = new NetworkReceiver();
        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);


        // 自定义心跳，每隔20秒向服务器发送心跳包
//        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
//        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                String heart = "心跳";
//                byte[] requestBody = new byte[0];
//                try {
//                    requestBody = heart.getBytes("gb2312");
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//                NettyClient.getInstance().sendMsgToServer(requestBody, new ChannelFutureListener() {    //3
//                    @Override
//                    public void operationComplete(ChannelFuture future) {
//                        if (future.isSuccess()) {                //4
//                            Log.d(Const.TAG,"Write heartbeat successful");
//                        } else {
//                            Log.e(Const.TAG,"Write heartbeat error");
//                        }
//                    }
//                });
//            }
//        }, 20, 20, TimeUnit.SECONDS);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NettyClient.getInstance().setListener(this);
        connect();
        Log.d(Const.TAG,"start service");
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onMessageResponse(ByteBuf byteBuf) {
        int indexLF = byteBuf.indexOf(0,byteBuf.capacity(),(byte)10);
        String showbyte = null;
        byte[] realByte;
        if (indexLF > -1) {
                realByte =byteBuf.copy(0,indexLF).array();
                String showb = "";
                for(int i=0;i<realByte.length;i++){//422字节
                    byte bytes = realByte[i];
                    int result = bytes&0xff;
                    showb += result+" ";
                }
                Log.d(Const.TAG,"data:"+showb);

        }else{
            realByte = byteBuf.array();
        }
        try {
            if (realByte.length!=0){
                showbyte = new String(realByte,"gb2312").trim();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if(mContext!=null && showbyte != null){
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra(MESSAGE, showbyte);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

    }

    private void connect(){
        if (!NettyClient.getInstance().getConnectStatus()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NettyClient.getInstance().connect();//连接服务器
                }
            }).start();
        }
    }

    @Override
    public void onServiceStatusConnectChanged(int statusCode) {
        if (statusCode == NettyListener.STATUS_CONNECT_SUCCESS) {
            Log.d(Const.TAG,"Tcp connected");
        } else {
            Log.d(Const.TAG,"Tcp connect error");
        }
    }


    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                        || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    connect();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        shutdown();
        NettyClient.getInstance().setReconnectNum(0);
        NettyClient.getInstance().disconnect();
    }

}
