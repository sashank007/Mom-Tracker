package com.example.myapplication.Fragments;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.Data.Expense;
import com.example.myapplication.Data.User;
import com.example.myapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.example.myapplication.Activities.MainActivity.CurrentUserMaxSpendingAmount;
import static com.firebase.ui.auth.ui.email.TroubleSigningInFragment.TAG;

public class DashboardFragment extends Fragment {
    private String[] monthName = {"January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November",
            "December"};

    private TextView totalExpense, tv_spentThisMonth;
    private DatabaseReference mDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser mUser;
    private int maxSpendingAmount;
    private Date selectedDate;
    private CaldroidFragment caldroidFragment;
    private boolean isFirstDate = true;
    private Date firstDate = new Date(), secondDate = new Date();

    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View v = lf.inflate(R.layout.fragment_dashboard, container, false);
        FloatingActionButton fab = v.findViewById(R.id.fab);
        Button allExpenses = v.findViewById(R.id.btn_allexpenses);

        selectedDate = new Date(System.currentTimeMillis());
        System.out.print("current date: " + selectedDate);
        firebaseAuth = FirebaseAuth.getInstance();
        mUser = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        totalExpense = v.findViewById(R.id.tv_totalexpense);
        tv_spentThisMonth = v.findViewById(R.id.tv_spentthismonth);
        Calendar c = Calendar.getInstance();   // this takes current date
        c.set(Calendar.DAY_OF_MONTH, 1);
        int maxDay = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.DAY_OF_MONTH, maxDay);
        retrieveExpenses(c.getTime(), c2.getTime());
        getMaxSpendingAmount();

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
                switchFragment(new ExpenseTrackerFragment());
            }
        });

        return v;

    }

    public void switchFragment(Fragment fragment) {
        Bundle args = new Bundle();
        args.putLong("DateSelected", selectedDate.getTime());
        fragment.setArguments(args);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void retrieveExpenses(final Date dateFrom, final Date dateTo) {

        Query myTopPostsQuery = mDatabase.child("expenses").child(mUser.getUid());
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            float exp = 0;
            long chosenDate = 0;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // TODO: handle the post
                    Expense currExp = postSnapshot.getValue(Expense.class);

                    long millisFrom = dateFrom.getTime();
                    long millisTo = dateTo.getTime();
                    if (currExp.currentDate >= millisFrom && currExp.currentDate < millisTo)
                        exp += currExp.amount;
                    System.out.println("post snapshot:" + currExp);
                    chosenDate = currExp.currentDate;
                    if (currExp.amount > CurrentUserMaxSpendingAmount)
                        colorExceeds(chosenDate);
                    else
                        colorDate(chosenDate);

                }

                updateUIExpense(exp);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private void colorDate(long millis) {
        ColorDrawable blue = new ColorDrawable(getResources().getColor(R.color.colorCalendarExpense));
        caldroidFragment.setBackgroundDrawableForDate(blue, new Date(millis));
        caldroidFragment.setTextColorForDate(R.color.white, new Date(millis));
        caldroidFragment.refreshView();

    }

    private void colorExceeds(long millis) {
        ColorDrawable blue = new ColorDrawable(getResources().getColor(R.color.colorCalendarExceedsExpense));
        caldroidFragment.setBackgroundDrawableForDate(blue, new Date(millis));
        caldroidFragment.setTextColorForDate(R.color.white, new Date(millis));
        caldroidFragment.refreshView();

    }

    private void updateUIExpense(float exp) {
        totalExpense.setText(String.format(Locale.US, "$%.2f", exp));
    }

    private void calendarListener() {
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                caldroidFragment.clearSelectedDates();
                ColorDrawable blue = new ColorDrawable(getResources().getColor(R.color.selectedDate));
                caldroidFragment.setSelectedDate(date);
                caldroidFragment.refreshView();
                selectedDate = date;
            }

            @Override
            public void onChangeMonth(int month, int year) {
                String text = "month: " + month + " year: " + year;

            }

            @Override
            public void onLongClickDate(Date date, View view) {

                if (isFirstDate) {
                    caldroidFragment.setSelectedDate(selectedDate);
                    caldroidFragment.refreshView();
                    showSnackBar(getActivity().findViewById(R.id.fragment_container), "Long click any other date ahead of time..");
                    isFirstDate = false;
                    firstDate = date;
                } else {
                    System.out.println("got first date and second: " + firstDate.toString() + " " + secondDate.toString());
                    secondDate = date;
                    isFirstDate = true;
                    caldroidFragment.setSelectedDates(firstDate, secondDate);

                    caldroidFragment.refreshView();
                    fetchCalendarPeriodExpense(firstDate, secondDate);
                }

            }

            @Override
            public void onCaldroidViewCreated() {

                caldroidFragment.setSelectedDate(selectedDate);
                caldroidFragment.refreshView();

            }

        };

        caldroidFragment.setCaldroidListener(listener);
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

                                                       }

        );
    }

    public void updateMaxSpendingValue(int val) {
        this.maxSpendingAmount = val;
    }

    public void showSnackBar(final View parent, final String text) {
        Snackbar sb = Snackbar.make(parent, text, Snackbar.LENGTH_LONG);
        sb.show();
    }

    public void fetchCalendarPeriodExpense(Date firstDate, Date secondDate) {
        retrieveExpenses(firstDate, secondDate);
        tv_spentThisMonth.setText("Here is how much you spent in this period");

    }

    public static List<Date> getDaysBetweenDates(Date startdate, Date enddate) {
        List<Date> dates = new ArrayList<Date>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startdate);

        while (calendar.getTime().before(enddate)) {
            Date result = calendar.getTime();
            dates.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        return dates;
    }
}
