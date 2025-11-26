package com.example.cafefidelidaqrdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import com.example.cafefidelidaqrdemo.databinding.ActivityMainBinding;
import com.example.cafefidelidaqrdemo.viewmodels.MainViewModel;
// Import de PerformanceMonitor removido para simplificación
import com.example.cafefidelidaqrdemo.repository.AuthRepository;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate iniciado");
        
        // Configurar Data Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Log.d("MainActivity", "Data binding configurado");
        
        // Configurar AuthRepository con contexto ANTES de inicializar ViewModel
        AuthRepository authRepository = AuthRepository.getInstance();
        authRepository.setContext(this);
        Log.d("MainActivity", "AuthRepository configurado con contexto");
        
        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        Log.d("MainActivity", "ViewModel inicializado");
        
        // Ocultar contenido hasta verificar autenticación
        hideMainContent();
        Log.d("MainActivity", "Contenido principal ocultado");
        
        // Configurar observadores ANTES de verificar autenticación
        setupObservers();
        Log.d("MainActivity", "Observadores configurados");
        
        // Verificar autenticación con un pequeño delay para permitir que la sesión se establezca
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            viewModel.checkAuthenticationStatus();
            Log.d("MainActivity", "Verificación de autenticación iniciada");
        }, 100); // 100ms delay
    }
    
    /**
     * Configura los observadores del ViewModel
     */
    private void setupObservers() {
        Log.d("MainActivity", "setupObservers iniciado");
        // Observar estado de autenticación
        viewModel.getIsAuthenticatedLiveData().observe(this, isAuthenticated -> {
            Log.d("MainActivity", "Observador de autenticación ejecutado. isAuthenticated: " + isAuthenticated);
            if (isAuthenticated != null) {
                if (!isAuthenticated) {
                    Log.d("MainActivity", "Usuario NO autenticado - redirigiendo a LoginActivity");
                    // Redirigir a LoginActivity si no está autenticado
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.d("MainActivity", "Usuario autenticado - inicializando aplicación principal");
                    // Usuario autenticado - inicializar el resto de la aplicación
                    initializeMainApp();
                }
            } else {
                Log.d("MainActivity", "isAuthenticated es null - esperando resultado");
            }
        });
        
        // Observar errores
        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                // Si hay error en autenticación, redirigir a login
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        
        // El título se maneja automáticamente con Data Binding
    }
    
    /**
     * Configura la navegación del bottom navigation
     */


    private void setLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
    }


    

    
    /**
     * Oculta el contenido principal durante la carga
     */
    private void hideMainContent() {
        if (binding != null) {
            binding.bottomNV.setVisibility(android.view.View.GONE);
            binding.fragmentFL.setVisibility(android.view.View.GONE);
        }
    }
    

    
    /**
     * Inicializa la aplicación principal después de verificar autenticación
     */
    private void initializeMainApp() {
        // Determinar qué actividad mostrar según el tipo de usuario
        AuthRepository authRepository = AuthRepository.getInstance();
        
        if (authRepository.isCurrentUserAdmin()) {
            // Redirigir a AdminMainActivity
            Intent intent = new Intent(this, AdminMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (authRepository.isCurrentUserCliente()) {
            // Redirigir a ClienteMainActivity
            Intent intent = new Intent(this, ClienteMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // Usuario sin tipo específico, redirigir al login
            setLogin();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Reportar estadísticas de rendimiento al cerrar la app
        // PerformanceMonitor removido para simplificación
    }
}
