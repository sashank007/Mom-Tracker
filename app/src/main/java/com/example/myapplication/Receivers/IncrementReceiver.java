package com.example.myapplication.Receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.example.myapplication.Data.Expense;
import com.example.myapplication.Data.User;
import com.example.myapplication.Services.StreakUpdaterService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;

public class IncrementReceiver extends BroadcastReceiver
{
    long endTime = 0 , startTime =0 , totalTime=0;
    public static final int REQUEST_CODE = 12345;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private FirebaseAuth firebaseAuth;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:wakelock");
        wl.acquire();
//
        // Put here YOUR code.
        endTime  = System.currentTimeMillis();
        totalTime = endTime - startTime;
//        context.startService(new Intent(context, UpdateHoursService.class));
        System.out.println("on receive alarm");
//        streakUpdater();
        Intent background = new Intent(context, StreakUpdaterService.class);
        context.startService(background);
        wl.release();
    }

    public void setAlarm(Context context)
    {
//        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, IncrementReceiver.class);
//
//        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
//        startTime = System.currentTimeMillis();
//        System.out.println("set alarm"  + startTime);
//        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime+1000 *60, pi); // Millisec * Second * Minute
        Calendar calendar = Calendar.getInstance();
        //@TODO:change to 12 am (0)
        calendar.set(Calendar.HOUR_OF_DAY,0  ); // For 12 am
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        System.out.println("calendar:" + calendar.toString());
        PendingIntent pi = PendingIntent.getBroadcast(context, 0,
                new Intent(context, IncrementReceiver.class),PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, IncrementReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }


    private void streakUpdater() {
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = firebaseAuth.getCurrentUser();
        Query myQuery = mDatabase.child("users").child(mUser.getUid());
        myQuery.addValueEventListener(new ValueEventListener() {
            List<Expense> myList = new ArrayList<>();

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            //update to streak
                User currentUser  = dataSnapshot.getValue(User.class) ;
                int currentStreak = currentUser.currentStreak;
                currentStreak++;
                updateStreak(currentStreak);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void updateStreak(int newStreak)
    {
        mDatabase.child("users").child(mUser.getUid()).child("currentStreak").setValue(newStreak);
    }


}