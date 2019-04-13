package com.example.myapplication.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.myapplication.Activities.MainActivity;
import com.example.myapplication.Data.Expense;
import com.example.myapplication.R;
import com.example.myapplication.Receivers.DismissReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.room.Database;

public class NotificationIntentService extends IntentService {


    public NotificationIntentService()
    {
        super("NotificationIntentService");
    }

    FirebaseAuth firebaseAuth;
    DatabaseReference mDatabase;
    FirebaseUser mUser;
    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
//        String amount = intent.getStringExtra("amount");
//        Bundle bundle = intent.getExtras();
//        String amount = bundle.getString("amount");
        String amount="0";
        firebaseAuth  = FirebaseAuth.getInstance();
        mUser  = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if(intent.hasExtra("amount"))
        amount = intent.getExtras().getString("amount");

        Log.d("NotificationIntentService","OnHandleIntent " + amount);
        pushNotification("Did you make a purchase of " + amount+"?" , "Mom sent you a message:",this);

    }
    private void updateExpenses(Expense exp)
    {
        String uniqueID = UUID.randomUUID().toString();
        mDatabase.child("expenses").child(mUser.getUid()).child(uniqueID).setValue(exp);


    }


    public void pushNotification(String msgText , String msgTitle , Context context )
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_02";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent sfIntent = new Intent(context, MainActivity.class);
        //dismiss the dialog
        Intent noIntent = new Intent(context, DismissReceiver.class);
        Intent yesIntent = new Intent(context, DismissReceiver.class);
        noIntent.putExtra("gotoFragment" , "Close");
        yesIntent.putExtra("gotoFragment"  , "ExpenseTrackerFragment");

        //Create the PendingIntent
        PendingIntent btPendingIntentNo = PendingIntent.getBroadcast(context, 1234, noIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent btPendingIntentYes = PendingIntent.getBroadcast(context, 1234, yesIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        NotificationCompat.Action actionno = new NotificationCompat.Action.Builder(R.drawable.ic_thumb_down_grey600_48dp, "No", btPendingIntentNo).build();
        NotificationCompat.Action actionyes = new NotificationCompat.Action.Builder(R.drawable.ic_thumb_up_grey600_48dp, "Yes", btPendingIntentYes).build();

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.mom_logo)
                .setTicker(getString(R.string.app_name))
                //     .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(msgTitle)
                .setContentText(msgText)
//                .addAction(R.drawable.ic_thumb_up_grey600_48dp , "No" , btPendingIntentNo)
//                .addAction(R.drawable.ic_thumb_down_grey600_48dp , "Yes" , btPendingIntentYes )
                .addAction(actionno)
                .addAction(actionyes)
                .setContentInfo("Info");

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }
}
