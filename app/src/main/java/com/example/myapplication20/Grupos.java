package com.example.myapplication20;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class Grupos extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private MemberAdapter adapter;
    private List<Member> memberList;
    private TextView groupIdTextView;
    private TextView groupNameTextView;
    private String currentGroupId;
    private ListenerRegistration groupListener;
    private ListenerRegistration memberListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_grupos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar RecyclerView
        recyclerView = findViewById(R.id.recycler_view_members);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        memberList = new ArrayList<>();
        adapter = new MemberAdapter(memberList);
        recyclerView.setAdapter(adapter);

        // Inicializar TextViews
        groupIdTextView = findViewById(R.id.group_id);
        groupNameTextView = findViewById(R.id.group_name);

        // Inicializar botón para salir del grupo
        Button leaveGroupButton = findViewById(R.id.leave_group_button);
        leaveGroupButton.setOnClickListener(v -> showLeaveGroupConfirmation());

        // Cargar miembros del grupo
        loadGroupMembers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (groupListener != null) {
            groupListener.remove();
        }
        if (memberListener != null) {
            memberListener.remove();
        }
    }

    private void loadGroupMembers() {
        String userId = auth.getCurrentUser().getUid();
        groupListener = db.collection("grupos").addSnapshotListener((task, e) -> {
            if (e != null) {
                Log.w("Grupos", "Error getting groups.", e);
                Toast.makeText(Grupos.this, "Error al obtener los grupos.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (task != null) {
                for (QueryDocumentSnapshot groupDocument : task) {
                    String groupId = groupDocument.getId();
                    db.collection("grupos").document(groupId).collection("miembros")
                            .document(userId).get().addOnCompleteListener(memberTask -> {
                                if (memberTask.isSuccessful() && memberTask.getResult().exists()) {
                                    String groupName = groupDocument.getString("nombre");

                                    // Mostrar el ID y nombre del grupo
                                    groupIdTextView.setText("ID del Grupo: " + groupId);
                                    groupNameTextView.setText("Nombre del Grupo: " + groupName);
                                    currentGroupId = groupId;

                                    if (memberListener != null) {
                                        memberListener.remove();
                                    }

                                    memberListener = db.collection("grupos").document(groupId).collection("miembros")
                                            .addSnapshotListener((innerMemberTask, innerE) -> {
                                                if (innerE != null) {
                                                    Log.w("Grupos", "Error getting members.", innerE);
                                                    Toast.makeText(Grupos.this, "Error al obtener los miembros del grupo.", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                if (innerMemberTask != null) {
                                                    memberList.clear();
                                                    for (QueryDocumentSnapshot document : innerMemberTask) {
                                                        Member member = document.toObject(Member.class);
                                                        memberList.add(member);
                                                    }
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });
                                }
                            });
                }
            }
        });
    }

    private void showLeaveGroupConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Salir del grupo")
                .setMessage("¿Seguro que quieres salirte del grupo?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        leaveGroup();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void leaveGroup() {
        String userId = auth.getCurrentUser().getUid();
        if (currentGroupId != null) {
            // Primero, elimina los registros del usuario en la subcolección "gastos"
            db.collection("grupos").document(currentGroupId).collection("gastos")
                    .whereEqualTo("usuarioId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete();
                        }

                        // Luego, elimina el documento del usuario en la subcolección "miembros"
                        db.collection("grupos").document(currentGroupId).collection("miembros").document(userId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(Grupos.this, "Has salido del grupo", Toast.LENGTH_SHORT).show();
                                    // Limpiar la información del grupo y los miembros
                                    groupIdTextView.setText("");
                                    groupNameTextView.setText("");
                                    memberList.clear();
                                    adapter.notifyDataSetChanged();
                                    currentGroupId = null;

                                    // Notificar a MainActivity para que actualice la interfaz
                                    Intent intent = new Intent(Grupos.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Grupos.this, "Error al salir del grupo", Toast.LENGTH_SHORT).show();
                                    Log.w("Grupos", "Error removing user from group.", e);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Grupos.this, "Error al eliminar los gastos del usuario", Toast.LENGTH_SHORT).show();
                        Log.w("Grupos", "Error removing user expenses from group.", e);
                    });
        } else {
            Toast.makeText(this, "No se encontró el grupo actual", Toast.LENGTH_SHORT).show();
        }
    }

    }
