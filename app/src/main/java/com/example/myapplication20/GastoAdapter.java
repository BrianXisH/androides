package com.example.myapplication20;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GastoAdapter extends RecyclerView.Adapter<GastoAdapter.GastoViewHolder> {

    private List<Gasto> gastos;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Gasto gasto);
    }

    public GastoAdapter(List<Gasto> gastos) {
        this.gastos = gastos;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gasto, parent, false);
        return new GastoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position) {
        Gasto gasto = gastos.get(position);
        holder.textViewNombreGasto.setText(gasto.getNombre());
        holder.textViewDescripcionGasto.setText(gasto.getDescripcion());
        holder.textViewMontoGasto.setText(String.valueOf(gasto.getMonto()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(gasto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return gastos.size();
    }

    public static class GastoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNombreGasto;
        TextView textViewDescripcionGasto;
        TextView textViewMontoGasto;

        public GastoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombreGasto = itemView.findViewById(R.id.textViewNombreGasto);
            textViewDescripcionGasto = itemView.findViewById(R.id.textViewDescripcionGasto);
            textViewMontoGasto = itemView.findViewById(R.id.textViewMontoGasto);
        }
    }
}