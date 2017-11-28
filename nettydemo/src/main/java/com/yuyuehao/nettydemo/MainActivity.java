package com.yuyuehao.nettydemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";
    private EditText etTitle;
    private EditText etContent;
    private TextView tvRes;
    private MessageBroadcastReceiver receiver;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    tvRes.setText((String)msg.obj);
                    break;
                case 1:
                    etTitle.setText((String)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etTitle = (EditText) findViewById(R.id.etTitle);
        etContent = (EditText) findViewById(R.id.etContent);
        tvRes = (TextView) findViewById(R.id.tvRes);
        startService(new Intent(this, NettyService.class));
        registerBroadcast();

    }

    public void connect(View view){

    }

    public void send(View view){
        String msg = "{\"record\":\"april.set_equipment\",\"equipment_id\":\"1\"}\n";

        try {
            NettyClient.getInstance().sendMsgToServer(msg.getBytes("gb2312"), new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()){
                        Log.d(Const.TAG,"Send Success");
                    }else{
                        Log.d(Const.TAG,"Send Default");
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private class MessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("message") !=null){
                Log.d(Const.TAG,"MainActivity:"+intent.getStringExtra("message"));
                Log.d(Const.TAG,"isJson:"+isJson(intent.getStringExtra("message")));
                if (isJson(intent.getStringExtra("message"))){
                    int cmd = getCmd(intent.getStringExtra("message"));
                    if (cmd==104){
                        Message msg = new Message();
                        msg.obj = intent.getStringExtra("message");
                        msg.what = 0;
                        mHandler.sendMessage(msg);
                    }else if(cmd ==103){
                        tvRes.setText(intent.getStringExtra("message"));
                    }
                }else{
                    Message msg = new Message();
                    msg.obj = intent.getStringExtra("message");
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                }
            }

        }
    }

    private void registerBroadcast() {
        receiver = new MessageBroadcastReceiver();
        IntentFilter filter = new IntentFilter("com.yuyuehao.nettydemo.broadcast");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private void unregisterBroadcast(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
        mHandler.removeCallbacksAndMessages(null);
    }


    /**
     * 获取命令编号
     * @param json
     * @return
     */
    public static Integer getCmd(String json){
        Integer cmd=0;
        try{
            JsonObject o2 = new JsonParser().parse(json).getAsJsonObject();//解析json
            cmd=o2.get("command").getAsInt();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return cmd;
    }

    public static String getMessage(String json){
        String  message="";
        try{
            JsonObject o2 = new JsonParser().parse(json).getAsJsonObject();//解析json
            message =o2.get("message").getAsString();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return message;
    }

    public static boolean isJson(String json){
        boolean isjson=true;
       try {
           new JsonParser().parse(json).getAsJsonObject();
       }catch (Exception e){
           isjson=false;
       }

        return isjson;
    }

    public static String getRes(String json){
        String  result="";
        try{
            JsonObject o2 = new JsonParser().parse(json).getAsJsonObject();//解析json
            result =o2.get("result").getAsString();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
