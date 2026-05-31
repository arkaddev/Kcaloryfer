package com.example.kcaloryfer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kcaloryfer.data.AppDatabase;
import com.example.kcaloryfer.data.PostureDay;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button addPostureButton;
    private TextView postureCounterText;

    private AppDatabase db;

    private static final long COOLDOWN = 5 * 60 * 1000; // 5 minut

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addPostureButton = findViewById(R.id.addPostureButton);
        postureCounterText = findViewById(R.id.postureCounterText);

        db = AppDatabase.getInstance(this);

        loadToday();

        addPostureButton.setOnClickListener(v -> {

            long now = System.currentTimeMillis();
            String today = getToday();

            PostureDay day = db.postureDao().getByDate(today);

            if (day == null) {
                day = new PostureDay(today, 1, now);
                db.postureDao().insert(day);

                postureCounterText.setText("Dzisiaj: 1");
                Toast.makeText(this, "Dodano", Toast.LENGTH_SHORT).show();
                return;
            }

            // cooldown
            if (now - day.lastAddedTime < COOLDOWN) {

                long remaining = (COOLDOWN - (now - day.lastAddedTime)) / 1000;

                Toast.makeText(this,
                        "Czekaj " + remaining + " sekund",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            day.count++;
            day.lastAddedTime = now;

            db.postureDao().update(day);

            postureCounterText.setText("Dzisiaj: " + day.count);

            Toast.makeText(this, "Dodano", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadToday() {
        String today = getToday();

        PostureDay day = db.postureDao().getByDate(today);

        int count = (day == null) ? 0 : day.count;

        postureCounterText.setText("Dzisiaj: " + count);
    }

    private String getToday() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
    }
}