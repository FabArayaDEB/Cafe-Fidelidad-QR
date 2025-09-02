package com.example.cafefidelidaqrdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cafefidelidaqrdemo.databinding.ActivityLoginEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginEmailBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Iniciando Sesión en Café Fidelidad");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateInfo();
            }
        });

        binding.btnRegister.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
        });

        binding.txtForgotPass.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RecuperarPassActivity.class));
        });
    }

    private String email, pass;
    private void validateInfo() {
        email = binding.lbEmail.getText().toString().trim();
        pass = binding.lbPass.getText().toString().trim();
        if (email.isEmpty()) {
            binding.lbEmail.setError("Correo Inválido");
            binding.lbEmail.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.lbEmail.setError("Email Inválido");
            binding.lbEmail.requestFocus();
        } else if (pass.isEmpty()) {
            binding.lbPass.setError("Contraseña Inválida");
            binding.lbPass.requestFocus();
        }
        else {
            loginUser();
        }
    }

    private void loginUser() {
        progressDialog.setMessage("Accediendo a tu cuenta de fidelidad...");
        progressDialog.show();

        // Corregido: usar signInWithEmailAndPassword para login
        firebaseAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "¡Bienvenido a Café Fidelidad!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(
                                LoginActivity.this,
                                "Error al iniciar sesión: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}