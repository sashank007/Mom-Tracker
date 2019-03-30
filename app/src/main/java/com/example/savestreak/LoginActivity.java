package com.example.savestreak;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.savestreak.Data.AppDatabase;
import com.example.savestreak.Data.User;

import java.util.HashSet;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;


public class LoginActivity extends AppCompatActivity {


    public static String INTENT_PHONE = "INTENT_PHONE";
    public static String INTENT_EMAIL = "INTENT_EMAIL";
    public static String INTENT_WORD = "INTENT_WORD";
    public static String INTENT_SETTINGS = "INTENT_SETTINGS";
    public static String INTENT_TIME_WATCHED = "INTENT_TIME_WATCHED";
    public static String INTENT_TIME_WATCHED_VIDEO = "INTENT_TIME_WATCHED_VIDEO";
    public static String INTENT_URI = "INTENT_URI";
    public static String INTENT_SERVER_ADDRESS = "INTENT_SERVER_ADDRESS";
    public static String INTENT_PRACTICE = "INTENT_PRACTICE";
    AppDatabase db;
    EditText et_email, et_phone , firstName , lastName ;
    String email ,first_name,last_name;
    String phone;
    SharedPreferences sharedPreferences;
    long time_to_login;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        et_email = (EditText) findViewById(R.id.et_email);
        et_phone = (EditText) findViewById(R.id.et_lastname);
        firstName = (EditText)findViewById(R.id.et_firstName);
        lastName = (EditText)findViewById(R.id.et_lastname);
        login = (Button) findViewById(R.id.bt_login);
        time_to_login = System.currentTimeMillis();
        sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        checkExistingUser();
//        else
//        {  Intent intent = new Intent(this, SettingsActivity.class);
//            intent.putExtra(INTENT_EMAIL, email);
//            intent.putExtra(INTENT_PHONE, phone);
//
//            startActivity(intent);
//            this.finish();
//
//
//        }

//        if(sharedPreferences.contains(INTENT_PHONE) && sharedPreferences.contains(INTENT_EMAIL)) {
//            if(sharedPreferences.contains(INTENT_SETTINGS)) {
//                Intent intent = new Intent(this, MainActivity.class);
//                startActivity(intent);
//                this.finish();
//            }
//            else
//            {     Intent intent = new Intent(this, SettingsActivity.class);
//                startActivity(intent);
//                this.finish();
//
//            }
//
//        }
        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                System.out.println("clicked login");
                login();
            }
        });
    }

    public void checkExistingUser() {
        User u = db.userDao().findByEmail(email);

        if(sharedPreferences.contains(INTENT_PHONE) && sharedPreferences.contains(INTENT_EMAIL))
        {
            System.out.println("has phone and email");
            if(sharedPreferences.contains(INTENT_SETTINGS))
            {

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                this.finish();
            }
            else
            {

                System.out.println("does not have settings:" + sharedPreferences.contains(INTENT_SETTINGS));
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                this.finish();
            }

        }
    }

    public void login() {

//
//        if( ContextCompat.checkSelfPermission(this,
//                Manifest.permission.RECEIVE_SMS)
//                != PackageManager.PERMISSION_GRANTED ) {
//
//            // Permission is not granted
//            // Should we show an explanation?
//            Toast.makeText(this,"Without access to SMS we cannot let you login." , Toast.LENGTH_LONG).show();
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.RECEIVE_SMS)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//            } else {
//                // No explanation needed; request the permission
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.READ_SMS},
//                        101);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//        }


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        100);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        } else {
            if (et_email.getText().toString().isEmpty() || et_phone.getText().toString().isEmpty()) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("ALERT");
                alertDialog.setMessage("Please Enter Login Information!");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            } else {

                email = et_email.getText().toString().trim().toLowerCase();
                first_name = firstName.getText().toString().trim().toLowerCase();
                last_name = lastName.getText().toString().trim().toLowerCase();
                phone = et_email.getText().toString().trim();
                System.out.println("first name :" + first_name);
                System.out.println("last name :"  + last_name);

                //user new
                User u = new User(first_name, last_name, 0, 0, 24, 0, email, phone);
                db.userDao().insertAll(u);

                System.out.println("inside login info into db " + db.userDao().findByName(first_name,last_name));
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(INTENT_EMAIL, email);
                intent.putExtra(INTENT_PHONE, phone);

                if (sharedPreferences.edit().putString(INTENT_EMAIL, email).commit() &&
                        sharedPreferences.edit().putString(INTENT_PHONE, phone).commit()) {

                    time_to_login = System.currentTimeMillis() - time_to_login;

                    sharedPreferences.edit().putInt(getString(R.string.login), sharedPreferences.getInt(getString(R.string.login), 0) + 1).apply();
                    HashSet<String> hashset = (HashSet<String>) sharedPreferences.getStringSet("LOGIN_TIME", new HashSet<String>());
                    hashset.add("LOGIN_ATTEMPT_" + sharedPreferences.getInt(getString(R.string.login), 0) + "_" + phone + "_" + email + "_" + time_to_login);
                    sharedPreferences.edit().putStringSet("LOGIN_TIME", hashset).apply();
                    startActivity(intent);
                    this.finish();

                }


            }
        }
    }

}
