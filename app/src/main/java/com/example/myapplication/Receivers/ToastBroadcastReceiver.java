package com.example.myapplication.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.myapplication.Services.BackgroundService;

public class ToastBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("backgroundService","inside toastbroadcastreceiver");
        Intent serviceIntent= new Intent(context, BackgroundService.class);
        context.startService(serviceIntent);

    }
}