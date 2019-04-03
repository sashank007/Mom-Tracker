package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.User;

import androidx.room.Room;

import static com.example.myapplication.LoginActivity.INTENT_EMAIL;
import static com.example.myapplication.LoginActivity.INTENT_SETTINGS;

public class SettingsActivity extends Activity {
    Button done;
    AppDatabase db;
    String maxSpendingAmount;
    EditText maxSpending , firstName , lastName;
    SharedPreferences sharedPreferences;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        done = (Button)findViewById(R.id.btn_done);
        maxSpending = (EditText)findViewById(R.id.et_maxspending);
        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                createNewUser(firstName.getText().toString() , lastName.getText().toString() , Integer.parseInt(maxSpending.getText().toString()));
                if(!maxSpending.getText().toString().equalsIgnoreCase("")) {
                    updateMaxSpending();
                    startActivity(new Intent(view.getContext(), MainActivity.class));
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please fill in the max amount you would spend everyday"  , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateMaxSpending() {
        Intent intent = getIntent();
        if (sharedPreferences.contains(INTENT_EMAIL)) {
            String email = sharedPreferences.getString(INTENT_EMAIL,"");
            System.out.println("update max spending email: " + email);
            User u = db.userDao().findByEmail(email);
            maxSpendingAmount = maxSpending.getText().toString().trim();
            //update shared prefs
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(INTENT_SETTINGS,maxSpendingAmount);
            editor.commit();
            System.out.println("shared prefs:" + sharedPreferences.toString());
            db.userDao().updateMaxSpending(Integer.parseInt(maxSpendingAmount) , u.uid);
            System.out.println("created new user in db: " + db.userDao().getAll().toArray());

        }
    }
}
