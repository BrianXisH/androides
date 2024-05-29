package com.example.myapplication20;

import android.os.Bundle;
import android.util.Log;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AgregarGasto extends AppCompatActivity {
    private EditText categoryInput, expenseNameInput, expenseAmountInput, expenseDescriptionInput;
    private Button addCategoryButton, addExpenseButton;
    private Spinner categorySpinner;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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

        categoryInput = findViewById(R.id.category_input);
        expenseNameInput = findViewById(R.id.expense_name_input);
        expenseAmountInput = findViewById(R.id.expense_amount_input);
        expenseDescriptionInput = findViewById(R.id.expense_description_input);
        addCategoryButton = findViewById(R.id.add_category_button);
        addExpenseButton = findViewById(R.id.add_expense_button);
        categorySpinner = findViewById(R.id.category_spinner);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadCategories();

        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
            }
        });

        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addExpense();
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

    private void addExpense() {
        String expenseName = expenseNameInput.getText().toString().trim();
        String expenseAmount = expenseAmountInput.getText().toString().trim();
        String expenseDescription = expenseDescriptionInput.getText().toString().trim();
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (expenseName.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese el nombre del gasto", Toast.LENGTH_SHORT).show();
            return;
        }

        if (expenseAmount.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese el monto del gasto", Toast.LENGTH_SHORT).show();
            return;
        }

        if (expenseDescription.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese la descripción del gasto", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "No se encontró al usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName();
        String userEmail = currentUser.getEmail();
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> expenseData = new HashMap<>();
        expenseData.put("nombreUsuario", userName);
        expenseData.put("fecha", currentDate);
        expenseData.put("nombreGasto", expenseName);
        expenseData.put("monto", Double.parseDouble(expenseAmount));
        expenseData.put("descripcion", expenseDescription);
        expenseData.put("categoria", selectedCategory);

        db.collection("grupos").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean foundGroup = false;
                        for (QueryDocumentSnapshot groupDocument : task.getResult()) {
                            String groupId = groupDocument.getId();
                            db.collection("grupos").document(groupId).collection("miembros")
                                    .document(userId).get().addOnCompleteListener(memberTask -> {
                                        if (memberTask.isSuccessful() && memberTask.getResult().exists()) {
                                            // Guardar el gasto en la subcolección gastos del grupo encontrado
                                            db.collection("grupos").document(groupId).collection("gastos")
                                                    .add(expenseData)
                                                    .addOnSuccessListener(documentReference -> Toast.makeText(AgregarGasto.this, "Gasto agregado exitosamente", Toast.LENGTH_SHORT).show())
                                                    .addOnFailureListener(e -> Toast.makeText(AgregarGasto.this, "Error al agregar el gasto", Toast.LENGTH_SHORT).show());
                                        }
                                    });

                            if (foundGroup) {
                                break;
                            }
                        }


                    } else {
                        Log.w("AgregarGasto", "Error getting groups.", task.getException());
                        Toast.makeText(AgregarGasto.this, "Error al obtener los grupos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
