package com.example.myapplication.Activities;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Data.Expense;
import com.example.myapplication.Receivers.Alarm;
import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.User;
import com.example.myapplication.Fragments.DashboardFragment;
import com.example.myapplication.Fragments.HomeFragment;
import com.example.myapplication.Fragments.ProfileFragment;
import com.example.myapplication.Receivers.DismissReceiver;
import com.example.myapplication.Receivers.IncrementReceiver;
import com.example.myapplication.R;
import com.example.myapplication.Receivers.NotificationsReceiver;
import com.example.myapplication.Receivers.ResponseBroadcastReceiver;
import com.example.myapplication.Receivers.SmsListener;
import com.example.myapplication.Receivers.SmsReceiver;
import com.example.myapplication.Receivers.ToastBroadcastReceiver;
import com.example.myapplication.Services.TestService;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.SyncFailedException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import static com.firebase.ui.auth.ui.email.TroubleSigningInFragment.TAG;

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
    public static int CurrentUserMaxSpendingAmount=0;
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
        //@TODO:remove
        setContentView(R.layout.activity_main);
        loadFragment(new HomeFragment());
        firebaseAuth  = FirebaseAuth.getInstance();
        mUser  = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        BottomNavigationView navigation =  findViewById(R.id.navigation);
        navigation.setBackgroundColor(getResources().getColor(R.color.white));
        myStateList=getColorStateList();
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//        navigation.setItemTextColor(myStateList);
        sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt("latestDollarAmount" , 30);
        editor.apply();
        System.out.println("added to shared preferences: " + sharedPreferences.getInt("latestDollarAmount",0));
        currentStreak = findViewById(R.id.tv_currentStreak);
        System.out.println("alarm up : ? " + checkAlarmUp());
        FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(
                        new GooglePlayDriver(this)
                );
        cancelJob(this,"UpdateStreakService");
//        createJob(dispatcher);
        dispatcher.mustSchedule(
                dispatcher.newJobBuilder()
                        .setService(TestService.class)
                        .setTag("UpdateStreakServiceJob")
                        .setLifetime(Lifetime.FOREVER)
                        .setRecurring(true)
                        .setTrigger(Trigger.executionWindow(0, 1800))
                        .build());
