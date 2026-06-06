package com.example.kcaloryfer.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double grams;
    public double protein;
    public double carbs;
    public double fat;
    public double kcal;
}