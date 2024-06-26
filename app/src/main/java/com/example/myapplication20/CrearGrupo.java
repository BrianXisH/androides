package com.example.myapplication20;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class CrearGrupo extends AppCompatActivity {

    private EditText groupNameEditText, groupIdCreateEditText, groupIdJoinEditText;
    private Button createGroupButton, joinGroupButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_grupo);

        groupNameEditText = findViewById(R.id.group_name);
        groupIdCreateEditText = findViewById(R.id.group_id_create);
        groupIdJoinEditText = findViewById(R.id.group_id_join);
        createGroupButton = findViewById(R.id.create_group_button);
        joinGroupButton = findViewById(R.id.join_group_button);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });

        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinGroup();
            }
        });
    }

    private void createGroup() {
        String groupName = groupNameEditText.getText().toString().trim();
        String groupId = groupIdCreateEditText.getText().toString().trim();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (TextUtils.isEmpty(groupName)) {
            groupNameEditText.setError("Por favor, ingrese el nombre del grupo");
            return;
        }

        if (TextUtils.isEmpty(groupId)) {
            groupIdCreateEditText.setError("Por favor, ingrese el ID del grupo");
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "No se encontró al usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("nombre", groupName);
        groupData.put("creadorId", currentUser.getUid());

        Map<String, String> memberData = new HashMap<>();
        memberData.put("nombre", currentUser.getDisplayName());
        memberData.put("email", currentUser.getEmail());

        db.collection("grupos").document(groupId).set(groupData)
                .addOnSuccessListener(documentReference -> {
                    db.collection("grupos").document(groupId).collection("miembros").document(currentUser.getUid()).set(memberData, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> Toast.makeText(CrearGrupo.this, "Grupo creado exitosamente", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(CrearGrupo.this, "Error al agregar miembro al grupo", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(CrearGrupo.this, "Error al crear el grupo", Toast.LENGTH_SHORT).show());
    }

    private void joinGroup() {
        String groupId = groupIdJoinEditText.getText().toString().trim();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (TextUtils.isEmpty(groupId)) {
            groupIdJoinEditText.setError("Por favor, ingrese el ID del grupo");
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "No se encontró al usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si el grupo existe
        db.collection("grupos").document(groupId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Verificar si el usuario ya está en algún grupo
                        db.collection("grupos").whereArrayContains("miembros", currentUser.getUid()).get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    boolean alreadyInGroup = false;
                                    boolean alreadyInThisGroup = false;

                                    for (QueryDocumentSnapshot groupDocument : queryDocumentSnapshots) {
                                        if (groupDocument.getId().equals(groupId)) {
                                            alreadyInThisGroup = true;
                                            break;
                                        }
                                        alreadyInGroup = true;
                                    }

                                    if (alreadyInThisGroup) {
                                        Toast.makeText(CrearGrupo.this, "Ya perteneces a este grupo.", Toast.LENGTH_SHORT).show();
                                    } else if (alreadyInGroup) {
                                        Toast.makeText(CrearGrupo.this, "Ya perteneces a un grupo. No puedes unirte a otro grupo.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Map<String, String> memberData = new HashMap<>();
                                        memberData.put("nombre", currentUser.getDisplayName());
                                        memberData.put("email", currentUser.getEmail());

                                        db.collection("grupos").document(groupId).collection("miembros").document(currentUser.getUid()).set(memberData, SetOptions.merge())
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(CrearGrupo.this, "Te has unido al grupo exitosamente", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(CrearGrupo.this, MainActivity.class);
                                                    intent.putExtra("grupoId", groupId); // Pasa el ID del grupo a la MainActivity
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(CrearGrupo.this, "Error al unirse al grupo", Toast.LENGTH_SHORT).show());
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(CrearGrupo.this, "Error al verificar la pertenencia a grupos", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(CrearGrupo.this, "El grupo no existe", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(CrearGrupo.this, "Error al verificar la existencia del grupo", Toast.LENGTH_SHORT).show());
    }
}
