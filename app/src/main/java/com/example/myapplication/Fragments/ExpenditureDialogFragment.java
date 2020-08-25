package com.example.myapplication.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.example.myapplication.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ExpenditureDialogFragment extends DialogFragment {
    FirebaseAuth firebaseAuth;
    DatabaseReference mDatabase;
    FirebaseUser mUser;
    private static int ZERO =0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = firebaseAuth.getCurrentUser();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.Confirmation_expenditure)
                .setPositiveButton(R.string.hellya, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.nope, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        removeStreak();
                       dialog.dismiss();

                    }
                });
        return builder.create();
    }

    private void showSnackBar(final View parent, final String text) {
        Snackbar sb = Snackbar.make(parent, text, Snackbar.LENGTH_LONG);
        sb.show();
    }

    private void removeStreak()
    {
        System.out.println("streak removed");
        mDatabase.child("users").child(mUser.getUid()).child("currentStreak").setValue(ZERO);
        showSnackBar(getActivity().findViewById(R.id.fragment_container) , "Oops..wrong decision. Your streak is now 0!");
    }
}
