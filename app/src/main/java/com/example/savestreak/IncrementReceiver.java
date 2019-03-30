package com.example.savestreak;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class IncrementReceiver extends BroadcastReceiver {
    static int i = 0;
    @Override
    public void onReceive(final Context context, Intent intent) {
        i+=1;
        System.out.println("called increment receive "  + i);
        Util.scheduleJob(context);
        // do the thing in here
        // including figuring out the next time you want to run
        // and scheduling another PendingIntent with the AlarmManager
    }
}