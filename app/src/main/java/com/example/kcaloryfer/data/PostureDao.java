package com.example.kcaloryfer.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface PostureDao {

    @Query("SELECT * FROM posture_days WHERE date = :date LIMIT 1")
    PostureDay getByDate(String date);

    @Insert
    void insert(PostureDay day);

    @Update
    void update(PostureDay day);
}