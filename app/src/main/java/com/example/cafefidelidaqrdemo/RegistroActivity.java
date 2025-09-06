package com.example.cafefidelidaqrdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cafefidelidaqrdemo.databinding.ActivityRegisterEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegistroActivity extends AppCompatActivity {

    private ActivityRegisterEmailBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterEmailBinding.inflate(getLayoutInflater());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creando tu cuenta de fidelidad");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.btnRegistro.setOnClickListener(view -> {
            validateInfo();
        });

        setContentView(binding.getRoot());
    }

    private String Nombre, email, pass, rpass;

    private void validateInfo(){
        Nombre = binding.lbName.getText().toString().trim();
        email = binding.lbEmail.getText().toString().trim();
        pass = binding.lbPass.getText().toString().trim();
        rpass = binding.lbNewPass.getText().toString().trim();
        
        if (Nombre.isEmpty()) {
            binding.lbName.setError("Ingresa tu nombre");
            binding.lbName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            binding.lbEmail.setError("Ingresa tu correo");
            binding.lbEmail.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            binding.lbPass.setError("Ingresa una contraseña");
            binding.lbPass.requestFocus();
            return;
        }
        if (rpass.isEmpty()) {
            binding.lbNewPass.setError("Confirma tu contraseña");
            binding.lbNewPass.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.lbEmail.setError("Correo Inválido");
            binding.lbEmail.requestFocus();
        }
        else if (!pass.equals(rpass)){
            binding.lbNewPass.setError("Las contraseñas no coinciden");
            binding.lbNewPass.requestFocus();
        } else {
            registerUser();
        }
    }

    private void registerUser() {
        progressDialog.setMessage("Creando tu cuenta de fidelidad...");
        progressDialog.show();
        
        // Crear usuario en Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        UpdateInfo();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(
                                RegistroActivity.this,
                                "Error al crear la cuenta: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // Guardar información del usuario en la base de datos
    private void UpdateInfo(){
        progressDialog.setMessage("Configurando tu programa de fidelidad...");

        String uid = firebaseAuth.getUid();
        String namesU = Nombre;
        String emailU = firebaseAuth.getCurrentUser().getEmail();
        long date = Contantes.getTimeD();

        // Datos del usuario para el programa de fidelidad
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("names", namesU);
        hashMap.put("email", emailU);
        hashMap.put("proveedor", "email");
        hashMap.put("estado", "activo");
        hashMap.put("imagen", "");
        hashMap.put("date", date);
        hashMap.put("puntos", Contantes.PUNTOS_BIENVENIDA); // Puntos de bienvenida
        hashMap.put("nivel", Contantes.NIVEL_BRONCE); // Nivel inicial
        hashMap.put("totalCompras", 0.0);
        hashMap.put("ultimaVisita", date);
        hashMap.put("telefono", ""); // Campo para RF-02
        hashMap.put("fechaNacimiento", ""); // Campo para RF-02

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Guardar también en SQLite local usando OfflineManager
                        OfflineManager offlineManager = OfflineManager.getInstance(RegistroActivity.this);
                        offlineManager.saveUserToLocal(hashMap, new OfflineManager.SaveUserCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(RegistroActivity.this, 
                                        "¡Bienvenido a Café Fidelidad! Has recibido 100 puntos de bienvenida", 
                                        Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(RegistroActivity.this, MainActivity.class));
                                    finishAffinity();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(RegistroActivity.this, 
                                        "Usuario registrado pero error al guardar localmente: " + error, 
                                        Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegistroActivity.this, MainActivity.class));
                                    finishAffinity();
                                });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(
                                RegistroActivity.this,
                                "Error al configurar la cuenta: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}