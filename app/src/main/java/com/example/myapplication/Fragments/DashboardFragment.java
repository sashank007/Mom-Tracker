package com.example.myapplication.Fragments;

import android.graphics.drawable.ColorDrawable;
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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.example.myapplication.Activities.MainActivity.CurrentUserMaxSpendingAmount;
import static com.firebase.ui.auth.ui.email.TroubleSigningInFragment.TAG;

public class DashboardFragment extends Fragment {
    String[]monthName={"January","February","March", "April", "May", "June", "July",
            "August", "September", "October", "November",
            "December"};
    AppDatabase db;
    User currentUser;
    TextView totalExpense , tv_spentThisMonth;
    private int totalExpenseVal =0;
    private DatabaseReference mDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser mUser;
    private Date selectedDate;
    private CaldroidFragment caldroidFragment;
    private int maxSpendingAmount;
    private boolean isFirstDate=true;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Date firstDate = new Date() , secondDate = new Date();
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View v = lf.inflate ( R.layout.fragment_dashboard, container, false );
        FloatingActionButton fab = v.findViewById(R.id.fab);
        Button allExpenses = v.findViewById(R.id.btn_allexpenses);

        selectedDate= new Date(System.currentTimeMillis());
        System.out.print("current date: " + selectedDate);
        firebaseAuth  = FirebaseAuth.getInstance();
        mUser  = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        totalExpense = v.findViewById(R.id.tv_totalexpense);
        tv_spentThisMonth = v.findViewById(R.id.tv_spentthismonth);
        Calendar c = Calendar.getInstance();   // this takes current date
        c.set(Calendar.DAY_OF_MONTH, 1);
        int maxDay = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar c2= Calendar.getInstance();
        c2.set(Calendar.DAY_OF_MONTH,maxDay);
        retrieveExpenses(c.getTime() , c2.getTime());
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

    private void retrieveExpenses(final Date dateFrom , final Date dateTo)
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
                                          long millisFrom = dateFrom.getTime();
                                          long millisTo = dateTo.getTime();
                                          if(currExp.currentDate>=millisFrom &&currExp.currentDate<millisTo)
                                            exp+=currExp.amount;
                                          System.out.println("post snapshot:" +currExp);
                                          chosenDate =  currExp.currentDate;
                                          if(currExp.amount>CurrentUserMaxSpendingAmount)
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
                                      // ...
                                  }

                              }

        );
    }

    private void colorDate(long millis)
    {
        ColorDrawable blue = new ColorDrawable(getResources().getColor(R.color.colorCalendarExpense));
        caldroidFragment.setBackgroundDrawableForDate(blue, new Date(millis));
        caldroidFragment.setTextColorForDate(R.color.white,new Date(millis));
        caldroidFragment.refreshView();

    }

    private void colorExceeds(long millis)
    {
        ColorDrawable blue = new ColorDrawable(getResources().getColor(R.color.colorCalendarExceedsExpense));
        caldroidFragment.setBackgroundDrawableForDate(blue, new Date(millis));
        caldroidFragment.setTextColorForDate(R.color.white,new Date(millis));
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
                ColorDrawable blue = new ColorDrawable(getResources().getColor(R.color.selectedDate));
                caldroidFragment.setSelectedDate(date);
//                caldroidFragment.setBackgroundDrawableForDate(blue,date);
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

              if(isFirstDate) {
                  caldroidFragment.setSelectedDate(selectedDate);
                  caldroidFragment.refreshView();
                  showSnackBar(getActivity().findViewById(R.id.fragment_container), "Long click any other date ahead of time..");
                  isFirstDate=false;
                  firstDate = date;
              }
              else
              {
                  System.out.println("got first date and second: " + firstDate.toString() + " " +  secondDate.toString());
                  secondDate=date;
                  isFirstDate=true;
//                  HashMap<Date, Drawable> colorMap = new HashMap<>();
//                  ColorDrawable blue = new ColorDrawable(getResources().getColor(R.color.selectedDate));
//                  List<Date> datesList = getDaysBetweenDates(firstDate,secondDate);
//                  for(int i = 0 ; i<datesList.size();i++)
//                  {
//                      colorMap.put(datesList.get(i),blue);
//
//                  }

//                  caldroidFragment.setBackgroundDrawableForDates(colorMap);
                  caldroidFragment.setSelectedDates(firstDate,secondDate);

                  caldroidFragment.refreshView();
                  fetchCalendarPeriodExpense(firstDate,secondDate);
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

    private void getMaxSpendingAmount()
    {
        Query myTopPostsQuery = mDatabase.child("users").child(mUser.getUid());

        // [START basic_query_value_listener]
        // My top posts by number of stars
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                           int exp=0;
                                                           @Override
                                                           public void onDataChange(DataSnapshot dataSnapshot) {

                                                               User user  = dataSnapshot.getValue(User.class);
                                                               exp=user.maxSpending;
                                                               System.out.print("maxSpendingValue  :" + exp );
                                                              updateMaxSpendingValue(exp);
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
    public void updateMaxSpendingValue(int val)
    {
        this.maxSpendingAmount  = val;
    }

    public void showSnackBar(final View parent, final String text) {
        Snackbar sb = Snackbar.make(parent, text, Snackbar.LENGTH_LONG);
        sb.show();
    }
    public void fetchCalendarPeriodExpense(Date firstDate , Date secondDate)
    {
        retrieveExpenses(firstDate,secondDate);
        tv_spentThisMonth.setText("Here is how much you spent in this period");

    }
    public static List<Date> getDaysBetweenDates(Date startdate, Date enddate)
    {
        List<Date> dates = new ArrayList<Date>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startdate);

        while (calendar.getTime().before(enddate))
        {
            Date result = calendar.getTime();
            dates.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        return dates;
    }
}
