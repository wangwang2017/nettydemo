package com.yuyuehao.nettydemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Wang
 * on 2017-11-27
 */

public class BaseActivity extends AppCompatActivity {

    private MessageBroadcastReceiver receiver;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        registerBroadcast();
    }

    private void registerBroadcast() {
        receiver = new MessageBroadcastReceiver();
        IntentFilter filter = new IntentFilter("com.yuyuehao.nettydemo.broadcast");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private class MessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Const.TAG,"MainActivity:"+intent.getStringExtra("message"));

        }
    }

    private void unregisterBroadcast(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }
}
