package com.example.savestreak.Data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {

    public User( String firstName, String lastName, int currentStreak, int highestStreak , long timeLeft, int maxSpending , String email , String phoneNumber) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.currentStreak = currentStreak;
        this.highestStreak = highestStreak;
        this.timeLeft=timeLeft;
        this.maxSpending=maxSpending;
        this.email = email;
        this.phoneNumber=phoneNumber;
    }

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    @ColumnInfo(name="currentStreak")
    public int currentStreak;

    @ColumnInfo(name="highestStreak")
    public int highestStreak;

    @ColumnInfo(name="timeLeft")
    public long timeLeft;
    @ColumnInfo(name="maxSpending")
    public int maxSpending;
    @ColumnInfo(name="phoneNumber")
    public String phoneNumber;
    @ColumnInfo(name="email")
    public String email;

}
