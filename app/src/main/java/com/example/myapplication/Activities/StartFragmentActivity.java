package com.example.myapplication.Activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.myapplication.Fragments.ExpenseTrackerFragment;
import com.example.myapplication.R;

public class StartFragmentActivity extends FragmentActivity {

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
                loadFragment(new ExpenseTrackerFragment());
            }
        }
    }


    private  boolean loadFragment(Fragment fragment) {
        //switching fragment
        Bundle args = new Bundle();
        args.putLong("DateSelected", System.currentTimeMillis());
        fragment.setArguments(args);
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
