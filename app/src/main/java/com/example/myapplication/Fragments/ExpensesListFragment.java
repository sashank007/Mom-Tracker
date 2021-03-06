package com.example.myapplication.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.Expense;
import com.example.myapplication.Data.ExpenseAdapter;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

public class ExpensesListFragment extends Fragment {
    private AppDatabase db;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private ListView listView;

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View v = lf.inflate(R.layout.fragment_expenseslist, container, false);

        //get db
        db = Room.databaseBuilder(getActivity(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = firebaseAuth.getCurrentUser();
        listView = v.findViewById(R.id.listview_expenses);

        getExpenses();
        return v;
    }


    private void getExpenses() {

        Query myQuery = mDatabase.child("expenses").child(mUser.getUid()).orderByChild("currentDate");
        myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            List<Expense> myList = new ArrayList<>();

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Expense exp = snap.getValue(Expense.class);
                    myList.add(exp);
                }
                updateExpensesList(myList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateExpensesList(List<Expense> myList) {
        System.out.println("my list:" + myList);
        ExpenseAdapter adapter = new ExpenseAdapter(getActivity(), R.layout.listview_item, myList);
        listView.setAdapter(adapter);
        sortList(myList);
    }

    private void sortList(List<Expense> list) {
        Collections.sort(list, new Comparator<Expense>() {
            public int compare(Expense ideaVal1, Expense ideaVal2) {

                // avoiding NullPointerException in case name is null
                Long val1 = ideaVal1.getCurrentDate();
                Long val2 = ideaVal2.getCurrentDate();
                return val2.compareTo(val1);
            }
        });
    }
}
