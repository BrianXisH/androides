package com.example.myapplication20;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private TextView saldoActualTextView;
    private TextView gastosTotalesTextView;
    private LinearLayout porcentajesLayout;
    private PieChartView pieChart;
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
        pieChart = findViewById(R.id.pieChart);
        porcentajesLayout = findViewById(R.id.porcentajesLayout);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intentSettings = new Intent(this, Settings.class);
                startActivity(intentSettings);
                return true;
            case R.id.action_logout:
                mAuth.signOut();
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                Intent intentLogout = new Intent(MainActivity.this, Login.class);
                startActivity(intentLogout);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                                                            Map<String, Double> userGastos = new HashMap<>();
                                                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                                Double monto = document.getDouble("monto");
                                                                String usuario = document.getString("nombreUsuario");
                                                                if (monto != null) {
                                                                    totalGastosGrupo += monto;
                                                                    if (usuario != null) {
                                                                        if (userGastos.containsKey(usuario)) {
                                                                            userGastos.put(usuario, userGastos.get(usuario) + monto);
                                                                        } else {
                                                                            userGastos.put(usuario, monto);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            gastosTotalesTextView.setText("$" + totalGastosGrupo);
                                                            actualizarGrafico(userGastos, totalGastosGrupo);
                                                            actualizarPorcentajes(userGastos, totalGastosGrupo);
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

    private void actualizarGrafico(Map<String, Double> userGastos, double totalGastosGrupo) {
        List<Float> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int[] palette = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA};

        int i = 0;
        for (Map.Entry<String, Double> entry : userGastos.entrySet()) {
            values.add((float) (entry.getValue() / totalGastosGrupo * 100));
            labels.add(entry.getKey());
            colors.add(palette[i % palette.length]);
            i++;
        }

        pieChart.setData(values, labels, colors);
    }

    private void actualizarPorcentajes(Map<String, Double> userGastos, double totalGastosGrupo) {
        porcentajesLayout.removeAllViews();

        for (Map.Entry<String, Double> entry : userGastos.entrySet()) {
            TextView textView = new TextView(this);
            String texto = entry.getKey() + ": " + String.format("%.1f%%", (entry.getValue() / totalGastosGrupo) * 100);
            textView.setText(texto);
            textView.setTextSize(16);
            porcentajesLayout.addView(textView);
        }
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
