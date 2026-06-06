package com.example.kcaloryfer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;

import com.example.kcaloryfer.data.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

public class MealsActivity extends AppCompatActivity {

    private AppDatabase db;

    private Spinner productSpinner;
    private TextView selectedInfo;
    private Button addButton;

    private LinearLayout consumedContainer;

    private List<Product> productList = new ArrayList<>();
    private Product selectedProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meals);

        db = Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
                        "kcaloryfer_db"
                )
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        productSpinner = findViewById(R.id.productSpinner);
        selectedInfo = findViewById(R.id.selectedInfo);
        addButton = findViewById(R.id.addButton);
        consumedContainer = findViewById(R.id.consumedContainer);

        Button exportMealsButton = findViewById(R.id.exportConsumedButton);
        Button importMealsButton = findViewById(R.id.importConsumedButton);

        exportMealsButton.setOnClickListener(v -> exportMeals());
        importMealsButton.setOnClickListener(v -> importMeals());

        loadProducts();
        loadConsumed();

        addButton.setOnClickListener(v -> addProduct());
    }

    // -------------------------
    // PRODUCTS
    // -------------------------
    private void loadProducts() {

        productList = db.productDao().getAll();

        List<String> names = new ArrayList<>();

        for (Product p : productList) {
            names.add(p.name + " | " + p.kcal + " kcal");
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        names);

        productSpinner.setAdapter(adapter);

        productSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {

                selectedProduct = productList.get(position);

                selectedInfo.setText(
                        selectedProduct.name +
                                "\nKcal: " + selectedProduct.kcal +
                                "\nB: " + selectedProduct.protein +
                                " W: " + selectedProduct.carbs +
                                " T: " + selectedProduct.fat
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // -------------------------
    // ADD CONSUMED
    // -------------------------
    private void addProduct() {

        if (selectedProduct == null) return;

        String date = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        ConsumedProduct c = new ConsumedProduct();

        c.name = selectedProduct.name;
        c.grams = selectedProduct.grams;
        c.protein = selectedProduct.protein;
        c.carbs = selectedProduct.carbs;
        c.fat = selectedProduct.fat;
        c.kcal = selectedProduct.kcal;
        c.date = date;

        db.consumedProductDao().insert(c);

        loadConsumed();
    }

    // -------------------------
    // SHOW CONSUMED
    // -------------------------
    private void loadConsumed() {

        consumedContainer.removeAllViews();

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        List<ConsumedProduct> list =
                db.consumedProductDao().getByDate(today);

        for (ConsumedProduct c : list) {

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView tv = new TextView(this);
            tv.setText(c.name + " | " + c.kcal + " kcal");
            tv.setPadding(20, 20, 20, 20);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            // ---------------- DELETE
            ImageButton del = new ImageButton(this);
            del.setImageResource(android.R.drawable.ic_delete);

            del.setOnClickListener(v -> {
                db.consumedProductDao().deleteById(c.id);
                loadConsumed();
            });

            // ---------------- EDIT
            Button edit = new Button(this);
            edit.setText("Edit");

            edit.setOnClickListener(v -> showEditDialog(c));

            row.addView(tv);
            row.addView(edit);
            row.addView(del);

            consumedContainer.addView(row);
        }
    }

    // -------------------------
    // EDIT
    // -------------------------
    private void showEditDialog(ConsumedProduct c) {

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Edytuj kcal");

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(c.kcal));

        b.setView(input);

        b.setPositiveButton("Zapisz", (d, w) -> {

            try {
                c.kcal = Double.parseDouble(input.getText().toString());
                db.consumedProductDao().insert(c); // lub update jeśli masz

                loadConsumed();
            } catch (Exception ignored) {}
        });

        b.setNegativeButton("Anuluj", null);
        b.show();
    }

    private void exportMeals() {

        try {

            JSONArray array = new JSONArray();

            List<ConsumedProduct> meals = db.consumedProductDao().getAll();

            for (ConsumedProduct m : meals) {

                JSONObject obj = new JSONObject();

                obj.put("name", m.name);
                obj.put("grams", m.grams);
                obj.put("protein", m.protein);
                obj.put("carbs", m.carbs);
                obj.put("fat", m.fat);
                obj.put("kcal", m.kcal);
                obj.put("date", m.date); // jeśli masz datę

                array.put(obj);
            }

            File file = new File(getFilesDir(), "meals_backup.json");

            FileWriter writer = new FileWriter(file);
            writer.write(array.toString(2));
            writer.close();

            Toast.makeText(this,
                    "Eksport OK:\n" + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {

            Toast.makeText(this,
                    "Błąd export: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void importMeals() {

        try {

            File file = new File(getFilesDir(), "meals_backup.json");

            BufferedReader br =
                    new BufferedReader(new FileReader(file));

            StringBuilder json = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                json.append(line);
            }

            br.close();

            JSONArray array = new JSONArray(json.toString());

            db.consumedProductDao().deleteAll();

            for (int i = 0; i < array.length(); i++) {

                JSONObject obj = array.getJSONObject(i);

                ConsumedProduct m = new ConsumedProduct();

                m.name = obj.getString("name");
                m.grams = obj.getDouble("grams");
                m.protein = obj.getDouble("protein");
                m.carbs = obj.getDouble("carbs");
                m.fat = obj.getDouble("fat");
                m.kcal = obj.getDouble("kcal");

                if (obj.has("date")) {
                    m.date = obj.getString("date");
                }

                db.consumedProductDao().insert(m);
            }

           loadConsumed();

            Toast.makeText(this,
                    "Import OK",
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {

            Toast.makeText(this,
                    "Błąd import: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}