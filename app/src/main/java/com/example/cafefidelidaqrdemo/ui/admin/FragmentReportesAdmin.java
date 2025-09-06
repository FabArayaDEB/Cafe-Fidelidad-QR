package com.example.cafefidelidaqrdemo.ui.admin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.data.entities.ReporteEntity;
import com.example.cafefidelidaqrdemo.database.models.TopCliente;
import com.example.cafefidelidaqrdemo.ui.admin.adapters.ReportesAdapter;
import com.example.cafefidelidaqrdemo.ui.admin.adapters.TopClientesAdapter;
import com.example.cafefidelidaqrdemo.ui.admin.viewmodels.ReportesAdminViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Fragment para gestión de reportes administrativos
 * Incluye filtros, visualización de datos y exportación CSV
 */
public class FragmentReportesAdmin extends Fragment {
    
    // ViewModels
    private ReportesAdminViewModel viewModel;
    
    // Vistas principales
    private SwipeRefreshLayout swipeRefreshLayout;
    private ScrollView scrollViewContent;
    private LinearLayout layoutNoData;
    private LinearLayout layoutOffline;
    private ProgressBar progressBarLoading;
    
    // Filtros
    private MaterialCardView cardFiltros;
    private ChipGroup chipGroupTipoReporte;
    private TextInputLayout layoutFechaInicio;
    private TextInputEditText editFechaInicio;
    private TextInputLayout layoutFechaFin;
    private TextInputEditText editFechaFin;
    private Spinner spinnerSucursal;
    private Spinner spinnerBeneficio;
    private Button btnAplicarFiltros;
    private Button btnLimpiarFiltros;
    
    // Métricas principales
    private MaterialCardView cardMetricas;
    private TextView textTotalVisitas;
    private TextView textTotalCanjes;
    private TextView textValorTotal;
    private TextView textPromedioVisitas;
    private TextView textPromedioCanjes;
    private TextView textTopCliente;
    
    // Lista de reportes
    private RecyclerView recyclerViewReportes;
    private ReportesAdapter reportesAdapter;
    
    // Top clientes
    private MaterialCardView cardTopClientes;
    private RecyclerView recyclerViewTopClientes;
    private TopClientesAdapter topClientesAdapter;
    
    // Acciones
    private FloatingActionButton fabExportar;
    private Button btnGenerarReporte;
    private Button btnRefrescar;
    
