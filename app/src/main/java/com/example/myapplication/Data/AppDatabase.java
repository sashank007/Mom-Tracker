package com.example.myapplication.Data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class , Expense.class}, version = 8,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ExpenseDao expenseDao();
}