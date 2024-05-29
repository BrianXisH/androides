package com.example.myapplication20;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class vista2 extends AppCompatActivity implements GastoAdapter.OnItemClickListener {

    private RecyclerView recyclerViewGastos;
    private GastoAdapter adapter;
    private List<Gasto> gastos;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String grupoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vista2);

        recyclerViewGastos = findViewById(R.id.recyclerViewGastos);
        recyclerViewGastos.setLayoutManager(new LinearLayoutManager(this));

        gastos = new ArrayList<>();
        adapter = new GastoAdapter(gastos);
        adapter.setOnItemClickListener(this);
        recyclerViewGastos.setAdapter(adapter);

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
                                                    .whereEqualTo("nombreUsuario", mAuth.getCurrentUser().getDisplayName())
                                                    .get()
                                                    .addOnCompleteListener(gastoTask -> {
                                                        if (gastoTask.isSuccessful()) {
                                                            for (QueryDocumentSnapshot document : gastoTask.getResult()) {
                                                                Gasto gasto = document.toObject(Gasto.class);
                                                                gasto.setId(document.getId());  // Asignar el ID del documento
                                                                gastos.add(gasto);
                                                            }
                                                            adapter.notifyDataSetChanged();
                                                        } else {
                                                            Toast.makeText(vista2.this, "Error al obtener los gastos.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(vista2.this, "Error al obtener los grupos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onItemClick(Gasto gasto) {
        Intent intent = new Intent(this, GastoDetallesActivity.class);
        intent.putExtra("gastoId", gasto.getId());
        intent.putExtra("grupoId", grupoId);
        intent.putExtra("nombre", gasto.getNombreGasto());
        intent.putExtra("categoria", gasto.getCategoria());
        intent.putExtra("fecha", gasto.getFecha());
        intent.putExtra("monto", String.valueOf(gasto.getMonto()));
        intent.putExtra("descripcion", gasto.getDescripcion());
        startActivity(intent);
    }
}
