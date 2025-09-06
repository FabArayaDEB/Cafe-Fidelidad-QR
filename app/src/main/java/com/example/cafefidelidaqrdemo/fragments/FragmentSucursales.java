package com.example.cafefidelidaqrdemo.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.SearchView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.adapters.SucursalesAdapter;
import com.example.cafefidelidaqrdemo.database.entities.SucursalEntity;
import com.example.cafefidelidaqrdemo.repository.SucursalRepository;
import com.example.cafefidelidaqrdemo.viewmodel.SucursalesViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class FragmentSucursales extends Fragment {
    
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private SucursalesViewModel viewModel;
    private SucursalesAdapter adapter;
    private FusedLocationProviderClient fusedLocationClient;
    
    // UI Components
    private RecyclerView recyclerViewSucursales;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearProgressIndicator progressIndicator;
    private MaterialTextView textViewEmpty;
    private MaterialTextView textViewOffline;
    private MaterialTextView textViewLocationDenied;
    private ChipGroup chipGroupOrden;
    private ChipGroup chipGroupEstado;
    private FloatingActionButton fabLocation;
    private FloatingActionButton fabScrollTop;
    private SearchView searchView;
    
    // Estado actual
    private String ordenSeleccionado = "nombre";
    private String estadoSeleccionado = "";
    private String queryBusqueda = "";
    private List<SucursalEntity> sucursalesOriginales = new ArrayList<>();
    private Location userLocation;
    private boolean locationPermissionGranted = false;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sucursales, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupViewModel();
        setupLocationClient();
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearchView();
        setupFilters();
        setupFabs();
        observeData();
        
        // Verificar permisos de ubicación
        checkLocationPermission();
        
        // Cargar datos iniciales
        viewModel.loadSucursales();
    }
    
    private void initializeViews(View view) {
        recyclerViewSucursales = view.findViewById(R.id.recyclerViewSucursales);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        textViewOffline = view.findViewById(R.id.textViewOffline);
        textViewLocationDenied = view.findViewById(R.id.textViewLocationDenied);
        chipGroupOrden = view.findViewById(R.id.chipGroupOrden);
        chipGroupEstado = view.findViewById(R.id.chipGroupEstado);
        fabLocation = view.findViewById(R.id.fabLocation);
        searchView = view.findViewById(R.id.searchView);
        fabScrollTop = view.findViewById(R.id.fabScrollTop);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SucursalesViewModel.class);
    }
    
    private void setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }
    
    private void setupRecyclerView() {
        adapter = new SucursalesAdapter();
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewSucursales.setLayoutManager(layoutManager);
        recyclerViewSucursales.setAdapter(adapter);
        
        // Listener para clicks en sucursales
        adapter.setOnSucursalClickListener(sucursal -> {
            showSucursalDetails(sucursal);
        });
        
        adapter.setOnSucursalLongClickListener(sucursal -> {
            showSucursalOptions(sucursal);
        });
        
        // Listener para scroll
        recyclerViewSucursales.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                // Mostrar/ocultar botón de scroll to top
                if (layoutManager.findFirstVisibleItemPosition() > 3) {
                    fabScrollTop.show();
                } else {
                    fabScrollTop.hide();
                }
            }
        });
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshSucursales();
            if (locationPermissionGranted) {
                getCurrentLocation();
            }
        });
        
        swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.coffee_secondary,
            R.color.coffee_accent
        );
    }
    
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryBusqueda = query != null ? query.trim() : "";
                applyFiltersAndSort();
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                queryBusqueda = newText != null ? newText.trim() : "";
                applyFiltersAndSort();
                return true;
            }
        });
        
        searchView.setOnCloseListener(() -> {
            queryBusqueda = "";
            applyFiltersAndSort();
            return false;
        });
    }
    
    private void setupFilters() {
        setupOrdenFilters();
        setupEstadoFilters();
    }
    
    private void setupOrdenFilters() {
        // Chip "Por nombre"
        Chip chipNombre = new Chip(getContext());
        chipNombre.setText("Por nombre");
        chipNombre.setCheckable(true);
        chipNombre.setChecked(true);
        chipNombre.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ordenSeleccionado = "nombre";
                uncheckOtherOrdenChips(chipNombre);
                applyFiltersAndSort();
            }
        });
        chipGroupOrden.addView(chipNombre);
        
        // Chip "Por distancia" (solo si hay ubicación)
        Chip chipDistancia = new Chip(getContext());
        chipDistancia.setText("Por distancia");
        chipDistancia.setCheckable(true);
        chipDistancia.setEnabled(locationPermissionGranted);
        chipDistancia.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ordenSeleccionado = "distancia";
                uncheckOtherOrdenChips(chipDistancia);
                applyFiltersAndSort();
            }
        });
        chipGroupOrden.addView(chipDistancia);
    }
    
    private void setupEstadoFilters() {
        // Chip "Todas"
        Chip chipTodas = new Chip(getContext());
        chipTodas.setText("Todas");
        chipTodas.setCheckable(true);
        chipTodas.setChecked(true);
        chipTodas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                estadoSeleccionado = "";
                uncheckOtherEstadoChips(chipTodas);
                applyFiltersAndSort();
            }
        });
        chipGroupEstado.addView(chipTodas);
        
        // Chip "Activas"
        Chip chipActivas = new Chip(getContext());
        chipActivas.setText("Activas");
        chipActivas.setCheckable(true);
        chipActivas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                estadoSeleccionado = "activo";
                uncheckOtherEstadoChips(chipActivas);
                applyFiltersAndSort();
            }
        });
        chipGroupEstado.addView(chipActivas);
    }
    
    private void setupFabs() {
        // FAB para obtener ubicación
        fabLocation.setOnClickListener(v -> {
            if (locationPermissionGranted) {
                getCurrentLocation();
            } else {
                requestLocationPermission();
            }
        });
        
        // FAB para scroll to top
        fabScrollTop.setOnClickListener(v -> {
            recyclerViewSucursales.smoothScrollToPosition(0);
        });
        fabScrollTop.hide();
    }
    
    private void observeData() {
        // Observar lista de sucursales
        viewModel.getSucursales().observe(getViewLifecycleOwner(), sucursales -> {
            if (sucursales != null) {
                sucursalesOriginales = new ArrayList<>(sucursales);
                applyFiltersAndSort();
            }
        });
        
        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                swipeRefreshLayout.setRefreshing(isLoading);
            }
        });
        
        // Observar errores
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
                viewModel.clearError();
            }
        });
        
        // Observar estado offline
        viewModel.getIsOffline().observe(getViewLifecycleOwner(), isOffline -> {
            if (isOffline != null) {
                textViewOffline.setVisibility(isOffline ? View.VISIBLE : View.GONE);
            }
        });
        
        // Observar permisos de ubicación
        viewModel.getLocationPermissionDenied().observe(getViewLifecycleOwner(), denied -> {
            if (denied != null) {
                textViewLocationDenied.setVisibility(denied ? View.VISIBLE : View.GONE);
                updateLocationChipState(!denied);
            }
        });
    }
    
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            viewModel.setLocationPermissionDenied(false);
            getCurrentLocation();
        } else {
            locationPermissionGranted = false;
            viewModel.setLocationPermissionDenied(true);
        }
    }
    
    private void requestLocationPermission() {
        requestPermissions(
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
            LOCATION_PERMISSION_REQUEST_CODE
        );
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                viewModel.setLocationPermissionDenied(false);
                getCurrentLocation();
            } else {
                locationPermissionGranted = false;
                viewModel.setLocationPermissionDenied(true);
                Toast.makeText(getContext(), 
                    "Permiso de ubicación denegado. No se podrán mostrar distancias.", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void getCurrentLocation() {
        if (!locationPermissionGranted) return;
        
        if (ActivityCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(location -> {
                if (location != null) {
                    userLocation = location;
                    viewModel.updateUserLocation(location.getLatitude(), location.getLongitude());
                    applyFiltersAndSort();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), 
                    "Error al obtener ubicación: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void applyFiltersAndSort() {
        List<SucursalEntity> sucursalesFiltradas = new ArrayList<>();
        
        for (SucursalEntity sucursal : sucursalesOriginales) {
            boolean pasaFiltroEstado = estadoSeleccionado.isEmpty() || 
                (estadoSeleccionado.equalsIgnoreCase("activo") && sucursal.getEstado().equals("activo")) ||
                (estadoSeleccionado.equalsIgnoreCase("inactivo") && !sucursal.getEstado().equals("activo"));
            
            boolean pasaFiltroBusqueda = queryBusqueda.isEmpty() || 
                sucursal.getNombre().toLowerCase().contains(queryBusqueda.toLowerCase()) ||
                sucursal.getDireccion().toLowerCase().contains(queryBusqueda.toLowerCase());
            
            if (pasaFiltroEstado && pasaFiltroBusqueda) {
                sucursalesFiltradas.add(sucursal);
            }
        }
        
        // Ordenar según criterio seleccionado
        if ("distancia".equals(ordenSeleccionado) && userLocation != null) {
            viewModel.getSucursalesWithDistance(userLocation.getLatitude(), 
                userLocation.getLongitude(), sucursalesWithDistance -> {
                    // Convertir SucursalRepository.SucursalWithDistance a SucursalesAdapter.SucursalItem
                    List<SucursalesAdapter.SucursalItem> items = new ArrayList<>();
                    for (SucursalRepository.SucursalWithDistance item : sucursalesWithDistance) {
                        // Convertir Sucursal a SucursalEntity (necesitamos crear un método de conversión)
                        SucursalEntity entity = convertToEntity(item.getSucursal());
                        items.add(new SucursalesAdapter.SucursalItem(entity, item.getDistance()));
                    }
                    adapter.submitListWithDistance(items);
                });
        } else {
            // Ordenar por nombre
            sucursalesFiltradas.sort((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()));
            // Convertir SucursalEntity a SucursalItem
            List<SucursalesAdapter.SucursalItem> items = new ArrayList<>();
            for (SucursalEntity entity : sucursalesFiltradas) {
                items.add(new SucursalesAdapter.SucursalItem(entity, 0.0)); // Sin distancia
            }
            adapter.submitListWithDistance(items);
        }
        
        // Mostrar mensaje si no hay sucursales
        if (sucursalesFiltradas.isEmpty()) {
            if (estadoSeleccionado.isEmpty() && queryBusqueda.isEmpty()) {
                textViewEmpty.setText("No hay sucursales disponibles");
            } else {
                textViewEmpty.setText("No se encontraron sucursales con los filtros aplicados");
            }
            textViewEmpty.setVisibility(View.VISIBLE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
        }
    }
    
    private void updateLocationChipState(boolean enabled) {
        for (int i = 0; i < chipGroupOrden.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupOrden.getChildAt(i);
            if ("Por distancia".equals(chip.getText().toString())) {
                chip.setEnabled(enabled);
                if (!enabled && chip.isChecked()) {
                    // Si estaba seleccionado y se deshabilita, seleccionar "Por nombre"
                    chip.setChecked(false);
                    ((Chip) chipGroupOrden.getChildAt(0)).setChecked(true);
                    ordenSeleccionado = "nombre";
                }
                break;
            }
        }
    }
    
    private void uncheckOtherOrdenChips(Chip selectedChip) {
        for (int i = 0; i < chipGroupOrden.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupOrden.getChildAt(i);
            if (chip != selectedChip) {
                chip.setChecked(false);
            }
        }
    }
    
    private void uncheckOtherEstadoChips(Chip selectedChip) {
        for (int i = 0; i < chipGroupEstado.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupEstado.getChildAt(i);
            if (chip != selectedChip) {
                chip.setChecked(false);
            }
        }
    }
    
    private void showSucursalDetails(SucursalEntity sucursal) {
        String message = String.format(
            "Sucursal: %s\nDirección: %s\nHorario: %s\nEstado: %s",
            sucursal.getNombre(),
            sucursal.getDireccion(),
            sucursal.getHorario(),
            sucursal.getEstado().equals("activo") ? "Activa" : "Inactiva"
        );
        
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
    
    private void showSucursalOptions(SucursalEntity sucursal) {
        // Aquí se podrían mostrar opciones como "Ver en mapa", "Llamar", etc.
        Toast.makeText(getContext(), 
            "Opciones para: " + sucursal.getNombre(), 
            Toast.LENGTH_SHORT).show();
    }
    
    private void showError(String error) {
        if (getView() != null) {
            Snackbar.make(getView(), error, Snackbar.LENGTH_LONG)
                .setAction("Reintentar", v -> viewModel.refreshSucursales())
                .show();
        }
    }
    
    private SucursalEntity convertToEntity(com.example.cafefidelidaqrdemo.models.Sucursal sucursal) {
        SucursalEntity entity = new SucursalEntity();
        entity.setId_sucursal(sucursal.getId());
        entity.setNombre(sucursal.getNombre());
        entity.setDireccion(sucursal.getDireccion());
        entity.setLat(sucursal.getLatitud());
        entity.setLon(sucursal.getLongitud());
        // Combinar horarios de apertura y cierre
        String horario = sucursal.getHorarioApertura() + " - " + sucursal.getHorarioCierre();
        entity.setHorario(horario);
        entity.setEstado(sucursal.isActiva() ? "activo" : "inactivo");
        return entity;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refrescar datos si es necesario
        if (sucursalesOriginales.isEmpty()) {
            viewModel.loadSucursales();
        }
        
        // Verificar permisos de ubicación por si cambiaron
        checkLocationPermission();
    }
}