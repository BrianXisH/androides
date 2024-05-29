package com.example.myapplication20;

public class Member {
    private String nombre;
    private String email;

    public Member() {
        // Constructor vacío necesario para Firestore
    }

    public Member(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }
}
