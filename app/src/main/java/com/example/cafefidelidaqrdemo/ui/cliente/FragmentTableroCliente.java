package com.example.cafefidelidaqrdemo.ui.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.database.entities.TableroEntity;
import com.example.cafefidelidaqrdemo.ui.cliente.viewmodels.TableroClienteViewModel;
import com.example.cafefidelidaqrdemo.ui.adapters.CanjesRecientesAdapter;
import com.example.cafefidelidaqrdemo.database.entities.CanjeEntity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Fragment del tablero personal del cliente
 * Muestra KPIs, progreso y recomendaciones personalizadas
 */
public class FragmentTableroCliente extends Fragment {
    
    // ViewModels
    private TableroClienteViewModel viewModel;
    
    // Vistas principales
    private SwipeRefreshLayout swipeRefreshLayout;
    private ScrollView scrollViewContent;
    private LinearLayout layoutOffline;
    private LinearLayout layoutNoData;
    private ProgressBar progressBarLoading;
    private TextView textDataSource;
    
    // Tarjeta de bienvenida
    private MaterialCardView cardBienvenida;
    private TextView textNombreCliente;
    private TextView textMensajeMotivacional;
    private TextView textUltimaVisita;
    private TextView textRachaVisitas;
    
    // Tarjeta de visitas
    private MaterialCardView cardVisitas;
    private TextView textTotalVisitas;
    private TextView textVisitasMes;
    private TextView textVisitasSemana;
    private TextView textSucursalFavorita;
    private LinearProgressIndicator progressMetaVisitas;
    private TextView textProgresoMeta;
    
    // Tarjeta de puntos y nivel
    private MaterialCardView cardPuntos;
    private TextView textPuntosDisponibles;
    private TextView textPuntosTotales;
    private TextView textPuntosCanjeados;
    private TextView textNivelFidelidad;
    private CircularProgressIndicator progressNivel;
    private TextView textProgresoNivel;
    private TextView textPuntosSiguienteNivel;
    
    // Tarjeta de beneficios
    private MaterialCardView cardBeneficios;
    private TextView textBeneficiosDisponibles;
    private TextView textBeneficiosCanjeables;
    private TextView textBeneficioRecomendado;
    private TextView textPuntosRecomendado;
    private Button btnVerBeneficios;
    private Button btnCanjearRecomendado;
    
    // Tarjeta de canjes recientes
    private MaterialCardView cardCanjes;
    private TextView textTotalCanjes;
    private TextView textCanjesMes;
    private TextView textValorTotalCanjes;
    private TextView textUltimoCanje;
    private RecyclerView recyclerViewCanjesRecientes;
    private CanjesRecientesAdapter canjesAdapter;
    
    // Tarjeta de recomendaciones
    private MaterialCardView cardRecomendaciones;
    private TextView textSucursalRecomendada;
    private Button btnVerSucursales;
    private Button btnVerHistorial;
    
