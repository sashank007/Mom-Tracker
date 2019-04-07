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
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    TextView tv_userName,tv_maxSpending ,tv_email ;
    private String firstName = "sashank";
    private String lastName = "tungaturthi";
    private static int RESULT_LOAD_IMAGE = 1;

    private DatabaseReference mUserReference;
    User currentUser;
    PieChart chart;
    String maxSpendingValue = "", userNameValue = "";
    AppDatabase db;
    Button profilePicButton;
    private String mUserEmail;
    private FirebaseUser mUser;
    private FirebaseAuth firebaseAuth;
    SelectableRoundedImageView profilePic;
    private DatabaseReference mDatabase;
    private  String mUserId ;
    private float bills , supplies , food , fun;
    final ArrayList<Float> expenses = new ArrayList<Float>();
    String types[] = {"bills" , "fun" , "food", "supplies"};

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View v = lf.inflate ( R.layout.fragment_profile, container, false );
        tv_userName = v.findViewById(R.id.tv_username);
        tv_email = v.findViewById(R.id.tv_email);
        profilePic = v.findViewById(R.id.iv_profilepic);
        profilePicButton = v.findViewById(R.id.btn_profilepic);
        tv_maxSpending = v.findViewById(R.id.tv_maxSpending);
        chart = v.findViewById(R.id.pieChart);
        firebaseAuth= FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser  = firebaseAuth.getCurrentUser();
        db = Room.databaseBuilder(getActivity(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        retrieveExpenses();
        populateValues();
        currentUser = db.userDao().findByName(firstName , lastName);
        fetchImage();
        imagePicker();
        return v;
    }


    private void retrieveExpenses()
    {
        int totalExp = 0;
        Query myQuery = mDatabase.child("expenses").child(mUser.getUid());

        // [START basic_query_value_listener]
        // My top posts by number of stars
        myQuery.addValueEventListener(new ValueEventListener() {
                      int food = 0 , bills = 0 , supplies = 0 , fun = 0;
                      @Override
                      public void onDataChange(DataSnapshot dataSnapshot) {

                          for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                              // TODO: handle the post
                              String type = postSnapshot.child("type").getValue().toString();
                              System.out.println("snapshot: " + postSnapshot.child("amount").getValue());
                              System.out.println("snapppps:" + type);
                              if(type.equals("food") )
                                  food += Integer.parseInt(postSnapshot.child("amount").getValue().toString());
                              else if(type.equals("supplies"))
                                  supplies+=Integer.parseInt(postSnapshot.child("amount").getValue().toString());
                              else if(type.equals("bills"))
                                  bills+=Integer.parseInt(postSnapshot.child("amount").getValue().toString());
                              else if(type.equals("fun"))
                                  fun+=Integer.parseInt(postSnapshot.child("amount").getValue().toString());
                              System.out.println("post snapshot:" + postSnapshot.toString());

                          }
                          System.out.println("final values :" + food + supplies + bills + fun);
                          updatePieChart(food , supplies , bills , fun);
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

    private void populateValues() {

        Query myQuery = mDatabase.child("users").child(mUser.getUid());
        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userEmail = dataSnapshot.child("email").getValue().toString();
                String userName = dataSnapshot.child("firstName").getValue().toString();
                String userMaxSpending = dataSnapshot.child("maxSpending").getValue().toString();
                Log.d("USER VALUES : "  , userEmail + userName  + userMaxSpending);
                tv_email.setText(userEmail);
                tv_maxSpending.setText("$" + userMaxSpending );
                tv_userName.setText(userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void updatePieChart(int food , int supplies  , int bills , int fun)
    {

        this.food = food;
        this.supplies=supplies;
        this.bills = bills;
        this.fun=fun;
        expenses.clear();
        expenses.add(this.bills);
        expenses.add(this.fun);
        expenses.add(this.food);
        expenses.add(this.supplies);
        System.out.println("EXPENSES : " + expenses);
        setupPieChart();

    }
    public void imagePicker()
    {
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

    private void fetchImage()
    {
        Query myQuery = mDatabase.child("users").child(mUser.getUid());

        // [START basic_query_value_listener]
        // My top posts by number of stars
        myQuery.addValueEventListener(new ValueEventListener() {
            String picturePath="";
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                picturePath=dataSnapshot.child("profilePic").getValue().toString();
              if(!picturePath.equals(""))
                  profilePic.setImageBitmap(BitmapFactory.decodeFile(picturePath));
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

    private void setupPieChart()
    {
        List<PieEntry> pieEntries =  new ArrayList<>();
        for(int i=0 ; i<expenses.size();i++)
        {
            pieEntries.add(new PieEntry(expenses.get(i) , types[i]));
        }
        PieDataSet dataSet =  new PieDataSet(pieEntries , "Your Expenditure");

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

    private void getAllExpenses()
    {
        List<Expense> lExpense = db.expenseDao().getExpenseById(currentUser.uid);


        for( Expense exp : lExpense)
        {
            System.out.println("exp type: " + exp.type);
            if(exp.type.contains("bills"))
            {
                bills+=exp.amount;

            }
            else if(exp.type.contains("supplies"))
            {
                supplies+=exp.amount;
            }
            else if(exp.type.contains("fun"))
            {
                fun+=exp.amount;
            }
            else if(exp.type.contains("food"))
            {
                food+=exp.amount;
            }

        }
        System.out.println("amount: " + " " + bills + " " + fun +  " " +food + " " + supplies);
        expenses.add(bills);
        expenses.add(fun);
        expenses.add(food);
        expenses.add(supplies);



    }



}
