package com.example.cafefidelidaqrdemo.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.adapter.BeneficiosAdminAdapter;
import com.example.cafefidelidaqrdemo.model.Beneficio;
import com.example.cafefidelidaqrdemo.ui.dialogs.BeneficioDialogFragment;
import com.example.cafefidelidaqrdemo.ui.dialogs.BeneficioDetailsDialogFragment;
import com.example.cafefidelidaqrdemo.viewmodel.BeneficiosAdminViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment para gestión de beneficios por administradores (CU-04.1)
 * Permite crear, editar, activar/desactivar y eliminar beneficios
 */
public class FragmentBeneficiosAdmin extends Fragment implements BeneficiosAdminAdapter.OnBeneficioActionListener {
    
    private static final String TAG = "FragmentBeneficiosAdmin";
    
    // Views
    private RecyclerView recyclerViewBeneficios;
    private FloatingActionButton fabNuevoBeneficio;
    private LinearProgressIndicator progressIndicator;
    private View emptyStateView;
    
    // Components
    private BeneficiosAdminViewModel viewModel;
    private BeneficiosAdminAdapter adapter;
    
    public static FragmentBeneficiosAdmin newInstance() {
        return new FragmentBeneficiosAdmin();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BeneficiosAdminViewModel.class);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_beneficios_admin, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
        
        // Cargar beneficios
        viewModel.loadBeneficios();
    }
    
    private void initViews(View view) {
        recyclerViewBeneficios = view.findViewById(R.id.recyclerViewBeneficios);
        // fabNuevoBeneficio = view.findViewById(R.id.fabNuevoBeneficio); // TODO: Agregar este ID al layout
        progressIndicator = view.findViewById(R.id.progressIndicator);
        emptyStateView = view.findViewById(R.id.emptyStateView);
    }
    
    private void setupRecyclerView() {
        adapter = new BeneficiosAdminAdapter(new ArrayList<>(), this);
        recyclerViewBeneficios.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewBeneficios.setAdapter(adapter);
    }
    
    private void setupObservers() {
        // Observar lista de beneficios
        viewModel.getBeneficios().observe(getViewLifecycleOwner(), beneficios -> {
            if (beneficios != null) {
                adapter.updateBeneficios(beneficios);
                updateEmptyState(beneficios.isEmpty());
            }
        });
        
        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                fabNuevoBeneficio.setEnabled(!isLoading);
            }
        });
        
        // Observar errores
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });
        
        // Observar resultado de operaciones
        viewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.isSuccess()) {
                    showSuccess(result.getMessage());
                } else {
                    showError(result.getMessage());
                }
            }
        });
    }
    
    private void setupClickListeners() {
        fabNuevoBeneficio.setOnClickListener(v -> {
            // Abrir dialog para crear nuevo beneficio
            showCreateBeneficioDialog();
        });
    }
    
    private void updateEmptyState(boolean isEmpty) {
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewBeneficios.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
    
    private void showCreateBeneficioDialog() {
        BeneficioDialogFragment dialog = BeneficioDialogFragment.newInstance(null);
        dialog.setOnBeneficioSavedListener(beneficio -> {
            viewModel.createBeneficio(beneficio);
        });
        dialog.show(getParentFragmentManager(), "CreateBeneficioDialog");
    }
    
    private void showEditBeneficioDialog(Beneficio beneficio) {
        BeneficioDialogFragment dialog = BeneficioDialogFragment.newInstance(beneficio);
        dialog.setOnBeneficioSavedListener(editedBeneficio -> {
            viewModel.updateBeneficio(editedBeneficio);
        });
        dialog.show(getParentFragmentManager(), "EditBeneficioDialog");
    }
    
    private void showDeleteConfirmationDialog(Beneficio beneficio) {
        new AlertDialog.Builder(getContext())
            .setTitle("Eliminar Beneficio")
            .setMessage("¿Está seguro que desea eliminar el beneficio '" + beneficio.getNombre() + "'?")
            .setPositiveButton("Eliminar", (dialog, which) -> {
                viewModel.deleteBeneficio(beneficio.getId());
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void showToggleActiveDialog(Beneficio beneficio) {
        String action = beneficio.isActivo() ? "desactivar" : "activar";
        String title = beneficio.isActivo() ? "Desactivar Beneficio" : "Activar Beneficio";
        
        new AlertDialog.Builder(getContext())
            .setTitle(title)
            .setMessage("¿Está seguro que desea " + action + " el beneficio '" + beneficio.getNombre() + "'?")
            .setPositiveButton("Confirmar", (dialog, which) -> {
                viewModel.toggleBeneficioActive(beneficio);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_LONG).show();
        }
    }
    
    private void showSuccess(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    // Implementación de BeneficiosAdminAdapter.OnBeneficioActionListener
    @Override
    public void onEditarBeneficio(Beneficio beneficio) {
        showEditBeneficioDialog(beneficio);
    }
    
    @Override
    public void onEliminarBeneficio(Beneficio beneficio) {
        showDeleteConfirmationDialog(beneficio);
    }
    
    @Override
    public void onToggleActiveBeneficio(Beneficio beneficio) {
        showToggleActiveDialog(beneficio);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refrescar datos al volver al fragment
        viewModel.refreshBeneficios();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias
        recyclerViewBeneficios = null;
        fabNuevoBeneficio = null;
        progressIndicator = null;
        emptyStateView = null;
        adapter = null;
    }
}