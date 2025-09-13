package com.example.cafefidelidaqrdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import com.example.cafefidelidaqrdemo.databinding.ActivityMainBinding;
import com.example.cafefidelidaqrdemo.viewmodels.MainViewModel;
import com.example.cafefidelidaqrdemo.fragments.FragmentQR;
import com.example.cafefidelidaqrdemo.fragments.FragmentPerfil;
import com.example.cafefidelidaqrdemo.fragments.FragmentPuntos;
import com.example.cafefidelidaqrdemo.utils.PerformanceMonitor;
import com.example.cafefidelidaqrdemo.CatalogoActivity;
import com.example.cafefidelidaqrdemo.offline.OfflineManager;
import com.example.cafefidelidaqrdemo.ui.admin.FragmentAdminDashboard;
import com.example.cafefidelidaqrdemo.ui.cliente.FragmentTableroCliente;
import com.example.cafefidelidaqrdemo.data.repositories.AuthRepository;
import com.google.android.material.navigation.NavigationBarView;
// import com.google.firebase.Firebase;
// import com.google.firebase.auth.FirebaseAuth;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private OfflineManager offlineManager;

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
                    Log.d("MainActivity", "Usuario NO autenticado - redirigiendo a OpcionesLoginActivity");
                    // Redirigir a OpcionesLoginActivity si no está autenticado
                    Intent intent = new Intent(this, OpcionesLoginActivity.class);
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
                Intent intent = new Intent(this, OpcionesLoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        
        // El título se maneja automáticamente con Data Binding
    }
    
    /**
     * Configura la navegación del bottom navigation
     */
    private void setupNavigation() {
        verFragPerfil();
        binding.bottomNV.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.item_perfil) {
                    verFragPerfil();
                    viewModel.setToolbarTitle("Mi Perfil");
                    return true;
                } else if (itemId == R.id.item_puntos) {
                    verFragPuntos();
                    viewModel.setToolbarTitle("Mis Puntos");
                    return true;
                } else if (itemId == R.id.item_catalogo) {
                    verCatalogo();
                    return true;
                } else if (itemId == R.id.item_beneficios) {
                    verBeneficios();
                    return true;
                } else if (itemId == R.id.item_qr) {
                    verFragQR();
                    viewModel.setToolbarTitle("Escanear QR");
                    return true;
                }
                else {
                    return false;
                }
            }
        });
    }

    private void setLogin() {
        startActivity(new Intent(this, OpcionesLoginActivity.class));
        finishAffinity();
    }

    private void verFragPerfil(){
        binding.tvTitulo.setText("Mi Perfil");

        FragmentPerfil fragment = new FragmentPerfil();
        FragmentTransaction FragmentTransaction = getSupportFragmentManager().beginTransaction();
        FragmentTransaction.replace(binding.fragmentFL.getId(), fragment, "FragmentPerfil");
        FragmentTransaction.commit();
    }
    
    private void verFragPuntos(){
        binding.tvTitulo.setText("Mis Puntos");

        FragmentPuntos fragment = new FragmentPuntos();
        FragmentTransaction FragmentTransaction = getSupportFragmentManager().beginTransaction();
        FragmentTransaction.replace(binding.fragmentFL.getId(), fragment, "FragmentPuntos");
        FragmentTransaction.commit();
    }
    
    private void verFragQR(){
        binding.tvTitulo.setText("Escanear QR");

        FragmentQR fragment = new FragmentQR();
        FragmentTransaction FragmentTransaction = getSupportFragmentManager().beginTransaction();
        FragmentTransaction.replace(binding.fragmentFL.getId(), fragment, "FragmentQR");
        FragmentTransaction.commit();
    }
    
    private void verCatalogo(){
        Intent intent = new Intent(this, CatalogoActivity.class);
        startActivity(intent);
    }
    
    private void verBeneficios(){
        Intent intent = new Intent(this, BeneficiosActivity.class);
        startActivity(intent);
    }
    
    private void configurarSincronizacionAutomatica() {
        // Configurar sincronización periódica cada 15 minutos
        PeriodicWorkRequest syncWorkRequest = new PeriodicWorkRequest.Builder(
                com.example.cafefidelidaqrdemo.offline.SyncWorker.class,
                15, TimeUnit.MINUTES)
                .build();
        
        WorkManager.getInstance(this).enqueue(syncWorkRequest);
        
        // Iniciar sincronización inmediata si hay datos pendientes
        offlineManager.iniciarSincronizacion();
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
     * Configura la interfaz para usuarios administradores
     */
    private void setupAdminInterface() {
        Log.d("MainActivity", "Configurando interfaz de administrador");
        
        // Ocultar bottom navigation para admin
        if (binding != null) {
            binding.bottomNV.setVisibility(android.view.View.GONE);
        }
        
        // Mostrar dashboard de admin
        FragmentAdminDashboard adminDashboard = new FragmentAdminDashboard();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentFL, adminDashboard)
                .commit();
    }
    
    /**
     * Configura la interfaz para usuarios clientes
     */
    private void setupClienteInterface() {
        Log.d("MainActivity", "Configurando interfaz de cliente");
        
        // Mostrar bottom navigation para clientes
        if (binding != null) {
            binding.bottomNV.setVisibility(android.view.View.VISIBLE);
        }
        
        // Configurar navegación normal para clientes
        setupNavigation();
        
        // Mostrar tablero de cliente por defecto
        FragmentTableroCliente tableroCliente = new FragmentTableroCliente();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentFL, tableroCliente)
                .commit();
    }
    
    /**
     * Inicializa la aplicación principal después de verificar autenticación
     */
    private void initializeMainApp() {
        // Mostrar contenido principal
        if (binding != null) {
            binding.bottomNV.setVisibility(android.view.View.VISIBLE);
            binding.fragmentFL.setVisibility(android.view.View.VISIBLE);
        }
        
        // Inicializar OfflineManager y configurar sincronización automática
        offlineManager = OfflineManager.getInstance(this);
        configurarSincronizacionAutomatica();
        
        // Determinar qué interfaz mostrar según el tipo de usuario
        AuthRepository authRepository = AuthRepository.getInstance();
        if (authRepository.isCurrentUserAdmin()) {
            Log.d("MainActivity", "Usuario administrador detectado, mostrando dashboard admin");
            setupAdminInterface();
        } else if (authRepository.isCurrentUserCliente()) {
            Log.d("MainActivity", "Usuario cliente detectado, mostrando interfaz cliente");
            setupClienteInterface();
        } else {
            Log.w("MainActivity", "Tipo de usuario no determinado, usando interfaz por defecto");
            setupNavigation();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Reportar estadísticas de rendimiento al cerrar la app
        PerformanceMonitor.reportStatistics();
    }
}