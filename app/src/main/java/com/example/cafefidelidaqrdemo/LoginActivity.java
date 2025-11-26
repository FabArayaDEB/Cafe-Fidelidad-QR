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
import com.example.cafefidelidaqrdemo.repository.AuthRepository;

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
        
        // Establecer contexto en AuthRepository
        com.example.cafefidelidaqrdemo.repository.AuthRepository.getInstance().setContext(this);
        
        // Configurar ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Iniciando Sesión en Café Fidelidad");
        progressDialog.setCanceledOnTouchOutside(false);
        
        // Configurar observadores
        setupObservers();
        
        //Mostrar credenciales de ejemplo
        Toast.makeText(this, "Ejemplos de acceso:\n" +
                "• cliente@test.com / 123456\n" +
                "• admin@test.com / 123456\n" +
                "• juan@email.com / 123456",
                Toast.LENGTH_LONG).show();

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ocultar teclado para evitar problemas de IME durante redirección
                try {
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    android.view.View focused = getCurrentFocus();
                    if (imm != null && focused != null) {
                        imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                    }
                } catch (Exception ignored) {}

                // Obtener datos de los campos
                String email = binding.lbEmail.getText().toString().trim();
                String password = binding.lbPass.getText().toString().trim();
                
                // DEBUG: Probar credenciales directamente
                AuthRepository authRepo = AuthRepository.getInstance();
                boolean testResult = authRepo.testCredentials(email, password);
                android.util.Log.d("LoginActivity", "Direct test result: " + testResult);
                
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

    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception ignored) {}
    }
    
    /**
     * Configura los observadores del ViewModel
     */
    private void setupObservers() {
        // Observar estado de carga
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                progressDialog.show();
            } else {
                progressDialog.dismiss();
            }
        });
        
        // Observar errores
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                // Mostrar error al usuario
                android.widget.Toast.makeText(this, error, android.widget.Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
        
        // Observar éxito en login
        viewModel.getLoginSuccess().observe(this, success -> {
            android.util.Log.d("LoginActivity", "LoginSuccess observed: " + success);
            if (success != null && success) {
                // Asegurar que el ProgressDialog esté cerrado para evitar WindowLeaked
                try {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                } catch (Exception ignored) {}

                // Determinar rol del usuario y redirigir a la actividad correcta
                com.example.cafefidelidaqrdemo.repository.AuthRepository authRepo = com.example.cafefidelidaqrdemo.repository.AuthRepository.getInstance();
                com.example.cafefidelidaqrdemo.repository.AuthRepository.LocalUser currentUser = authRepo.getCurrentUser();

                Intent intent;
                if (currentUser != null && currentUser.isAdmin()) {
                    android.util.Log.d("LoginActivity", "Login exitoso como ADMIN, redirigiendo a AdminMainActivity");
                    intent = new Intent(this, AdminMainActivity.class);
                } else {
                    android.util.Log.d("LoginActivity", "Login exitoso como CLIENTE, redirigiendo a ClienteMainActivity");
                    intent = new Intent(this, ClienteMainActivity.class);
                }

                startActivity(intent);
                // Limpiar flags y cerrar actividad con el diálogo ya descartado
                viewModel.clearLoginSuccess();
                finish();
            }
        });
        
        // Observar errores de validación de email
        viewModel.getEmailError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.lbEmail.setError(error);
            } else {
                binding.lbEmail.setError(null);
            }
        });
        
        // Observar errores de validación de contraseña
        viewModel.getPasswordError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.lbPass.setError(error);
            } else {
                binding.lbPass.setError(null);
            }
        });
    }

    // Métodos de validación y login ahora manejados por el ViewModel
}
