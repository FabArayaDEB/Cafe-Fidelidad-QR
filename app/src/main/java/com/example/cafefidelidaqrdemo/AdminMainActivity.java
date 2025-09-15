package com.example.cafefidelidaqrdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.cafefidelidaqrdemo.databinding.ActivityAdminMainBinding;
import com.example.cafefidelidaqrdemo.viewmodels.MainViewModel;
import com.example.cafefidelidaqrdemo.ui.admin.FragmentAdminDashboard;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;
import com.example.cafefidelidaqrdemo.utils.PerformanceMonitor;
import com.example.cafefidelidaqrdemo.offline.OfflineManager;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;
    private MainViewModel viewModel;
    private OfflineManager offlineManager;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Verificar que el usuario sea administrador
        authRepository = AuthRepository.getInstance();
        authRepository.setContext(this);
        
        if (!authRepository.isCurrentUserAdmin()) {
            // Si no es admin, redirigir al login
            Toast.makeText(this, "Acceso denegado. Solo administradores.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }
        
        // Configurar Data Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin_main);
        
        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        
        // Configurar interfaz de administrador
        setupAdminInterface();
        
        // Configurar botón de logout
        binding.btnLogout.setOnClickListener(v -> logout());
        
        // Inicializar OfflineManager y configurar sincronización automática
        initializeOfflineManager();
    }
    
    /**
     * Configura la interfaz específica para administradores
     */
    private void setupAdminInterface() {
        // Mostrar dashboard de admin
        FragmentAdminDashboard adminDashboard = new FragmentAdminDashboard();
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentFL, adminDashboard)
                .commit();
    }
    
    /**
     * Inicializa el OfflineManager y configura la sincronización automática
     */
    private void initializeOfflineManager() {
        offlineManager = OfflineManager.getInstance(this);
        configurarSincronizacionAutomatica();
    }
    
    /**
     * Configura la sincronización automática de datos
     */
    private void configurarSincronizacionAutomatica() {
        // Usar el método estático de SyncWorker para programar sincronización periódica
        com.example.cafefidelidaqrdemo.offline.SyncWorker.schedulePeriodicSync(this);
        
        // Iniciar sincronización inmediata si hay datos pendientes
        if (offlineManager != null) {
            offlineManager.iniciarSincronizacion();
        }
    }
    
    /**
     * Cierra la sesión del administrador
     */
    private void logout() {
        if (authRepository != null) {
            authRepository.logout();
        }
        redirectToLogin();
    }
    
    /**
     * Redirige al usuario a la pantalla de login
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, OpcionesLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Reportar estadísticas de rendimiento al cerrar la app
        PerformanceMonitor.reportStatistics();
    }
    
    @Override
    public void onBackPressed() {
        // Para administradores, el botón atrás cierra sesión
        super.onBackPressed();
        logout();
    }
}