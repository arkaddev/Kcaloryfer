package com.example.kcaloryfer;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kcaloryfer.data.AppDatabase;
import com.example.kcaloryfer.data.ConsumedProduct;
import com.example.kcaloryfer.data.Product;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddProductActivity extends AppCompatActivity {

    private AppDatabase db;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        db = AppDatabase.getInstance(this);
        container = findViewById(R.id.container);

        loadProducts();
    }

    private void loadProducts() {

        List<Product> products = db.productDao().getAll();

        container.removeAllViews();

        for (Product p : products) {

            TextView tv = new TextView(this);

            tv.setText(p.name + " | " + p.kcal + " kcal");
            tv.setTextSize(18);
            tv.setPadding(20, 20, 20, 20);

            tv.setOnClickListener(v -> {

                ConsumedProduct c = new ConsumedProduct();

                c.name = p.name;
                c.grams = p.grams;
                c.protein = p.protein;
                c.carbs = p.carbs;
                c.fat = p.fat;
                c.kcal = p.kcal;

                c.date = new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(new Date());

                db.consumedProductDao().insert(c);

                Toast.makeText(this,
                        "Dodano: " + p.name,
                        Toast.LENGTH_SHORT).show();
            });

            container.addView(tv);
        }
    }
}