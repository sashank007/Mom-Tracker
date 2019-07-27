package com.example.myapplication.Services;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.Activities.MainActivity;
import com.example.myapplication.Data.User;
import com.example.myapplication.R;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class TestService extends JobService
{
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = TestService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters job)
    {
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = firebaseAuth.getCurrentUser();
            Log.i(TAG, "onStartJob: my job service class is called.");
        StatFs statFs = new StatFs(Environment.getRootDirectory().getPath());
//        pushNotification("Alarm triggered","Alarm!");
        if(checkUpdateExpenses())
            pushNotification("Did you fill in your expenses for the day?" , "Mom sent you a message:");
        if(isMidnight())
            streakUpdater();
        Log.i("STREAK UPDATE SERVICE",
                "Free space is " +
                        (statFs.getAvailableBlocksLong() *
                                statFs.getBlockSizeLong() /
                                1024) +
                        " Kb"
        );
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job)
    {
        return false;
    }


    private void streakUpdater() {

        Query myQuery = mDatabase.child("users").child(mUser.getUid());
        myQuery.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //update to streak
                User currentUser  = dataSnapshot.getValue(User.class) ;
                System.out.println("current user :" + currentUser.currentStreak);
                int currentStreak = currentUser.currentStreak;
                int highestStreak = currentUser.highestStreak;
                updateStreak(currentStreak);
                if(currentStreak+1>highestStreak)
                    highestStreakUpdated(currentStreak+1);

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
        Toast.makeText(this, "Updated streak!!!!!!" , Toast.LENGTH_LONG).show();
        pushNotification("Updated your streak : " + newStreak , "Mom sent you a message:");
    }
    private boolean checkUpdateExpenses()
    {
        Calendar rightNow = Calendar.getInstance();
        int currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY); // return the hour in 24 hrs format (ranging from 0-23)
        if(currentHourIn24Format==18)
            return true;
        return false;
    }

    private boolean isMidnight()
    {
        Calendar rightNow = Calendar.getInstance();
        int currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY); // return the hour in 24 hrs format (ranging from 0-23)
        int currentMinute = rightNow.get(Calendar.MINUTE);
        Log.d(TAG,"CURRENT MINUTE:" + currentMinute+currentHourIn24Format);
        return (currentHourIn24Format==0&&currentMinute==1);
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
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
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

    private void highestStreakUpdated(int newStreak)
    { mDatabase.child("users").child(mUser.getUid()).child("highestStreak").setValue(newStreak);
        pushNotification("Way to go honey you just got a new max streak : " + newStreak , "Mom sent you a message:");

    }
}