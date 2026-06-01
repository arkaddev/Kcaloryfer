package com.example.kcaloryfer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kcaloryfer.data.AppDatabase;
import com.example.kcaloryfer.data.ConsumedProduct;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button settingsButton;
    private Button addProductButton;

    private TextView summaryText;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsButton = findViewById(R.id.settingsButton);
        addProductButton = findViewById(R.id.addProductButton);
        summaryText = findViewById(R.id.summaryText);

        db = AppDatabase.getInstance(this);

        settingsButton.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        addProductButton.setOnClickListener(v ->
                startActivity(new Intent(this, AddProductActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSummary();
    }

    private void loadSummary() {

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        List<ConsumedProduct> list =
                db.consumedProductDao().getByDate(today);

        double kcal = 0;
        double protein = 0;
        double carbs = 0;
        double fat = 0;

        for (ConsumedProduct p : list) {
            kcal += p.kcal;
            protein += p.protein;
            carbs += p.carbs;
            fat += p.fat;
        }

        summaryText.setText(
                "Dziś:\n" +
                        "kcal: " + kcal + "\n" +
                        "B: " + protein + " g\n" +
                        "W: " + carbs + " g\n" +
                        "T: " + fat + " g"
        );
    }
}