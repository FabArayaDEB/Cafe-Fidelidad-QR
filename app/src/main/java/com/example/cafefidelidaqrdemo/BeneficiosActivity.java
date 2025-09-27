package com.example.cafefidelidaqrdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.adapters.BeneficioAdapter;
import com.example.cafefidelidaqrdemo.databinding.ActivityBeneficiosBinding;
import com.example.cafefidelidaqrdemo.managers.BeneficioManager;
import com.example.cafefidelidaqrdemo.models.Beneficio;
import com.example.cafefidelidaqrdemo.utils.SessionManager;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BeneficiosActivity extends AppCompatActivity implements BeneficioAdapter.OnBeneficioClickListener {
    private static final String TAG = "BeneficiosActivity";
    
    private ActivityBeneficiosBinding binding;
    private BeneficioAdapter beneficioAdapter;
    private BeneficioManager beneficioManager;
    private SessionManager sessionManager;
    
    private List<Beneficio> todosBeneficios;
    private List<Beneficio> beneficiosDisponibles;
    private List<Beneficio> beneficiosUsados;
    private List<Beneficio> beneficiosExpirados;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Verificar autenticación antes de mostrar contenido
        if (!isUserAuthenticated()) {
            redirectToLogin();
            return;
        }
        
        binding = ActivityBeneficiosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        initializeComponents();
        setupUI();
        loadBeneficios();
    }
    
    /**
     * Verifica si el usuario está autenticado
     */
    private boolean isUserAuthenticated() {
        AuthRepository authRepository = AuthRepository.getInstance();
        return authRepository.isUserLoggedIn();
    }
    
    /**
     * Redirige al usuario a la pantalla de login
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, OpcionesLoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void initializeComponents() {
        beneficioManager = new BeneficioManager(this);
        sessionManager = new SessionManager(this);
        
        todosBeneficios = new ArrayList<>();
        beneficiosDisponibles = new ArrayList<>();
        beneficiosUsados = new ArrayList<>();
        beneficiosExpirados = new ArrayList<>();
    }
    
    private void setupUI() {
        // Configurar toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mis Beneficios");
        }
        
        // Configurar RecyclerView
        binding.recyclerViewBeneficios.setLayoutManager(new LinearLayoutManager(this));
        beneficioAdapter = new BeneficioAdapter(this, beneficiosDisponibles, this);
        binding.recyclerViewBeneficios.setAdapter(beneficioAdapter);
        
        // Configurar tabs
        setupTabs();
        
        // Configurar botón de actualizar
        binding.btnActualizar.setOnClickListener(v -> {
            evaluarNuevosBeneficios();
            loadBeneficios();
        });
        
        // Configurar swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            loadBeneficios();
            binding.swipeRefreshLayout.setRefreshing(false);
        });
    }
    
    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Disponibles"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Usados"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Expirados"));
        
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        mostrarBeneficiosDisponibles();
                        break;
                    case 1:
                        mostrarBeneficiosUsados();
                        break;
                    case 2:
                        mostrarBeneficiosExpirados();
                        break;
                }
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void loadBeneficios() {
        try {
            String clienteId = sessionManager.getUserId();
            if (clienteId == null) {
                Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // TODO: Obtener beneficios reales desde Firebase
            // Por ahora usar lista vacía para que compile
            List<Beneficio> todosBeneficiosTemp = new ArrayList<>();
            todosBeneficios = beneficioManager.obtenerBeneficiosDisponibles(clienteId, todosBeneficiosTemp);
            
            // Clasificar beneficios por estado
            clasificarBeneficios();
            
            // Mostrar beneficios disponibles por defecto
            mostrarBeneficiosDisponibles();
            
            // Actualizar contadores en las tabs
            actualizarContadoresTabs();
            
            // Mostrar/ocultar mensaje vacío
            actualizarVisibilidadMensajeVacio();
            
        } catch (Exception e) {
            Log.e(TAG, "Error cargando beneficios", e);
            Toast.makeText(this, "Error cargando beneficios", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void clasificarBeneficios() {
        beneficiosDisponibles.clear();
        beneficiosUsados.clear();
        beneficiosExpirados.clear();
        
        for (Beneficio beneficio : todosBeneficios) {
            switch (beneficio.getEstado()) {
                case "disponible":
                    if (beneficio.esValido()) {
                        beneficiosDisponibles.add(beneficio);
                    } else {
                        beneficiosExpirados.add(beneficio);
                    }
                    break;
                case "usado":
                    beneficiosUsados.add(beneficio);
                    break;
                case "expirado":
                    beneficiosExpirados.add(beneficio);
                    break;
                default:
                    break;
            }
        }
    }
    
    private void mostrarBeneficiosDisponibles() {
        beneficioAdapter.updateBeneficios(beneficiosDisponibles);
        binding.btnActualizar.setVisibility(View.VISIBLE);
    }
    
    private void mostrarBeneficiosUsados() {
        beneficioAdapter.updateBeneficios(beneficiosUsados);
        binding.btnActualizar.setVisibility(View.GONE);
    }
    
    private void mostrarBeneficiosExpirados() {
        beneficioAdapter.updateBeneficios(beneficiosExpirados);
        binding.btnActualizar.setVisibility(View.GONE);
    }
    
    private void actualizarContadoresTabs() {
        TabLayout.Tab tabDisponibles = binding.tabLayout.getTabAt(0);
        TabLayout.Tab tabUsados = binding.tabLayout.getTabAt(1);
        TabLayout.Tab tabExpirados = binding.tabLayout.getTabAt(2);
        
        if (tabDisponibles != null) {
            tabDisponibles.setText("Disponibles (" + beneficiosDisponibles.size() + ")");
        }
        if (tabUsados != null) {
            tabUsados.setText("Usados (" + beneficiosUsados.size() + ")");
        }
        if (tabExpirados != null) {
            tabExpirados.setText("Expirados (" + beneficiosExpirados.size() + ")");
        }
    }
    
    private void actualizarVisibilidadMensajeVacio() {
        boolean hayBeneficios = !todosBeneficios.isEmpty();
        binding.recyclerViewBeneficios.setVisibility(hayBeneficios ? View.VISIBLE : View.GONE);
        binding.layoutMensajeVacio.setVisibility(hayBeneficios ? View.GONE : View.VISIBLE);
        
        if (!hayBeneficios) {
            binding.tvMensajeVacio.setText("No tienes beneficios disponibles");
            binding.tvSubmensajeVacio.setText("Sigue visitando nuestras cafeterías para obtener beneficios exclusivos");
        }
    }
    
    private void evaluarNuevosBeneficios() {
        try {
            String clienteId = sessionManager.getUserId();
            if (clienteId != null) {
                // TODO: Implementar evaluación de beneficios automáticos
                // Requiere lista de visitas del cliente
                // List<Beneficio> nuevosBeneficios = beneficioManager.evaluarBeneficiosAutomaticos(clienteId, visitasRecientes);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error evaluando nuevos beneficios", e);
        }
    }
    
    private void mostrarDialogoNuevosBeneficios(List<Beneficio> nuevosBeneficios) {
        StringBuilder mensaje = new StringBuilder("¡Nuevos beneficios obtenidos!\n\n");
        
        for (Beneficio beneficio : nuevosBeneficios) {
            mensaje.append("• ").append(beneficio.getNombre()).append("\n");
            mensaje.append("  ").append(beneficio.getDescripcion()).append("\n\n");
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("¡Felicidades!")
            .setMessage(mensaje.toString())
            .setPositiveButton("¡Genial!", null)
            .setIcon(R.drawable.ic_star)
            .show();
    }
    
    public void onBeneficioClick(Beneficio beneficio) {
        // Mostrar detalles del beneficio
        // TODO: Implementar DetalleBeneficioActivity
        Toast.makeText(this, "Detalles de: " + beneficio.getNombre(), Toast.LENGTH_SHORT).show();
    }
    
    public void onUsarBeneficioClick(Beneficio beneficio) {
        if ("disponible".equals(beneficio.getEstado()) && beneficio.esValido()) {
            // TODO: Implementar CanjeBeneficioActivity
            Toast.makeText(this, "Usando beneficio: " + beneficio.getNombre(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Este beneficio no está disponible", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Beneficio canjeado exitosamente, recargar lista
            loadBeneficios();
            Toast.makeText(this, "Beneficio canjeado exitosamente", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recargar beneficios al volver a la actividad
        loadBeneficios();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}