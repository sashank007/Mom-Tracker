package com.example.myapplication.Activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.hololo.tutorial.library.PermissionStep;
import com.hololo.tutorial.library.Step;
import com.hololo.tutorial.library.TutorialActivity;

public class HelperActivity extends TutorialActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addFragment(new Step.Builder().setTitle("Hi Honey!")
                .setContent("My name is Mom and I am going to help you keep track of your money. But first, I'm going to need some permissions...")
                .setBackgroundColor(Color.parseColor("#FF0957")) // int background color
//                .setDrawable(R.drawable.Muriel_Bagge) // int top drawable
//                .setDrawable(R.drawable.muriel)

//                .setSummary("")
                .build());
        // Permission Step
        addFragment(new PermissionStep.Builder().setTitle("Storage permission")
                .setContent("I'm gonna need access to your storage first")
                .setBackgroundColor(Color.parseColor("#FF0957"))

                .setSummary(("continue to other permissions"))
                .setPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .build());
        addFragment(new PermissionStep.Builder().setTitle("SMS permission")
                .setContent("I'm gonna need access to your smses too. Don't worry. I promise not to share it with anyone else!")
                .setBackgroundColor(Color.parseColor("#FF0957"))
//                .setDrawable(R.drawable.ss_1)
                .setSummary(("continue and learn"))
                .setPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS,Manifest.permission.SEND_SMS})
                .build());
        addFragment(new Step.Builder().setTitle("Enable SMS Alerts")
                .setContent("To help me keep track better, I'm gonna need you to enable SMS alerts from your bank. Don't worry. This is very easy and can be done from your banking app.")
                .setBackgroundColor(Color.parseColor("#FF0957"))
//                .setDrawable(R.drawable.ss_1)
                .setSummary(("continue and learn"))
//                .setPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS,Manifest.permission.SEND_SMS})
                .build());
        addFragment(new Step.Builder().setTitle("Streak")
                .setContent("Also, one last thing. I keep track of the number of days you don't spend more than your allowance. The streak will increase at the end of the day if you are a good kid!")
                .setBackgroundColor(Color.parseColor("#FF0957"))
//                .setDrawable(R.drawable.ss_1)
                .setSummary((""))
//                .setPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS,Manifest.permission.SEND_SMS})
                .build());
    }
    @Override
    public void finishTutorial() {
        // Your implementation
        startActivity(new Intent(this,MainActivity.class));
    }
    @Override
    public void currentFragmentPosition(int position) {

    }
}