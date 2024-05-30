package com.example.myapplication20;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrarUsuario extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar_usuario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void registrarUsuario(View view) {
        String name = ((EditText) findViewById(R.id.user_name)).getText().toString();
        String email = ((EditText) findViewById(R.id.user_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.user_password)).getText().toString();

        if (password.length() < 6) {
            Toast.makeText(RegistrarUsuario.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Usuario registrado exitosamente
                        FirebaseUser user = auth.getCurrentUser();

                        // Guardar información adicional del usuario en Firestore
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("email", email);

                        db.collection("users").document(user.getUid()).set(userData)
                                .addOnSuccessListener(aVoid -> Log.d("Registro", "Usuario registrado y nombre guardado en Firestore"))
                                .addOnFailureListener(e -> Log.w("Registro", "Error al guardar el nombre en Firestore", e));

                        // Actualizar el perfil del usuario
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Log.d("Registro", "Perfil del usuario actualizado con el nombre");
                                        Toast.makeText(RegistrarUsuario.this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
                                        // Redirigir al usuario a la pantalla de inicio de sesión
                                        startActivity(new Intent(RegistrarUsuario.this, Login.class));
                                        finish();
                                    } else {
                                        Toast.makeText(RegistrarUsuario.this, "Error al actualizar el perfil: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        // Error al registrar el usuario
                        Log.w("Registro", "Error al registrar el usuario", task.getException());
                        Toast.makeText(RegistrarUsuario.this, "Error al registrar el usuario: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
