package com.example.kcaloryfer.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ConsumedProduct {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public double grams;
    public double protein;
    public double carbs;
    public double fat;
    public double kcal;

    public String date;
}