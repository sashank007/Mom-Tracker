package com.example.savestreak;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.savestreak.Data.AppDatabase;
import com.example.savestreak.Data.Expense;
import com.example.savestreak.Data.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

public class DashboardFragment extends Fragment {
    String[]monthName={"January","February","March", "April", "May", "June", "July",
            "August", "September", "October", "November",
            "December"};
    AppDatabase db;
    User currentUser;
    TextView totalExpense;
    private int totalExpenseVal =0;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View v = lf.inflate ( R.layout.fragment_dashboard, container, false );
        FloatingActionButton fab = v.findViewById(R.id.fab);
        Button allExpenses = v.findViewById(R.id.btn_allexpenses);
        db = Room.databaseBuilder(getActivity(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        currentUser = db.userDao().findByName(MainActivity.firstName , MainActivity.lastName);
        totalExpense = v.findViewById(R.id.tv_totalexpense);
        totalExpenseVal=getTotalExpenditure();
        totalExpense.setText("$" + Integer.toString(totalExpenseVal));
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

    public int getTotalExpenditure()
    {
        int totalExp=0;
        List<Expense> lExpense = db.expenseDao().getExpenseById(currentUser.uid);
        String currentMonth = getCurrentMonth();
        for( Expense exp : lExpense)
        {
            if(getMonth(exp.currentDate)==getCurrentMonth() && getYear(exp.currentDate)==getCurrentYear())
            totalExp+=exp.amount;



        }
        return totalExp;
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
}
