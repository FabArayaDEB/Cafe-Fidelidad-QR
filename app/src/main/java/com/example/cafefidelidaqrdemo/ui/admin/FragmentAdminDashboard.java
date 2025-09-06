package com.example.cafefidelidaqrdemo.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.fragment.app.FragmentTransaction;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.databinding.FragmentAdminDashboardBinding;
import com.example.cafefidelidaqrdemo.ui.admin.viewmodels.AdminDashboardViewModel;
import com.example.cafefidelidaqrdemo.ui.admin.FragmentProductosAdmin;
import com.example.cafefidelidaqrdemo.ui.admin.FragmentBeneficiosAdmin;
import com.example.cafefidelidaqrdemo.ui.admin.FragmentSucursalesAdmin;

/**
 * Fragment principal del módulo de administración
 * Proporciona navegación a las diferentes secciones administrativas
 */
public class FragmentAdminDashboard extends Fragment {
    
    private FragmentAdminDashboardBinding binding;
    private AdminDashboardViewModel viewModel;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        setupUI();
        setupObservers();
        setupClickListeners();
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);
    }
    
    private void setupUI() {
        // Configurar título
        binding.textViewTitulo.setText("Panel de Administración");
        
        // Configurar subtítulo con información del usuario
        binding.textViewSubtitulo.setText("Gestión de catálogo y sucursales");
        
        // Configurar iconos y textos de las tarjetas
        setupCardProductos();
        setupCardBeneficios();
        setupCardSucursales();
        setupCardEstadisticas();
        setupCardConfiguracion();
    }
    
    private void setupCardProductos() {
        binding.cardProductos.setOnClickListener(v -> navegarAProductos());
        binding.iconProductos.setImageResource(R.drawable.ic_shopping_cart);
        binding.textTituloProductos.setText("Productos");
        binding.textDescripcionProductos.setText("Gestionar catálogo de productos");
    }
    
    private void setupCardBeneficios() {
        binding.cardBeneficios.setOnClickListener(v -> navegarABeneficios());
        binding.iconBeneficios.setImageResource(R.drawable.ic_gift);
        binding.textTituloBeneficios.setText("Beneficios");
        binding.textDescripcionBeneficios.setText("Administrar promociones y ofertas");
    }
    
    private void setupCardSucursales() {
        binding.cardSucursales.setOnClickListener(v -> navegarASucursales());
        binding.iconSucursales.setImageResource(R.drawable.ic_location);
        binding.textTituloSucursales.setText("Sucursales");
        binding.textDescripcionSucursales.setText("Gestionar ubicaciones y horarios");
    }
    
    private void setupCardEstadisticas() {
        binding.cardEstadisticas.setOnClickListener(v -> navegarAEstadisticas());
        binding.iconEstadisticas.setImageResource(R.drawable.ic_analytics);
        binding.textTituloEstadisticas.setText("Estadísticas");
        binding.textDescripcionEstadisticas.setText("Ver reportes y métricas");
    }
    
    private void setupCardConfiguracion() {
        binding.cardConfiguracion.setOnClickListener(v -> navegarAConfiguracion());
        binding.iconConfiguracion.setImageResource(R.drawable.ic_settings);
        binding.textTituloConfiguracion.setText("Configuración");
        binding.textDescripcionConfiguracion.setText("Ajustes del sistema");
    }
    
    private void setupObservers() {
        // Observar estadísticas generales
        viewModel.getCountProductosActivos().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.textContadorProductos.setText(String.valueOf(count));
                binding.textContadorProductos.setVisibility(View.VISIBLE);
            }
        });
        
        viewModel.getCountBeneficiosActivos().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.textContadorBeneficios.setText(String.valueOf(count));
                binding.textContadorBeneficios.setVisibility(View.VISIBLE);
            }
        });
        
        viewModel.getCountSucursalesActivas().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.textContadorSucursales.setText(String.valueOf(count));
                binding.textContadorSucursales.setVisibility(View.VISIBLE);
            }
        });
        
        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                
                // Deshabilitar tarjetas durante la carga
                setCardsEnabled(!isLoading);
            }
        });
        
        // Observar mensajes de error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });
        
        // Observar mensajes de éxito
        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearSuccessMessage();
            }
        });
        
        // Observar actividad reciente
        viewModel.getRecentActivities().observe(getViewLifecycleOwner(), actividades -> {
            if (actividades != null && !actividades.isEmpty()) {
                mostrarActividadReciente(actividades);
            }
        });
    }
    
    private void setupClickListeners() {
        // Botón de actualizar estadísticas
        binding.buttonActualizar.setOnClickListener(v -> {
            viewModel.actualizarEstadisticas();
            Toast.makeText(getContext(), "Actualizando estadísticas...", Toast.LENGTH_SHORT).show();
        });
        
        // Botón de sincronización
        binding.buttonSincronizar.setOnClickListener(v -> {
            viewModel.sincronizarDatos();
            Toast.makeText(getContext(), "Sincronizando con servidor...", Toast.LENGTH_SHORT).show();
        });
        
        // Swipe to refresh
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.actualizarTodo();
            binding.swipeRefreshLayout.setRefreshing(false);
        });
    }
    
    private void setCardsEnabled(boolean enabled) {
        binding.cardProductos.setEnabled(enabled);
        binding.cardBeneficios.setEnabled(enabled);
        binding.cardSucursales.setEnabled(enabled);
        binding.cardEstadisticas.setEnabled(enabled);
        binding.cardConfiguracion.setEnabled(enabled);
        
        float alpha = enabled ? 1.0f : 0.6f;
        binding.cardProductos.setAlpha(alpha);
        binding.cardBeneficios.setAlpha(alpha);
        binding.cardSucursales.setAlpha(alpha);
        binding.cardEstadisticas.setAlpha(alpha);
        binding.cardConfiguracion.setAlpha(alpha);
    }
    
    private void mostrarActividadReciente(java.util.List<AdminDashboardViewModel.RecentActivity> actividades) {
        // Mostrar las últimas 3 actividades
        StringBuilder sb = new StringBuilder();
        int count = Math.min(3, actividades.size());
        
        for (int i = 0; i < count; i++) {
            AdminDashboardViewModel.RecentActivity activity = actividades.get(i);
            sb.append("• ").append(activity.descripcion);
            if (i < count - 1) {
                sb.append("\n");
            }
        }
        
        // TODO: Corregir binding.text - no existe en el layout
        // binding.text.setText(sb.toString());
        binding.layoutActividadReciente.setVisibility(View.VISIBLE);
    }
    
    // ========== MÉTODOS DE NAVEGACIÓN ==========
    
    private void navegarAProductos() {
        try {
            FragmentProductosAdmin fragment = new FragmentProductosAdmin();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            // TODO: Implementar fragment_container en el layout
            // transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al navegar a productos", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navegarABeneficios() {
        try {
            FragmentBeneficiosAdmin fragment = new FragmentBeneficiosAdmin();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            // TODO: Implementar fragment_container en el layout
            // transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al navegar a beneficios", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navegarASucursales() {
        try {
            FragmentSucursalesAdmin fragment = new FragmentSucursalesAdmin();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            // TODO: Implementar fragment_container en el layout
            // transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al navegar a sucursales", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navegarAEstadisticas() {
        try {
            // TODO: Implementar FragmentEstadisticasAdmin cuando esté disponible
            Toast.makeText(getContext(), "Estadísticas - Próximamente", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al navegar a estadísticas", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navegarAConfiguracion() {
        try {
            // TODO: Implementar FragmentConfiguracionAdmin cuando esté disponible
            Toast.makeText(getContext(), "Configuración - Próximamente", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al navegar a configuración", Toast.LENGTH_SHORT).show();
        }
    }
    
    // ========== MÉTODOS DE CICLO DE VIDA ==========
    
    @Override
    public void onResume() {
        super.onResume();
        // Actualizar estadísticas al volver al dashboard
        viewModel.actualizarEstadisticas();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    
    // ========== MÉTODOS PÚBLICOS ==========
    
    /**
     * Método para mostrar notificaciones desde otros fragments
     */
    public void mostrarNotificacion(String mensaje, boolean esError) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, 
                    esError ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Método para forzar actualización de estadísticas
     */
    public void actualizarEstadisticas() {
        if (viewModel != null) {
            viewModel.actualizarEstadisticas();
        }
    }
    
    /**
     * Método para verificar si hay cambios pendientes de sincronización
     */
    public void verificarSincronizacion() {
        if (viewModel != null) {
            viewModel.verificarCambiosPendientes();
        }
    }
}