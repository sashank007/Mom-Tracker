package com.example.myapplication.Activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.myapplication.R;

public class StartFragmentActivity extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String menuFragment = getIntent().getStringExtra("gotoFragment");
       Log.d("NOTIFICATION","inside sf activity"  + menuFragment);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // If menuFragment is defined, then this activity was launched with a fragment selection
        if (menuFragment != null) {
            // Here we can decide what do to -- perhaps load other parameters from the intent extras such as IDs, etc
            if (menuFragment.equals("ExpenseTrackerFragment")) {
//                loadFragment(new ExpenseTrackerFragment());
                Intent i = new Intent(this,MainActivity.class);
                i.putExtra("FragmentCall","ExpenseTrackerFragment");
                String amount = getIntent().getExtras().getString("amount");
                i.putExtra("amount",amount);
                startActivity(i);
            }
        }
    }


//    private  boolean loadFragment(Fragment fragment) {
//        //switching fragment
//        Bundle args = new Bundle();
//        String amount = getIntent().getExtras().getString("amount");
//        args.putString("amount",amount);
//        args.putString("FragmentCall","ExpenseTrackerFragment");
//        Log.d("STARTFRAGMENTACTIVITY","received amount from dismiss receiver: " + amount);
//        args.putLong("DateSelected", System.currentTimeMillis());
//        fragment.setArguments(args);
//
////        if (fragment != null) {
////            getSupportFragmentManager()
////                    .beginTransaction()
////                    .replace(R.id.fragment_container, fragment)
////                    .commit();
////            return true;
////        }
//        Intent i = new Intent(this,MainActivity.class);
////        i.putExtras(args);
//        i.putExtra("FragmentCall" , "ExpenseTrackerFragment");
//        i.putExtra("amount",amount);
//        startActivity(i);
//        return false;
//    }
}
