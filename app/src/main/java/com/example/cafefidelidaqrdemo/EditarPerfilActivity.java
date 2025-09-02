package com.example.cafefidelidaqrdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.cafefidelidaqrdemo.databinding.ActivityEditarDatosBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditarPerfilActivity extends AppCompatActivity {

    private ActivityEditarDatosBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditarDatosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Actualizando perfil de fidelidad");
        progressDialog.setCanceledOnTouchOutside(false);

        loadUserInfo();

        binding.ImgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserInfo();
            }
        });
    }

    private void loadUserInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String nombres = "" + snapshot.child("names").getValue();
                        String imagen = "" + snapshot.child("imagen").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String puntos = "" + snapshot.child("puntos").getValue();
                        String nivel = "" + snapshot.child("nivel").getValue();
                        String totalCompras = "" + snapshot.child("totalCompras").getValue();

                        binding.tvNombres.setText(nombres);
                        
                        // Solo actualizar el nombre ya que este layout solo tiene ese campo

                        Glide.with(EditarPerfilActivity.this)
                                .load(imagen)
                                .placeholder(R.drawable.ic_account)
                                .into(binding.ivPerfil);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditarPerfilActivity.this, 
                            "Error al cargar información: " + error.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo() {
        String nombres = binding.tvNombres.getText().toString().trim();
        
        if (nombres.isEmpty()) {
            binding.tvNombres.setError("Ingrese su nombre");
            binding.tvNombres.requestFocus();
            return;
        }

        progressDialog.setMessage("Actualizando información del perfil...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("names", nombres);
        // Actualizar fecha de última modificación
        hashMap.put("ultimaModificacion", System.currentTimeMillis());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(EditarPerfilActivity.this, 
                            "Perfil actualizado correctamente", 
                            Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(EditarPerfilActivity.this, 
                            "Error al actualizar perfil: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }
}