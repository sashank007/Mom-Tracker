package com.example.savestreak.Data;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Expense {

    public Expense(String type, int amount , int uid,long currentDate)
    {
        this.type=type;
        this.uid=uid;
        this.amount=amount;
        this.currentDate = currentDate;
    }

    @PrimaryKey(autoGenerate = true)
    public int exid;

    @ColumnInfo(name="type")
    public String type;

    @ColumnInfo(name="amount")
    public int amount;

    @ColumnInfo(name="currentDate")
    public long currentDate;

    @ColumnInfo(name="uid")
    public int uid;



}
