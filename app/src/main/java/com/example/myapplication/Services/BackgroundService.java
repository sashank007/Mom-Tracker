package com.example.myapplication.Services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.myapplication.Receivers.ResponseBroadcastReceiver;

import androidx.annotation.Nullable;

public class BackgroundService extends IntentService {
        public static final String ACTION="ke.co.appslab.androidbackgroundservices.Receivers.ResponseBroadcastReceiver";
        private static final String  MOM_KEEPS_TRACK_PACKAGE_NAME = "com.example.myapplication";
        private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

        // Must create a default constructor
    public BackgroundService() {
        // Used to name the worker thread, important only for debugging.
        super("backgroundService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // This describes what will happen when service is triggered
        Log.d("backgroundService","Service running");

//        //create a broadcast to send the toast message
//        Intent toastIntent= new Intent(this,ResponseBroadcastReceiver.class );
//        toastIntent.putExtra("toastMessage","I'm running after ever 15 minutes");
//        sendBroadcast(toastIntent);

        // Finally we register a receiver to tell the MainActivity when a notification has been received
        NotificationBroadcastReceiver notificationsReceiver = new NotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MOM_KEEPS_TRACK_PACKAGE_NAME);

        registerReceiver(notificationsReceiver , intentFilter);

    }

    /**

     * We use this Broadcast Receiver to notify the Main Activity when
     * a new notification has arrived, so it can properly change the
     * notification image
     * */
    public class NotificationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("Title");
            String text = intent.getStringExtra("Text");
            String dollarAmount = intent.getStringExtra("DollarAmount");

            Log.d("NOTIFICATION_RECEIVER" , "GOT TITLE : " + dollarAmount + " " + text);
            Intent i = new Intent(context, NotificationIntentService.class );
            i.putExtra("amount", dollarAmount);
            context.startService(i);
        }
    }
}