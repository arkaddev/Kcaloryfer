package com.example.kcaloryfer.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ConsumedProductDao {

    @Insert
    void insert(ConsumedProduct product);

    @Query("SELECT * FROM ConsumedProduct WHERE date = :date")
    List<ConsumedProduct> getByDate(String date);

    @Query("DELETE FROM ConsumedProduct WHERE id = :id")
    void deleteById(int id);
}