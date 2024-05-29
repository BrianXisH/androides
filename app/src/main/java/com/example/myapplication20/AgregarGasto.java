package com.example.myapplication20;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgregarGasto extends AppCompatActivity {
    private Spinner categorySpinner;
    private EditText categoryInput;
    private Button addCategoryButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_gasto);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        categorySpinner = findViewById(R.id.category_spinner);
        categoryInput = findViewById(R.id.category_input);
        addCategoryButton = findViewById(R.id.add_category_button);
        db = FirebaseFirestore.getInstance();

        loadCategories();

        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
            }
        });
    }

    private void loadCategories() {
        db.collection("categorias")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> categories = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String categoryName = document.getString("nombre");
                            if (categoryName != null) {
                                categories.add(categoryName);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        categorySpinner.setAdapter(adapter);
                    } else {
                        Toast.makeText(AgregarGasto.this, "Error al cargar categorías", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addCategory() {
        String categoryName = categoryInput.getText().toString().trim();
        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese un nombre para la categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> category = new HashMap<>();
        category.put("nombre", categoryName);

        db.collection("categorias")
                .add(category)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AgregarGasto.this, "Categoría agregada con éxito", Toast.LENGTH_SHORT).show();
                    categoryInput.setText("");  // Limpia el campo de entrada después de agregar la nueva categoría
                    loadCategories();  // Actualiza el spinner después de agregar la nueva categoría
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AgregarGasto.this, "Error al agregar categoría", Toast.LENGTH_SHORT).show();
                });
    }
}
