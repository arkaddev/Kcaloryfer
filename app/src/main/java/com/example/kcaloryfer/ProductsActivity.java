package com.example.kcaloryfer;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
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
        setContentView(R.layout.activity_products); // możesz zmienić na activity_products

        db = AppDatabase.getInstance(this);

        container = findViewById(R.id.productsContainer);
        addProductButton = findViewById(R.id.addProductButton);

        addProductButton.setOnClickListener(v -> showAddDialog());

        Button exportButton = findViewById(R.id.exportButton);
        Button importButton = findViewById(R.id.importButton);

        exportButton.setOnClickListener(v -> exportProducts());
        importButton.setOnClickListener(v -> importProducts());

        loadProducts();
    }


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
            main.setText(p.name + " | " + p.grams + "g | kcal: " + p.kcal);
            main.setTextSize(14);
            main.setTypeface(null, Typeface.BOLD);


            TextView sub = new TextView(this);
            sub.setText("B:" + p.protein + " W:" + p.carbs + " T:" + p.fat);
            sub.setTextSize(12);

            textLayout.addView(main);
            textLayout.addView(sub);

            Button edit = new Button(this);
            edit.setText("Edit");
            edit.setTextSize(10);


            edit.setOnClickListener(v -> showEditDialog(p));

            ImageButton delete = new ImageButton(this);
            delete.setImageResource(android.R.drawable.ic_delete);
            delete.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            delete.setPadding(5, 2, 5, 2);

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


    private void showAddDialog() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        EditText name = new EditText(this);
        name.setHint("Nazwa");

        EditText grams = new EditText(this);
        grams.setHint("Gramy");
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

        layout.addView(name);
        layout.addView(grams);
        layout.addView(protein);
        layout.addView(carbs);
        layout.addView(fat);
        layout.addView(kcal);

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

                    db.productDao().insert(p);

                    Toast.makeText(this, "Dodano", Toast.LENGTH_SHORT).show();

                    loadProducts();
                })
                .setNegativeButton("Anuluj", null)
                .show();
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
                    p.kcal = parse(kcal);

                    db.productDao().update(p);

                    loadProducts();
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }



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

                array.put(obj);
            }

            File file = new File(
                    getFilesDir(),
                    "products_backup.json"
            );

            FileWriter writer = new FileWriter(file);

            writer.write(array.toString(2));

            writer.close();

            Toast.makeText(
                    this,
                    "Zapisano:\n" + file.getAbsolutePath(),
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    private void importProducts() {

        try {

            File file = new File(
                    getFilesDir(),
                    "products_backup.json"
            );

            BufferedReader br =
                    new BufferedReader(new FileReader(file));

            StringBuilder json = new StringBuilder();

            String line;

            while ((line = br.readLine()) != null) {
                json.append(line);
            }

            br.close();

            JSONArray array =
                    new JSONArray(json.toString());

            db.productDao().deleteAll();

            for (int i = 0; i < array.length(); i++) {

                JSONObject obj =
                        array.getJSONObject(i);

                Product p = new Product();

                p.name = obj.getString("name");
                p.grams = obj.getDouble("grams");
                p.protein = obj.getDouble("protein");
                p.carbs = obj.getDouble("carbs");
                p.fat = obj.getDouble("fat");
                p.kcal = obj.getDouble("kcal");

                db.productDao().insert(p);
            }

            loadProducts();

            Toast.makeText(
                    this,
                    "Import zakończony",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    private double parse(EditText e) {
        String s = e.getText().toString();
        return s.isEmpty() ? 0 : Double.parseDouble(s);
    }
}