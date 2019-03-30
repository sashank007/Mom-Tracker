package com.example.savestreak;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.savestreak.Data.AppDatabase;
import com.example.savestreak.Data.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

public class MainActivity extends AppCompatActivity  {

    AppDatabase db;
    private TextView greeting, tvCountDownTimer;
    private TextView currentStreak;
    public static String firstName = "sashank";
    public static String lastName = "tungaturthi";
    private String userEmail = "sashank.tungaturthi@gmail.com";
    public long dayMillis = 1000 * 60 * 60 * 24;
    ColorStateList myStateList;
    //@TODO:Change this to an hour
    public long hourTimer = 1000 * 60;
    public static int hourInterval = 1000 * 60;
    private int uid;
    private long hoursLeft;
    User currentUser;

    public static long currentTime = System.currentTimeMillis();
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    loadFragment(new HomeFragment());
                    return true;
                case R.id.navigation_dashboard:
                    loadFragment(new DashboardFragment());
                    return true;
                case R.id.navigation_notification:
                    loadFragment(new ProfileFragment());
                    return true;

            }
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadFragment(new HomeFragment());
        BottomNavigationView navigation =  findViewById(R.id.navigation);
        navigation.setBackgroundColor(getResources().getColor(R.color.white));
        myStateList=getColorStateList();
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setItemTextColor(myStateList);
        currentStreak = findViewById(R.id.tv_currentStreak);

        Intent alarmIntent = new Intent(this, IncrementReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, 60000, 11000, pendingIntent);

//        if(!isMyServiceRunning(IncrementService.class))
//            this.startService(myIntent);

        SmsReceiver.bindListener(new SmsListener() {
            @Override
            public void messageReceived(String messageText) {
                messageText = messageText.toLowerCase();

                Log.e("Message", messageText);
                Toast.makeText(MainActivity.this, "Message: " + messageText, Toast.LENGTH_LONG).show();
                // If your OTP is six digits number, you may use the below code
                if (messageText.contains("debit")) {
                    currentStreak.setText("0");
                }

                Toast.makeText(MainActivity.this, "Message : " + messageText, Toast.LENGTH_LONG).show();

            }
        });

    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    //    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Get extra data included in the Intent
//            int currentStreakVal = db.userDao().getCurrentStreak("new" ,"user");
//
//            System.out.println("inside mMessageReceiver , got message from service and updating:"  + currentStreakVal);
//            currentStreak.setText(Integer.toString(currentStreakVal));
//        }
//    };
//    private BroadcastReceiver mUpdateHours = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Get extra data included in the Intent
//            long currentHours = db.userDao().getTimeLeft(firstName , lastName);
////            if (currentHours==1) {
//
////                System.out.println("current hours is 1");
////                incrementCounter();
////            }
//            System.out.println("inside mUpdateHours , got message from decrementService " + currentHours);
////            tvCountDownTimer.setText("Hours left: " + Long.toString(currentHours));
//        }
//    };

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
//    @Override
//    protected void onDestroy() {
//
////        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateHours);
//
////        super.onDestroy();
//        System.out.println("destroyed service in MainActivity onDestroy");
//
//    }

    private ColorStateList getColorStateList()
    {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] { android.R.attr.state_pressed},
                new int[] {-android.R.attr.state_pressed} // pressed

        };

        int[] colors = new int[] {
                Color.GRAY,
                Color.GRAY,
                getColor(R.color.colorLight),
                getColor(R.color.colorLight)
        };
        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }
}
