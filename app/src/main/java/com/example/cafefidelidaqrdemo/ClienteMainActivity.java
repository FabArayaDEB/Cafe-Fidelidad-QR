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
import com.example.cafefidelidaqrdemo.ui.cliente.FragmentProgresoFidelizacion;
import com.example.cafefidelidaqrdemo.fragments.FragmentSucursales;
import com.example.cafefidelidaqrdemo.viewmodels.MainViewModel;
import com.example.cafefidelidaqrdemo.fragments.FragmentPerfil;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;
// Imports simplificados - PerformanceMonitor y OfflineManager removidos
import com.google.android.material.navigation.NavigationBarView;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class ClienteMainActivity extends AppCompatActivity {

    private ActivityClienteMainBinding binding;
    private MainViewModel viewModel;
    // OfflineManager removido para simplificación
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

        // Botón de acceso rápido a sucursales en la toolbar
        if (binding.btnSucursales != null) {
            binding.btnSucursales.setOnClickListener(v -> abrirSucursales());
        }
        
        // Funcionalidad offline removida para simplificación
    }
    
    /**
     * Configura la interfaz específica para clientes
     */
    private void setupClienteInterface() {
        // Configurar navegación normal para clientes
        setupNavigation();
        
        // Mostrar perfil de cliente por defecto
        FragmentPerfil fragmentPerfil = new FragmentPerfil();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentFL, fragmentPerfil)
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
                } else if (itemId == R.id.item_sucursales) {
                    abrirSucursales();
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
        binding.tvTitulo.setText("Mis Sellos");
        // TODO: Implementar fragmento de sellos simplificado
        Toast.makeText(this, "Funcionalidad de sellos en desarrollo", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Muestra el fragmento de QR
     */
    private void verFragQR(){
        binding.tvTitulo.setText("Escanear QR");
        // TODO: Implementar fragmento de QR simplificado
        Toast.makeText(this, "Funcionalidad de QR en desarrollo", Toast.LENGTH_SHORT).show();
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
        FragmentProgresoFidelizacion fragment = new FragmentProgresoFidelizacion();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentFL.getId(), fragment, "FragmentPerfil");
        fragmentTransaction.commit();
    }

    /**
     * Abre el listado de sucursales
     */
    private void abrirSucursales() {
        viewModel.setToolbarTitle("Sucursales");
        FragmentSucursales fragment = new FragmentSucursales();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentFL.getId(), fragment, "FragmentSucursales");
        fragmentTransaction.commit();
    }
    
    // Métodos de sincronización offline removidos para simplificación
    
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
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Reportar estadísticas de rendimiento al cerrar la app
        // PerformanceMonitor removido para simplificación
    }
}
