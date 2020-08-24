package com.example.myapplication.Data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Expense {

    public Expense() {
    }

    public Expense(String type, float amount, long currentDate, String subType) {
        this.type = type;
        this.amount = amount;
        this.currentDate = currentDate;
        this.subType = subType;
    }

    @PrimaryKey(autoGenerate = true)
    public int exid;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "subType")
    public String subType;


    @ColumnInfo(name = "amount")
    public float amount;

    @ColumnInfo(name = "currentDate")
    public long currentDate;

    @ColumnInfo(name = "uid")
    public int uid;

    public long getCurrentDate() {
        return this.currentDate;
    }

}
