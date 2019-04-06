package com.example.myapplication.Utils;

import android.provider.ContactsContract;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Util {

    FirebaseAuth firebaseAuth;
    DatabaseReference mDatabase;
    FirebaseUser mUser;
    // schedule the start of the service every 10 - 30 seconds
//    public static void scheduleJob(Context context) {
//        ComponentName serviceComponent = new ComponentName(context, IncrementService.class);
//        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
//        builder.setMinimumLatency(1 * 1000); // wait at least
//        builder.setOverrideDeadline(3 * 1000); // maximum delay
//        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
//        //builder.setRequiresDeviceIdle(true); // device should be idle
//        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
//        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
//        jobScheduler.schedule(builder.build());
//    }
//
//    public static void getFirebaseInstance(DatabaseReference mDb , FirebaseAuth fbAuth  , FirebaseUser fbUser)
//    {
//
//        firebaseAuth = FirebaseAuth.getInstance();
//        mDatabase = FirebaseDatabase.getInstance().getReference();
//        mUser = firebaseAuth.getCurrentUser();
//
//    }
}