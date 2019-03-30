package com.example.savestreak;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.savestreak.Data.AppDatabase;
import com.example.savestreak.Data.Expense;
import com.example.savestreak.Data.ExpenseAdapter;
import com.example.savestreak.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

public class ExpensesListFragment extends Fragment {
    AppDatabase db;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View v = lf.inflate ( R.layout.fragment_expenseslist, container, false );
        //get db
        db = Room.databaseBuilder(getActivity(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        List<Expense> listExpenses = db.expenseDao().getAll();
        ExpenseAdapter adapter = new ExpenseAdapter(getActivity(),R.layout.listview_item,listExpenses);
        ListView listView = v.findViewById(R.id.listview_expenses);
        listView.setAdapter(adapter);
        return v;
    }
}
