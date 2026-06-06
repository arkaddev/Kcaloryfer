package com.example.kcaloryfer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kcaloryfer.data.AppDatabase;
import com.example.kcaloryfer.data.Meal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Button productButton;
    private Button addMealButton;
    private TextView todaySummaryText;
    private TextView weekSummaryText;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productButton = findViewById(R.id.productButton);
        addMealButton = findViewById(R.id.addMealButton);
        todaySummaryText = findViewById(R.id.todaySummaryText);
        weekSummaryText = findViewById(R.id.weekSummaryText);


        db = AppDatabase.getInstance(this);

        productButton.setOnClickListener(v ->
                startActivity(new Intent(this, ProductsActivity.class)));

        addMealButton.setOnClickListener(v ->
                startActivity(new Intent(this, MealsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodaySummary();
        loadWeekSummary();
    }

    private void loadTodaySummary() {

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(new Date());


        List<Meal> list =
                db.MealDao().getByDate(today);


        double kcal = 0;
        double protein = 0;
        double carbs = 0;
        double fat = 0;

        for (Meal p : list) {
            kcal += p.kcal;
            protein += p.protein;
            carbs += p.carbs;
            fat += p.fat;
        }

//        todaySummaryText.setText(
//                "Dziś:\n" +
//                        "kcal: " + kcal + "\n" +
//                        "B: " + protein + " g\n" +
//                        "W: " + carbs + " g\n" +
//                        "T: " + fat + " g"
//        );

        todaySummaryText.setText(
                "Dziś:\n" +
                        "kcal: " + String.format(Locale.US, "%.1f", kcal) + "\n" +
                        "B: " + String.format(Locale.US, "%.1f", protein) + " g\n" +
                        "W: " + String.format(Locale.US, "%.1f", carbs) + " g\n" +
                        "T: " + String.format(Locale.US, "%.1f", fat) + " g"
        );
    }

    private void loadWeekSummary() {

        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

        int diff;
        if (currentDay == Calendar.SUNDAY) {
            diff = -6;
        } else {
            diff = Calendar.MONDAY - currentDay;
        }

        calendar.add(Calendar.DAY_OF_MONTH, diff);

        String weekStart = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH, 6);

        String weekEnd = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(calendar.getTime());

        List<Meal> weekList =
                db.MealDao().getBetweenDates(
                        weekStart,
                        weekEnd
                );

        double kcal = 0;
        double protein = 0;
        double carbs = 0;
        double fat = 0;

        Set<String> activeDays = new HashSet<>();

        for (Meal p : weekList) {
            kcal += p.kcal;
            protein += p.protein;
            carbs += p.carbs;
            fat += p.fat;

            activeDays.add(p.date);
        }

        int daysCount = activeDays.size();

        if (daysCount > 0) {
            kcal /= daysCount;
            protein /= daysCount;
            carbs /= daysCount;
            fat /= daysCount;
        }

        weekSummaryText.setText(
                "Średnia tygodniowa (" + daysCount + " dni):\n" +
                        "kcal: " + String.format(Locale.US, "%.0f", kcal) + "\n" +
                        "B: " + String.format(Locale.US, "%.1f", protein) + " g\n" +
                        "W: " + String.format(Locale.US, "%.1f", carbs) + " g\n" +
                        "T: " + String.format(Locale.US, "%.1f", fat) + " g"
        );
    }
}