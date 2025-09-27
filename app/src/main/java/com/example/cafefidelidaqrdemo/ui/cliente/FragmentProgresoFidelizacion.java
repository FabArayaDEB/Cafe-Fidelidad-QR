package com.example.cafefidelidaqrdemo.ui.cliente;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.adapters.BeneficiosDisponiblesAdapter;
import com.example.cafefidelidaqrdemo.models.Beneficio;
import com.example.cafefidelidaqrdemo.ui.dialogs.BeneficioDetailsDialogFragment;
import com.example.cafefidelidaqrdemo.viewmodels.MisBeneficiosViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment simplificado para mostrar beneficios disponibles del cliente
 */
public class FragmentProgresoFidelizacion extends Fragment {
    
    private static final String TAG = "FragmentProgresoFidelizacion";
    
    // Views principales
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerBeneficiosDisponibles;
    private TextView textBeneficiosDisponiblesTitle;
    private View emptyStateView;
    
    // Adapters
    private BeneficiosDisponiblesAdapter beneficiosDisponiblesAdapter;
    
    // ViewModels
    private MisBeneficiosViewModel beneficioViewModel;
    
    public static FragmentProgresoFidelizacion newInstance() {
        return new FragmentProgresoFidelizacion();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beneficioViewModel = new ViewModelProvider(this).get(MisBeneficiosViewModel.class);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progreso_fidelizacion, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerViews();
        setupSwipeRefresh();
        setupObservers();
        
        // Cargar beneficios disponibles
        beneficioViewModel.refreshBeneficios();
    }
    
    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerBeneficiosDisponibles = view.findViewById(R.id.recyclerViewBeneficiosDisponibles);
        // textBeneficiosDisponiblesTitle = view.findViewById(R.id.textBeneficiosDisponiblesTitle); // ID no existe en layout
        emptyStateView = view.findViewById(R.id.layoutEmpty);
    }
    
    private void setupRecyclerViews() {
        // Setup beneficios disponibles
        beneficiosDisponiblesAdapter = new BeneficiosDisponiblesAdapter(new ArrayList<>(), this::onBeneficioClick);
        
        recyclerBeneficiosDisponibles.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerBeneficiosDisponibles.setAdapter(beneficiosDisponiblesAdapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            beneficioViewModel.refreshBeneficios();
        });
    }
    
    private void setupObservers() {
        // Observar beneficios disponibles
        beneficioViewModel.getBeneficiosDisponibles().observe(getViewLifecycleOwner(), this::updateBeneficiosDisponibles);
        
        // Observar estado de carga
        beneficioViewModel.getIsRefreshing().observe(getViewLifecycleOwner(), isRefreshing -> {
            swipeRefreshLayout.setRefreshing(isRefreshing);
        });
        
        // Observar errores
        beneficioViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                // Mostrar error (simplificado)
                showEmptyState();
            }
        });
    }
    
    private void updateBeneficiosDisponibles(List<Beneficio> beneficios) {
        if (beneficios != null && !beneficios.isEmpty()) {
            beneficiosDisponiblesAdapter.updateBeneficios(beneficios);
            recyclerBeneficiosDisponibles.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        } else {
            showEmptyState();
        }
    }
    
    private void showEmptyState() {
        recyclerBeneficiosDisponibles.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
    }
    
    private void onBeneficioClick(Beneficio beneficio) {
        BeneficioDetailsDialogFragment dialog = BeneficioDetailsDialogFragment.newInstance(beneficio);
        dialog.show(getParentFragmentManager(), "BeneficioDetailsDialog");
    }
}