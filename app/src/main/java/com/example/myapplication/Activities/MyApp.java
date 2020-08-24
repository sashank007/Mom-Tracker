package com.example.myapplication.Activities;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.myapplication.Receivers.Alarm;
import com.example.myapplication.Receivers.IncrementReceiver;
import com.example.myapplication.Services.TestService;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Trigger;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Your methods here...
//        startAlarm();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //@TODO: UNcomment this when publishing
//        if(!prefs.getBoolean("firstTime", false)) {
            // run your one time code
//            startUpdateJob();
//            SharedPreferences.Editor editor = prefs.edit();
//            editor.putBoolean("firstTime", true);
//            editor.commit();
//            Log.d("MYAPP","First time running job");
//        }
//        else
//        {
//            Log.d("MYAPP","Job already started");
//        }

    }

    public void startAlarm()
    {
        System.out.println("start alarm");
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), Alarm.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, IncrementReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                30000, pIntent);

    }
    private void startUpdateJob()
    {
        FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(
                        new GooglePlayDriver(getApplicationContext())
                );
        dispatcher.mustSchedule(
                dispatcher.newJobBuilder()
                        .setService(TestService.class)
                        .setTag("UpdateStreakService")
                        .setRecurring(true)
                        .setTrigger(Trigger.executionWindow(5, 30))
                        .build()
        );
    }
}