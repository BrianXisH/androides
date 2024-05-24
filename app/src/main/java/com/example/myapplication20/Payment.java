package com.example.myapplication20;

public class Payment {
    private long id;
    private String name;
    private long dueDate; // Se puede utilizar un long para almacenar la fecha como un timestamp

    public Payment() {
        // Constructor vacío requerido por SQLite
    }

    public Payment(String name, long dueDate) {
        this.name = name;
        this.dueDate = dueDate;
    }

    public Payment(long id, String name, long dueDate) {
        this.id = id;
        this.name = name;
        this.dueDate = dueDate; // Asignar el valor de dueDate
    }

    // Getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }



}
