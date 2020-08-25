package com.example.myapplication.Data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ExpenseDao {
    @Query("SELECT * FROM expense")
    List<Expense> getAll();

    @Insert
    void insertAll(Expense... expenses);

    @Query("SELECT * FROM expense WHERE uid =:uid")
    List<Expense> getExpenseById(int uid);
}