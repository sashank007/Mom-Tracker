package com.example.savestreak;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class ExpenditureDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
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
        // Create the AlertDialog object and return it
        return builder.create();
    }
    public void showSnackBar(final View parent, final String text) {
        Snackbar sb = Snackbar.make(parent, text, Snackbar.LENGTH_LONG);
        sb.show();
    }
    private void removeStreak()
    {
        System.out.println("streak removed");
    }
}
