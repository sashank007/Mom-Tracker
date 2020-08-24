package com.example.myapplication.Services;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class NotificationListeningService extends Service {

    private static final String  MOM_KEEPS_TRACK_PACKAGE_NAME = "com.example.myapplication";

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; //this defines this service to stay alive
    }

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationBroadcastReceiver notificationsReceiver = new NotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MOM_KEEPS_TRACK_PACKAGE_NAME);

        registerReceiver(notificationsReceiver , intentFilter);
    }
    @Override
    public IBinder onBind(Intent intent){
        return null;
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
