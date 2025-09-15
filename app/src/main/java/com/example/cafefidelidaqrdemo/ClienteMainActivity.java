package com.example.cafefidelidaqrdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.cafefidelidaqrdemo.databinding.ActivityClienteMainBinding;
import com.example.cafefidelidaqrdemo.viewmodels.MainViewModel;
import com.example.cafefidelidaqrdemo.fragments.FragmentQR;
import com.example.cafefidelidaqrdemo.fragments.FragmentPerfil;
import com.example.cafefidelidaqrdemo.fragments.FragmentPuntos;
import com.example.cafefidelidaqrdemo.ui.cliente.FragmentTableroCliente;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;
import com.example.cafefidelidaqrdemo.utils.PerformanceMonitor;
import com.example.cafefidelidaqrdemo.offline.OfflineManager;
import com.google.android.material.navigation.NavigationBarView;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class ClienteMainActivity extends AppCompatActivity {

    private ActivityClienteMainBinding binding;
    private MainViewModel viewModel;
    private OfflineManager offlineManager;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Verificar que el usuario sea cliente
        authRepository = AuthRepository.getInstance();
        authRepository.setContext(this);
        
        if (!authRepository.isCurrentUserCliente()) {
            // Si no es cliente, redirigir al login
            Toast.makeText(this, "Acceso denegado. Solo clientes.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }
        
        // Configurar Data Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cliente_main);
        
        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        
        // Configurar interfaz de cliente
        setupClienteInterface();
        
        // Configurar botón de logout
        binding.btnLogout.setOnClickListener(v -> logout());
        
        // Inicializar OfflineManager y configurar sincronización automática
        initializeOfflineManager();
    }
    
    /**
     * Configura la interfaz específica para clientes
     */
    private void setupClienteInterface() {
        // Configurar navegación normal para clientes
        setupNavigation();
        
        // Mostrar tablero de cliente por defecto
        FragmentTableroCliente tableroCliente = new FragmentTableroCliente();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentFL, tableroCliente)
                .commit();
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
    
    /**
     * Muestra el fragmento de perfil
     */
    private void verFragPerfil(){
        binding.tvTitulo.setText("Mi Perfil");

        FragmentPerfil fragment = new FragmentPerfil();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentFL.getId(), fragment, "FragmentPerfil");
        fragmentTransaction.commit();
    }
    
    /**
     * Muestra el fragmento de puntos
     */
    private void verFragPuntos(){
        binding.tvTitulo.setText("Mis Puntos");

        FragmentPuntos fragment = new FragmentPuntos();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentFL.getId(), fragment, "FragmentPuntos");
        fragmentTransaction.commit();
    }
    
    /**
     * Muestra el fragmento de QR
     */
    private void verFragQR(){
        binding.tvTitulo.setText("Escanear QR");

        FragmentQR fragment = new FragmentQR();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentFL.getId(), fragment, "FragmentQR");
        fragmentTransaction.commit();
    }
    
    /**
     * Abre la actividad de catálogo
     */
    private void verCatalogo(){
        Intent intent = new Intent(this, CatalogoActivity.class);
        startActivity(intent);
    }
    
    /**
     * Abre la actividad de beneficios
     */
    private void verBeneficios(){
        Intent intent = new Intent(this, BeneficiosActivity.class);
        startActivity(intent);
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
        // Configurar sincronización periódica cada 15 minutos
        PeriodicWorkRequest syncWorkRequest = new PeriodicWorkRequest.Builder(
                com.example.cafefidelidaqrdemo.offline.SyncWorker.class,
                15, TimeUnit.MINUTES)
                .build();
        
        WorkManager.getInstance(this).enqueue(syncWorkRequest);
        
        // Iniciar sincronización inmediata si hay datos pendientes
        if (offlineManager != null) {
            offlineManager.iniciarSincronizacion();
        }
    }
    
    /**
     * Cierra la sesión del cliente
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
}