//        if(!checkAlarmUp())
//            scheduleAlarm();
//        streakUpdater();
        getMaxSpendingAmount();
        //job dispatcher

       SmsReceiver.bindListener(new SmsListener() {
            @Override
            public void messageReceived(String messageText) {
                int dollarAmount=0;
               System.out.println("inside sm sreceiver");

//                Toast.makeText(MainActivity.this, "Message: " + messageText, Toast.LENGTH_LONG).show();
                if (messageText.contains("debit")) {
                    String[] words = messageText.split(" ");

                    for(int i = 0 ; i<words.length;i++)
                    {
                        if(words[i].startsWith("$")) {
//                            dollarAmount = Integer.parseInt(words[i].substring(1));
//                            Intent intent = new Intent(getApplicationContext(),NotificationsReceiver.class);
//                            Bundle b = new Bundle();
//                            b.putString("amount", "20");
//                            intent.putExtras(b);
//                            editor = sharedPreferences.edit();
//                            editor.putInt("latestDollarAmount" , dollarAmount);
//                            editor.apply();
//                            Expense exp = new Expense("food",dollarAmount,System.currentTimeMillis(),"misc");
//                            updateExpenses(exp);
//                            sendBroadcast(intent);

                        }

                    }


//

                }


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

//    public void scheduleAlarm() {
//        // Construct an intent that will execute the AlarmReceiver
//        Intent intent = new Intent(getApplicationContext(), Alarm.class);
//        // Create a PendingIntent to be triggered when the alarm goes off
//        final PendingIntent pIntent = PendingIntent.getBroadcast(this, IncrementReceiver.REQUEST_CODE,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        // Setup periodic alarm every every half hour from this point onwards
//        long firstMillis = System.currentTimeMillis(); // alarm is set right away
//        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
//        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
//        alarm.setExact(AlarmManager.RTC_WAKEUP,
//                60000, pIntent);
//    }
public void scheduleAlarm()
{
    Intent toastIntent= new Intent(getApplicationContext(), ToastBroadcastReceiver.class);
    PendingIntent toastAlarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, toastIntent,PendingIntent.FLAG_UPDATE_CURRENT);
    long startTime=System.currentTimeMillis(); //alarm starts immediately
    AlarmManager backupAlarmMgr=(AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
    backupAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,startTime,60000,toastAlarmIntent); // alarm will repeat after every 15 minutes

}

    private void updateStreak(int newStreak)
{
        System.out.println("new streak:"  +newStreak);
//        mDatabase.child("users").child(mUser.getUid()).child("currentStreak").setValue(newStreak);
//        pushNotification("Updated your streak : " + newStreak , "Streak Update");
}

    public void pushNotification(String msgText , String msgTitle )
    {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_02";

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

        Intent sfIntent = new Intent(this, MainActivity.class);
        //dismiss the dialog
        Intent noIntent = new Intent(this, DismissReceiver.class);
        Intent yesIntent = new Intent(this, DismissReceiver.class);
        noIntent.putExtra("gotoFragment" , "Close");
        yesIntent.putExtra("gotoFragment"  , "ExpenseTrackerFragment");

        //Create the PendingIntent
        PendingIntent btPendingIntentNo = PendingIntent.getBroadcast(this, 1234, noIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent btPendingIntentYes = PendingIntent.getBroadcast(this, 1234, yesIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        NotificationCompat.Action actionno = new NotificationCompat.Action.Builder(R.drawable.ic_thumb_down_grey600_48dp, "No", btPendingIntentNo).build();
        NotificationCompat.Action actionyes = new NotificationCompat.Action.Builder(R.drawable.ic_thumb_up_grey600_48dp, "Yes", btPendingIntentYes).build();

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.mom_logo)
                .setTicker(getString(R.string.app_name))
                //     .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(msgTitle)
                .setContentText(msgText)
//                .addAction(R.drawable.ic_thumb_up_grey600_48dp , "No" , btPendingIntentNo)
//                .addAction(R.drawable.ic_thumb_down_grey600_48dp , "Yes" , btPendingIntentYes )
                .addAction(actionno)
                .addAction(actionyes)
                .setContentInfo("Info");

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }
    private void updateExpenses(Expense exp)
    {
        Log.d("updating expense: " ,exp.toString());
        String uniqueID = UUID.randomUUID().toString();
        mDatabase.child("expenses").child(mUser.getUid()).child(uniqueID).setValue(exp);


    }

    private static Job createJob(FirebaseJobDispatcher dispatcher)
    {
        return dispatcher.newJobBuilder()
                //persist the task across boots
                .setLifetime(Lifetime.FOREVER)
                //.setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                //call this service when the criteria are met.
                .setService(TestService.class)
                //unique id of the task
                .setTag("UPDATESTREAKJOB")
                //don't overwrite an existing job with the same tag
                .setReplaceCurrent(false)
                // We are mentioning that the job is periodic.
                .setRecurring(true)
                // Run every 30 min from now. You can modify it to your use.
                .setTrigger(Trigger.executionWindow(5, 30 ))
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                //.setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                //Run this job only when the network is available.
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
    }

    /**
     * 2018 September 27 - Thursday - 06:42 PM
     * cancel job method
     *
     * this method will cancel the job USE THIS WHEN YOU DON'T WANT TO USE THE SERVICE ANYMORE.
     **/
    private void cancelJob(Context context, String jobTag)
    {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        //Cancel all the jobs for this package
        dispatcher.cancelAll();
        // Cancel the job for this tag
        dispatcher.cancel(jobTag);
    }


    private void getMaxSpendingAmount()
    {
        Query myTopPostsQuery = mDatabase.child("users").child(mUser.getUid());

        // [START basic_query_value_listener]
        // My top posts by number of stars
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                           int exp=0;
                                                           @Override
                                                           public void onDataChange(DataSnapshot dataSnapshot) {

                                                               User user  = dataSnapshot.getValue(User.class);
                                                               exp=user.maxSpending;
                                                               System.out.print("maxSpendingValue  :" + exp );
                                                               updateMaxSpendingValue(exp);
                                                           }

                                                           @Override
                                                           public void onCancelled(DatabaseError databaseError) {
                                                               // Getting Post failed, log a message
                                                               Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                                               // ...
                                                           }

                                                       }

        );
    }
    public void updateMaxSpendingValue(int val)
    { CurrentUserMaxSpendingAmount=val;
    }
}

