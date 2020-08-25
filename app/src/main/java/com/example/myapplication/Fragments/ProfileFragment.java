package com.example.myapplication.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.firebase.ui.auth.ui.email.TroubleSigningInFragment;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import static android.app.Activity.RESULT_OK;
import static com.example.myapplication.Activities.MainActivity.CurrentUserMaxSpendingAmount;
import static com.firebase.ui.auth.ui.email.TroubleSigningInFragment.TAG;

public class ProfileFragment extends Fragment {
    private TextView tv_userName, tv_maxSpending, tv_email, tv_summary, tv_totalExpense;
    private String firstName = "sashank";
    private String lastName = "tungaturthi";
    private static int RESULT_LOAD_IMAGE = 1;

    private DatabaseReference mUserReference;
    private User currentUser;
    private PieChart chart;
    private String maxSpendingValue = "", userNameValue = "";
    private AppDatabase db;
    private Button profilePicButton;
    private String mUserEmail;
    private FirebaseUser mUser;
    private FirebaseAuth firebaseAuth;
    private SelectableRoundedImageView profilePic;
    private DatabaseReference mDatabase;
    private String mUserId;
    private float bills, supplies, food, fun, shopping;
    private final ArrayList<Float> expenses = new ArrayList<Float>();
    private String types[] = {"bills", "fun", "food", "supplies", "shopping"};

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View v = lf.inflate(R.layout.fragment_profile, container, false);
        tv_userName = v.findViewById(R.id.tv_username);
        tv_email = v.findViewById(R.id.tv_email);
        profilePic = v.findViewById(R.id.iv_profilepic);
        profilePicButton = v.findViewById(R.id.btn_profilepic);
        tv_maxSpending = v.findViewById(R.id.tv_maxSpending);
        tv_summary = v.findViewById(R.id.tv_summary);
        tv_totalExpense = v.findViewById(R.id.tv_totalexpense);
        chart = v.findViewById(R.id.pieChart);
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = firebaseAuth.getCurrentUser();
        db = Room.databaseBuilder(getActivity(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        retrieveExpenses();
        populateValues();
        currentUser = db.userDao().findByName(firstName, lastName);
        fetchImage();
        imagePicker();
        getThisMonthExpenses();

        return v;
    }

    private void getThisMonthExpenses() {
        Calendar c = Calendar.getInstance();   // this takes current date
        c.set(Calendar.DAY_OF_MONTH, 1);
        int maxDay = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.DAY_OF_MONTH, maxDay);
        retrieveExpenses(c.getTime(), c2.getTime());
    }


    private void showItems() {
//        tv_email.setVisibility(View.VISIBLE);
//        tv_maxSpending.setVisibility(View.VISIBLE);
//        profilePic.setVisibility(View.VISIBLE);
//        profilePicButton.setVisibility(View.VISIBLE);
        chart.setVisibility(View.VISIBLE);
        tv_userName.setVisibility(View.VISIBLE);
        tv_summary.setVisibility((View.VISIBLE));
        tv_totalExpense.setVisibility(View.VISIBLE);
    }