    // Estados
    private Date fechaInicioSeleccionada;
    private Date fechaFinSeleccionada;
    private String tipoReporteSeleccionado = "visitas";
    private Long sucursalSeleccionada;
    private String beneficioSeleccionado;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reportes_admin, container, false);
        
        initializeViews(view);
        setupViewModel();
        setupRecyclerViews();
        setupListeners();
        setupObservers();
        
        // Cargar datos iniciales
        loadInitialData();
        
        return view;
    }
    
    private void initializeViews(View view) {
        // Vistas principales
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_reportes);
        scrollViewContent = view.findViewById(R.id.scroll_view_content);
        layoutNoData = view.findViewById(R.id.layout_no_data);
        layoutOffline = view.findViewById(R.id.layout_offline);
        progressBarLoading = view.findViewById(R.id.progress_bar_loading);
        
        // Filtros
        cardFiltros = view.findViewById(R.id.card_filtros);
        chipGroupTipoReporte = view.findViewById(R.id.chip_group_tipo_reporte);
        layoutFechaInicio = view.findViewById(R.id.layout_fecha_inicio);
        editFechaInicio = view.findViewById(R.id.edit_fecha_inicio);
        layoutFechaFin = view.findViewById(R.id.layout_fecha_fin);
        editFechaFin = view.findViewById(R.id.edit_fecha_fin);
        spinnerSucursal = view.findViewById(R.id.spinner_sucursal);
        spinnerBeneficio = view.findViewById(R.id.spinner_beneficio);
        btnAplicarFiltros = view.findViewById(R.id.btn_aplicar_filtros);
        btnLimpiarFiltros = view.findViewById(R.id.btn_limpiar_filtros);
        
        // Métricas
        cardMetricas = view.findViewById(R.id.card_metricas);
        textTotalVisitas = view.findViewById(R.id.text_total_visitas);
        textTotalCanjes = view.findViewById(R.id.text_total_canjes);
        textValorTotal = view.findViewById(R.id.text_valor_total);
        textPromedioVisitas = view.findViewById(R.id.text_promedio_visitas);
        textPromedioCanjes = view.findViewById(R.id.text_promedio_canjes);
        textTopCliente = view.findViewById(R.id.text_top_cliente);
        
        // Listas
        recyclerViewReportes = view.findViewById(R.id.recycler_view_reportes);
        cardTopClientes = view.findViewById(R.id.card_top_clientes);
        recyclerViewTopClientes = view.findViewById(R.id.recycler_view_top_clientes);
        
        // Acciones
        fabExportar = view.findViewById(R.id.fab_exportar);
        btnGenerarReporte = view.findViewById(R.id.btn_generar_reporte);
        btnRefrescar = view.findViewById(R.id.btn_refrescar);
        
        // Configurar fechas por defecto (último mes)
        Calendar cal = Calendar.getInstance();
        fechaFinSeleccionada = cal.getTime();
        cal.add(Calendar.MONTH, -1);
        fechaInicioSeleccionada = cal.getTime();
        
        editFechaInicio.setText(dateFormat.format(fechaInicioSeleccionada));
        editFechaFin.setText(dateFormat.format(fechaFinSeleccionada));
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ReportesAdminViewModel.class);
    }
    
    private void setupRecyclerViews() {
        // Adapter para reportes
        reportesAdapter = new ReportesAdapter();
        reportesAdapter.setOnReporteClickListener(new ReportesAdapter.OnReporteClickListener() {
            @Override
            public void onReporteClick(ReporteEntity reporte) {
                FragmentReportesAdmin.this.onReporteClick(reporte);
            }
            
            @Override
            public void onReporteExportClick(ReporteEntity reporte) {
                // Implementar exportación individual si es necesario
            }
        });
        recyclerViewReportes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewReportes.setAdapter(reportesAdapter);
        
        // Adapter para top clientes
        topClientesAdapter = new TopClientesAdapter();
        topClientesAdapter.setOnClienteClickListener(new TopClientesAdapter.OnClienteClickListener() {
            @Override
            public void onClienteClick(TopCliente cliente) {
                // Convertir TopCliente a TopClienteInfo
                ReportesAdminViewModel.TopClienteInfo clienteInfo = new ReportesAdminViewModel.TopClienteInfo(
                    cliente.clienteId, cliente.nombre, "", cliente.totalVisitas, 
                    cliente.totalCanjes, cliente.valorTotalCanjes, cliente.sucursalFavorita, 
                    null, ""
                );
                FragmentReportesAdmin.this.onTopClienteClick(clienteInfo);
            }
            
            @Override
            public void onClienteInfoClick(TopCliente cliente) {
                // Implementar información detallada si es necesario
            }
        });
        recyclerViewTopClientes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTopClientes.setAdapter(topClientesAdapter);
    }
    
    private void setupListeners() {
        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        
        // Filtros de tipo de reporte
        chipGroupTipoReporte.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    tipoReporteSeleccionado = selectedChip.getTag().toString();
                }
            }
        });
        
        // Selectores de fecha
        editFechaInicio.setOnClickListener(v -> showDatePicker(true));
        editFechaFin.setOnClickListener(v -> showDatePicker(false));
        
        // Botones de filtros
        btnAplicarFiltros.setOnClickListener(v -> aplicarFiltros());
        btnLimpiarFiltros.setOnClickListener(v -> limpiarFiltros());
        
        // Acciones principales
        btnGenerarReporte.setOnClickListener(v -> generarReporte());
        btnRefrescar.setOnClickListener(v -> refreshData());
        fabExportar.setOnClickListener(v -> exportarCSV());
        
        // Spinners
        spinnerSucursal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    sucursalSeleccionada = (Long) parent.getItemAtPosition(position);
                } else {
                    sucursalSeleccionada = null;
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sucursalSeleccionada = null;
            }
        });
        
        spinnerBeneficio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    beneficioSeleccionado = parent.getItemAtPosition(position).toString();
                } else {
                    beneficioSeleccionado = null;
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                beneficioSeleccionado = null;
            }
        });
    }
    
    private void setupObservers() {
        // Estados de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBarLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            swipeRefreshLayout.setRefreshing(isLoading);
        });
        
        // Mensajes de error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
        
        // Mensajes de éxito
        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), success -> {
            if (success != null && !success.isEmpty()) {
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Estado offline
        viewModel.getIsOfflineMode().observe(getViewLifecycleOwner(), isOffline -> {
            layoutOffline.setVisibility(isOffline ? View.VISIBLE : View.GONE);
        });
        
        // Lista de reportes
        viewModel.getReportes().observe(getViewLifecycleOwner(), reportes -> {
            if (reportes != null) {
                reportesAdapter.updateReportes(reportes);
                
                // Mostrar/ocultar vista de sin datos
                if (reportes.isEmpty()) {
                    layoutNoData.setVisibility(View.VISIBLE);
                    scrollViewContent.setVisibility(View.GONE);
                } else {
                    layoutNoData.setVisibility(View.GONE);
                    scrollViewContent.setVisibility(View.VISIBLE);
                }
            }
        });
        
        // Métricas
        viewModel.getMetricas().observe(getViewLifecycleOwner(), metricas -> {
            if (metricas != null) {
                actualizarMetricas(metricas);
            }
        });
        
        // Top clientes
        viewModel.getTopClientes().observe(getViewLifecycleOwner(), topClientes -> {
            if (topClientes != null) {
                topClientesAdapter.updateTopClientes(topClientes);
                cardTopClientes.setVisibility(topClientes.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
        
        // Datos para exportar
        viewModel.getExportResult().observe(getViewLifecycleOwner(), exportResult -> {
            if (exportResult != null && !exportResult.isEmpty()) {
                // Manejar resultado de exportación
                Toast.makeText(getContext(), "Exportación completada: " + exportResult, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void loadInitialData() {
        // Cargar datos de sucursales y beneficios para los spinners
        viewModel.cargarDatosIniciales();
        
        // Aplicar filtros por defecto
        aplicarFiltros();
    }
    
    private void showDatePicker(boolean isStartDate) {
        Calendar cal = Calendar.getInstance();
        Date currentDate = isStartDate ? fechaInicioSeleccionada : fechaFinSeleccionada;
        cal.setTime(currentDate);
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            getContext(),
            (view, year, month, dayOfMonth) -> {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(year, month, dayOfMonth);
                Date selectedDate = selectedCal.getTime();
                
                if (isStartDate) {
                    fechaInicioSeleccionada = selectedDate;
                    editFechaInicio.setText(dateFormat.format(selectedDate));
                    
                    // Validar que fecha inicio sea menor que fecha fin
                    if (fechaFinSeleccionada.before(selectedDate)) {
                        layoutFechaInicio.setError("La fecha de inicio debe ser anterior a la fecha de fin");
                    } else {
                        layoutFechaInicio.setError(null);
                    }
                } else {
                    fechaFinSeleccionada = selectedDate;
                    editFechaFin.setText(dateFormat.format(selectedDate));
                    
                    // Validar que fecha fin sea mayor que fecha inicio
                    if (selectedDate.before(fechaInicioSeleccionada)) {
                        layoutFechaFin.setError("La fecha de fin debe ser posterior a la fecha de inicio");
                    } else {
                        layoutFechaFin.setError(null);
                    }
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void aplicarFiltros() {
        // Validar fechas
        if (fechaInicioSeleccionada.after(fechaFinSeleccionada)) {
            Toast.makeText(getContext(), "Las fechas seleccionadas no son válidas", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Aplicar filtros en el ViewModel
        ReportesAdminViewModel.FiltrosReporte filtros = new ReportesAdminViewModel.FiltrosReporte(
            tipoReporteSeleccionado,
            fechaInicioSeleccionada,
            fechaFinSeleccionada,
            sucursalSeleccionada != null ? sucursalSeleccionada.toString() : null,
            beneficioSeleccionado
        );
        viewModel.aplicarFiltros(filtros);
    }
    
    private String formatearFecha(Date fecha) {
        if (fecha == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(fecha);
    }
    
    private void limpiarFiltros() {
        // Resetear tipo de reporte
        chipGroupTipoReporte.check(R.id.chip_visitas);
        tipoReporteSeleccionado = "visitas";
        
        // Resetear fechas (último mes)
        Calendar cal = Calendar.getInstance();
        fechaFinSeleccionada = cal.getTime();
        cal.add(Calendar.MONTH, -1);
        fechaInicioSeleccionada = cal.getTime();
        
        editFechaInicio.setText(dateFormat.format(fechaInicioSeleccionada));
        editFechaFin.setText(dateFormat.format(fechaFinSeleccionada));
        
        // Resetear spinners
        spinnerSucursal.setSelection(0);
        spinnerBeneficio.setSelection(0);
        sucursalSeleccionada = null;
        beneficioSeleccionado = null;
        
        // Limpiar errores
        layoutFechaInicio.setError(null);
        layoutFechaFin.setError(null);
        
        // Aplicar filtros limpios
        aplicarFiltros();
    }
    
    private void generarReporte() {
        // Crear filtros con los valores seleccionados
        ReportesAdminViewModel.FiltrosReporte filtros = new ReportesAdminViewModel.FiltrosReporte(
            tipoReporteSeleccionado,
            fechaInicioSeleccionada,
            fechaFinSeleccionada,
            sucursalSeleccionada != null ? sucursalSeleccionada.toString() : null,
            beneficioSeleccionado
        );
        viewModel.aplicarFiltros(filtros);
    }
    
    private void refreshData() {
        viewModel.refrescarReportes();
    }
    
    private void exportarCSV() {
        String nombreArchivo = "reporte_" + System.currentTimeMillis() + ".csv";
        viewModel.exportarCSV(nombreArchivo);
    }
    
    private void actualizarMetricas(ReportesAdminViewModel.ReporteMetricas metricas) {
        textTotalVisitas.setText(String.valueOf(metricas.totalVisitas));
        textTotalCanjes.setText(String.valueOf(metricas.totalCanjes));
        textValorTotal.setText(String.format(Locale.getDefault(), "$%.2f", metricas.valorTotalCanjes));
        textPromedioVisitas.setText(String.format(Locale.getDefault(), "%.1f", metricas.promedioVisitasDiarias));
        textPromedioCanjes.setText(String.format(Locale.getDefault(), "%.1f", metricas.promedioCanjesDiarios));
        // textTopCliente no está disponible en ReporteMetricas, usar un valor por defecto
        if (textTopCliente != null) {
            textTopCliente.setText("N/A");
        }
    }
    
    private void guardarYCompartirCSV(String csvData) {
        try {
            // Crear archivo temporal
            File cacheDir = getContext().getCacheDir();
            File csvFile = new File(cacheDir, "reporte_" + System.currentTimeMillis() + ".csv");
            
            // Escribir datos
            FileWriter writer = new FileWriter(csvFile);
            writer.write(csvData);
            writer.close();
            
            // Compartir archivo
            Uri fileUri = FileProvider.getUriForFile(
                getContext(),
                getContext().getPackageName() + ".fileprovider",
                csvFile
            );
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporte de Café Fidelidad");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Compartir reporte"));
            
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al exportar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    // Callbacks de clicks
    private void onReporteClick(ReporteEntity reporte) {
        // Mostrar detalles del reporte
        // Mostrar detalles del reporte usando el ID
        String reporteId = reporte.getId();
        if (reporteId != null) {
            // TODO: Implementar mostrar detalles del reporte
            Toast.makeText(getContext(), "Detalles del reporte: " + reporteId, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void onTopClienteClick(ReportesAdminViewModel.TopClienteInfo cliente) {
        // Navegar a detalles del cliente
        Toast.makeText(getContext(), "Ver detalles de: " + cliente.nombre, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewModel != null) {
            viewModel.cleanup();
        }
    }
}