    // Acciones rápidas
    private LinearLayout layoutAccionesRapidas;
    private Button btnEscanearQR;
    private Button btnVerMapa;
    private Button btnContacto;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tablero_cliente, container, false);
        
        initializeViews(view);
        setupViewModel();
        setupRecyclerView();
        setupListeners();
        setupObservers();
        
        // Cargar datos del tablero
        loadTableroData();
        
        return view;
    }
    
    private void initializeViews(View view) {
        // Solo inicializar vistas que existen en el layout actual
        // El layout actual es básico, por lo que comentamos las inicializaciones
        // hasta que se implemente un layout completo
        
        // Vistas principales - comentadas porque no existen en el layout actual
        // swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_tablero);
        // scrollViewContent = view.findViewById(R.id.scroll_view_content);
        // layoutOffline = view.findViewById(R.id.layout_offline);
        // layoutNoData = view.findViewById(R.id.layout_no_data);
        // progressBarLoading = view.findViewById(R.id.progress_bar_loading);
        // textDataSource = view.findViewById(R.id.text_data_source);
        
        // Acciones rápidas
        // layoutAccionesRapidas = view.findViewById(R.id.layout_acciones_rapidas);
        // btnEscanearQR = view.findViewById(R.id.btn_escanear_qr);
        // btnVerMapa = view.findViewById(R.id.btn_ver_mapa);
        // btnContacto = view.findViewById(R.id.btn_contacto);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TableroClienteViewModel.class);
    }
    
    private void setupRecyclerView() {
        // canjesAdapter = new CanjesRecientesAdapter(new ArrayList<>(), this::onCanjeRecenteClick);
        // recyclerViewCanjesRecientes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // recyclerViewCanjesRecientes.setAdapter(canjesAdapter);
    }
    
    private void setupListeners() {
        // Listeners comentados porque las vistas no están inicializadas
        // debido a que el layout actual es básico
        
        // Swipe to refresh
        // if (swipeRefreshLayout != null) {
        //     swipeRefreshLayout.setOnRefreshListener(this::refreshTablero);
        // }
        
        // Clicks en tarjetas para navegar a detalles
        // if (cardVisitas != null) cardVisitas.setOnClickListener(v -> navigateToVisitas());
        // if (cardPuntos != null) cardPuntos.setOnClickListener(v -> navigateToPuntos());
        // if (cardBeneficios != null) cardBeneficios.setOnClickListener(v -> navigateToBeneficios());
        // if (cardCanjes != null) cardCanjes.setOnClickListener(v -> navigateToCanjes());
        
        // Botones de beneficios
        // if (btnVerBeneficios != null) btnVerBeneficios.setOnClickListener(v -> navigateToBeneficios());
        // if (btnCanjearRecomendado != null) btnCanjearRecomendado.setOnClickListener(v -> canjearBeneficioRecomendado());
        
        // Botones de recomendaciones
        // if (btnVerSucursales != null) btnVerSucursales.setOnClickListener(v -> navigateToSucursales());
        // if (btnVerHistorial != null) btnVerHistorial.setOnClickListener(v -> navigateToHistorial());
        
        // Acciones rápidas - comentadas porque no están inicializadas
        // if (btnEscanearQR != null) btnEscanearQR.setOnClickListener(v -> abrirEscanerQR());
        // if (btnVerMapa != null) btnVerMapa.setOnClickListener(v -> abrirMapa());
        // if (btnContacto != null) btnContacto.setOnClickListener(v -> abrirContacto());
    }
    
    private void setupObservers() {
        // Estados de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBarLoading != null) progressBarLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(isLoading);
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
            if (layoutOffline != null) layoutOffline.setVisibility(isOffline ? View.VISIBLE : View.GONE);
        });
        
        // Fuente de datos
        viewModel.getDataSource().observe(getViewLifecycleOwner(), dataSource -> {
            if (dataSource != null && textDataSource != null) {
                switch (dataSource) {
                    case "cache":
                        textDataSource.setText("Datos locales");
                        textDataSource.setVisibility(View.VISIBLE);
                        break;
                    case "api":
                        textDataSource.setText("Datos actualizados");
                        textDataSource.setVisibility(View.VISIBLE);
                        break;
                    case "mixed":
                        textDataSource.setText("Datos mixtos");
                        textDataSource.setVisibility(View.VISIBLE);
                        break;
                    default:
                        textDataSource.setVisibility(View.GONE);
                        break;
                }
            }
        });
        
        // Datos del tablero
        viewModel.getTableroCliente().observe(getViewLifecycleOwner(), tablero -> {
            if (tablero != null) {
                actualizarTablero(tablero);
                if (layoutNoData != null) layoutNoData.setVisibility(View.GONE);
                if (scrollViewContent != null) scrollViewContent.setVisibility(View.VISIBLE);
            } else {
                if (layoutNoData != null) layoutNoData.setVisibility(View.VISIBLE);
                if (scrollViewContent != null) scrollViewContent.setVisibility(View.GONE);
            }
        });
        
        // Canjes recientes
        // viewModel.getCanjesRecientes().observe(getViewLifecycleOwner(), canjes -> {
        //     if (canjes != null) {
        //         canjesAdapter.updateCanjes(canjes);
        //     }
        // });
    }
    
    private void loadTableroData() {
        // Obtener ID del cliente desde SharedPreferences o argumentos
        String clienteId = "cliente_demo"; // TODO: Obtener ID real del cliente autenticado
        viewModel.cargarTableroCliente(clienteId);
    }
    
    private void refreshTablero() {
        String clienteId = "cliente_demo"; // TODO: Obtener ID real del cliente autenticado
        viewModel.refrescarTablero(clienteId);
    }
    
    private void actualizarTablero(TableroEntity tablero) {
        // Tarjeta de bienvenida
        if (textNombreCliente != null) textNombreCliente.setText(tablero.getNombreCliente() != null ? tablero.getNombreCliente() : "Cliente");
        if (textMensajeMotivacional != null) textMensajeMotivacional.setText(tablero.getMensajeMotivacional());
        
        if (textUltimaVisita != null) {
            if (tablero.getUltimaVisita() != null) {
                textUltimaVisita.setText("Última visita: " + dateFormat.format(tablero.getUltimaVisita()));
            } else {
                textUltimaVisita.setText("Sin visitas registradas");
            }
        }
        
        if (textRachaVisitas != null) {
            if (tablero.getRachaVisitas() > 0) {
                textRachaVisitas.setText("Racha: " + tablero.getRachaVisitas() + " días");
                textRachaVisitas.setVisibility(View.VISIBLE);
            } else {
                textRachaVisitas.setVisibility(View.GONE);
            }
        }
        
        // Tarjeta de visitas
        if (textTotalVisitas != null) textTotalVisitas.setText(String.valueOf(tablero.getTotalVisitas()));
        if (textVisitasMes != null) textVisitasMes.setText(String.valueOf(tablero.getVisitasMesActual()));
        if (textVisitasSemana != null) textVisitasSemana.setText(String.valueOf(tablero.getVisitasSemanaActual()));
        
        if (tablero.getSucursalFavoritaNombre() != null) {
            textSucursalFavorita.setText("Favorita: " + tablero.getSucursalFavoritaNombre());
        } else {
            textSucursalFavorita.setText("Sin sucursal favorita");
        }
        
        // Progreso de meta de visitas
        if (tablero.getMetaVisitasMes() > 0) {
            progressMetaVisitas.setProgress((int) tablero.getProgresoMetaVisitas());
            textProgresoMeta.setText(String.format(Locale.getDefault(), 
                "%.0f%% de tu meta (%d/%d visitas)", 
                tablero.getProgresoMetaVisitas(), 
                tablero.getVisitasMesActual(), 
                tablero.getMetaVisitasMes()));
            progressMetaVisitas.setVisibility(View.VISIBLE);
            textProgresoMeta.setVisibility(View.VISIBLE);
        } else {
            progressMetaVisitas.setVisibility(View.GONE);
            textProgresoMeta.setVisibility(View.GONE);
        }
        
        // Tarjeta de puntos
        textPuntosDisponibles.setText(String.valueOf(tablero.getPuntosDisponibles()));
        textPuntosTotales.setText(String.valueOf(tablero.getPuntosTotales()));
        textPuntosCanjeados.setText(String.valueOf(tablero.getPuntosCanjeados()));
        
        if (tablero.getNivelFidelidad() != null) {
            textNivelFidelidad.setText(tablero.getNivelFidelidad());
        } else {
            textNivelFidelidad.setText("Bronce");
        }
        
        // Progreso de nivel
        progressNivel.setProgress((int) tablero.getProgresoNivel());
        textProgresoNivel.setText(tablero.getEstadoProgreso());
        
        if (tablero.getPuntosSiguienteNivel() > 0) {
            textPuntosSiguienteNivel.setText(String.format(Locale.getDefault(), 
                "%d puntos para el siguiente nivel", tablero.getPuntosSiguienteNivel()));
        } else {
            textPuntosSiguienteNivel.setText("¡Nivel máximo alcanzado!");
        }
        
        // Tarjeta de beneficios
        textBeneficiosDisponibles.setText(String.valueOf(tablero.getBeneficiosDisponibles()));
        textBeneficiosCanjeables.setText(String.valueOf(tablero.getBeneficiosCanjeables()));
        
        if (tablero.getBeneficioRecomendadoNombre() != null) {
            textBeneficioRecomendado.setText(tablero.getBeneficioRecomendadoNombre());
            textPuntosRecomendado.setText(String.valueOf(tablero.getBeneficioRecomendadoPuntos()) + " puntos");
            btnCanjearRecomendado.setEnabled(tablero.puedeCanjeaBeneficioRecomendado());
            btnCanjearRecomendado.setText(tablero.puedeCanjeaBeneficioRecomendado() ? "Canjear" : "Puntos insuficientes");
        } else {
            textBeneficioRecomendado.setText("Sin recomendaciones");
            textPuntosRecomendado.setText("");
            btnCanjearRecomendado.setEnabled(false);
        }
        
        // Tarjeta de canjes
        textTotalCanjes.setText(String.valueOf(tablero.getTotalCanjes()));
        textCanjesMes.setText(String.valueOf(tablero.getCanjesMesActual()));
        textValorTotalCanjes.setText(String.format(Locale.getDefault(), "$%.2f", tablero.getValorTotalCanjes()));
        
        if (tablero.getUltimoCanjeFecha() != null && tablero.getUltimoCanjeBeneficio() != null) {
            textUltimoCanje.setText(String.format(Locale.getDefault(), 
                "%s - %s", 
                tablero.getUltimoCanjeBeneficio(), 
                dateFormat.format(tablero.getUltimoCanjeFecha())));
        } else {
            textUltimoCanje.setText("Sin canjes recientes");
        }
        
        // Tarjeta de recomendaciones
        if (tablero.getSucursalRecomendadaNombre() != null) {
            textSucursalRecomendada.setText("Te recomendamos visitar: " + tablero.getSucursalRecomendadaNombre());
        } else {
            textSucursalRecomendada.setText("Explora nuestras sucursales");
        }
    }
    
    // Métodos de navegación
    private void navigateToVisitas() {
        Toast.makeText(getContext(), "Navegando a historial de visitas", Toast.LENGTH_SHORT).show();
        // TODO: Implementar navegación
    }
    
    private void navigateToPuntos() {
        Toast.makeText(getContext(), "Navegando a detalles de puntos", Toast.LENGTH_SHORT).show();
        // TODO: Implementar navegación
    }
    
    private void navigateToBeneficios() {
        Toast.makeText(getContext(), "Navegando a beneficios disponibles", Toast.LENGTH_SHORT).show();
        // TODO: Implementar navegación
    }
    
    private void navigateToCanjes() {
        Toast.makeText(getContext(), "Navegando a historial de canjes", Toast.LENGTH_SHORT).show();
        // TODO: Implementar navegación
    }
    
    private void navigateToSucursales() {
        Toast.makeText(getContext(), "Navegando a mapa de sucursales", Toast.LENGTH_SHORT).show();
        // TODO: Implementar navegación
    }
    
    private void navigateToHistorial() {
        Toast.makeText(getContext(), "Navegando a historial completo", Toast.LENGTH_SHORT).show();
        // TODO: Implementar navegación
    }
    
    // Acciones
    private void canjearBeneficioRecomendado() {
        viewModel.canjearBeneficioRecomendado();
    }
    
    private void abrirEscanerQR() {
        Toast.makeText(getContext(), "Abriendo escáner QR", Toast.LENGTH_SHORT).show();
        // TODO: Implementar escáner QR
    }
    
    private void abrirMapa() {
        Toast.makeText(getContext(), "Abriendo mapa de sucursales", Toast.LENGTH_SHORT).show();
        // TODO: Implementar mapa
    }
    
    private void abrirContacto() {
        Toast.makeText(getContext(), "Abriendo información de contacto", Toast.LENGTH_SHORT).show();
        // TODO: Implementar contacto
    }
    
    // Callbacks
    private void onCanjeRecenteClick(TableroClienteViewModel.CanjeReciente canje) {
        Toast.makeText(getContext(), "Ver detalles del canje: " + canje.beneficio, Toast.LENGTH_SHORT).show();
        // TODO: Mostrar detalles del canje
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewModel != null) {
            viewModel.cleanup();
        }
    }
}