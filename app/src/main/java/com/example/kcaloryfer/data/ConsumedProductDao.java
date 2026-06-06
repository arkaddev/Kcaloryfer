package com.example.kcaloryfer.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ConsumedProductDao {

    @Insert
    void insert(ConsumedProduct product);

    @Query("SELECT * FROM ConsumedProduct ORDER BY id DESC")
    List<ConsumedProduct> getAll();

    @Query("DELETE FROM ConsumedProduct WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM ConsumedProduct WHERE date = :date")
    List<ConsumedProduct> getByDate(String date);

    @Query("SELECT * FROM ConsumedProduct WHERE date BETWEEN :startDate AND :endDate")
    List<ConsumedProduct> getBetweenDates(String startDate, String endDate);

    @Query("DELETE FROM ConsumedProduct")
    void deleteAll();
}