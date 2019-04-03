package com.example.myapplication.Data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user")
    List<User> getAll();

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    List<User> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    User findByName(String first, String last);

    @Query("SELECT * FROM user WHERE email LIKE :email LIMIT 1")
    User findByEmail(String email);

    @Query("SELECT currentStreak FROM user WHERE email LIKE :email LIMIT 1")
    int getCurrentStreak(String email);

    @Query("SELECT timeLeft FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    long getTimeLeft(String first, String last);

    @Query("SELECT highestStreak FROM user WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
    int getHighestStreak(String first , String last);

    @Query("UPDATE user SET currentStreak= :currentStreak  WHERE uid = :uid")
    void updateCurrentStreak(int currentStreak , int uid);

    @Query("UPDATE user SET timeLeft = :timeLeft WHERE uid = :uid")
    void updateTimeLeft(long timeLeft , int uid);

    @Query("UPDATE user SET maxSpending = :maxSpending WHERE uid = :uid")
    void updateMaxSpending(int maxSpending , int uid);

    @Query("UPDATE user SET  first_name = :firstName , last_name = :lastName, maxSpending = :maxSpending WHERE uid = :uid")
    void updateSettings(String firstName , String lastName , String maxSpending,  int uid);

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);
}