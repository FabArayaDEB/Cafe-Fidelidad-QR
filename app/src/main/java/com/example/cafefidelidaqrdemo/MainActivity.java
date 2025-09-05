package com.example.cafefidelidaqrdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.cafefidelidaqrdemo.databinding.ActivityMainBinding;
import com.example.cafefidelidaqrdemo.fragment.FragmentQR;
import com.example.cafefidelidaqrdemo.fragment.FragmentPerfil;
import com.example.cafefidelidaqrdemo.fragment.FragmentPuntos;
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
    private FirebaseAuth firebaseAuth;
    private OfflineManager offlineManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            setLogin();
        }
        
        // Inicializar OfflineManager y configurar sincronización automática
        offlineManager = OfflineManager.getInstance(this);
        configurarSincronizacionAutomatica();
        
        verFragPerfil();
        binding.bottomNV.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.item_perfil) {
                    verFragPerfil();
                    return true;
                } else if (itemId == R.id.item_puntos) {
                    verFragPuntos();
                    return true;
                } else if (itemId == R.id.item_catalogo) {
                    verCatalogo();
                    return true;
                } else if (itemId == R.id.item_beneficios) {
                    verBeneficios();
                    return true;
                } else if (itemId == R.id.item_qr) {
                    verFragQR();
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