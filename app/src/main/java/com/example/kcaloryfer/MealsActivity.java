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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

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
        consumedContainer = findViewById(R.id.mealsContainer);

        Button exportMealsButton = findViewById(R.id.exportMealsButton);
        Button importMealsButton = findViewById(R.id.importMealsButton);

        exportMealsButton.setOnClickListener(v -> exportMeals());
        importMealsButton.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Import danych")
                    .setMessage("UWAGA: Import usunie wszystkie obecne posiłki i zastąpi je danymi z backupu. Czy na pewno chcesz kontynuować?")
                    .setPositiveButton("Tak, importuj", (dialog, which) -> importMeals())
                    .setNegativeButton("Anuluj", null)
                    .show();

        });

        loadProducts();
        loadMeals();

        addButton.setOnClickListener(v -> addProduct());
    }

    // =========================
    // PRODUCTS
    // =========================
    private void loadProducts() {

        productList = db.productDao().getAll();

        Collections.sort(productList, (a, b) ->
                a.name.compareToIgnoreCase(b.name)
        );

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
                                " T: " + selectedProduct.fat +
                                "\nPorcja: " + selectedProduct.servingLabel +
                                " (" + selectedProduct.servingGrams + " g)"
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // =========================
    // ADD MEAL (GRAMY / PORCJE)
    // =========================
    private void addProduct() {

        if (selectedProduct == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(selectedProduct.name);

        // INFO
        TextView info = new TextView(this);
        info.setText(
                "Porcja: " + selectedProduct.servingLabel + "\n" +
                        selectedProduct.servingGrams + " g\n" +
                        "Kcal / 100g: " + selectedProduct.kcal
        );
        info.setPadding(40, 20, 40, 20);

        // INPUT
        EditText input = new EditText(this);
        input.setHint("np. 150");
        input.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        // =========================
        // RADIO BUTTONS
        // =========================
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.HORIZONTAL);

        RadioButton rbGrams = new RadioButton(this);
        rbGrams.setText("Gramy");
        rbGrams.setId(100);
        rbGrams.setChecked(true);

        RadioButton rbPortions = new RadioButton(this);
        rbPortions.setText("Porcje");
        rbPortions.setId(200);

        group.addView(rbGrams);
        group.addView(rbPortions);

        // LAYOUT
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        layout.addView(info);
        layout.addView(group);
        layout.addView(input);

        builder.setView(layout);

        // =========================
        // SAVE
        // =========================
        builder.setPositiveButton("Dodaj", (dialog, which) -> {

            try {
                double value = Double.parseDouble(input.getText().toString());

                double grams;

                int selectedId = group.getCheckedRadioButtonId();

                if (selectedId == rbGrams.getId()) {
                    grams = value;
                } else {
                    grams = value * selectedProduct.servingGrams;
                }

                double ratio = grams / 100.0;

                String date = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd",
                        java.util.Locale.getDefault()
                ).format(new java.util.Date());

                Meal m = new Meal();
                m.name = selectedProduct.name;
                m.grams = grams;

                m.kcal = selectedProduct.kcal * ratio;
                m.protein = selectedProduct.protein * ratio;
                m.carbs = selectedProduct.carbs * ratio;
                m.fat = selectedProduct.fat * ratio;
                m.date = date;

                db.MealDao().insert(m);

                loadMeals();

            } catch (Exception e) {
                Toast.makeText(this, "Błędna wartość", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Anuluj", null);
        builder.show();
    }

    // =========================
    // SHOW MEALS
    // =========================
    private void loadMeals() {

        consumedContainer.removeAllViews();

        String today = new java.text.SimpleDateFormat(
                "yyyy-MM-dd",
                java.util.Locale.getDefault()
        ).format(new java.util.Date());

        List<Meal> list = db.MealDao().getByDate(today);

        for (Meal c : list) {

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView tv = new TextView(this);
            tv.setText(c.name + " | " + c.kcal + " kcal");
            tv.setPadding(20, 20, 20, 20);
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            ));

            ImageButton del = new ImageButton(this);
            del.setImageResource(android.R.drawable.ic_delete);
            del.setOnClickListener(v -> {
                db.MealDao().deleteById(c.id);
                loadMeals();
            });

            Button edit = new Button(this);
            edit.setText("Edit");
            edit.setOnClickListener(v -> showEditDialog(c));

            row.addView(tv);
            row.addView(edit);
            row.addView(del);

            consumedContainer.addView(row);
        }
    }

    // =========================
    // EDIT
    // =========================
    private void showEditDialog(Meal c) {

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Edytuj kcal");

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(c.kcal));

        b.setView(input);

        b.setPositiveButton("Zapisz", (d, w) -> {
            try {
                c.kcal = Double.parseDouble(input.getText().toString());
                db.MealDao().insert(c);
                loadMeals();
            } catch (Exception ignored) {}
        });

        b.setNegativeButton("Anuluj", null);
        b.show();
    }

    // =========================
    // EXPORT
    // =========================
    private void exportMeals() {

        try {
            JSONArray array = new JSONArray();
            List<Meal> meals = db.MealDao().getAll();

            for (Meal m : meals) {
                JSONObject obj = new JSONObject();
                obj.put("name", m.name);
                obj.put("grams", m.grams);
                obj.put("protein", m.protein);
                obj.put("carbs", m.carbs);
                obj.put("fat", m.fat);
                obj.put("kcal", m.kcal);
                obj.put("date", m.date);
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

    // =========================
    // IMPORT
    // =========================
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

            db.MealDao().deleteAll();

            for (int i = 0; i < array.length(); i++) {

                JSONObject obj = array.getJSONObject(i);

                Meal m = new Meal();
                m.name = obj.getString("name");
                m.grams = obj.getDouble("grams");
                m.protein = obj.getDouble("protein");
                m.carbs = obj.getDouble("carbs");
                m.fat = obj.getDouble("fat");
                m.kcal = obj.getDouble("kcal");

                if (obj.has("date")) {
                    m.date = obj.getString("date");
                }

                db.MealDao().insert(m);
            }

            loadMeals();

            Toast.makeText(this, "Import OK", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this,
                    "Błąd import: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}