package com.example.cafefidelidaqrdemo.ui.admin;

import android.annotation.SuppressLint;
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
import com.example.cafefidelidaqrdemo.models.Sucursal;
import com.example.cafefidelidaqrdemo.ui.admin.adapters.SucursalesAdminAdapter;
import com.example.cafefidelidaqrdemo.ui.admin.viewmodels.SucursalesAdminViewModel;

// Google Location Services
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
// import com.google.android.gms.maps.CameraUpdateFactory;
// import com.google.android.gms.maps.GoogleMap;
// import com.google.android.gms.maps.OnMapReadyCallback;
// import com.google.android.gms.maps.SupportMapFragment;
// import com.google.android.gms.maps.model.LatLng;
// import com.google.android.gms.maps.model.MarkerOptions;

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
public class FragmentSucursalesAdmin extends Fragment /* implements OnMapReadyCallback */ {
    
    private FragmentSucursalesAdminBinding binding;
    private SucursalesAdminViewModel viewModel;
    private SucursalesAdminAdapter adapter;
    private List<Sucursal> sucursalesList = new ArrayList<>();
    private boolean mostrarSoloActivas = true;
    // private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    // private Geocoder geocoder;
    
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
        // geocoder = new Geocoder(getContext(), Locale.getDefault());
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
            public void onSucursalClick(Sucursal sucursal) {
                mostrarDetalleSucursal(sucursal);
            }
            
            public void onEditarClick(Sucursal sucursal) {
                mostrarDialogoEditarSucursal(sucursal);
            }
            
            public void onToggleActivoClick(Sucursal sucursal) {
                toggleEstadoSucursal(sucursal);
            }
            
            public void onVerEnMapaClick(Sucursal sucursal) {
                mostrarSucursalEnMapa(sucursal);
            }
            
            public void onEliminarClick(Sucursal sucursal) {
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
        // Configurar toolbar
        if (binding.toolbar != null) {
            binding.toolbar.setNavigationOnClickListener(v -> volverAlMenuPrincipal());
        }
        
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
        
        // Botón limpiar búsqueda no está disponible en este layout
        
        // Botón toggle vista no está disponible en este layout
    }
    
