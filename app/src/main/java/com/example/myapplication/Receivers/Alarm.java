package com.example.myapplication.Receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.Services.BackgroundService;
import com.example.myapplication.Services.NotificationIntentService;
import com.example.myapplication.Services.StreakUpdaterService;
import com.example.myapplication.Services.TestService;

import java.util.Calendar;

public class Alarm extends BroadcastReceiver
{
    long endTime = 0 , startTime =0 , totalTime=0;

    @Override
    public void onReceive(Context context, Intent intent)
    {

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:wakelock");
        wl.acquire();

        // Put here YOUR code.
        endTime  = System.currentTimeMillis();
        totalTime = endTime - startTime;
        Log.d("ALARM","Alarm!!!!!!");
        Toast.makeText(context, "Welcome --------1", Toast.LENGTH_LONG).show();
//        Intent background = new Intent(context , NotificationIntentService.class);
//        context.startService(background);
        // For example
//        updateDb(context);
//        context.startService(new Intent(context , StreakUpdaterService.class));
//        context.startService(new Intent(context, UpdateHoursService.class));
//        Intent background = new Intent(context, StreakUpdaterService.class);
//        context.startService(background);
        wl.release();

    }

    public void updateDb(Context context)
    {

    }
    public void setAlarm(Context context)
    {
//        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
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
                new Intent(context, Alarm.class),PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

}