    private void retrieveExpenses() {
        int totalExp = 0;
        Query myQuery = mDatabase.child("expenses").child(mUser.getUid());

        // [START basic_query_value_listener]
        // My top posts by number of stars
        myQuery.addValueEventListener(new ValueEventListener() {
                                          float food = 0, bills = 0, supplies = 0, fun = 0, shopping = 0;

                                          @Override
                                          public void onDataChange(DataSnapshot dataSnapshot) {

                                              for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                  // TODO: handle the post
                                                  String type = postSnapshot.child("type").getValue().toString();
                                                  System.out.println("snapshot: " + postSnapshot.child("amount").getValue());
                                                  System.out.println("snapppps:" + type);
                                                  if (postSnapshot.child("amount").getValue() != null) {
                                                      if (type.equals("food"))
                                                          food += Float.parseFloat(postSnapshot.child("amount").getValue().toString());
                                                      else if (type.equals("supplies"))
                                                          supplies += Float.parseFloat(postSnapshot.child("amount").getValue().toString());
                                                      else if (type.equals("bills"))
                                                          bills += Float.parseFloat(postSnapshot.child("amount").getValue().toString());
                                                      else if (type.equals("fun"))
                                                          fun += Float.parseFloat(postSnapshot.child("amount").getValue().toString());
                                                      else if (type.equals("shopping"))
                                                          shopping += Float.parseFloat(postSnapshot.child("amount").getValue().toString());

                                                  }
                                              }
                                              updatePieChart(food, supplies, bills, fun, shopping);
                                          }

                                          @Override
                                          public void onCancelled(DatabaseError databaseError) {
                                              // Getting Post failed, log a message
                                              Log.w(TroubleSigningInFragment.TAG, "loadPost:onCancelled", databaseError.toException());
                                              // ...
                                          }

                                      }

        );
    }


    private void retrieveExpenses(final Date dateFrom, final Date dateTo) {
        int totalExp = 0;
        Query myTopPostsQuery = mDatabase.child("expenses").child(mUser.getUid());

        // [START basic_query_value_listener]
        // My top posts by number of stars
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                           int exp = 0;
                                                           long chosenDate = 0;

                                                           @Override
                                                           public void onDataChange(DataSnapshot dataSnapshot) {

                                                               for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                                   // TODO: handle the post
                                                                   Expense currExp = postSnapshot.getValue(Expense.class);
                                                                   System.out.println("snapshot: " + postSnapshot.child("amount").getValue());
//                                          exp +=Integer.parseInt( postSnapshot.child("amount").getValue().toString());
                                                                   long millisFrom = dateFrom.getTime();
                                                                   long millisTo = dateTo.getTime();
                                                                   if (currExp.currentDate >= millisFrom && currExp.currentDate < millisTo)
                                                                       exp += currExp.amount;
                                                                   System.out.println("post snapshot:" + currExp);
                                                                   chosenDate = currExp.currentDate;
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

    private void updateUIExpense(int exp) {
        tv_totalExpense.setText(" $" + Integer.toString(exp));
    }

    private void populateValues() {

        Query myQuery = mDatabase.child("users").child(mUser.getUid());
        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userEmail = dataSnapshot.child("email").getValue().toString();
                String userName = dataSnapshot.child("firstName").getValue().toString();
                String userMaxSpending = dataSnapshot.child("maxSpending").getValue().toString();
                Log.d("USER VALUES : ", userEmail + userName + userMaxSpending);
                tv_email.setText(userEmail);
                tv_maxSpending.setText("$" + userMaxSpending);
                tv_userName.setText(userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updatePieChart(float food, float supplies, float bills, float fun, float shopping) {

        this.food = food;
        this.supplies = supplies;
        this.bills = bills;
        this.fun = fun;
        this.shopping = shopping;
        expenses.clear();
        expenses.add(this.bills);
        expenses.add(this.fun);
        expenses.add(this.food);
        expenses.add(this.supplies);
        expenses.add(this.shopping);

        setupPieChart();

    }

    public void imagePicker() {
        profilePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);

            }
        });
    }

    private void fetchImage() {
        Query myQuery = mDatabase.child("users").child(mUser.getUid());

        // [START basic_query_value_listener]
        // My top posts by number of stars
        myQuery.addValueEventListener(new ValueEventListener() {
            String picturePath = "";

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                picturePath = dataSnapshot.child("profilePic").getValue().toString();
                if (!picturePath.equals(""))
                    profilePic.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                showItems();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            //update in db
            mDatabase.child("users").child(mUser.getUid()).child("profilePic").setValue(picturePath);
            cursor.close();
            profilePic.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }
    }

    private void setupPieChart() {
        List<PieEntry> pieEntries = new ArrayList<>();
        for (int i = 0; i < expenses.size(); i++) {
            pieEntries.add(new PieEntry(expenses.get(i), types[i]));
        }
        PieDataSet dataSet = new PieDataSet(pieEntries, "Your Expenditure");

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        dataSet.setColor(Color.GRAY);
        ArrayList<Integer> colors = new ArrayList<>();

//        for (int c : ColorTemplate.VORDIPLOM_COLORS)
//            colors.add(c);
//
//        for (int c : ColorTemplate.JOYFUL_COLORS)
//            colors.add(c);
//
//        for (int c : ColorTemplate.COLORFUL_COLORS)
//            colors.add(c);
//
        for (int c : ColorTemplate.MATERIAL_COLORS)
            colors.add(c);

//        for (int c : ColorTemplate.PASTEL_COLORS)
//            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(chart));
        data.setValueTextSize(9f);
        data.setValueTextColor(Color.WHITE);

        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);

        chart.setDragDecelerationFrictionCoef(0.95f);


        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);

        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);

        chart.setHoleRadius(50f);
        chart.setTransparentCircleRadius(50f);

        chart.setDrawCenterText(true);

        chart.setRotationAngle(0);
        // enable rotation of the chart by touch
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        chart.setEntryLabelTextSize(9f);

        chart.setData(data);
        chart.invalidate();
    }

    private void getAllExpenses() {
        List<Expense> lExpense = db.expenseDao().getExpenseById(currentUser.uid);


        for (Expense exp : lExpense) {
            System.out.println("exp type: " + exp.type);
            if (exp.type.contains("bills")) {
                bills += exp.amount;

            } else if (exp.type.contains("supplies")) {
                supplies += exp.amount;
            } else if (exp.type.contains("fun")) {
                fun += exp.amount;
            } else if (exp.type.contains("food")) {
                food += exp.amount;
            }

        }
        System.out.println("amount: " + " " + bills + " " + fun + " " + food + " " + supplies);
        expenses.add(bills);
        expenses.add(fun);
        expenses.add(food);
        expenses.add(supplies);


    }


}
