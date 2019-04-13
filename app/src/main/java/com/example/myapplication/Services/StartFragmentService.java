package com.example.myapplication.Services;


import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.Activities.MainActivity;
import com.example.myapplication.Activities.StartFragmentActivity;
import com.example.myapplication.Data.Expense;
import com.example.myapplication.Data.User;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

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