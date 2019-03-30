package com.example.savestreak;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.savestreak.Data.AppDatabase;
import com.example.savestreak.Data.Expense;
import com.example.savestreak.Data.User;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.joooonho.SelectableRoundedImageView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    TextView userName,maxSpending;
    private String firstName = "sashank";
    private String lastName = "tungaturthi";
    private static int RESULT_LOAD_IMAGE = 1;
    User currentUser;
    PieChart chart;
    String maxSpendingValue = "", userNameValue = "";
    AppDatabase db;
    Button profilePicButton;
    SelectableRoundedImageView profilePic;
    float bills , supplies , food , fun;
    final ArrayList<Float> expenses = new ArrayList<Float>();
    String types[] = {"bills" , "fun" , "food", "supplies"};
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View v = lf.inflate ( R.layout.fragment_profile, container, false );
        userName = v.findViewById(R.id.tv_username);
        profilePic = v.findViewById(R.id.iv_profilepic);
        profilePicButton = v.findViewById(R.id.btn_profilepic);
        maxSpending = v.findViewById(R.id.tv_maxSpending);
        chart = v.findViewById(R.id.pieChart);
        db = Room.databaseBuilder(getActivity(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        currentUser = db.userDao().findByName(firstName , lastName);
        if(currentUser!=null)
        {
            maxSpendingValue = Integer.toString(currentUser.maxSpending);
            userNameValue=currentUser.firstName;

        }
        userName.setText(userNameValue);
        maxSpending.setText("$"+maxSpendingValue);
        getAllExpenses();
        setupPieChart();
        imagePicker();



        return v;
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
