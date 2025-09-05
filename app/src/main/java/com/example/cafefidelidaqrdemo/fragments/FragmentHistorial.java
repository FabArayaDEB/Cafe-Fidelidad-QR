package com.example.cafefidelidaqrdemo.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.adapters.HistorialAdapter;
import com.example.cafefidelidaqrdemo.models.HistorialItem;
import com.example.cafefidelidaqrdemo.viewmodels.HistorialViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment para ver el historial de visitas y canjes (CU-02.2)
 */
public class FragmentHistorial extends Fragment {
    
    // Views
    private RecyclerView recyclerView;
    private HistorialAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvSyncStatus, tvFilterInfo;
    private ChipGroup chipGroupFiltros;
    private Chip chipTodos, chipVisitas, chipCanjes, chipPendientes, chipEnviados;
    private Button btnFiltroFecha;
    private FloatingActionButton fabScrollTop;
    
    // ViewModel
    private HistorialViewModel viewModel;
    
    // Estado de filtros
    private Date fechaInicio = null;
    private Date fechaFin = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    // Paginación
    private boolean isLoading = false;
    private boolean isLastPage = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historial, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initViewModel();
        setupRecyclerView();
        setupListeners();
        loadInitialData();
    }
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_historial);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        tvSyncStatus = view.findViewById(R.id.tv_sync_status);
        tvFilterInfo = view.findViewById(R.id.tv_filter_info);
        
        // Filtros
        chipGroupFiltros = view.findViewById(R.id.chip_group_filtros);
        chipTodos = view.findViewById(R.id.chip_todos);
        chipVisitas = view.findViewById(R.id.chip_visitas);
        chipCanjes = view.findViewById(R.id.chip_canjes);
        chipPendientes = view.findViewById(R.id.chip_pendientes);
        chipEnviados = view.findViewById(R.id.chip_enviados);
        btnFiltroFecha = view.findViewById(R.id.btn_filtro_fecha);
        
        fabScrollTop = view.findViewById(R.id.fab_scroll_top);
        
        // Estado inicial de filtros
        chipTodos.setChecked(true);
        updateFilterInfo();
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(HistorialViewModel.class);
        
        // Observar lista de historial
        viewModel.getHistorialItems().observe(getViewLifecycleOwner(), this::updateHistorialList);
        
        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            isLoading = loading;
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            swipeRefreshLayout.setRefreshing(loading);
        });
        
        // Observar errores
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
        
        // Observar estado de sincronización
        viewModel.getSyncStatus().observe(getViewLifecycleOwner(), this::updateSyncStatus);
        
        // Observar estado de conectividad
        viewModel.getNetworkStatus().observe(getViewLifecycleOwner(), this::updateNetworkStatus);
    }
    
    private void setupRecyclerView() {
        adapter = new HistorialAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        // Configurar paginación
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    
                    // Cargar más datos cuando se acerque al final
                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                            loadMoreData();
                        }
                    }
                    
                    // Mostrar/ocultar FAB de scroll
                    if (firstVisibleItemPosition > 10) {
                        fabScrollTop.show();
                    } else {
                        fabScrollTop.hide();
                    }
                }
            }
        });
    }
    
    private void setupListeners() {
        // Pull to refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        
        // Filtros de tipo
        chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> {
            applyFilters();
        });
        
        // Filtro de fecha
        btnFiltroFecha.setOnClickListener(v -> showDateRangeDialog());
        
        // Scroll to top
        fabScrollTop.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));
        
        // Click en items del historial
        adapter.setOnItemClickListener(this::onHistorialItemClick);
    }
    
    private void loadInitialData() {
        String clienteId = getClienteId();
        if (clienteId != null) {
            viewModel.loadHistorial(clienteId);
        }
    }
    
    private String getClienteId() {
        // Implementar lógica para obtener ID del cliente actual
        return "cliente_actual_id"; // Placeholder
    }
    
    private void updateHistorialList(List<HistorialItem> items) {
        if (items != null) {
            adapter.updateItems(items);
            
            // Mostrar mensaje si está vacío
            if (items.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setText(getEmptyMessage());
            } else {
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
            
            // Actualizar estado de paginación
            isLastPage = items.size() < HistorialViewModel.PAGE_SIZE;
        }
    }
    
    private String getEmptyMessage() {
        if (hasActiveFilters()) {
            return "No se encontraron registros con los filtros aplicados";
        } else {
            return "Aún no tienes visitas o canjes registrados";
        }
    }
    
    private boolean hasActiveFilters() {
        return !chipTodos.isChecked() || fechaInicio != null || fechaFin != null;
    }
    
    private void updateSyncStatus(Boolean isSynced) {
        if (isSynced != null) {
            if (isSynced) {
                tvSyncStatus.setText("✓ Datos sincronizados");
                tvSyncStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvSyncStatus.setText("⚠ Datos pendientes de sincronización");
                tvSyncStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            }
        }
    }
    
    private void updateNetworkStatus(Boolean isConnected) {
        if (isConnected != null && !isConnected) {
            Toast.makeText(getContext(), "Sin conexión - Mostrando datos locales", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void refreshData() {
        String clienteId = getClienteId();
        if (clienteId != null) {
            viewModel.refreshHistorial(clienteId);
        }
    }
    
    private void loadMoreData() {
        String clienteId = getClienteId();
        if (clienteId != null) {
            viewModel.loadMoreHistorial(clienteId);
        }
    }
    
    private void applyFilters() {
        HistorialViewModel.FiltroTipo filtroTipo = HistorialViewModel.FiltroTipo.TODOS;
        HistorialViewModel.FiltroEstado filtroEstado = HistorialViewModel.FiltroEstado.TODOS;
        
        // Determinar filtro de tipo
        if (chipVisitas.isChecked()) {
            filtroTipo = HistorialViewModel.FiltroTipo.VISITAS;
        } else if (chipCanjes.isChecked()) {
            filtroTipo = HistorialViewModel.FiltroTipo.CANJES;
        }
        
        // Determinar filtro de estado
        if (chipPendientes.isChecked()) {
            filtroEstado = HistorialViewModel.FiltroEstado.PENDIENTES;
        } else if (chipEnviados.isChecked()) {
            filtroEstado = HistorialViewModel.FiltroEstado.ENVIADOS;
        }
        
        viewModel.applyFilters(filtroTipo, filtroEstado, fechaInicio, fechaFin);
        updateFilterInfo();
    }
    
    private void updateFilterInfo() {
        StringBuilder info = new StringBuilder();
        
        // Tipo
        if (chipVisitas.isChecked()) {
            info.append("Visitas");
        } else if (chipCanjes.isChecked()) {
            info.append("Canjes");
        } else {
            info.append("Todos");
        }
        
        // Estado
        if (chipPendientes.isChecked()) {
            info.append(" • Pendientes");
        } else if (chipEnviados.isChecked()) {
            info.append(" • Enviados");
        }
        
        // Fechas
        if (fechaInicio != null || fechaFin != null) {
            info.append(" • ");
            if (fechaInicio != null && fechaFin != null) {
                info.append(dateFormat.format(fechaInicio))
                    .append(" - ")
                    .append(dateFormat.format(fechaFin));
            } else if (fechaInicio != null) {
                info.append("Desde ").append(dateFormat.format(fechaInicio));
            } else {
                info.append("Hasta ").append(dateFormat.format(fechaFin));
            }
        }
        
        tvFilterInfo.setText(info.toString());
    }
    
    private void showDateRangeDialog() {
        // Crear diálogo personalizado para selección de rango de fechas
        DateRangeDialogFragment dialog = new DateRangeDialogFragment();
        dialog.setDateRange(fechaInicio, fechaFin);
        dialog.setOnDateRangeSelectedListener((inicio, fin) -> {
            fechaInicio = inicio;
            fechaFin = fin;
            
            // Actualizar texto del botón
            if (inicio != null || fin != null) {
                StringBuilder btnText = new StringBuilder("📅 ");
                if (inicio != null && fin != null) {
                    btnText.append(dateFormat.format(inicio))
                           .append(" - ")
                           .append(dateFormat.format(fin));
                } else if (inicio != null) {
                    btnText.append("Desde ").append(dateFormat.format(inicio));
                } else {
                    btnText.append("Hasta ").append(dateFormat.format(fin));
                }
                btnFiltroFecha.setText(btnText.toString());
            } else {
                btnFiltroFecha.setText("📅 Filtrar por fecha");
            }
            
            applyFilters();
        });
        
        dialog.show(getParentFragmentManager(), "date_range_dialog");
    }
    
    private void onHistorialItemClick(HistorialItem item) {
        // Mostrar detalles del item
        HistorialDetailDialogFragment dialog = new HistorialDetailDialogFragment();
        dialog.setHistorialItem(item);
        dialog.show(getParentFragmentManager(), "historial_detail_dialog");
    }
    
    /**
     * Limpia todos los filtros
     */
    public void clearFilters() {
        chipTodos.setChecked(true);
        fechaInicio = null;
        fechaFin = null;
        btnFiltroFecha.setText("📅 Filtrar por fecha");
        applyFilters();
    }
    
    /**
     * Fuerza la sincronización
     */
    public void forceSync() {
        viewModel.forceSync();
    }
}