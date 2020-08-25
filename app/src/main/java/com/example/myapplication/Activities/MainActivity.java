package com.example.myapplication.Activities;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.myapplication.Fragments.ExpenseTrackerFragment;
import com.example.myapplication.Data.User;
import com.example.myapplication.Fragments.DashboardFragment;
import com.example.myapplication.Fragments.HomeFragment;
import com.example.myapplication.Fragments.ProfileFragment;
import com.example.myapplication.R;
import com.example.myapplication.Services.NotificationIntentService;
import com.example.myapplication.Services.NotificationListeningService;
import com.example.myapplication.Services.TestService;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Lifetime;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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

    private AlertDialog enableNotificationListenerAlertDialog;

    private static final String MOM_KEEPS_TRACK_PACKAGE_NAME = "com.example.myapplication";
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    loadFragment(new ProfileFragment());
                    return true;
                case R.id.navigation_dashboard:
                    loadFragment(new DashboardFragment());
                    return true;
                case R.id.navigation_profile:
                    loadFragment(new HomeFragment());
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        loadFragment(new ProfileFragment());

        firebaseAuth = FirebaseAuth.getInstance();
        mUser = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //setting action bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Intent i = getIntent();
        if (i.hasExtra("FragmentCall")) {

            String amount = this.getIntent().getExtras().getString("amount");
            callRequiredFragment(this.getIntent().getExtras().getString("FragmentCall"), amount);
        }

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setBackgroundColor(getResources().getColor(R.color.white));

        myStateList = getColorStateList();
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt("latestDollarAmount", 30);
        editor.apply();

        //job dispatcher
        dispatcher =
                new FirebaseJobDispatcher(
                        new GooglePlayDriver(this)
                );
//        scheduleJob();
        if (!sharedPreferences.getBoolean("firstTime", false)) {
            dispatcher.cancelAll();
//            scheduleJob();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTime", true);
            editor.apply();
            Log.d("MYAPP", "First time running job");
        } else {
            Log.d("MYAPP", "Job already started");
        }

        getMaxSpendingAmount();

        //service for listening to notifications
        if (!isNotificationServiceEnabled()) {
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }

        startBackgroundService();
    }

    private void startBackgroundService() {
        Intent i = new Intent(this, NotificationListeningService.class);
        startService(i);
    }

    /**
     * We use this Broadcast Receiver to notify the Main Activity when
     * a new notification has arrived, so it can properly change the
     * notification image
     */
    public class NotificationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("Title");
            String text = intent.getStringExtra("Text");
            String dollarAmount = intent.getStringExtra("DollarAmount");

            Log.d("NOTIFICATION_RECEIVER", "GOT TITLE : " + dollarAmount + " " + text);
            Intent i = new Intent(context, NotificationIntentService.class);
            i.putExtra("amount", dollarAmount);
            context.startService(i);
        }
    }

    private void scheduleJob() {
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

    private void getMaxSpendingAmount() {
        Query myTopPostsQuery = mDatabase.child("users").child(mUser.getUid());
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
            }
        });
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
                startActivity(new Intent(this, LoginActivity.class));

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

    public void callRequiredFragment(String extra, String amount) {

        if (extra.equals("ExpenseTrackerFragment")) {
            Fragment fragment = new ExpenseTrackerFragment();
            Bundle args = new Bundle();

            args.putString("amount", amount);
            args.putString("FragmentCall", "ExpenseTrackerFragment");

            args.putLong("DateSelected", System.currentTimeMillis());
            fragment.setArguments(args);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     *
     * @return True if enabled, false otherwise.
     */
    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     *
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildNotificationServiceAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return (alertDialogBuilder.create());
    }
}


