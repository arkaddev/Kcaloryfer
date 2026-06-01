package com.example.kcaloryfer;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kcaloryfer.data.AppDatabase;
import com.example.kcaloryfer.data.Product;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private AppDatabase db;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = AppDatabase.getInstance(this);

        container = findViewById(R.id.productsContainer);

        EditText name = findViewById(R.id.nameEdit);
        EditText grams = findViewById(R.id.gramsEdit);
        EditText protein = findViewById(R.id.proteinEdit);
        EditText carbs = findViewById(R.id.carbsEdit);
        EditText fat = findViewById(R.id.fatEdit);
        EditText kcal = findViewById(R.id.kcalEdit);

        Button save = findViewById(R.id.saveButton);

        save.setOnClickListener(v -> {

            Product p = new Product();
            p.name = name.getText().toString();
            p.grams = parse(grams);
            p.protein = parse(protein);
            p.carbs = parse(carbs);
            p.fat = parse(fat);
            p.kcal = parse(kcal);

            db.productDao().insert(p);

            Toast.makeText(this, "Dodano", Toast.LENGTH_SHORT).show();

            loadProducts();
        });

        loadProducts();
    }

    private void loadProducts() {

        container.removeAllViews();

        List<Product> list = db.productDao().getAll();

        for (Product p : list) {

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(20, 20, 20, 20);

            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);

            TextView main = new TextView(this);
            main.setText(p.name + " | " + p.grams + "g | kcal: " + p.kcal);
            main.setTextSize(18);

            TextView sub = new TextView(this);
            sub.setText("B:" + p.protein + " W:" + p.carbs + " T:" + p.fat);

            textLayout.addView(main);
            textLayout.addView(sub);

            Button edit = new Button(this);
            edit.setText("Edit");
            edit.setOnClickListener(v -> showEditDialog(p));

            ImageButton delete = new ImageButton(this);
            delete.setImageResource(android.R.drawable.ic_delete);
            delete.setBackgroundColor(android.graphics.Color.TRANSPARENT);

            delete.setOnClickListener(v ->
                    new AlertDialog.Builder(this)
                            .setMessage("Usunąć produkt?")
                            .setPositiveButton("Tak", (d, w) -> {
                                db.productDao().deleteById(p.id);
                                loadProducts();
                            })
                            .setNegativeButton("Nie", null)
                            .show()
            );

            row.addView(textLayout);
            row.addView(edit);
            row.addView(delete);

            container.addView(row);
        }
    }

    private void showEditDialog(Product p) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        EditText name = new EditText(this);
        name.setText(p.name);

        EditText kcal = new EditText(this);
        kcal.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        kcal.setText(String.valueOf(p.kcal));

        layout.addView(name);
        layout.addView(kcal);

        new AlertDialog.Builder(this)
                .setTitle("Edytuj produkt")
                .setView(layout)
                .setPositiveButton("Zapisz", (d, w) -> {

                    p.name = name.getText().toString();
                    p.kcal = Double.parseDouble(kcal.getText().toString());

                    db.productDao().update(p);

                    loadProducts();
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private double parse(EditText e) {
        String s = e.getText().toString();
        return s.isEmpty() ? 0 : Double.parseDouble(s);
    }
}