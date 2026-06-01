package com.example.kcaloryfer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kcaloryfer.data.AppDatabase;

public class MainActivity extends AppCompatActivity {

    private Button settingsButton;
    private Button addProductButton;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsButton = findViewById(R.id.settingsButton);
        addProductButton = findViewById(R.id.addProductButton);

        db = AppDatabase.getInstance(this);

        // ⚙️ Produkty (dodawanie / edycja)
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // 🍽 Dodaj do dnia (ConsumedProduct)
        addProductButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
            startActivity(intent);
        });
    }
}