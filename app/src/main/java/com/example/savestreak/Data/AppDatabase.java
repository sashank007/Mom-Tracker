package com.example.savestreak.Data;

import com.example.savestreak.Data.User;
import com.example.savestreak.Data.UserDao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class , Expense.class}, version = 6,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ExpenseDao expenseDao();
}