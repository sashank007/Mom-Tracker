package com.example.myapplication.Receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.Services.NotificationIntentService;

public class NotificationsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                NotificationIntentService.class.getName());
        String amount =intent.getExtras().getString("amount");
        Toast.makeText(context,"amount: " + amount  , Toast.LENGTH_LONG).show();
        System.out.println("NOTIFICATION RECEIVER"  + amount);
        Log.d("NOTIFICATIONRECEIVER" , amount);
        Intent i = new Intent(context,NotificationIntentService.class );
        i.putExtra("amount","30");
        context.startService(i);
//      Start the service, keeping the device awake while it is launching.
//      startWakefulService(context, (intent.setComponent(comp)));
//      setResultCode(Activity.RESULT_OK);
    }

}