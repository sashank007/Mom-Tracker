package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.Expense;
import com.example.myapplication.Data.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import static com.firebase.ui.auth.ui.email.TroubleSigningInFragment.TAG;

public class DashboardFragment extends Fragment {
    String[]monthName={"January","February","March", "April", "May", "June", "July",
            "August", "September", "October", "November",
            "December"};
    AppDatabase db;
    User currentUser;
    TextView totalExpense;
    private int totalExpenseVal =0;
    private DatabaseReference mDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser mUser;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View v = lf.inflate ( R.layout.fragment_dashboard, container, false );
        FloatingActionButton fab = v.findViewById(R.id.fab);
        Button allExpenses = v.findViewById(R.id.btn_allexpenses);
        db = Room.databaseBuilder(getActivity(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        firebaseAuth  = FirebaseAuth.getInstance();
        mUser  = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = db.userDao().findByName(MainActivity.firstName , MainActivity.lastName);
        totalExpense = v.findViewById(R.id.tv_totalexpense);
        retrieveExpenses();
        allExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchFragment(new ExpensesListFragment());
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Adding expense", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
               switchFragment(new ExpenseTrackerFragment());
            }
        });
        return v;


    }

    public void switchFragment(Fragment fragment)
    {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void retrieveExpenses()
    {
        int totalExp = 0;
        Query myTopPostsQuery = mDatabase.child("expenses").child(mUser.getUid());

        // [START basic_query_value_listener]
        // My top posts by number of stars
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
                                  int exp=0;
                                  @Override
                                  public void onDataChange(DataSnapshot dataSnapshot) {

                                      for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                          // TODO: handle the post
                                          System.out.println("snapshot: " + postSnapshot.child("amount").getValue());
                                          exp +=Integer.parseInt( postSnapshot.child("amount").getValue().toString());
                                          System.out.println("post snapshot:" + postSnapshot.toString());

                                      }

                                      updateUIExpense(exp);
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

    public String getMonth( long millis)
    {
        Date date = new Date(millis);
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        String month=monthName[c.get(Calendar.MONTH)];

        return month;
    }

    public String getCurrentMonth()
    {
        Calendar c = Calendar.getInstance();

        String month=monthName[c.get(Calendar.MONTH)];

        return month;
    }
    public int getYear(long millis)

    {
        Date d = new Date(millis);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int year = c.get(Calendar.YEAR);
        System.out.println("current year: " + year);
        return year;
    }

    public int getCurrentYear() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        System.out.println("current year: " + year);
        return year;
    }
    private void updateUIExpense(int exp)
    {
        totalExpense.setText(" $" + Integer.toString(exp));
    }
}