    private void setupSearchView() {
        binding.editTextBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                
                // Funcionalidad de filtrado no implementada en el adapter
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupMap() {
        // Mapa no está disponible en este layout
    }
    
    // @Override
    /*
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
    */
    
    private void actualizarListaSucursales(List<Sucursal> sucursales) {
        if (sucursales != null) {
            sucursalesList.clear();
            sucursalesList.addAll(sucursales);
            adapter.notifyDataSetChanged();
            
            // Actualizar marcadores en el mapa
            // actualizarMarcadoresMapa();
            
            // Mostrar/ocultar RecyclerView según contenido
            if (sucursales.isEmpty()) {
                binding.recyclerViewSucursales.setVisibility(View.GONE);
                // TextView de lista vacía no está disponible en el layout
            } else {
                binding.recyclerViewSucursales.setVisibility(View.VISIBLE);
            }
        }
    }
    
    /*
    private void actualizarMarcadoresMapa() {
        if (mMap != null) {
            mMap.clear();
            
            for (Sucursal sucursal : sucursalesList) {
                if (sucursal.getLatitud() != 0 && sucursal.getLongitud() != 0) {
                LatLng posicion = new LatLng(sucursal.getLatitud(), sucursal.getLongitud());
                    
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(posicion)
                            .title(sucursal.getNombre())
                            .snippet(sucursal.getDireccion());
                    
                    // Cambiar color según estado
                    if (!sucursal.isActiva()) {
                        // Marcador gris para sucursales inactivas
                        markerOptions.alpha(0.5f);
                    }
                    
                    mMap.addMarker(markerOptions);
                }
            }
        }
    }
    */
    
    private void toggleFiltroActivas() {
        mostrarSoloActivas = !mostrarSoloActivas;
        actualizarTextoFiltro();
        // Evitar acumulación de múltiples observers al alternar el filtro
        viewModel.getSucursalesActivas().removeObservers(getViewLifecycleOwner());
        viewModel.getAllSucursales().removeObservers(getViewLifecycleOwner());

        if (mostrarSoloActivas) {
            viewModel.getSucursalesActivas().observe(getViewLifecycleOwner(), this::actualizarListaSucursales);
        } else {
            viewModel.getAllSucursales().observe(getViewLifecycleOwner(), this::actualizarListaSucursales);
        }
    }
    
    private void actualizarTextoFiltro() {
        // buttonFiltro no está disponible en el layout
        // binding.buttonFiltro.setText(
        //         mostrarSoloActivas ? "Mostrar Todas" : "Solo Activas"
        // );
    }
    
    private void toggleVistaMapaLista() {
        // Funcionalidad de toggle entre vista mapa/lista no implementada
        // Los layouts layoutLista y layoutMapa no están disponibles
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
                    Sucursal nuevaSucursal = crearSucursalDesdeFormulario(dialogBinding);
                    viewModel.crearSucursal(nuevaSucursal);
                    dialog.dismiss();
                }
            });
        });
        
        configurarFormularioSucursal(dialogBinding, null);
        dialog.show();
    }
    
    private void mostrarDialogoEditarSucursal(Sucursal sucursal) {
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
                    Sucursal sucursalEditada = crearSucursalDesdeFormulario(dialogBinding);
                    sucursalEditada.setId(sucursal.getId());
                    viewModel.actualizarSucursal(sucursalEditada);
                    dialog.dismiss();
                }
            });
        });
        
        configurarFormularioSucursal(dialogBinding, sucursal);
        dialog.show();
    }
    
    private void configurarFormularioSucursal(DialogSucursalBinding dialogBinding, Sucursal sucursal) {
        // Configurar spinner de días
        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        ArrayAdapter<String> adapterDias = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, diasSemana);
        adapterDias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        if (sucursal != null) {
            // Modo edición
            dialogBinding.editTextNombre.setText(sucursal.getNombre());
            dialogBinding.editTextDireccion.setText(sucursal.getDireccion());
            dialogBinding.editTextImagenUrl.setText(sucursal.getImagenUrl());
            // editTextCiudad, editTextTelefono, editTextEmail, editTextGerente,
            // editTextCapacidad, editTextDescripcion y switchActivo no están disponibles en el layout
            
            latitudSeleccionada = sucursal.getLatitud();
            longitudSeleccionada = sucursal.getLongitud();
            horarioAperturaSeleccionado = sucursal.getHorarioApertura();
            horarioCierreSeleccionado = sucursal.getHorarioCierre();
            
            // textViewUbicacion no está disponible en el layout
        } else {
            // Modo creación
            // switchActivo no está disponible en el layout
            obtenerUbicacionActual(dialogBinding);
        }
        
        // Configurar botones de horario
        // dialogBinding.buttonHorarioApertura.setText(horarioAperturaSeleccionado);
        dialogBinding.editTextHorario.setText(horarioCierreSeleccionado);
        
        // dialogBinding.buttonHorarioApertura.setOnClickListener(v -> 
        //        mostrarSelectorHorario(true, dialogBinding));
        
        dialogBinding.editTextHorario.setOnClickListener(v -> 
                mostrarSelectorHorario(false, dialogBinding));
        
        // Configurar botones de ubicación
        dialogBinding.btnUbicacionActual.setOnClickListener(v -> 
                obtenerUbicacionActual(dialogBinding));
        
        dialogBinding.btnSeleccionarMapa.setOnClickListener(v -> 
                seleccionarUbicacionEnMapa(dialogBinding));
    }
    
    @SuppressLint("MissingPermission")
    private void obtenerUbicacionActual(DialogSucursalBinding dialogBinding) {
        // Verificar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(getContext(), 
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(getContext(), 
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            // Solicitar permisos
            requestPermissions(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1001);
            return;
        }
        
        // Obtener ubicación actual usando Google Location Services
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitudSeleccionada = location.getLatitude();
                        longitudSeleccionada = location.getLongitude();
                        
                        // Actualizar campos de coordenadas
                        dialogBinding.editTextLatitud.setText(String.valueOf(latitudSeleccionada));
                        dialogBinding.editTextLongitud.setText(String.valueOf(longitudSeleccionada));
                        
                        Toast.makeText(getContext(), "Ubicación actual obtenida", Toast.LENGTH_SHORT).show();
                    } else {
                        // Fallback a ubicación de ejemplo si no se puede obtener la ubicación
                        latitudSeleccionada = 4.6097;
                        longitudSeleccionada = -74.0817;
                        
                        dialogBinding.editTextLatitud.setText(String.valueOf(latitudSeleccionada));
                        dialogBinding.editTextLongitud.setText(String.valueOf(longitudSeleccionada));
                        
                        Toast.makeText(getContext(), "No se pudo obtener ubicación. Usando ubicación de ejemplo (Bogotá)", Toast.LENGTH_LONG).show();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Fallback a ubicación de ejemplo en caso de error
                    latitudSeleccionada = 4.6097;
                    longitudSeleccionada = -74.0817;
                    
                    dialogBinding.editTextLatitud.setText(String.valueOf(latitudSeleccionada));
                    dialogBinding.editTextLongitud.setText(String.valueOf(longitudSeleccionada));
                    
                    Toast.makeText(getContext(), "Error al obtener ubicación: " + e.getMessage() + ". Usando ubicación de ejemplo", Toast.LENGTH_LONG).show();
                }
            });
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
                        // dialogBinding.buttonHorarioApertura.setText(horario);
                    } else {
                        horarioCierreSeleccionado = horario;
                        dialogBinding.editTextHorario.setText(horario);
                    }
                },
                hora, minuto, true
        );
        
        timePickerDialog.show();
    }
    
    private void seleccionarUbicacionEnMapa(DialogSucursalBinding dialogBinding) {
        // Implementación simplificada - permitir entrada manual de coordenadas
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccionar Ubicación");
        builder.setMessage("Puede ingresar las coordenadas manualmente en los campos de Latitud y Longitud, o usar algunas ubicaciones predefinidas:");
        
        builder.setPositiveButton("Bogotá Centro", (dialog, which) -> {
            latitudSeleccionada = 4.6097;
            longitudSeleccionada = -74.0817;
            dialogBinding.editTextLatitud.setText(String.valueOf(latitudSeleccionada));
            dialogBinding.editTextLongitud.setText(String.valueOf(longitudSeleccionada));
        });
        
        builder.setNeutralButton("Medellín Centro", (dialog, which) -> {
            latitudSeleccionada = 6.2442;
            longitudSeleccionada = -75.5812;
            dialogBinding.editTextLatitud.setText(String.valueOf(latitudSeleccionada));
            dialogBinding.editTextLongitud.setText(String.valueOf(longitudSeleccionada));
        });
        
        builder.setNegativeButton("Cancelar", null);
        
        builder.show();
    }
    
    private void buscarDireccion(DialogSucursalBinding dialogBinding) {
        String direccion = dialogBinding.editTextDireccion.getText().toString().trim();
        // Campo ciudad no disponible en el layout
        
        if (direccion.isEmpty()) {
            Toast.makeText(getContext(), "Ingrese dirección", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // try {
        //     List<Address> addresses = geocoder.getFromLocationName(
        //             direccion + ", Colombia", 1);
        //     
        //     if (!addresses.isEmpty()) {
        //         Address address = addresses.get(0);
        //         latitudSeleccionada = address.getLatitude();
        //         longitudSeleccionada = address.getLongitude();
        //         
        //         // dialogBinding.textViewUbicacion.setText(
        //         //         String.format("Lat: %.6f, Lng: %.6f", latitudSeleccionada, longitudSeleccionada));
        //         
        //         Toast.makeText(getContext(), "Ubicación encontrada", Toast.LENGTH_SHORT).show();
        //     } else {
        //         Toast.makeText(getContext(), "No se encontró la dirección", Toast.LENGTH_SHORT).show();
        //     }
        // } catch (IOException e) {
        //     Toast.makeText(getContext(), "Error al buscar dirección", Toast.LENGTH_SHORT).show();
        // }
        
        // Funcionalidad de geocoding deshabilitada
        Toast.makeText(getContext(), "Búsqueda de direcciones temporalmente deshabilitada", Toast.LENGTH_SHORT).show();
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
        
        // Campo ciudad no disponible en el layout
        
        // Validar coordenadas
        if (latitudSeleccionada == 0.0 || longitudSeleccionada == 0.0) {
            Toast.makeText(getContext(), "Debe seleccionar una ubicación válida", Toast.LENGTH_SHORT).show();
            esValido = false;
        }
        
        // Campo capacidad no disponible en el layout
        
        return esValido;
    }
    
    private Sucursal crearSucursalDesdeFormulario(DialogSucursalBinding dialogBinding) {
        Sucursal sucursal = new Sucursal();
        
        // Generar ID único para la sucursal
        int sucursalId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        sucursal.setId(String.valueOf(sucursalId));
        
        sucursal.setNombre(dialogBinding.editTextNombre.getText().toString().trim());
        sucursal.setDireccion(dialogBinding.editTextDireccion.getText().toString().trim());
        String imagenUrl = dialogBinding.editTextImagenUrl.getText() != null
                ? dialogBinding.editTextImagenUrl.getText().toString().trim()
                : "";
        sucursal.setImagenUrl(imagenUrl);
        // Campos ciudad, telefono y email no disponibles en database.entities.SucursalEntity
        sucursal.setLatitud(latitudSeleccionada);
        sucursal.setLongitud(longitudSeleccionada);
        // Usar horarios separados en models.Sucursal
        sucursal.setHorarioApertura(horarioAperturaSeleccionado);
        sucursal.setHorarioCierre(horarioCierreSeleccionado);
        // Campo diasOperacion no disponible en database.entities.SucursalEntity
        // if (!diasSeleccionados.isEmpty()) {
        //     sucursal.setDiasOperacion(String.join(",", diasSeleccionados));
        // }
        // Campo gerente no disponible en database.entities.SucursalEntity
        // Establecer estado activo por defecto
        sucursal.setActiva(true);
        
        // Campo capacidadMaxima no disponible en database.entities.SucursalEntity
        
        return sucursal;
    }
    
    private void mostrarDetalleSucursal(Sucursal sucursal) {
        String detalles = String.format(
                "Nombre: %s\n" +
                "Dirección: %s\n" +
                "Horario: %s - %s\n" +
                "Estado: %s\n" +
                "Coordenadas: %.6f, %.6f",
                sucursal.getNombre(),
                sucursal.getDireccion(),
                sucursal.getHorarioApertura(),
                sucursal.getHorarioCierre(),
                sucursal.isActiva() ? "Activa" : "Inactiva",
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
    
    private void mostrarSucursalEnMapa(Sucursal sucursal) {
        // if (mMap != null) {
        //     LatLng posicion = new LatLng(sucursal.getLatitud(), sucursal.getLongitud());
        //     mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posicion, 15));
        //     
        //     // Cambiar a vista de mapa no implementado
        //     // Los layouts no están disponibles
        // }
        
        // Método deshabilitado - Google Maps removido
    }
    
    private void toggleEstadoSucursal(Sucursal sucursal) {
        String accion = sucursal.isActiva() ? "desactivar" : "activar";
        String mensaje = String.format("¿Está seguro que desea %s la sucursal '%s'?", accion, sucursal.getNombre());
        
        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar acción")
                .setMessage(mensaje)
                .setPositiveButton("Sí", (dialog, which) -> {
                    long sucursalId = Long.parseLong(sucursal.getId());
                    if (sucursal.isActiva()) {
                        viewModel.desactivarSucursal(sucursalId, "Desactivada por administrador");
                    } else {
                        viewModel.activarSucursal(sucursalId);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    
    private void confirmarEliminacionSucursal(Sucursal sucursal) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Sucursal")
                .setMessage(String.format("¿Está seguro que desea eliminar la sucursal '%s'?\n\nEsta acción no se puede deshacer.", sucursal.getNombre()))
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.eliminarSucursal(Long.parseLong(sucursal.getId()));
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
    
    private void volverAlMenuPrincipal() {
        // Navegar de vuelta al dashboard de administración
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
