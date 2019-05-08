package com.example.myapplication.Receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.Activities.StartFragmentActivity;
import com.example.myapplication.R;
import com.example.myapplication.Services.StartFragmentService;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

public class DismissReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context, "Notification Dialog Closed",
//                Toast.LENGTH_LONG).show();
        if(intent.getStringExtra("gotoFragment").equals("Close")) {
            Log.d("Notification:", "Notification Dialog Closed");
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(0);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(1);
        }
        else
        {
            Log.d("Notification:" , "Opening expense tracker fragment");
            String amount = intent.getExtras().getString("amount");
            Intent i = new Intent(context , StartFragmentActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("gotoFragment","ExpenseTrackerFragment");
            i.putExtra("amount",amount);
//            i.putExtra("gotoFragment","ExpenseTrackerFragment");
            context.startActivity(i);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(1);

        }

    }

}
