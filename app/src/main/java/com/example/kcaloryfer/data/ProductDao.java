package com.example.kcaloryfer.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kcaloryfer.data.Product;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    void insert(Product product);

    @Query("SELECT * FROM Product")
    List<Product> getAll();

    @Query("DELETE FROM Product WHERE id = :id")
    void deleteById(int id);

    @Update
    void update(Product product);

    @Query("DELETE FROM Product")
    void deleteAll();
}