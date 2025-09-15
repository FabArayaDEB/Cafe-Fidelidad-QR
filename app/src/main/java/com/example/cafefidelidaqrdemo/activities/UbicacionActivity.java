package com.example.cafefidelidaqrdemo.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.adapters.UbicacionAdapter;
import com.example.cafefidelidaqrdemo.viewmodels.UbicacionViewModel;

import java.util.ArrayList;

public class UbicacionActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 1002;
    
    // UI Components
    private TextView tvLocationStatus;
    private TextView tvCurrentLocation;
    private TextView tvLocationCount;
    private TextView tvBranchVisits;
    private Switch switchTracking;
    private Button btnGetLocation;
    private Button btnRequestPermissions;
    private Button btnOpenSettings;
    private Button btnSyncLocations;
    private Button btnCleanOldLocations;
    private RecyclerView recyclerViewLocations;
    
    // ViewModel y Adapter
    private UbicacionViewModel ubicacionViewModel;
    private UbicacionAdapter ubicacionAdapter;
    
    // Estado
    private boolean isTrackingEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);
        
        initializeViews();
        initializeViewModel();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
        
        // Establecer usuario actual (ejemplo)
        ubicacionViewModel.setCurrentUserId(1); // Cambiar por el ID real del usuario logueado
    }

    private void initializeViews() {
        tvLocationStatus = findViewById(R.id.tv_location_status);
        tvCurrentLocation = findViewById(R.id.tv_current_location);
        tvLocationCount = findViewById(R.id.tv_location_count);
        tvBranchVisits = findViewById(R.id.tv_branch_visits);
        switchTracking = findViewById(R.id.switch_tracking);
        btnGetLocation = findViewById(R.id.btn_get_location);
        btnRequestPermissions = findViewById(R.id.btn_request_permissions);
        btnOpenSettings = findViewById(R.id.btn_open_settings);
        btnSyncLocations = findViewById(R.id.btn_sync_locations);
        btnCleanOldLocations = findViewById(R.id.btn_clean_old_locations);
        recyclerViewLocations = findViewById(R.id.recycler_view_locations);
    }

    private void initializeViewModel() {
        ubicacionViewModel = new ViewModelProvider(this).get(UbicacionViewModel.class);
    }

    private void setupRecyclerView() {
        ubicacionAdapter = new UbicacionAdapter(new ArrayList<>());
        recyclerViewLocations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLocations.setAdapter(ubicacionAdapter);
    }

    private void setupClickListeners() {
        // Switch de seguimiento
        switchTracking.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ubicacionViewModel.startLocationTracking();
            } else {
                ubicacionViewModel.stopLocationTracking();
            }
        });
        
        // Botón obtener ubicación
        btnGetLocation.setOnClickListener(v -> {
            ubicacionViewModel.getCurrentLocationOnce();
        });
        
        // Botón solicitar permisos
        btnRequestPermissions.setOnClickListener(v -> {
            requestLocationPermissions();
        });
        
        // Botón abrir configuración
        btnOpenSettings.setOnClickListener(v -> {
            openLocationSettings();
        });
        
        // Botón sincronizar
        btnSyncLocations.setOnClickListener(v -> {
            ubicacionViewModel.syncLocations();
        });
        
        // Botón limpiar ubicaciones antiguas
        btnCleanOldLocations.setOnClickListener(v -> {
            showCleanOldLocationsDialog();
        });
    }

    private void observeViewModel() {
        // Observar estado de permisos
        ubicacionViewModel.getHasLocationPermissions().observe(this, hasPermissions -> {
            updateUIBasedOnPermissions(hasPermissions);
        });
        
        // Observar estado de ubicación habilitada
        ubicacionViewModel.getIsLocationEnabled().observe(this, isEnabled -> {
            updateUIBasedOnLocationEnabled(isEnabled);
        });
        
        // Observar ubicación actual
        ubicacionViewModel.getCurrentLocation().observe(this, location -> {
            updateCurrentLocationDisplay(location);
        });
        
        // Observar estado de seguimiento
        ubicacionViewModel.getIsTracking().observe(this, isTracking -> {
            isTrackingEnabled = isTracking;
            switchTracking.setChecked(isTracking);
        });
        
        // Observar estado de operaciones
        ubicacionViewModel.getLocationStatus().observe(this, status -> {
            tvLocationStatus.setText(status);
        });
        
        // Observar conteo de ubicaciones
        ubicacionViewModel.getLocationCount().observe(this, count -> {
            tvLocationCount.setText("Ubicaciones guardadas: " + count);
        });
        
        // Observar visitas a sucursales
        ubicacionViewModel.getBranchVisitCount().observe(this, count -> {
            tvBranchVisits.setText("Visitas a sucursales: " + count);
        });
        
        // Observar ubicaciones recientes
        ubicacionViewModel.getRecentLocations().observe(this, locations -> {
            ubicacionAdapter.updateLocations(locations);
        });
    }

    private void updateUIBasedOnPermissions(boolean hasPermissions) {
        btnRequestPermissions.setEnabled(!hasPermissions);
        btnGetLocation.setEnabled(hasPermissions);
        switchTracking.setEnabled(hasPermissions);
        
        if (!hasPermissions) {
            tvLocationStatus.setText("Permisos de ubicación requeridos");
            switchTracking.setChecked(false);
        }
    }

    private void updateUIBasedOnLocationEnabled(boolean isEnabled) {
        btnOpenSettings.setEnabled(!isEnabled);
        
        if (!isEnabled) {
            tvLocationStatus.setText("Ubicación deshabilitada en el dispositivo");
            switchTracking.setChecked(false);
        }
    }

    private void updateCurrentLocationDisplay(Location location) {
        if (location != null) {
            String locationText = String.format(
                "Lat: %.6f, Lng: %.6f\nPrecisión: %.1f metros",
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy()
            );
            tvCurrentLocation.setText(locationText);
        } else {
            tvCurrentLocation.setText("Ubicación no disponible");
        }
    }

    private void requestLocationPermissions() {
        String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        };
        
        // Verificar si ya tenemos los permisos
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }
        
        if (!allPermissionsGranted) {
            // Mostrar explicación si es necesario
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermissionExplanationDialog(permissions);
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ubicacionViewModel.onPermissionResult(true);
        }
    }

    private void showPermissionExplanationDialog(String[] permissions) {
        new AlertDialog.Builder(this)
            .setTitle("Permisos de Ubicación")
            .setMessage("Esta aplicación necesita acceso a la ubicación para guardar y rastrear tus visitas a las sucursales del café.")
            .setPositiveButton("Conceder", (dialog, which) -> {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            })
            .setNegativeButton("Cancelar", (dialog, which) -> {
                Toast.makeText(this, "Los permisos de ubicación son necesarios para esta funcionalidad", Toast.LENGTH_LONG).show();
            })
            .show();
    }

    private void openLocationSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, LOCATION_SETTINGS_REQUEST_CODE);
    }

    private void showCleanOldLocationsDialog() {
        String[] options = {"7 días", "30 días", "90 días", "180 días"};
        int[] days = {7, 30, 90, 180};
        
        new AlertDialog.Builder(this)
            .setTitle("Limpiar Ubicaciones Antiguas")
            .setItems(options, (dialog, which) -> {
                ubicacionViewModel.cleanOldLocations(days[which]);
                Toast.makeText(this, "Limpiando ubicaciones de más de " + days[which] + " días", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            
            ubicacionViewModel.onPermissionResult(allPermissionsGranted);
            
            if (allPermissionsGranted) {
                Toast.makeText(this, "Permisos de ubicación concedidos", Toast.LENGTH_SHORT).show();
                ubicacionViewModel.checkLocationEnabled();
            } else {
                Toast.makeText(this, "Permisos de ubicación denegados", Toast.LENGTH_LONG).show();
                
                // Verificar si el usuario marcó "No volver a preguntar"
                boolean shouldShowRationale = false;
                for (String permission : permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        shouldShowRationale = true;
                        break;
                    }
                }
                
                if (!shouldShowRationale) {
                    showGoToSettingsDialog();
                }
            }
        }
    }

    private void showGoToSettingsDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Permisos Requeridos")
            .setMessage("Los permisos de ubicación han sido denegados permanentemente. Puedes habilitarlos en la configuración de la aplicación.")
            .setPositiveButton("Ir a Configuración", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == LOCATION_SETTINGS_REQUEST_CODE) {
            // Verificar si la ubicación fue habilitada
            ubicacionViewModel.checkLocationEnabled();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verificar estado de permisos y ubicación al regresar a la actividad
        ubicacionViewModel.checkLocationEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detener seguimiento si está activo para ahorrar batería
        if (isTrackingEnabled) {
            ubicacionViewModel.stopLocationTracking();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // El ViewModel se encarga de limpiar recursos automáticamente
    }
}