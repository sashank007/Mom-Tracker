package com.example.myapplication.Services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.example.myapplication.Receivers.ResponseBroadcastReceiver;

import androidx.annotation.Nullable;

public class BackgroundService extends IntentService {
    public static final String ACTION="ke.co.appslab.androidbackgroundservices.Receivers.ResponseBroadcastReceiver";

    // Must create a default constructor
    public BackgroundService() {
        // Used to name the worker thread, important only for debugging.
        super("backgroundService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // This describes what will happen when service is triggered
        Log.i("backgroundService","Service running");

        //create a broadcast to send the toast message
        Intent toastIntent= new Intent(this,ResponseBroadcastReceiver.class );
        toastIntent.putExtra("toastMessage","I'm running after ever 15 minutes");
        sendBroadcast(toastIntent);

    }
}