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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Data.Expense;
import com.example.myapplication.Fragments.ExpenseTrackerFragment;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import static com.firebase.ui.auth.ui.email.TroubleSigningInFragment.TAG;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "Debug";
    public static String MY_FRAGMENT = "MY_FRAGMENT";

    ColorStateList myStateList;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    public static int CurrentUserMaxSpendingAmount = 0;
    private FirebaseAuth firebaseAuth;
    private FirebaseJobDispatcher dispatcher;

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
        firebaseAuth = FirebaseAuth.getInstance();
        mUser = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //setting action bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        Intent i = getIntent();
        Log.d("MAINACTIVITY","GETTING INTENT");
        if(i.hasExtra("FragmentCall"))
        {

                    Log.d("MAINACTIVITY","inside hasExtra");
            String amount = this.getIntent().getExtras().getString("amount");
            callRequiredFragment(this.getIntent().getExtras().getString("FragmentCall"),amount);
        }

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setBackgroundColor(getResources().getColor(R.color.white));

        myStateList = getColorStateList();
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//        navigation.setItemTextColor(myStateList);
        sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt("latestDollarAmount", 30);
        editor.apply();

//        cancelJob(this,"UpdateStreakServiceJob");
        //job dispatcher
        dispatcher =
                new FirebaseJobDispatcher(
                        new GooglePlayDriver(this)
                );
        scheduleJob();
        if(!sharedPreferences.getBoolean("firstTime", false)) {
            dispatcher.cancelAll();
            scheduleJob();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
        Log.d("MYAPP","First time running job");
       }
        else
        {
            Log.d("MYAPP","Job already started");
}

        getMaxSpendingAmount();

    }


    private void scheduleJob()
    {
        dispatcher.mustSchedule(
                dispatcher.newJobBuilder()
                        .setService(TestService.class)
                        .setTag("UpdateStreakServiceJob")
                        .setLifetime(Lifetime.FOREVER)
                        .setRecurring(true)
                        .setTrigger(Trigger.executionWindow(60, 65))
                        .build());
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


    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, "MY_FRAGMENT")
                    .commit();
            return true;
        }
        return false;
    }

    private ColorStateList getColorStateList() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{android.R.attr.state_pressed},
                new int[]{-android.R.attr.state_pressed} // pressed

        };

        int[] colors = new int[]{
                getColor(R.color.colorGrey),
                Color.GRAY,
                getColor(R.color.colorPrimaryDark),
                getColor(R.color.colorPrimaryDark)
        };
        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }


    public void pushNotification(String msgText, String msgTitle) {
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
        noIntent.putExtra("gotoFragment", "Close");
        yesIntent.putExtra("gotoFragment", "ExpenseTrackerFragment");

        //Create the PendingIntent
        PendingIntent btPendingIntentNo = PendingIntent.getBroadcast(this, 1234, noIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent btPendingIntentYes = PendingIntent.getBroadcast(this, 1234, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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

    private void updateExpenses(Expense exp) {
        Log.d("updating expense: ", exp.toString());
        String uniqueID = UUID.randomUUID().toString();
        mDatabase.child("expenses").child(mUser.getUid()).child(uniqueID).setValue(exp);


    }

    private static Job createJob(FirebaseJobDispatcher dispatcher) {
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
                .setTrigger(Trigger.executionWindow(5, 30))
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                //.setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                //Run this job only when the network is available.
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
    }


    private void cancelJob(Context context, String jobTag) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        //Cancel all the jobs for this package
        dispatcher.cancelAll();
        // Cancel the job for this tag
        dispatcher.cancel(jobTag);
    }


    private void getMaxSpendingAmount() {
        Query myTopPostsQuery = mDatabase.child("users").child(mUser.getUid());

        // [START basic_query_value_listener]
        // My top posts by number of stars
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                           int exp = 0;

                                                           @Override
                                                           public void onDataChange(DataSnapshot dataSnapshot) {

                                                               User user = dataSnapshot.getValue(User.class);
                                                               exp = user.maxSpending;
                                                               System.out.print("maxSpendingValue  :" + exp);
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

    public void updateMaxSpendingValue(int val) {
        CurrentUserMaxSpendingAmount = val;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // User chose the "Settings" item, show the app settings UI...
                refreshView();
                return true;
            case R.id.action_logout:
                firebaseAuth.signOut();
                startActivity(new Intent(this,LoginActivity.class));

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void refreshView() {

        Fragment frg = null;
        frg = getSupportFragmentManager().findFragmentByTag(MY_FRAGMENT);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commit();

    }

    public void callRequiredFragment(String extra , String amount)
    {
        Log.d("MAINACTIVITY","sindie call required fragment");
        if(extra.equals("ExpenseTrackerFragment"))
        {
            Fragment fragment = new ExpenseTrackerFragment();
            Bundle args = new Bundle();

            args.putString("amount",amount);
            args.putString("FragmentCall","ExpenseTrackerFragment");
            Log.d("STARTFRAGMENTACTIVITY","received amount from start fragment activity"  + amount);
            args.putLong("DateSelected", System.currentTimeMillis());
            fragment.setArguments(args);

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

        }
        }
    }
}


