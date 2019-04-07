package com.example.myapplication.Fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.Expense;
import com.example.myapplication.Data.User;
import com.example.myapplication.Activities.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.Utils.Util;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.Calendar;
import java.util.Date;

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
    private Date selectedDate;
    private CaldroidFragment caldroidFragment;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View v = lf.inflate ( R.layout.fragment_dashboard, container, false );
        FloatingActionButton fab = v.findViewById(R.id.fab);
        Button allExpenses = v.findViewById(R.id.btn_allexpenses);
        db = Room.databaseBuilder(getActivity(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        selectedDate= new Date(System.currentTimeMillis());
        System.out.print("current date: " + selectedDate);
        firebaseAuth  = FirebaseAuth.getInstance();
        mUser  = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = db.userDao().findByName(MainActivity.firstName , MainActivity.lastName);
        totalExpense = v.findViewById(R.id.tv_totalexpense);
        retrieveExpenses();

        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));

        caldroidFragment.setArguments(args);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.calendar_container, caldroidFragment)
                .commit();
        calendarListener();

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
        Bundle args = new Bundle();
        args.putLong("DateSelected", selectedDate.getTime());
        fragment.setArguments(args);
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
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                  int exp=0;
                                  long chosenDate=0;
                                  @Override
                                  public void onDataChange(DataSnapshot dataSnapshot) {

                                      for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                          // TODO: handle the post
                                          Expense currExp = postSnapshot.getValue(Expense.class);
                                          System.out.println("snapshot: " + postSnapshot.child("amount").getValue());
//                                          exp +=Integer.parseInt( postSnapshot.child("amount").getValue().toString());
                                          exp+=currExp.amount;
                                          System.out.println("post snapshot:" +currExp);
                                          chosenDate =  currExp.currentDate;
                                          colorDate(chosenDate);
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

    private void colorDate(long millis)
    {
        ColorDrawable blue = new ColorDrawable(getResources().getColor(R.color.colorSecondary));
        caldroidFragment.setBackgroundDrawableForDate(blue, new Date(millis));
        caldroidFragment.refreshView();

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

    private void calendarListener()
    {
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                caldroidFragment.clearSelectedDates();
                caldroidFragment.setSelectedDate(date);
//                caldroidFragment.setTextColorForDate(R.color.colorAccent, date);
                caldroidFragment.refreshView();
                selectedDate =date;
            }

            @Override
            public void onChangeMonth(int month, int year) {
                String text = "month: " + month + " year: " + year;

            }

            @Override
            public void onLongClickDate(Date date, View view) {

            }

            @Override
            public void onCaldroidViewCreated() {

                caldroidFragment.setSelectedDate(selectedDate);
                caldroidFragment.refreshView();

            }

        };

        caldroidFragment.setCaldroidListener(listener);
    }
}
