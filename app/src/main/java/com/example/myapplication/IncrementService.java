package com.example.myapplication;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Room;

public class IncrementService extends Service {
    TextView currentStreak;
    public int counter=0;
    private String firstName = "sashank";
    private String lastName = "tungaturthi";
    private static String CHANNEL_DEFAULT_IMPORTANCE= "High";
    private static String userEmail="sashank.tungaturthi@gmail.com";
    private static int ONGOING_NOTIFICATION_ID=1;
    private int uid;
    public static final String BROADCAST_ACTION = "com.example.tracking.updateprogress";
    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";
    private User currentUser;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    AppDatabase db;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = Room.databaseBuilder(getApplicationContext() , AppDatabase.class , "production")
                .allowMainThreadQueries()
                .build();
        currentUser = db.userDao().findByName(firstName , lastName);
        // Query the database and show alarm if it applies
        Toast.makeText(this,"started increment service" , Toast.LENGTH_SHORT).show();
//        IncrementListener.onIncrementReceived(10);
//        counter = intent.getStringExtra("currentStreak");
        intent = new Intent(BROADCAST_ACTION);
        sendBroadcast(intent);
        Thread myThread = null;
        System.out.println("fired increment service");
        incrementCounter();

        return START_NOT_STICKY;
    }
//
//
//    private void startMyOwnForeground(){
//        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
//        String channelName = "My Background Service";
//        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
//        chan.setLightColor(Color.BLUE);
//        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
//        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        assert manager != null;
//        manager.createNotificationChannel(chan);
//        int currentStreak = db.userDao().getCurrentStreak("sas" , "hunk");
//
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
//        Notification notification = notificationBuilder.setOngoing(true)
//                .setSmallIcon(R.drawable.ic_launcher_background)
//                .setContentTitle("Your current streak: " + currentStreak)
//                .setPriority(NotificationManager.IMPORTANCE_MIN)
//                .setCategory(Notification.CATEGORY_SERVICE)
//                .build();
//        startForeground(2, notification);
//    }

    class CountDownRunner implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    startCountDown();
                    Thread.sleep(1000); // Pause of 1 Second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }
    private void startCountDown() {
        {
            mHandler.post(new Runnable() {
                public void run() {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");


                        Date systemDate = Calendar.getInstance().getTime();
                        System.out.println("system date:" + systemDate.toString());
                        String myDate = sdf.format(systemDate);
                        Date Date1 = sdf.parse(myDate);
                        Date Date2 = sdf.parse("24:00:00 am");

                        long millse = Date2.getTime() - Date1.getTime();
                        long mills = (millse);

                        int Hours = (int) (mills / (1000 * 60 * 60));
                        int Mins = (int) (mills / (1000 * 60)) % 60;
                        long Secs = (int) (mills / 1000) % 60;
                        System.out.println("hours mins secs in service : " + Hours + " " + Mins + " " + " " + Secs);
                        if(Hours==6&&Mins==1&&Secs==1)
//                            requestExpensesUpdate();
                        if (Hours == 6&& Mins == 29&& Secs == 1) {

//                            incrementCounter();
                        }
                        String diff = Hours + ":" + Mins + ":" + Secs; // updated value every1 second
//                        tvCountDownTimer.setText(diff);
                    } catch (Exception e) {

                    }
                }
            });
        }


    }
    private void incrementCounter()
    {
        int currentStreakVal = db.userDao().getCurrentStreak(userEmail);
        uid = currentUser.uid;
        currentStreakVal++;
        db.userDao().updateCurrentStreak(currentStreakVal, uid);
        System.out.println("incrementing count : " + currentStreakVal);
        pushNotification("Incremented your streak: " + currentStreakVal ,"Streak Update" );
    }
    private void decrementHours()
    {
        long currentHours = db.userDao().getTimeLeft("new", "user");
        if(currentHours==1)
            currentHours=24;
        else
            currentHours--;
        System.out.println("decrementing hours: " + currentHours );
        currentUser=db.userDao().findByName("new" , "user");
        db.userDao().updateTimeLeft(currentHours,currentUser.uid);
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.set(
                alarm.RTC_WAKEUP,
                MainActivity.currentTime + (1000),
                PendingIntent.getService(this, 0, new Intent(this, IncrementService.class), PendingIntent.FLAG_UPDATE_CURRENT)
        );

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // I want to restart this service again in one hour
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        System.out.println("destroyed incrementService");

    }


    private void sendBroadcastMessage(int val) {
        Intent intent = new Intent("custom-event-name");
        intent.putExtra("streak" , Integer.toString(val));
        System.out.println("send broadcast message");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        }

    private void startForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent)
                .build());
    }
    public void pushNotification(String msgText , String msgTitle)
    {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
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


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setTicker(getString(R.string.app_name))
                //     .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(msgTitle)
                .setContentText(msgText)
                .setContentInfo("Info");

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }

}