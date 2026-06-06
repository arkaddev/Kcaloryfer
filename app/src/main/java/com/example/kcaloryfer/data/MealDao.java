package com.example.kcaloryfer.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MealDao {

    @Insert
    void insert(Meal product);

    @Query("SELECT * FROM meals ORDER BY id DESC")
    List<Meal> getAll();

    @Query("DELETE FROM meals WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM meals WHERE date = :date")
    List<Meal> getByDate(String date);

    @Query("SELECT * FROM meals WHERE date BETWEEN :startDate AND :endDate")
    List<Meal> getBetweenDates(String startDate, String endDate);

    @Query("DELETE FROM meals")
    void deleteAll();
}