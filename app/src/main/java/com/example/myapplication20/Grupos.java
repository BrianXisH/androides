package com.example.myapplication20;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Grupos extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private MemberAdapter adapter;
    private List<Member> memberList;
    private TextView groupIdTextView;
    private TextView groupNameTextView;

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

        // Cargar miembros del grupo
        loadGroupMembers();
    }

    private void loadGroupMembers() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("grupos").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean foundGroup = false;
                        for (QueryDocumentSnapshot groupDocument : task.getResult()) {
                            String groupId = groupDocument.getId();
                            db.collection("grupos").document(groupId).collection("miembros")
                                    .document(userId).get().addOnCompleteListener(memberTask -> {
                                        if (memberTask.isSuccessful() && memberTask.getResult().exists()) {
                                            String groupName = groupDocument.getString("nombre");

                                            // Mostrar el ID y nombre del grupo
                                            groupIdTextView.setText("ID del Grupo: " + groupId);
                                            groupNameTextView.setText("Nombre del Grupo: " + groupName);

                                            db.collection("grupos").document(groupId).collection("miembros").get()
                                                    .addOnCompleteListener(innerMemberTask -> {
                                                        if (innerMemberTask.isSuccessful()) {
                                                            memberList.clear();
                                                            for (QueryDocumentSnapshot document : innerMemberTask.getResult()) {
                                                                Member member = document.toObject(Member.class);
                                                                memberList.add(member);
                                                            }
                                                            adapter.notifyDataSetChanged();
                                                        } else {
                                                            Log.w("Grupos", "Error getting members.", innerMemberTask.getException());
                                                            Toast.makeText(Grupos.this, "Error al obtener los miembros del grupo.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    });

                            if (foundGroup) {
                                break;
                            }
                        }


                    } else {
                        Log.w("Grupos", "Error getting groups.", task.getException());
                        Toast.makeText(Grupos.this, "Error al obtener los grupos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
