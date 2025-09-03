package com.example.cafefidelidadqr;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cafefidelidadqr.databinding.ActivityMainBinding;
import com.example.cafefidelidadqr.fragment.FragmentPerfil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Verificar si el usuario está logueado
        checkUserAuthentication();
        
        // Configurar navegación inferior
        setupBottomNavigation();
        
        // Cargar fragmento inicial (perfil)
        if (savedInstanceState == null) {
            loadFragment(new FragmentPerfil());
        }


    }

    private void checkUserAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Usuario no logueado, redirigir a OpcionesLoginActivity
            Intent intent = new Intent(MainActivity.this, OpcionesLoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
    
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                
                if (item.getItemId() == R.id.item_perfil) {
                    selectedFragment = new FragmentPerfil();
                }
                // Aquí se pueden agregar más fragmentos en el futuro
                
                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });
    }
    
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Verificar autenticación cada vez que la actividad se inicia
        checkUserAuthentication();
    }
}