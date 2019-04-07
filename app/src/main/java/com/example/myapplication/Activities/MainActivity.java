package com.example.myapplication.Activities;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Alarm;
import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.User;
import com.example.myapplication.Fragments.DashboardFragment;
import com.example.myapplication.Fragments.HomeFragment;
import com.example.myapplication.Fragments.ProfileFragment;
import com.example.myapplication.Receivers.IncrementReceiver;
import com.example.myapplication.R;
import com.example.myapplication.Receivers.SmsListener;
import com.example.myapplication.Receivers.SmsReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity  {

    AppDatabase db;
    public static String TAG="Debug";
    private TextView greeting, tvCountDownTimer;
    private TextView currentStreak;
    public static String firstName = "sashank";
    public static String lastName = "tungaturthi";
    private String userEmail = "sashank.tungaturthi@gmail.com";
    public long dayMillis = 1000 * 60 * 60 * 24;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    ColorStateList myStateList;
    SharedPreferences sharedPreferences;
    //@TODO:Change this to an hour
    public long hourTimer = 1000 * 60;
    SharedPreferences.Editor editor;
    public static int hourInterval = 1000 * 60;
    private int uid;
    private long hoursLeft;
    User currentUser;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private FirebaseAuth firebaseAuth;

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
                case R.id.navigation_profile:
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
//        navigation.setItemTextColor(myStateList);
        sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        currentStreak = findViewById(R.id.tv_currentStreak);
        System.out.println("alarm up : ? " + checkAlarmUp());
//        if(!checkAlarmUp())
            scheduleAlarm();
//        streakUpdater();

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

//
//    private void fireService() {
//
//        editor = sharedPreferences.edit();
//        editor.remove(INTENT_FIRST_TIME);
//        editor.commit();
//
//
//        Intent myIntent = new Intent(this, IncrementReceiver.class);
//
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(  MainActivity.this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
////        myIntent.setData((Uri.parse("custom://"+System.currentTimeMillis())));
////        alarmManager.cancel(pendingIntent);
////
////        Calendar alarmStartTime = Calendar.getInstance();
////        Calendar now = Calendar.getInstance();
////        alarmStartTime.set(Calendar.HOUR_OF_DAY,2);
////        alarmStartTime.set(Calendar.MINUTE, 49);
////        alarmStartTime.set(Calendar.SECOND, 0);
////        if (now.after(alarmStartTime)) {
////            Log.d("Hey","Added a day");
////            alarmStartTime.add(Calendar.DATE, 1);
////        }
////        System.out.println("added a day");
////        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
////        Log.d("Alarm","Alarms set for everyday 8 am.");
//
//
//        System.out.println("shared pref has INTENT_FIRST_TIME  ? : " + sharedPreferences.contains(INTENT_FIRST_TIME));
//
//        Calendar firingCal = Calendar.getInstance();
//        Calendar currentCal = Calendar.getInstance();
//
//        firingCal.set(Calendar.HOUR, 2); // At the hour you wanna fire
//        firingCal.set(Calendar.MINUTE, 43); // Particular minute
//        firingCal.set(Calendar.SECOND, 0); // particular second
//
//        long intendedTime = firingCal.getTimeInMillis();
//        long currentTime = currentCal.getTimeInMillis();
//
//        if (intendedTime >= currentTime) {
//            // you can add buffer time too here to ignore some small differences in milliseconds
//            // set from today
//            System.out.println("intended time > =  current time "  );
//            System.out.println("intended time :" + new Date(intendedTime));
//            System.out.println("current time: " + new Date(currentTime));
//            alarmManager.setRepeating(AlarmManager.RTC, intendedTime, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
//        } else {
//            // set from next day
//            // you might consider using calendar.add() for adding one day to the current day
//            firingCal.add(Calendar.DAY_OF_MONTH, 1);
//            System.out.println("added a day : " + firingCal.toString());
//            intendedTime = firingCal.getTimeInMillis();
//            alarmManager.setRepeating(AlarmManager.RTC, currentTime, 1000*60, pendingIntent);
//        }
//    }


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
//        public void onReceive(Context context, Inten`t intent) {
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
              getColor(R.color.colorGrey),
                Color.GRAY,
                getColor(R.color.colorPrimaryDark),
                getColor(R.color.colorPrimaryDark)
        };
        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }
    private boolean checkAlarmUp()
    {
        boolean alarmUp = (PendingIntent.getBroadcast(this, 0,
                new Intent(this, IncrementReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        System.out.println("alarm is up  ?  : " + alarmUp);
        return alarmUp;

    }
//
//    public void scheduleAlarm() {
//        // Construct an intent that will execute the AlarmReceiver
//        Intent intent = new Intent(getApplicationContext(), IncrementReceiver.class);
//        // Create a PendingIntent to be triggered when the alarm goes off
//        final PendingIntent pIntent = PendingIntent.getBroadcast(this, REQUEST_CODE,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        // Setup periodic alarm every every half hour from this point onwards
//        long firstMillis = System.currentTimeMillis(); // alarm is set right away
//        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
//        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
//        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
//                30000, pIntent);
//
//    }

    public void scheduleAlarm() {
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


    private void updateStreak(int newStreak)
    {
        System.out.println("new streak:"  +newStreak);
//        mDatabase.child("users").child(mUser.getUid()).child("currentStreak").setValue(newStreak);
//        pushNotification("Updated your streak : " + newStreak , "Streak Update");
    }


}

