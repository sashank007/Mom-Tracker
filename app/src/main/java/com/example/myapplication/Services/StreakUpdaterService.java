package com.example.myapplication.Services;


import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.Activities.MainActivity;
import com.example.myapplication.Data.Expense;
import com.example.myapplication.Data.User;
import com.example.myapplication.R;
import com.example.myapplication.Receivers.ResponseBroadcastReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class StreakUpdaterService extends IntentService {

    private boolean isRunning;
    private Context context;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private FirebaseAuth firebaseAuth;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public StreakUpdaterService() {
        // Used to name the worker thread, important only for debugging.
        super("StreakUpdaterService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // This describes what will happen when service is triggered
        Log.i("backgroundService","Service  called on handle intent");
        streakUpdater();
        //create a broadcast to send the toast message
        Intent toastIntent= new Intent(this, ResponseBroadcastReceiver.class );
        toastIntent.putExtra("toastMessage","I'm running after ever 15 minutes");
        sendBroadcast(toastIntent);

    }

//    @Override
//    public void onCreate() {
//        this.context = this;
//        this.isRunning = false;
//
//        Log.d("ALARM","onCreate StreakUpdaterService");
//
//    }

    private Runnable myTask = new Runnable() {
        public void run() {
            // Do something here
            stopSelf();
        }
    };

    //    @Override
    ////    public void onDestroy() {
    ////        this.isRunning = false;
    ////    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
////        if(!this.isRunning) {
////            this.isRunning = true;
////
////        }
//        super.onStartCommand(intent, flags, startId);
//        Toast.makeText(this , "on create in streak updater" , Toast.LENGTH_LONG).show();
////        pushNotification("Wakeup called"  , "Wakeup");
////        if(isMidnight())
//        streakUpdater();
//        return START_STICKY;
//    }

    private void streakUpdater() {
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = firebaseAuth.getCurrentUser();
        Query myQuery = mDatabase.child("users").child(mUser.getUid());
        myQuery.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //update to streak
                User currentUser  = dataSnapshot.getValue(User.class) ;
                System.out.println("current user :" + currentUser.currentStreak);
                int currentStreak = currentUser.currentStreak;
                updateStreak(currentStreak);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void updateStreak(int currStreak)
    {
        int newStreak = currStreak+1;
        mDatabase.child("users").child(mUser.getUid()).child("currentStreak").setValue(newStreak);
    Toast.makeText(this, "Updated streak!!!!!!1" , Toast.LENGTH_LONG).show();
//        pushNotification("Updated your streak : " + newStreak , "Mom sent you a message:");
    }

    private boolean isMidnight()
    {
        Calendar rightNow = Calendar.getInstance();
        int currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY); // return the hour in 24 hrs format (ranging from 0-23)
        if(currentHourIn24Format==0)
            return true;
        return false;
    }
    public void pushNotification(String msgText , String msgTitle )
    {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

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
        Intent intent = new Intent(getApplicationContext() , MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, 0);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.mom_logo)
                .setTicker(getString(R.string.app_name))
                //     .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(msgTitle)
                .setContentText(msgText)
                .setContentIntent(pendingIntent)
                .setContentInfo("Info");

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }


}