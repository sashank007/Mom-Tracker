package com.example.savestreak;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.savestreak.Data.AppDatabase;
import com.example.savestreak.Data.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

public class HomeFragment extends Fragment {

    AppDatabase db;
    private TextView greeting, tvCountDownTimer;
    private TextView currentStreak;
    private String firstName = "sashank";
    private String lastName = "tungaturthi";
    private String userEmail = "sashank.tungaturthi@gmail.com";
    public long dayMillis = 1000 * 60 * 60 * 24;
    ColorStateList myStateList;
    //@TODO:Change this to an hour
    public long hourTimer = 1000 * 60;
    public static int hourInterval = 1000 * 60;
    private int uid;
    private long hoursLeft;
    User currentUser;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View v = lf.inflate ( R.layout.fragment_home, container, false );
        Thread myThread = null;
        Runnable myRunnableThread = new CountDownRunner();
        myThread = new Thread(myRunnableThread);
        myThread.start();

        currentStreak =  v.findViewById(R.id.tv_currentStreak);
//        greeting =v.findViewById(R.id.tv_greeting);
        tvCountDownTimer =v.findViewById(R.id.tv_countDown);
        db = Room.databaseBuilder(getActivity(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        currentUser = db.userDao().findByName(firstName , lastName);


//        updateCurrentUser();
        if (currentUser!=null) {
            updateCurrentUser();
        }

        return v;
    }
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

    private void updateCurrentUser() {

//            greeting.setText("Hey " + currentUser.firstName);
            int currentStreakVal = db.userDao().getCurrentStreak(userEmail);
            uid = currentUser.uid;
            db.userDao().updateTimeLeft(24, uid);
            hoursLeft = db.userDao().getTimeLeft(firstName, lastName);
//            tvCountDownTimer.setText("Hours left:" + hoursLeft);
            System.out.println("hours left:" + hoursLeft);
            int highestStreakVal = currentUser.highestStreak;
            if (highestStreakVal < currentStreakVal)
                highestStreakVal = currentStreakVal;
            System.out.println("current streak: " + currentStreakVal);
            currentStreak.setText(Integer.toString(currentStreakVal));

    }
    private void incrementCounter() {
        System.out.println("incrementing counter");
        int currentStreakVal = db.userDao().getCurrentStreak(userEmail);
        currentStreakVal++;
        db.userDao().updateCurrentStreak(currentStreakVal, uid);
        pushNotification("Updated current streak: " + currentStreakVal , "Streak Update");
        currentStreak.setText(Integer.toString(currentStreakVal));

    }

//    private void createNotificationChannel() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = getString(R.string.channel_name);
//            String description = getString(R.string.channel_description);
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }
    private void startCountDown() {
        {
            mHandler.post(new Runnable() {
                public void run() {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");


                        Date systemDate = Calendar.getInstance().getTime();
//                        System.out.println("system date:" + systemDate.toString());
                        String myDate = sdf.format(systemDate);
                        Date Date1 = sdf.parse(myDate);
                        Date Date2 = sdf.parse("24:00:00 am");

                        long millse = Date2.getTime() - Date1.getTime();
                        long mills = (millse);

                        int Hours = (int) (mills / (1000 * 60 * 60));
                        int Mins = (int) (mills / (1000 * 60)) % 60;
                        long Secs = (int) (mills / 1000) % 60;
//                        System.out.println("hours mins secs : " + Hours + " " + Mins + " " + " " + Secs);
                        if(Hours==6&&Mins==1&&Secs==1)
                            requestExpensesUpdate();
                        if (Hours == 6&& Mins == 29&& Secs == 1) {

                            incrementCounter();
                        }
                        String diff = Hours + ":" + Mins + ":" + Secs; // updated value every1 second
                        tvCountDownTimer.setText(diff);
                    } catch (Exception e) {

                    }
                }
            });
        }


    }


    public void requestExpensesUpdate()
    {
        pushNotification("Did you fill in your expenses for the day?!!"  , "Quick!");
    }
    public void pushNotification(String msgText , String msgTitle)
    {
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
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


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getActivity(), NOTIFICATION_CHANNEL_ID);

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
