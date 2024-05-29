package com.example.myapplication20;

public class Gasto {
    private String id;
    private String nombreGasto;  // Cambiado de 'nombre' a 'nombreGasto'
    private String categoria;
    private String fecha;
    private double monto;
    private String descripcion;
    private String nombreUsuario;  // Si también necesitas mostrar el nombre del usuario

    // Constructor vacío requerido para Firestore
    public Gasto() {}

    public Gasto(String id, String nombreGasto, String categoria, String fecha, double monto, String descripcion, String nombreUsuario) {
        this.id = id;
        this.nombreGasto = nombreGasto;
        this.categoria = categoria;
        this.fecha = fecha;
        this.monto = monto;
        this.descripcion = descripcion;
        this.nombreUsuario = nombreUsuario;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getNombreGasto() {
        return nombreGasto;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getFecha() {
        return fecha;
    }

    public double getMonto() {
        return monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    // Setter for ID
    public void setId(String id) {
        this.id = id;
    }
}
