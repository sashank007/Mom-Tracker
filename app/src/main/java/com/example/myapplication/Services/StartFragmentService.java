package com.example.myapplication.Services;


import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;

import com.example.myapplication.Activities.StartFragmentActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class StartFragmentService extends Service {

    private boolean isRunning;
    private Context context;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private FirebaseAuth firebaseAuth;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.context = this;
        this.isRunning = false;

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent i = new Intent(this.context , StartFragmentActivity.class);
        Log.d("Notification" , "inside onStartCommand StartFragmentSerivce");

        i.putExtra("gotoFragment","ExpenseTrackerFragment");
        startActivity(i);
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;

    }



}