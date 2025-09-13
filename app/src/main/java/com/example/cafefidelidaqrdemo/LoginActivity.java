package com.example.cafefidelidaqrdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import com.example.cafefidelidaqrdemo.databinding.ActivityLoginEmailBinding;
import com.example.cafefidelidaqrdemo.viewmodels.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginEmailBinding binding;
    private LoginViewModel viewModel;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configurar Data Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_email);
        
        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        
        // Configurar ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Iniciando Sesión en Café Fidelidad");
        progressDialog.setCanceledOnTouchOutside(false);
        
        // Configurar observadores
        setupObservers();

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtener datos de los campos
                String email = binding.lbEmail.getText().toString().trim();
                String password = binding.lbPass.getText().toString().trim();
                
                // Actualizar ViewModel
                viewModel.setEmail(email);
                viewModel.setPassword(password);
                
                // Realizar login
                viewModel.login();
            }
        });

        binding.btnRegister.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
        });

        binding.txtForgotPass.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RecuperarPassActivity.class));
        });
    }
    
    /**
     * Configura los observadores del ViewModel
     */
    private void setupObservers() {
        // Observar estado de carga
        viewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                progressDialog.show();
            } else {
                progressDialog.dismiss();
            }
        });
        
        // Observar errores
        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                // Mostrar error al usuario
                android.widget.Toast.makeText(this, error, android.widget.Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
        
        // Observar éxito en login
        viewModel.getLoginSuccessLiveData().observe(this, success -> {
            if (success != null && success) {
                // Redirigir a MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                viewModel.clearLoginSuccess();
            }
        });
        
        // Observar errores de validación de email
        viewModel.getEmailErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.lbEmail.setError(error);
            } else {
                binding.lbEmail.setError(null);
            }
        });
        
        // Observar errores de validación de contraseña
        viewModel.getPasswordErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.lbPass.setError(error);
            } else {
                binding.lbPass.setError(null);
            }
        });
    }

    // Métodos de validación y login ahora manejados por el ViewModel
}