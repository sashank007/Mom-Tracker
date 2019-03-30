package com.example.savestreak;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import android.widget.TextView;
import android.widget.Toast;

import com.example.savestreak.Data.AppDatabase;
import com.example.savestreak.Data.User;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Room;

public class UpdateHoursService extends Service {
    TextView currentStreak;
    public int counter=0;
    private static String CHANNEL_DEFAULT_IMPORTANCE= "High";
    private static int ONGOING_NOTIFICATION_ID=1;
    private String userEmail = "sashank.tungaturthi@gmail.com";
    public static final String BROADCAST_ACTION = "com.example.tracking.updateprogress";
    private User currentUser;
    private  long AlarmTime;
    private String firstName="sashank" , lastName="tungaturthi";
    AppDatabase db;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("on start command Update hour service");
        db = Room.databaseBuilder(getApplicationContext() , AppDatabase.class , "production")
                .allowMainThreadQueries()
                .build();
        // Query the database and show alarm if it applies
        Toast.makeText(this,"hours left:" + db.userDao().getTimeLeft(firstName,lastName) , Toast.LENGTH_SHORT).show();
        intent = new Intent(BROADCAST_ACTION);
        sendBroadcast(intent);
        System.out.println("received broadcast from alarm ");
//        incrementCounter();
        decrementHours();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
        stopSelf();
        return START_STICKY;
    }


    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        int currentStreak = db.userDao().getCurrentStreak(userEmail);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Your current streak: " + currentStreak)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private void decrementHours()
    {
        long currentHours = db.userDao().getTimeLeft(firstName,lastName);
        if(currentHours==1)
            currentHours=24;
        else
            currentHours--;
        System.out.println("in decrement hours alarm time:" + currentHours );
//        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
//        alarm.set(
//                alarm.RTC_WAKEUP,
//                AlarmTime,
//                PendingIntent.getService(this, 0, new Intent(this, UpdateHoursService.class), 0)
//        );
        sendBroadcastMessage(currentHours);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

    }

    private void sendBroadcastMessage(long timeLeft) {
        Intent intent = new Intent("custom-event-name2");
        System.out.println("send broadcast message");
        AlarmTime = db.userDao().getTimeLeft(firstName,lastName) + MainActivity.hourInterval;
        currentUser = db.userDao().findByName(firstName,lastName);
        db.userDao().updateTimeLeft( timeLeft,currentUser.uid);
        System.out.println("updated time left: " + timeLeft );
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

}