package com.example.myapplication20;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class GastoDetallesActivity extends AppCompatActivity {

    private TextView txtNombre, txtCategoria, txtFecha, txtMonto, txtDescripcion;
    private String gastoId;
    private String grupoId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gasto_detalles);

        txtNombre = findViewById(R.id.txtNombre);
        txtCategoria = findViewById(R.id.txtCategoria);
        txtFecha = findViewById(R.id.txtFecha);
        txtMonto = findViewById(R.id.txtMonto);
        txtDescripcion = findViewById(R.id.txtDescripcion);

        db = FirebaseFirestore.getInstance();

        // Obtener los datos del Intent
        if (getIntent() != null) {
            gastoId = getIntent().getStringExtra("gastoId");
            grupoId = getIntent().getStringExtra("grupoId");
            txtNombre.setText(getIntent().getStringExtra("nombre"));
            txtCategoria.setText(getIntent().getStringExtra("categoria"));
            txtFecha.setText(getIntent().getStringExtra("fecha"));
            txtMonto.setText(getIntent().getStringExtra("monto"));
            txtDescripcion.setText(getIntent().getStringExtra("descripcion"));
        }

        Button btnEliminar = findViewById(R.id.btnEliminar);
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(GastoDetallesActivity.this)
                        .setTitle("Eliminar gasto")
                        .setMessage("¿Estás seguro de que quieres eliminar este gasto?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                eliminarGasto();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    private void eliminarGasto() {
        if (grupoId != null && gastoId != null) {
            db.collection("grupos").document(grupoId).collection("gastos").document(gastoId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(GastoDetallesActivity.this, "Gasto eliminado con éxito", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(GastoDetallesActivity.this, "Error al eliminar el gasto", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
