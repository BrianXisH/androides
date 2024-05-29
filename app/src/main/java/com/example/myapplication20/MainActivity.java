package com.example.myapplication20;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class    MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);



}


    public void detalles(View view) {
        Intent lanzardetalles= new Intent(this, vista2.class);
        startActivity(lanzardetalles);

    } public void historial(View view) {
        Intent lanzarhistorial= new Intent(this, Historial_Pagos.class);
        startActivity(lanzarhistorial);
    } public void recordatorio(View view) {
        Intent lanzarrecordatorio= new Intent(this, Recordatorio_Pagos.class);
        startActivity(lanzarrecordatorio);
    }public void settings(View view) {
        Intent lanzarsettings= new Intent(this, Settings.class);
        startActivity(lanzarsettings);
    }

    public void agregarPAgo(View view) {
        Intent lanzaragregarpago= new Intent(this, AgregarGasto.class);
        startActivity(lanzaragregarpago);
    }

    public void grupos(View view) {
        Intent lanzargrupos= new Intent(this, Grupos.class);
        startActivity(lanzargrupos);
    }

    public void unirme_crear_grupo(View view) {
        Intent lanzarunirme_crear_grupo= new Intent(this, CrearGrupo.class);
        startActivity(lanzarunirme_crear_grupo);
    }
}