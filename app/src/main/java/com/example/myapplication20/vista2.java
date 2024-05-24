package com.example.myapplication20;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class vista2 extends AppCompatActivity implements GastoAdapter.OnItemClickListener {

    private RecyclerView recyclerViewGastos;
    private GastoAdapter adapter;
    private List<Gasto> gastos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vista2);

        recyclerViewGastos = findViewById(R.id.recyclerViewGastos);
        recyclerViewGastos.setLayoutManager(new LinearLayoutManager(this));

        gastos = new ArrayList<>();
        // Aquí puedes agregar los gastos a la lista, por ejemplo:
        gastos.add(new Gasto("Gasto 1", "Fecha 1", 50.00));
        gastos.add(new Gasto("Gasto 2",  "Fecha 2", 30.00));
        gastos.add(new Gasto("Gasto 3",  "Fecha 3", 20.00));

        adapter = new GastoAdapter(gastos);
        adapter.setOnItemClickListener(this);
        recyclerViewGastos.setAdapter(adapter);
    }

    @Override
    public void onItemClick(Gasto gasto) {
        Intent intent = new Intent(this, GastoDetallesActivity.class);
        intent.putExtra("nombre", gasto.getNombre());
        intent.putExtra("monto", gasto.getMonto());
        intent.putExtra("descripcion", gasto.getDescripcion());
        startActivity(intent);
    }
}