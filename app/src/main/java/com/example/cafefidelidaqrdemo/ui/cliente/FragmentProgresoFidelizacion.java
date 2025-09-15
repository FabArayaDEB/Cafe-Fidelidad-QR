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
import com.example.cafefidelidaqrdemo.adapters.ProgresoFidelizacionAdapter;
import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;
import com.example.cafefidelidaqrdemo.ui.dialogs.BeneficioDetailsDialogFragment;
import com.example.cafefidelidaqrdemo.ui.dialogs.ProximoBeneficioDialogFragment;
import com.example.cafefidelidaqrdemo.viewmodels.ProgresoViewModel;
import com.example.cafefidelidaqrdemo.models.ProximoBeneficio;
import com.example.cafefidelidaqrdemo.models.ProgresoGeneral;
import com.example.cafefidelidaqrdemo.models.SyncStatus;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment para mostrar el progreso del cliente hacia beneficios (CU-04.2)
 * Muestra contador de visitas, beneficios disponibles y progreso hacia próximos beneficios
 */
public class FragmentProgresoFidelizacion extends Fragment {
    
    private static final String TAG = "FragmentProgresoFidelizacion";
    
    // Views principales
    private SwipeRefreshLayout swipeRefreshLayout;
    private View layoutProgreso;
    private View layoutBeneficiosDisponibles;
    private View layoutProximosBeneficios;
    private View emptyStateView;
    private View errorStateView;
    
    // Views de progreso general
    private TextView textTotalVisitas;
    private TextView textProgresoGeneral;
    private CircularProgressIndicator progressCircular;
    private TextView textEstadoSync;
    
    // Views de beneficios disponibles
    private RecyclerView recyclerBeneficiosDisponibles;
    private TextView textBeneficiosDisponiblesTitle;
    private BeneficiosDisponiblesAdapter beneficiosDisponiblesAdapter;
    
    // Views de próximos beneficios
    private RecyclerView recyclerProximosBeneficios;
    private TextView textProximosBeneficiosTitle;
    private ProgresoFidelizacionAdapter proximosBeneficiosAdapter;
    
    // Components
    private ProgresoViewModel viewModel;
    
    public static FragmentProgresoFidelizacion newInstance() {
        return new FragmentProgresoFidelizacion();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProgresoViewModel.class);
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
        
