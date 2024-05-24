package com.example.myapplication20;

public class Gasto {
    private String nombre;
    private String descripcion;
    private double monto;

    public Gasto(String nombre, String descripcion, double monto) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.monto = monto;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getMonto() {
        return monto;
    }
}