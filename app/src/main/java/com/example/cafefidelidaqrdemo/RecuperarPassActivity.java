package com.example.cafefidelidaqrdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cafefidelidaqrdemo.databinding.ActivityRecuperarPassBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RecuperarPassActivity extends AppCompatActivity {

    private ActivityRecuperarPassBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRecuperarPassBinding.inflate(getLayoutInflater());
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Recuperando acceso a tu cuenta");
        progressDialog.setCanceledOnTouchOutside(false);

        setContentView(binding.getRoot());

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        
        binding.btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String Email = "";

    private void validateData() {
        Email = binding.etEmail.getText().toString().trim();

        if (Email.isEmpty()) {
            binding.etEmail.setError("Ingrese su correo electrónico");
            binding.etEmail.requestFocus();
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            binding.etEmail.setError("Correo electrónico inválido");
            binding.etEmail.requestFocus();
        } else {
            recoverPassword();
        }
    }

    private void recoverPassword() {
        progressDialog.setMessage("Enviando instrucciones de recuperación a " + Email);
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(Email)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(RecuperarPassActivity.this, 
                        "Se han enviado las instrucciones de recuperación a:\n" + Email + 
                        "\n\nRevisa tu bandeja de entrada y spam.", 
                        Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    String errorMessage = "Error al enviar el correo de recuperación";
                    if (e.getMessage().contains("no user record")) {
                        errorMessage = "No existe una cuenta registrada con este correo";
                    }
                    Toast.makeText(RecuperarPassActivity.this, 
                        errorMessage + ": " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
    }
}