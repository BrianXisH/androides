package com.example.myapplication20;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private TextView saldoActualTextView;
    private TextView gastosTotalesTextView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration gastosListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        saldoActualTextView = findViewById(R.id.saldoActualTextView);
        gastosTotalesTextView = findViewById(R.id.gastosTotales);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        calcularSaldoActual();
        calcularGastosTotalesGrupo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gastosListenerRegistration != null) {
            gastosListenerRegistration.remove();
        }
    }

    private void calcularSaldoActual() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No se encontró al usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("grupos").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot groupDocument : task.getResult()) {
                            String groupId = groupDocument.getId();
                            db.collection("grupos").document(groupId).collection("miembros")
                                    .document(userId).get().addOnCompleteListener(memberTask -> {
                                        if (memberTask.isSuccessful() && memberTask.getResult().exists()) {
                                            gastosListenerRegistration = db.collection("grupos").document(groupId).collection("gastos")
                                                    .whereEqualTo("nombreUsuario", currentUser.getDisplayName())
                                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                            if (e != null) {
                                                                Toast.makeText(MainActivity.this, "Error al obtener los gastos.", Toast.LENGTH_SHORT).show();
                                                                return;
                                                            }

                                                            double totalGastos = 0;
                                                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                                Double monto = document.getDouble("monto");
                                                                if (monto != null) {
                                                                    totalGastos += monto;
                                                                }
                                                            }
                                                            saldoActualTextView.setText("$" + totalGastos);
                                                        }
                                                    });
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error al obtener los grupos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void calcularGastosTotalesGrupo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No se encontró al usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("grupos").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot groupDocument : task.getResult()) {
                            String groupId = groupDocument.getId();
                            db.collection("grupos").document(groupId).collection("miembros")
                                    .document(userId).get().addOnCompleteListener(memberTask -> {
                                        if (memberTask.isSuccessful() && memberTask.getResult().exists()) {
                                            gastosListenerRegistration = db.collection("grupos").document(groupId).collection("gastos")
                                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                            if (e != null) {
                                                                Toast.makeText(MainActivity.this, "Error al obtener los gastos del grupo.", Toast.LENGTH_SHORT).show();
                                                                return;
                                                            }

                                                            double totalGastosGrupo = 0;
                                                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                                Double monto = document.getDouble("monto");
                                                                if (monto != null) {
                                                                    totalGastosGrupo += monto;
                                                                }
                                                            }
                                                            gastosTotalesTextView.setText("$" + totalGastosGrupo);
                                                        }
                                                    });
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error al obtener los grupos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void detalles(View view) {
        Intent lanzardetalles = new Intent(this, vista2.class);
        startActivity(lanzardetalles);
    }

    public void historial(View view) {
        Intent lanzarhistorial = new Intent(this, Historial_Pagos.class);
        startActivity(lanzarhistorial);
    }

    public void recordatorio(View view) {
        Intent lanzarrecordatorio = new Intent(this, Recordatorio_Pagos.class);
        startActivity(lanzarrecordatorio);
    }

    public void settings(View view) {
        Intent lanzarsettings = new Intent(this, Settings.class);
        startActivity(lanzarsettings);
    }

    public void agregarPAgo(View view) {
        Intent lanzaragregarpago = new Intent(this, AgregarGasto.class);
        startActivity(lanzaragregarpago);
    }

    public void grupos(View view) {
        Intent lanzargrupos = new Intent(this, Grupos.class);
        startActivity(lanzargrupos);
    }

    public void unirme_crear_grupo(View view) {
        Intent lanzarunirme_crear_grupo = new Intent(this, CrearGrupo.class);
        startActivity(lanzarunirme_crear_grupo);

    }
}