        // Cargar datos iniciales
        String clienteId = "cliente_demo"; // TODO: Obtener ID real del cliente autenticado
        viewModel.loadProgresoData(clienteId);
    }
    
    private void initViews(View view) {
        // Layouts principales
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        layoutBeneficiosDisponibles = view.findViewById(R.id.layoutBeneficiosDisponibles);
        layoutProximosBeneficios = view.findViewById(R.id.layoutProximosBeneficios);
        emptyStateView = view.findViewById(R.id.layoutEmpty);
        
        // Views de progreso
        textTotalVisitas = view.findViewById(R.id.textTotalVisitas);
        progressCircular = view.findViewById(R.id.progressCircular);
        textEstadoSync = view.findViewById(R.id.textSyncStatus);
        
        // Views de beneficios disponibles
        recyclerBeneficiosDisponibles = view.findViewById(R.id.recyclerViewBeneficiosDisponibles);
        
        // Views de próximos beneficios
        recyclerProximosBeneficios = view.findViewById(R.id.recyclerViewProximosBeneficios);
    }
    
    private void setupRecyclerViews() {
        // RecyclerView para beneficios disponibles
        beneficiosDisponiblesAdapter = new BeneficiosDisponiblesAdapter(
            new ArrayList<>(), 
            this::onBeneficioDisponibleClick
        );
        // recyclerBeneficiosDisponibles.setLayoutManager(
        //         new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        // );
        // recyclerBeneficiosDisponibles.setAdapter(beneficiosDisponiblesAdapter);
        
        // RecyclerView para próximos beneficios
        // proximosBeneficiosAdapter = new ProgresoFidelizacionAdapter(
        //     new ArrayList<>(),
        //     this::onProximoBeneficioClick
        // );
        // recyclerProximosBeneficios.setLayoutManager(new LinearLayoutManager(getContext()));
        // recyclerProximosBeneficios.setAdapter(proximosBeneficiosAdapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshProgresoData();
        });
        
        swipeRefreshLayout.setColorSchemeResources(
            R.color.color_primary,
            R.color.color_secondary,
            R.color.color_tertiary
        );
    }
    
    private void setupObservers() {
        // Observar progreso general
        viewModel.getProgresoGeneral().observe(getViewLifecycleOwner(), progreso -> {
            if (progreso != null) {
                updateProgresoGeneral(progreso);
            }
        });
        
        // Observar beneficios disponibles
        viewModel.getBeneficiosDisponibles().observe(getViewLifecycleOwner(), beneficios -> {
            if (beneficios != null) {
                updateBeneficiosDisponibles(beneficios);
            }
        });
        
        // Observar próximos beneficios
        viewModel.getProximosBeneficios().observe(getViewLifecycleOwner(), proximosBeneficios -> {
            if (proximosBeneficios != null) {
                updateProximosBeneficios(proximosBeneficios);
            }
        });
        
        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                swipeRefreshLayout.setRefreshing(isLoading);
            }
        });
        
        // Observar errores
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showErrorState(error);
            } else {
                hideErrorState();
            }
        });
        
        // Observar estado de sincronización
        viewModel.getSyncStatus().observe(getViewLifecycleOwner(), syncStatus -> {
            updateSyncStatus(syncStatus);
        });
        
        // Observar si hay datos
        viewModel.getHasData().observe(getViewLifecycleOwner(), hasData -> {
            updateEmptyState(!hasData);
        });
    }
    
    private void updateProgresoGeneral(ProgresoGeneral progreso) {
        // Actualizar total de visitas usando visitasActuales
        textTotalVisitas.setText(String.format("Total de visitas: %d", progreso.getVisitasActuales()));
        
        // Actualizar progreso circular usando progresoNivel
        if (progreso.getProgresoNivel() >= 0) {
            progressCircular.setProgress((int) (progreso.getProgresoNivel() * 100));
            progressCircular.setVisibility(View.VISIBLE);
            
            // Texto de progreso usando métodos disponibles
            if (progreso.getPuntosRestantesParaProximoNivel() > 0) {
                // textProgresoGeneral.setText(String.format(
                //     "%d/%d puntos para próximo nivel",
                //     progreso.getPuntosActuales(),
                //     progreso.getPuntosParaProximoNivel()
                // ));
            } else {
                // textProgresoGeneral.setText("¡Felicidades! Has alcanzado el máximo nivel");
            }
        } else {
            progressCircular.setVisibility(View.GONE);
            // textProgresoGeneral.setText("Configura tu progreso para ver los datos");
        }
    }
    
    private void updateBeneficiosDisponibles(List<BeneficioEntity> beneficios) {
        if (beneficios.isEmpty()) {
            // layoutBeneficiosDisponibles.setVisibility(View.GONE);
        } else {
            // layoutBeneficiosDisponibles.setVisibility(View.VISIBLE);
            // textBeneficiosDisponiblesTitle.setText(
        //         String.format("Beneficios Disponibles (%d)", beneficiosDisponibles.size())
        // );
            beneficiosDisponiblesAdapter.updateBeneficios(beneficios);
        }
    }
    
    private void updateProximosBeneficios(List<ProximoBeneficio> proximosBeneficios) {
        if (proximosBeneficios.isEmpty()) {
            // layoutProximosBeneficios.setVisibility(View.GONE);
        } else {
            // layoutProximosBeneficios.setVisibility(View.VISIBLE);
            // textProximosBeneficiosTitle.setText(
        //         String.format("Próximos Beneficios (%d)", proximosBeneficios.size())
        // );
            // proximosBeneficiosAdapter.updateProximosBeneficios(proximosBeneficios);
        }
    }
    
    private void updateSyncStatus(SyncStatus syncStatus) {
        if (syncStatus == null) {
            // textEstadoSync.setVisibility(View.GONE);
            return;
        }
        
        // textEstadoSync.setVisibility(View.VISIBLE);
        
        // switch (syncStatus.getEstado()) {
        //     case SINCRONIZADO:
        //         textEstadoSync.setText("✓ Datos actualizados");
        //         textEstadoSync.setTextColor(getResources().getColor(R.color.color_success, null));
        //         break;
        //     case ESTIMADO:
        //         textEstadoSync.setText("Datos estimados (sin conexión)");
        //         textEstadoSync.setTextColor(getResources().getColor(R.color.color_warning, null));
        //         break;
        //     case SINCRONIZANDO:
        //         textEstadoSync.setText("Sincronizando...");
        //         textEstadoSync.setTextColor(getResources().getColor(R.color.color_info, null));
        //         break;
        //     case ERROR:
        //         textEstadoSync.setText("Error de sincronización");
        //         textEstadoSync.setTextColor(getResources().getColor(R.color.color_error, null));
        //         break;
        // }
    }
    
    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateView.setVisibility(View.VISIBLE);
            // layoutProgreso.setVisibility(View.GONE);
            // layoutBeneficiosDisponibles.setVisibility(View.GONE);
        // layoutProximosBeneficios.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            // layoutProgreso.setVisibility(View.VISIBLE);
        }
    }
    
    private void showErrorState(String error) {
        // TODO: Agregar errorStateView al layout
        /*
        errorStateView.setVisibility(View.VISIBLE);
        TextView textError = errorStateView.findViewById(R.id.textError);
        if (textError != null) {
            textError.setText(error);
        }
        */
    }
    
    private void hideErrorState() {
        // TODO: Agregar errorStateView al layout
        // errorStateView.setVisibility(View.GONE);
    }
    
    // Callbacks de clicks
    private void onBeneficioDisponibleClick(BeneficioEntity beneficio) {
        // Mostrar detalles del beneficio disponible
        BeneficioDetailsDialogFragment dialog = BeneficioDetailsDialogFragment.newInstance(beneficio);
        dialog.show(getParentFragmentManager(), "BeneficioDetailsDialog");
    }
    
    private void onProximoBeneficioClick(ProximoBeneficio proximoBeneficio) {
        // Mostrar detalles del próximo beneficio
        ProximoBeneficioDialogFragment dialog = ProximoBeneficioDialogFragment.newInstance(proximoBeneficio);
        dialog.show(getParentFragmentManager(), "ProximoBeneficioDialog");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refrescar datos al volver al fragment
        viewModel.refreshProgresoData();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias
        swipeRefreshLayout = null;
        // layoutProgreso = null;
        // layoutBeneficiosDisponibles = null;
        // layoutProximosBeneficios = null;
        emptyStateView = null;
        errorStateView = null;
        beneficiosDisponiblesAdapter = null;
        proximosBeneficiosAdapter = null;
    }
}