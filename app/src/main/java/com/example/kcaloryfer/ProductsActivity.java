package com.example.kcaloryfer;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kcaloryfer.data.AppDatabase;
import com.example.kcaloryfer.data.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

public class ProductsActivity extends AppCompatActivity {

    private AppDatabase db;
    private LinearLayout container;
    private Button addProductButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        db = AppDatabase.getInstance(this);

        container = findViewById(R.id.productsContainer);
        addProductButton = findViewById(R.id.addProductButton);

        addProductButton.setOnClickListener(v -> showAddDialog());

        findViewById(R.id.exportButton).setOnClickListener(v -> exportProducts());
        findViewById(R.id.importButton).setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Import produktów")
                    .setMessage("Import usunie wszystkie aktualne produkty i zastąpi je backupem. Kontynuować?")
                    .setPositiveButton("Tak", (d, w) -> importProducts())
                    .setNegativeButton("Anuluj", null)
                    .show();

        });
        loadProducts();
    }

    // =========================
    // LOAD PRODUCTS
    // =========================
    private void loadProducts() {

        container.removeAllViews();

        List<Product> list = db.productDao().getAll();

        for (Product p : list) {

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(10, 10, 10, 10);

            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);

            TextView main = new TextView(this);
            main.setText(
                    p.name +
                            " | kcal: " + p.kcal
            );
            main.setTextSize(14);
            main.setTypeface(null, Typeface.BOLD);

            TextView main2 = new TextView(this);
            main2.setText(
                  p.servingLabel +
                            " (" + p.servingGrams + "g)"
            );
            main2.setTextSize(12);


            TextView sub = new TextView(this);
            sub.setText("B:" + p.protein + " W:" + p.carbs + " T:" + p.fat);
            sub.setTextSize(12);

            textLayout.addView(main);
            textLayout.addView(main2);
            textLayout.addView(sub);

            Button edit = new Button(this);
            edit.setText("Edit");
            edit.setTextSize(10);

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

    // =========================
    // ADD PRODUCT
    // =========================
    private void showAddDialog() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        EditText name = new EditText(this);
        name.setHint("Nazwa");

        EditText grams = new EditText(this);
        grams.setHint("Gramy (bazowo)");
        grams.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        EditText protein = new EditText(this);
        protein.setHint("Białko");
        protein.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        EditText carbs = new EditText(this);
        carbs.setHint("Węglowodany");
        carbs.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        EditText fat = new EditText(this);
        fat.setHint("Tłuszcz");
        fat.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        EditText kcal = new EditText(this);
        kcal.setHint("Kcal");
        kcal.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        EditText servingGrams = new EditText(this);
        servingGrams.setHint("Gramatura porcji (np. 150)");
        servingGrams.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        EditText servingLabel = new EditText(this);
        servingLabel.setHint("Opis porcji (np. 1 porcja)");

        layout.addView(name);
        layout.addView(grams);
        layout.addView(protein);
        layout.addView(carbs);
        layout.addView(fat);
        layout.addView(kcal);
        layout.addView(servingGrams);
        layout.addView(servingLabel);

        new AlertDialog.Builder(this)
                .setTitle("Nowy produkt")
                .setView(layout)
                .setPositiveButton("Dodaj", (d, w) -> {

                    Product p = new Product();

                    p.name = name.getText().toString();
                    p.grams = parse(grams);
                    p.protein = parse(protein);
                    p.carbs = parse(carbs);
                    p.fat = parse(fat);
                    p.kcal = parse(kcal);

                    p.servingGrams = parse(servingGrams);
                    p.servingLabel = servingLabel.getText().toString();

                    db.productDao().insert(p);

                    loadProducts();
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    // =========================
    // EDIT PRODUCT
    // =========================
    private void showEditDialog(Product p) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        EditText name = new EditText(this);
        name.setText(p.name);

        EditText grams = new EditText(this);
        grams.setText(String.valueOf(p.grams));

        EditText protein = new EditText(this);
        protein.setText(String.valueOf(p.protein));

        EditText carbs = new EditText(this);
        carbs.setText(String.valueOf(p.carbs));

        EditText fat = new EditText(this);
        fat.setText(String.valueOf(p.fat));

        EditText kcal = new EditText(this);
        kcal.setText(String.valueOf(p.kcal));

        EditText servingGrams = new EditText(this);
        servingGrams.setText(String.valueOf(p.servingGrams));

        EditText servingLabel = new EditText(this);
        servingLabel.setText(p.servingLabel);

        layout.addView(name);
        layout.addView(grams);
        layout.addView(protein);
        layout.addView(carbs);
        layout.addView(fat);
        layout.addView(kcal);
        layout.addView(servingGrams);
        layout.addView(servingLabel);

        new AlertDialog.Builder(this)
                .setTitle("Edytuj produkt")
                .setView(layout)
                .setPositiveButton("Zapisz", (d, w) -> {

                    p.name = name.getText().toString();
                    p.grams = parse(grams);
                    p.protein = parse(protein);
                    p.carbs = parse(carbs);
                    p.fat = parse(fat);
                    p.kcal = parse(kcal);

                    p.servingGrams = parse(servingGrams);
                    p.servingLabel = servingLabel.getText().toString();

                    db.productDao().update(p);

                    loadProducts();
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    // =========================
    // EXPORT / IMPORT
    // =========================
    private void exportProducts() {

        try {

            JSONArray array = new JSONArray();

            List<Product> products = db.productDao().getAll();

            for (Product p : products) {

                JSONObject obj = new JSONObject();

                obj.put("name", p.name);
                obj.put("grams", p.grams);
                obj.put("protein", p.protein);
                obj.put("carbs", p.carbs);
                obj.put("fat", p.fat);
                obj.put("kcal", p.kcal);

                obj.put("servingGrams", p.servingGrams);
                obj.put("servingLabel", p.servingLabel);

                array.put(obj);
            }

            File file = new File(getFilesDir(), "products_backup.json");
            FileWriter writer = new FileWriter(file);
            writer.write(array.toString(2));
            writer.close();

            Toast.makeText(this,
                    "Eksport zakończony pomyślnie",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            Toast.makeText(this,
                    "Błąd eksportu: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
    private void importProducts() {

        try {

            File file = new File(getFilesDir(), "products_backup.json");

            BufferedReader br = new BufferedReader(new FileReader(file));

            StringBuilder json = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                json.append(line);
            }

            br.close();

            JSONArray array = new JSONArray(json.toString());

            db.productDao().deleteAll();

            for (int i = 0; i < array.length(); i++) {

                JSONObject obj = array.getJSONObject(i);

                Product p = new Product();

                p.name = obj.getString("name");
                p.grams = obj.getDouble("grams");
                p.protein = obj.getDouble("protein");
                p.carbs = obj.getDouble("carbs");
                p.fat = obj.getDouble("fat");
                p.kcal = obj.getDouble("kcal");

                p.servingGrams = obj.optDouble("servingGrams", 100);
                p.servingLabel = obj.optString("servingLabel", "1 porcja");

                db.productDao().insert(p);
            }

            loadProducts();

        } catch (Exception ignored) {}
    }

    private double parse(EditText e) {
        String s = e.getText().toString();
        return s.isEmpty() ? 0 : Double.parseDouble(s);
    }
}