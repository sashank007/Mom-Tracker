package com.example.savestreak;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.savestreak.Data.AppDatabase;
import com.example.savestreak.Data.Expense;
import com.example.savestreak.Data.User;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.room.Room;

public class ExpenseTrackerFragment extends Fragment {
    EditText expenditureAmount , expenditureType;
    AppDatabase db;
    Button done;
    int expenditureValue ;
    String expenditureTypeValue;
    User currentUser;
    Spinner dropdown;
    ArrayAdapter<CharSequence> adapter;
    TextInputLayout til_expenditureAmount  , til_expenditureType;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = Room.databaseBuilder(getActivity(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        currentUser = db.userDao().findByName(MainActivity.firstName , MainActivity.lastName);

        LayoutInflater lf = getActivity().getLayoutInflater();
        View v = lf.inflate ( R.layout.fragment_expensetracker, container, false );
        dropdown = v.findViewById(R.id.spinner_expensetype);
        getType();
        expenditureAmount = v.findViewById(R.id.et_expenditureamount);
        expenditureType = v.findViewById(R.id.et_expendituretype);
        til_expenditureAmount = v.findViewById(R.id.til_expamount);
        til_expenditureType = v.findViewById(R.id.til_exptype);
        done = v.findViewById(R.id.btn_expendituredone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(submitForm()) {
                    expenditureValue = Integer.parseInt(expenditureAmount.getText().toString());
                    updateExpenses(expenditureTypeValue, expenditureValue);
                    if (exceedsBudget())
                        showDialog();
                    switchFragment(new DashboardFragment());
                }
            }
        });
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                System.out.println("item selected: " + adapter.getItem(position));
                expenditureTypeValue= adapter.getItem(position).toString().toLowerCase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return v;
    }
    public void showDialog() {
        FragmentManager fm = getFragmentManager();
        ExpenditureDialogFragment expenditureDialogFragment = new ExpenditureDialogFragment();
        expenditureDialogFragment.show(fm,"fragment_dialog");
    }
    public boolean exceedsBudget()
    {
        int expAmount =expenditureValue;
        if (expAmount>currentUser.maxSpending)
        {
            return true;
        }
        return false;
    }
    public void switchFragment(Fragment fragment)
    {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    private void updateExpenses(String type, int amount)
    {


        db.expenseDao().insertAll( new Expense(type,amount,currentUser.uid,System.currentTimeMillis()));
        System.out.println("updated expenses :" + type + amount);
        List<Expense> listExpense= db.expenseDao().getAll();
        System.out.println("listExpense:" + listExpense);


    }
    private void getType()
    {

//create a list of items for the spinner.
       adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.exp_types_array, android.R.layout.simple_spinner_item);
//set the spinners adapter to the previously created one.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        dropdown.setAdapter(adapter);
    }

    private boolean submitForm()
    {
        if(!validateAmount())
            return false;

        return true;
    }

    private boolean validateAmount() {
        if (expenditureAmount.getText().toString().trim().isEmpty()) {
            til_expenditureAmount.setError(getString(R.string.err_msg_amount));
            requestFocus(expenditureAmount);
            return false;
        } else {
            til_expenditureAmount.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

}
