package com.example.cafefidelidaqrdemo.ui.admin;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.databinding.FragmentSucursalesAdminBinding;
import com.example.cafefidelidaqrdemo.databinding.DialogSucursalBinding;
import com.example.cafefidelidaqrdemo.data.entities.SucursalEntity;
import com.example.cafefidelidaqrdemo.ui.admin.adapters.SucursalesAdminAdapter;
import com.example.cafefidelidaqrdemo.ui.admin.viewmodels.SucursalesAdminViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Fragment para administración CRUD de sucursales
 * Permite crear, editar, activar/desactivar sucursales con mapas y horarios
 */
public class FragmentSucursalesAdmin extends Fragment implements OnMapReadyCallback {
    
    private FragmentSucursalesAdminBinding binding;
    private SucursalesAdminViewModel viewModel;
    private SucursalesAdminAdapter adapter;
    private List<SucursalEntity> sucursalesList = new ArrayList<>();
    private boolean mostrarSoloActivas = true;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;
    
    // Variables para el diálogo de sucursal
    private double latitudSeleccionada = 0.0;
    private double longitudSeleccionada = 0.0;
    private String horarioAperturaSeleccionado = "08:00";
    private String horarioCierreSeleccionado = "18:00";
    private List<String> diasSeleccionados = new ArrayList<>();
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        geocoder = new Geocoder(getContext(), Locale.getDefault());
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSucursalesAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        setupRecyclerView();
        setupUI();
        setupObservers();
        setupClickListeners();
        setupSearchView();
        setupMap();
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sucursales_admin, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_toggle_filter) {
            toggleFiltroActivas();
            return true;
        } else if (id == R.id.action_view_map) {
            toggleVistaMapaLista();
            return true;
        } else if (id == R.id.action_export) {
            exportarSucursales();
            return true;
        } else if (id == R.id.action_sync) {
            sincronizarSucursales();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SucursalesAdminViewModel.class);
    }
    
    private void setupRecyclerView() {
        SucursalesAdminAdapter.OnSucursalClickListener listener = new SucursalesAdminAdapter.OnSucursalClickListener() {
            public void onSucursalClick(SucursalEntity sucursal) {
                mostrarDetalleSucursal(sucursal);
            }
            
            public void onEditarClick(SucursalEntity sucursal) {
                mostrarDialogoEditarSucursal(sucursal);
            }
            
            public void onToggleActivoClick(SucursalEntity sucursal) {
                toggleEstadoSucursal(sucursal);
            }
            
            public void onVerEnMapaClick(SucursalEntity sucursal) {
                mostrarSucursalEnMapa(sucursal);
            }
            
            public void onEliminarClick(SucursalEntity sucursal) {
                // Implementación temporal
                Toast.makeText(getContext(), "Eliminar: " + sucursal.getNombre(), Toast.LENGTH_SHORT).show();
            }
        };
        
        adapter = new SucursalesAdminAdapter(sucursalesList, listener);
        
        binding.recyclerViewSucursales.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewSucursales.setAdapter(adapter);
        
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                binding.recyclerViewSucursales.getContext(),
                LinearLayoutManager.VERTICAL);
        binding.recyclerViewSucursales.addItemDecoration(dividerItemDecoration);
    }
    
    private void setupUI() {
        // El título se establece en el toolbar del layout XML
        actualizarTextoFiltro();
        
        // SwipeRefreshLayout no está disponible en este layout
        
        // Inicialmente mostrar lista
        binding.recyclerViewSucursales.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
    }
    
    private void setupObservers() {
        // Observar lista de sucursales
        if (mostrarSoloActivas) {
            viewModel.getSucursalesActivas().observe(getViewLifecycleOwner(), this::actualizarListaSucursales);
        } else {
            viewModel.getAllSucursales().observe(getViewLifecycleOwner(), this::actualizarListaSucursales);
        }
        
        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                // SwipeRefreshLayout no está disponible en este layout
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
        
        // Observar estadísticas
        viewModel.getCountSucursalesActivas().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.textViewSucursalesActivas.setText(String.valueOf(count));
            }
        });
        
        viewModel.getCountSucursalesInactivas().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.textViewSucursalesInactivas.setText(String.valueOf(count));
            }
        });
    }
    
    private void setupClickListeners() {
        binding.fabAgregarSucursal.setOnClickListener(v -> mostrarDialogoNuevaSucursal());
        
        // SwipeRefreshLayout no está disponible en este layout
        
        // Botón de filtro no está disponible en este layout
        
        binding.buttonLimpiarBusqueda.setOnClickListener(v -> {
            binding.editTextBuscar.setText("");
            binding.buttonLimpiarBusqueda.setVisibility(View.GONE);
        });
        
        binding.buttonToggleVista.setOnClickListener(v -> toggleVistaMapaLista());
    }
    
    private void setupSearchView() {
        binding.editTextBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                
                if (query.isEmpty()) {
                    binding.buttonLimpiarBusqueda.setVisibility(View.GONE);
                    adapter.filtrarSucursales("");
                } else {
                    binding.buttonLimpiarBusqueda.setVisibility(View.VISIBLE);
                    adapter.filtrarSucursales(query);
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Configurar mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        
        // Centrar en Colombia
        LatLng colombia = new LatLng(4.5709, -74.2973);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(colombia, 6));
        
        // Agregar marcadores de sucursales existentes
        actualizarMarcadoresMapa();
        
        // Configurar click en mapa para seleccionar ubicación
        mMap.setOnMapClickListener(latLng -> {
            // Este listener se usará en los diálogos de creación/edición
        });
    }
    
    private void actualizarListaSucursales(List<SucursalEntity> sucursales) {
        if (sucursales != null) {
            sucursalesList.clear();
            sucursalesList.addAll(sucursales);
            adapter.notifyDataSetChanged();
            
            // Actualizar marcadores en el mapa
            actualizarMarcadoresMapa();
            
            // Mostrar/ocultar mensaje de lista vacía
            if (sucursales.isEmpty()) {
                binding.textViewListaVacia.setVisibility(View.VISIBLE);
                binding.textViewListaVacia.setText(
                        mostrarSoloActivas ? 
                        "No hay sucursales activas" : 
                        "No hay sucursales registradas"
                );
                binding.recyclerViewSucursales.setVisibility(View.GONE);
            } else {
                binding.textViewListaVacia.setVisibility(View.GONE);
                binding.recyclerViewSucursales.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void actualizarMarcadoresMapa() {
        if (mMap != null) {
            mMap.clear();
            
            for (SucursalEntity sucursal : sucursalesList) {
                if (sucursal.getLatitud() != 0 && sucursal.getLongitud() != 0) {
                    LatLng posicion = new LatLng(sucursal.getLatitud(), sucursal.getLongitud());
                    
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(posicion)
                            .title(sucursal.getNombre())
                            .snippet(sucursal.getDireccion());
                    
                    // Cambiar color según estado
                    if (!sucursal.isActivo()) {
                        // Marcador gris para sucursales inactivas
                        markerOptions.alpha(0.5f);
                    }
                    
                    mMap.addMarker(markerOptions);
                }
            }
        }
    }
    
    private void toggleFiltroActivas() {
        mostrarSoloActivas = !mostrarSoloActivas;
        actualizarTextoFiltro();
        
        if (mostrarSoloActivas) {
            viewModel.getSucursalesActivas().observe(getViewLifecycleOwner(), this::actualizarListaSucursales);
        } else {
            viewModel.getAllSucursales().observe(getViewLifecycleOwner(), this::actualizarListaSucursales);
        }
    }
    
    private void actualizarTextoFiltro() {
        binding.buttonFiltro.setText(
                mostrarSoloActivas ? "Mostrar Todas" : "Solo Activas"
        );
    }
    
    private void toggleVistaMapaLista() {
        if (binding.layoutLista.getVisibility() == View.VISIBLE) {
            // Cambiar a vista de mapa
            binding.layoutLista.setVisibility(View.GONE);
            binding.layoutMapa.setVisibility(View.VISIBLE);
            binding.buttonToggleVista.setText("Ver Lista");
        } else {
            // Cambiar a vista de lista
            binding.layoutLista.setVisibility(View.VISIBLE);
            binding.layoutMapa.setVisibility(View.GONE);
            binding.buttonToggleVista.setText("Ver Mapa");
        }
    }
    
    private void mostrarDialogoNuevaSucursal() {
        DialogSucursalBinding dialogBinding = DialogSucursalBinding.inflate(getLayoutInflater());
        
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Nueva Sucursal")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Crear", null)
                .setNegativeButton("Cancelar", null)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validarFormularioSucursal(dialogBinding)) {
                    SucursalEntity nuevaSucursal = crearSucursalDesdeFormulario(dialogBinding);
                    viewModel.crearSucursal(nuevaSucursal);
                    dialog.dismiss();
                }
            });
        });
        
        configurarFormularioSucursal(dialogBinding, null);
        dialog.show();
    }
    
    private void mostrarDialogoEditarSucursal(SucursalEntity sucursal) {
        DialogSucursalBinding dialogBinding = DialogSucursalBinding.inflate(getLayoutInflater());
        
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Editar Sucursal")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validarFormularioSucursal(dialogBinding)) {
                    SucursalEntity sucursalEditada = crearSucursalDesdeFormulario(dialogBinding);
                    sucursalEditada.setId(sucursal.getId());
                    sucursalEditada.setVersion(sucursal.getVersion());
                    sucursalEditada.setFechaCreacion(sucursal.getFechaCreacion());
                    viewModel.actualizarSucursal(sucursalEditada);
                    dialog.dismiss();
                }
            });
        });
        
        configurarFormularioSucursal(dialogBinding, sucursal);
        dialog.show();
    }
    
    private void configurarFormularioSucursal(DialogSucursalBinding dialogBinding, SucursalEntity sucursal) {
        // Configurar spinner de días
        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        ArrayAdapter<String> adapterDias = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, diasSemana);
        adapterDias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        if (sucursal != null) {
            // Modo edición
            dialogBinding.editTextNombre.setText(sucursal.getNombre());
            dialogBinding.editTextDireccion.setText(sucursal.getDireccion());
            dialogBinding.editTextCiudad.setText(sucursal.getCiudad());
            dialogBinding.editTextTelefono.setText(sucursal.getTelefono());
            dialogBinding.editTextEmail.setText(sucursal.getEmail());
            dialogBinding.editTextGerente.setText(sucursal.getGerente());
            dialogBinding.editTextCapacidad.setText(String.valueOf(sucursal.getCapacidadMaxima()));
            dialogBinding.editTextDescripcion.setText(sucursal.getDescripcion());
            dialogBinding.switchActivo.setChecked(sucursal.isActivo());
            
            latitudSeleccionada = sucursal.getLatitud();
            longitudSeleccionada = sucursal.getLongitud();
            horarioAperturaSeleccionado = sucursal.getHorarioApertura();
            horarioCierreSeleccionado = sucursal.getHorarioCierre();
            
            dialogBinding.textViewUbicacion.setText(
                    String.format("Lat: %.6f, Lng: %.6f", latitudSeleccionada, longitudSeleccionada));
        } else {
            // Modo creación
            dialogBinding.switchActivo.setChecked(true);
            obtenerUbicacionActual(dialogBinding);
        }
        
        // Configurar botones de horario
        dialogBinding.buttonHorarioApertura.setText(horarioAperturaSeleccionado);
        dialogBinding.buttonHorarioCierre.setText(horarioCierreSeleccionado);
        
        dialogBinding.buttonHorarioApertura.setOnClickListener(v -> 
                mostrarSelectorHorario(true, dialogBinding));
        
        dialogBinding.buttonHorarioCierre.setOnClickListener(v -> 
                mostrarSelectorHorario(false, dialogBinding));
        
        // Configurar botón de ubicación
        dialogBinding.buttonSeleccionarUbicacion.setOnClickListener(v -> 
                seleccionarUbicacionEnMapa(dialogBinding));
        
        // Configurar geocodificación inversa
        dialogBinding.buttonBuscarDireccion.setOnClickListener(v -> 
                buscarDireccion(dialogBinding));
    }
    
    private void obtenerUbicacionActual(DialogSucursalBinding dialogBinding) {
        if (ActivityCompat.checkSelfPermission(getContext(), 
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            latitudSeleccionada = location.getLatitude();
                            longitudSeleccionada = location.getLongitude();
                            
                            dialogBinding.textViewUbicacion.setText(
                                    String.format("Lat: %.6f, Lng: %.6f", latitudSeleccionada, longitudSeleccionada));
                        }
                    });
        }
    }
    
    private void mostrarSelectorHorario(boolean esApertura, DialogSucursalBinding dialogBinding) {
        Calendar calendar = Calendar.getInstance();
        int hora = esApertura ? 8 : 18;
        int minuto = 0;
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    String horario = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    
                    if (esApertura) {
                        horarioAperturaSeleccionado = horario;
                        dialogBinding.buttonHorarioApertura.setText(horario);
                    } else {
                        horarioCierreSeleccionado = horario;
                        dialogBinding.buttonHorarioCierre.setText(horario);
                    }
                },
                hora, minuto, true
        );
        
        timePickerDialog.show();
    }
    
    private void seleccionarUbicacionEnMapa(DialogSucursalBinding dialogBinding) {
        // Implementar selector de ubicación en mapa
        Toast.makeText(getContext(), "Toque en el mapa para seleccionar ubicación", Toast.LENGTH_SHORT).show();
    }
    
    private void buscarDireccion(DialogSucursalBinding dialogBinding) {
        String direccion = dialogBinding.editTextDireccion.getText().toString().trim();
        String ciudad = dialogBinding.editTextCiudad.getText().toString().trim();
        
        if (direccion.isEmpty() || ciudad.isEmpty()) {
            Toast.makeText(getContext(), "Ingrese dirección y ciudad", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            List<Address> addresses = geocoder.getFromLocationName(
                    direccion + ", " + ciudad + ", Colombia", 1);
            
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                latitudSeleccionada = address.getLatitude();
                longitudSeleccionada = address.getLongitude();
                
                dialogBinding.textViewUbicacion.setText(
                        String.format("Lat: %.6f, Lng: %.6f", latitudSeleccionada, longitudSeleccionada));
                
                Toast.makeText(getContext(), "Ubicación encontrada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No se encontró la dirección", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al buscar dirección", Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean validarFormularioSucursal(DialogSucursalBinding dialogBinding) {
        boolean esValido = true;
        
        // Validar nombre
        String nombre = dialogBinding.editTextNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            dialogBinding.editTextNombre.setError("El nombre es obligatorio");
            esValido = false;
        }
        
        // Validar dirección
        String direccion = dialogBinding.editTextDireccion.getText().toString().trim();
        if (direccion.isEmpty()) {
            dialogBinding.editTextDireccion.setError("La dirección es obligatoria");
            esValido = false;
        }
        
        // Validar ciudad
        String ciudad = dialogBinding.editTextCiudad.getText().toString().trim();
        if (ciudad.isEmpty()) {
            dialogBinding.editTextCiudad.setError("La ciudad es obligatoria");
            esValido = false;
        }
        
        // Validar coordenadas
        if (latitudSeleccionada == 0.0 || longitudSeleccionada == 0.0) {
            Toast.makeText(getContext(), "Debe seleccionar una ubicación válida", Toast.LENGTH_SHORT).show();
            esValido = false;
        }
        
        // Validar capacidad
        String capacidadStr = dialogBinding.editTextCapacidad.getText().toString().trim();
        if (!capacidadStr.isEmpty()) {
            try {
                int capacidad = Integer.parseInt(capacidadStr);
                if (capacidad <= 0) {
                    dialogBinding.editTextCapacidad.setError("La capacidad debe ser mayor a 0");
                    esValido = false;
                }
            } catch (NumberFormatException e) {
                dialogBinding.editTextCapacidad.setError("Ingrese una capacidad válida");
                esValido = false;
            }
        }
        
        return esValido;
    }
    
    private SucursalEntity crearSucursalDesdeFormulario(DialogSucursalBinding dialogBinding) {
        SucursalEntity sucursal = new SucursalEntity();
        
        sucursal.setNombre(dialogBinding.editTextNombre.getText().toString().trim());
        sucursal.setDireccion(dialogBinding.editTextDireccion.getText().toString().trim());
        sucursal.setCiudad(dialogBinding.editTextCiudad.getText().toString().trim());
        sucursal.setTelefono(dialogBinding.editTextTelefono.getText().toString().trim());
        sucursal.setEmail(dialogBinding.editTextEmail.getText().toString().trim());
        sucursal.setLatitud(latitudSeleccionada);
        sucursal.setLongitud(longitudSeleccionada);
        sucursal.setHorarioApertura(horarioAperturaSeleccionado);
        sucursal.setHorarioCierre(horarioCierreSeleccionado);
        sucursal.setDiasOperacion(String.join(",", diasSeleccionados));
        sucursal.setGerente(dialogBinding.editTextGerente.getText().toString().trim());
        sucursal.setDescripcion(dialogBinding.editTextDescripcion.getText().toString().trim());
        sucursal.setActivo(dialogBinding.switchActivo.isChecked());
        
        String capacidadStr = dialogBinding.editTextCapacidad.getText().toString().trim();
        if (!capacidadStr.isEmpty()) {
            sucursal.setCapacidadMaxima(Integer.parseInt(capacidadStr));
        } else {
            sucursal.setCapacidadMaxima(50); // Valor por defecto
        }
        
        long currentTime = System.currentTimeMillis();
        sucursal.setFechaCreacion(currentTime);
        sucursal.setFechaModificacion(currentTime);
        sucursal.setCreadoPor("admin");
        sucursal.setModificadoPor("admin");
        sucursal.setVersion(1);
        
        return sucursal;
    }
    
    private void mostrarDetalleSucursal(SucursalEntity sucursal) {
        String detalles = String.format(
                "Nombre: %s\n" +
                "Dirección: %s\n" +
                "Ciudad: %s\n" +
                "Teléfono: %s\n" +
                "Email: %s\n" +
                "Horario: %s - %s\n" +
                "Capacidad: %d personas\n" +
                "Gerente: %s\n" +
                "Estado: %s\n" +
                "Coordenadas: %.6f, %.6f",
                sucursal.getNombre(),
                sucursal.getDireccion(),
                sucursal.getCiudad(),
                sucursal.getTelefono(),
                sucursal.getEmail(),
                sucursal.getHorarioApertura(),
                sucursal.getHorarioCierre(),
                sucursal.getCapacidadMaxima(),
                sucursal.getGerente(),
                sucursal.isActivo() ? "Activa" : "Inactiva",
                sucursal.getLatitud(),
                sucursal.getLongitud()
        );
        
        new AlertDialog.Builder(getContext())
                .setTitle("Detalle de Sucursal")
                .setMessage(detalles)
                .setPositiveButton("Cerrar", null)
                .setNeutralButton("Editar", (dialog, which) -> mostrarDialogoEditarSucursal(sucursal))
                .setNegativeButton("Ver en Mapa", (dialog, which) -> mostrarSucursalEnMapa(sucursal))
                .show();
    }
    
    private void mostrarSucursalEnMapa(SucursalEntity sucursal) {
        if (mMap != null) {
            LatLng posicion = new LatLng(sucursal.getLatitud(), sucursal.getLongitud());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posicion, 15));
            
            // Cambiar a vista de mapa
            binding.layoutLista.setVisibility(View.GONE);
            binding.layoutMapa.setVisibility(View.VISIBLE);
            binding.buttonToggleVista.setText("Ver Lista");
        }
    }
    
    private void toggleEstadoSucursal(SucursalEntity sucursal) {
        String accion = sucursal.isActivo() ? "desactivar" : "activar";
        String mensaje = String.format("¿Está seguro que desea %s la sucursal '%s'?", accion, sucursal.getNombre());
        
        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar acción")
                .setMessage(mensaje)
                .setPositiveButton("Sí", (dialog, which) -> {
                    if (sucursal.isActivo()) {
                        viewModel.desactivarSucursal(sucursal.getId(), "Desactivada por administrador");
                    } else {
                        viewModel.activarSucursal(sucursal.getId());
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    
    private void confirmarEliminacionSucursal(SucursalEntity sucursal) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Sucursal")
                .setMessage(String.format("¿Está seguro que desea eliminar la sucursal '%s'?\n\nEsta acción no se puede deshacer.", sucursal.getNombre()))
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.eliminarSucursal(sucursal.getId());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    private void exportarSucursales() {
        viewModel.exportarSucursales();
        Toast.makeText(getContext(), "Exportando sucursales...", Toast.LENGTH_SHORT).show();
    }
    
    private void sincronizarSucursales() {
        viewModel.sincronizarConServidor();
        Toast.makeText(getContext(), "Sincronizando con servidor...", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}