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
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private OfflineManager offlineManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configurar Data Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        
        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        
        // Configurar observadores
        setupObservers();
        
        // Inicializar OfflineManager y configurar sincronización automática
        offlineManager = OfflineManager.getInstance(this);
        configurarSincronizacionAutomatica();
        
        // Configurar navegación
        setupNavigation();
        
        // Verificar autenticación
        viewModel.checkAuthenticationStatus();
    }
    
    /**
     * Configura los observadores del ViewModel
     */
    private void setupObservers() {
        // Observar estado de autenticación
        viewModel.getIsAuthenticatedLiveData().observe(this, isAuthenticated -> {
            if (isAuthenticated != null && !isAuthenticated) {
                // Redirigir a LoginActivity si no está autenticado
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        
        // Observar errores
        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                // Mostrar error al usuario
                // TODO: Implementar manejo de errores
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Reportar estadísticas de rendimiento al cerrar la app
        PerformanceMonitor.reportStatistics();
    }
}