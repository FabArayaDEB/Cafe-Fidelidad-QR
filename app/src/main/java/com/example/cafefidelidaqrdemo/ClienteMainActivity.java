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
import com.example.cafefidelidaqrdemo.fragments.FragmentPerfil;
import com.example.cafefidelidaqrdemo.fragments.FragmentProductos;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;
import com.google.android.material.navigation.NavigationBarView;
import com.example.cafefidelidaqrdemo.fragments.FragmentSucursales;

public class ClienteMainActivity extends AppCompatActivity {

    private ActivityClienteMainBinding binding;
    private MainViewModel viewModel;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar que el usuario sea cliente
        authRepository = AuthRepository.getInstance();
        authRepository.setContext(this);

        if (!authRepository.isCurrentUserCliente()) {
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
    }

    private void setupClienteInterface() {
        setupNavigation();

        // Mostrar perfil de cliente por defecto
        verFragPerfil();
    }

    private void setupNavigation() {
        binding.bottomNV.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.item_perfil) {
                    verFragPerfil();
                    return true;
                } else if (itemId == R.id.item_sucursales) {
                    verSucursales();
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

    private void verFragPerfil(){
        viewModel.setToolbarTitle("Mi Perfil");
        binding.tvTitulo.setText("Mi Perfil");

        FragmentPerfil fragment = new FragmentPerfil();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentFL.getId(), fragment, "FragmentPerfil");
        fragmentTransaction.commit();
    }

    private void verFragPuntos(){
        viewModel.setToolbarTitle("Mis Puntos");
        binding.tvTitulo.setText("Mis Puntos");
        // TODO: Implementar fragmento de puntos
        Toast.makeText(this, "Funcionalidad de puntos en desarrollo", Toast.LENGTH_SHORT).show();
    }

    private void verFragQR(){
        viewModel.setToolbarTitle("Escanear QR");
        binding.tvTitulo.setText("Escanear QR");
        // TODO: Implementar fragmento de QR
        Toast.makeText(this, "Funcionalidad de QR en desarrollo", Toast.LENGTH_SHORT).show();
    }

    private void verCatalogo(){
        viewModel.setToolbarTitle("Catálogo");
        binding.tvTitulo.setText("Catálogo");

        // En lugar de Intent, cargamos el Fragment
        FragmentProductos fragment = new FragmentProductos();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentFL.getId(), fragment, "FragmentProductos");
        fragmentTransaction.commit();
    }

    private void verSucursales() {
        viewModel.setToolbarTitle("Nuestras Sucursales");
        binding.tvTitulo.setText("Sucursales");

        FragmentSucursales fragment = new FragmentSucursales();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentFL.getId(), fragment, "FragmentSucursales");
        fragmentTransaction.commit();
    }

    private void verBeneficios(){
        Intent intent = new Intent(this, BeneficiosActivity.class);
        startActivity(intent);
    }

    private void logout() {
        if (authRepository != null) {
            authRepository.logout();
        }
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, OpcionesLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}