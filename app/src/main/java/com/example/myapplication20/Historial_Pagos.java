package com.example.myapplication20;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Historial_Pagos extends AppCompatActivity {

    private ListView listViewGastos;
    private ArrayAdapter<String> adapter;
    private List<String> gastosList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String grupoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial_pagos);


        listViewGastos = findViewById(R.id.list_view_gastos);
        gastosList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gastosList);
        listViewGastos.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        cargarGastos();
    }

    private void cargarGastos() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("grupos").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot groupDocument : task.getResult()) {
                            String groupId = groupDocument.getId();
                            db.collection("grupos").document(groupId).collection("miembros")
                                    .document(userId).get().addOnCompleteListener(memberTask -> {
                                        if (memberTask.isSuccessful() && memberTask.getResult().exists()) {
                                            grupoId = groupId;  // Asignar el grupoId del usuario
                                            db.collection("grupos").document(groupId).collection("gastos")
                                                    .get()
                                                    .addOnCompleteListener(gastoTask -> {
                                                        if (gastoTask.isSuccessful()) {
                                                            gastosList.clear();
                                                            for (QueryDocumentSnapshot document : gastoTask.getResult()) {
                                                                String nombreUsuario = document.getString("nombreUsuario");
                                                                String fecha = document.getString("fecha");
                                                                String nombreGasto = document.getString("nombreGasto");
                                                                double monto = document.getDouble("monto");

                                                                String gastoItem = nombreUsuario + " - " + fecha + " - " + nombreGasto + " - $" + monto;
                                                                gastosList.add(gastoItem);
                                                            }
                                                            adapter.notifyDataSetChanged();
                                                        } else {
                                                            Toast.makeText(Historial_Pagos.this, "Error al obtener los gastos.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(Historial_Pagos.this, "Error al obtener los grupos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
