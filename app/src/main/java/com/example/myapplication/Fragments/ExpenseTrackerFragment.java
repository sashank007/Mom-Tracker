package com.example.myapplication.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.Expense;
import com.example.myapplication.Data.User;
import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import static com.firebase.ui.auth.ui.email.TroubleSigningInFragment.TAG;

public class ExpenseTrackerFragment extends Fragment {
    EditText et_expenditureAmount , et_expenditureType , et_expenditureSubType;
    AppDatabase db;
    Button done;
    int expenditureValue ;
    private int maxSpendingValue;
    TextView tvDate;
    private DatabaseReference mDatabase;
    private FirebaseAuth firebaseAuth;
    private long selectedDate;
    String expenditureTypeValue , expenditureSubTypeValue;
    User currentUser;
    Spinner dropdown;
    ArrayAdapter<CharSequence> adapter;
    private FirebaseUser mUser;
    TextInputLayout til_expenditureAmount  , til_expenditureType , til_expenditureSubType;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        firebaseAuth  = FirebaseAuth.getInstance();
        mUser  = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        selectedDate= getArguments().getLong("DateSelected");
        System.out.print("got selected date in expense tracker: " + selectedDate);
        LayoutInflater lf = getActivity().getLayoutInflater();
        View v = lf.inflate ( R.layout.fragment_expensetracker, container, false );
        dropdown = v.findViewById(R.id.spinner_expensetype);
        tvDate = v.findViewById(R.id.tv_date);
        DateFormat spf= new SimpleDateFormat("dd MMM yyyy");
        tvDate.setText("Creating an expense for " + spf.format(selectedDate));
        getType();
        getMaxSpendingAmount();
        et_expenditureAmount = v.findViewById(R.id.et_expenditureamount);
        et_expenditureSubType = v.findViewById(R.id.et_expendituresubtype);
        til_expenditureAmount = v.findViewById(R.id.til_expamount);
        til_expenditureType = v.findViewById(R.id.til_exptype);
        done = v.findViewById(R.id.btn_expendituredone);
        if(getActivity().getIntent().hasExtra("amount"))
            et_expenditureAmount.setText(getActivity().getIntent().getStringExtra("amount"));
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(submitForm()) {
                    expenditureValue = Integer.parseInt(et_expenditureAmount.getText().toString());
                    expenditureSubTypeValue = et_expenditureSubType.getText().toString();
                    System.out.println("expense: " + expenditureValue + " " + expenditureSubTypeValue  + " " + expenditureTypeValue);
//                    updateExpenses(expenditureTypeValue, expenditureValue);

                    //update expenses in firebase
                    Expense newExpense = new Expense(expenditureTypeValue , expenditureValue , selectedDate, expenditureSubTypeValue);
                    updateExpenses(newExpense);
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
        int expAmount = expenditureValue;
        System.out.print("exp amount : " + expAmount );
        System.out.print("maxSpendingValue : " + maxSpendingValue);
        if (expAmount>maxSpendingValue)
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
    private void updateExpenses(Expense exp)
    {
        String uniqueID = UUID.randomUUID().toString();
        mDatabase.child("expenses").child(mUser.getUid()).child(uniqueID).setValue(exp);


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
        if (et_expenditureAmount.getText().toString().trim().isEmpty()) {
            til_expenditureAmount.setError(getString(R.string.err_msg_amount));
            requestFocus(til_expenditureAmount);
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
                                                          updateMaxSpending(exp);
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

    private void updateMaxSpending(int exp)
    {
        this.maxSpendingValue = exp;

    }
}
