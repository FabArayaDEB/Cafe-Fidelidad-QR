package com.example.cafefidelidaqrdemo.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;
import com.example.cafefidelidaqrdemo.utils.ValidationUtils;
import com.example.cafefidelidaqrdemo.viewmodels.PerfilViewModel;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment para ver y editar el perfil del cliente (CU-02.1)
 */
public class FragmentPerfil extends Fragment {
    
    // Views
    private TextInputLayout tilNombre, tilEmail, tilTelefono, tilFechaNac;
    private EditText etNombre, etEmail, etTelefono, etFechaNac;
    private Button btnGuardar, btnCancelar;
    private ProgressBar progressBar;
    private TextView tvSyncStatus, tvLastSync;
    
    // ViewModel
    private PerfilViewModel viewModel;
    
    // Estado
    private ClienteEntity clienteOriginal;
    private boolean isEditing = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initViewModel();
        setupListeners();
        loadClienteData();
    }
    
    private void initViews(View view) {
        // TextInputLayouts
        tilNombre = view.findViewById(R.id.til_nombre);
        tilEmail = view.findViewById(R.id.til_email);
        tilTelefono = view.findViewById(R.id.til_telefono);
        tilFechaNac = view.findViewById(R.id.til_fecha_nac);
        
        // EditTexts
        etNombre = view.findViewById(R.id.et_nombre);
        etEmail = view.findViewById(R.id.et_email);
        etTelefono = view.findViewById(R.id.et_telefono);
        etFechaNac = view.findViewById(R.id.et_fecha_nac);
        
        // Buttons
        btnGuardar = view.findViewById(R.id.btn_guardar);
        btnCancelar = view.findViewById(R.id.btn_cancelar);
        
        // Status views
        progressBar = view.findViewById(R.id.progress_bar);
        tvSyncStatus = view.findViewById(R.id.tv_sync_status);
        tvLastSync = view.findViewById(R.id.tv_last_sync);
        
        // Inicialmente en modo solo lectura
        setEditMode(false);
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);
        
        // Observar datos del cliente
        viewModel.getCliente().observe(getViewLifecycleOwner(), this::updateUI);
        
        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnGuardar.setEnabled(!isLoading);
        });
        
        // Observar errores
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                if (error.startsWith("CONFLICT:")) {
                    showConflictDialog(error.substring(9));
                } else {
                    showError(error);
                }
                viewModel.clearError();
            }
        });
        
        // Observar estado de sincronización
        viewModel.getSyncStatus().observe(getViewLifecycleOwner(), this::updateSyncStatus);
        
        // Observar éxito en guardado
        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Perfil guardado exitosamente", Toast.LENGTH_SHORT).show();
                setEditMode(false);
            }
        });
    }
    
    private void setupListeners() {
        // Botón guardar
        btnGuardar.setOnClickListener(v -> {
            if (isEditing) {
                saveChanges();
            } else {
                setEditMode(true);
            }
        });
        
        // Botón cancelar
        btnCancelar.setOnClickListener(v -> {
            if (isEditing) {
                cancelChanges();
            }
        });
        
        // Selector de fecha
        etFechaNac.setOnClickListener(v -> showDatePicker());
        
        // Validación en tiempo real
        setupRealTimeValidation();
    }
    
    private void setupRealTimeValidation() {
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isEditing) {
                    validateEmail();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        etTelefono.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isEditing) {
                    validateTelefono();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        etNombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isEditing) {
                    validateNombre();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadClienteData() {
        // Obtener ID del cliente desde SharedPreferences o argumentos
        String clienteId = getClienteId();
        if (clienteId != null) {
            viewModel.loadCliente(clienteId);
        }
    }
    
    private String getClienteId() {
        // Implementar lógica para obtener ID del cliente actual
        // Por ejemplo, desde SharedPreferences o argumentos del fragment
        return "cliente_actual_id"; // Placeholder
    }
    
    private void updateUI(ClienteEntity cliente) {
        if (cliente != null) {
            clienteOriginal = cliente;
            
            etNombre.setText(cliente.getNombre());
            etEmail.setText(cliente.getEmail());
            etTelefono.setText(cliente.getTelefono());
            
            if (cliente.getFecha_nac() != null) {
                etFechaNac.setText(dateFormat.format(cliente.getFecha_nac()));
            }
            
            updateLastSyncInfo(cliente);
        }
    }
    
    private void updateSyncStatus(Boolean isSynced) {
        if (isSynced != null) {
            if (isSynced) {
                tvSyncStatus.setText("✓ Sincronizado");
                tvSyncStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvSyncStatus.setText("⚠ Pendiente de sincronización");
                tvSyncStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            }
        }
    }
    
    private void updateLastSyncInfo(ClienteEntity cliente) {
        if (cliente.getLastSync() > 0) {
            Date lastSync = new Date(cliente.getLastSync());
            SimpleDateFormat syncFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvLastSync.setText("Última sincronización: " + syncFormat.format(lastSync));
        } else {
            tvLastSync.setText("Sin sincronizar");
        }
    }
    
    private void setEditMode(boolean editing) {
        isEditing = editing;
        
        // Habilitar/deshabilitar campos
        etNombre.setEnabled(editing);
        etEmail.setEnabled(editing);
        etTelefono.setEnabled(editing);
        etFechaNac.setEnabled(editing);
        
        // Cambiar texto del botón
        btnGuardar.setText(editing ? "Guardar" : "Editar");
        
        // Mostrar/ocultar botón cancelar
        btnCancelar.setVisibility(editing ? View.VISIBLE : View.GONE);
        
        // Limpiar errores de validación
        if (!editing) {
            clearValidationErrors();
        }
    }
    
    private void saveChanges() {
        if (validateAllFields()) {
            ClienteEntity updatedCliente = createUpdatedCliente();
            viewModel.updateCliente(updatedCliente);
        }
    }
    
    private void cancelChanges() {
        // Restaurar valores originales
        if (clienteOriginal != null) {
            updateUI(clienteOriginal);
        }
        setEditMode(false);
    }
    
    private boolean validateAllFields() {
        boolean isValid = true;
        
        isValid &= validateNombre();
        isValid &= validateEmail();
        isValid &= validateTelefono();
        isValid &= validateFechaNacimiento();
        
        return isValid;
    }
    
    private boolean validateNombre() {
        String nombre = etNombre.getText().toString().trim();
        if (!ValidationUtils.isValidName(nombre)) {
            tilNombre.setError("Nombre debe tener al menos 2 caracteres");
            return false;
        }
        tilNombre.setError(null);
        return true;
    }
    
    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();
        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Email no válido");
            return false;
        }
        tilEmail.setError(null);
        return true;
    }
    
    private boolean validateTelefono() {
        String telefono = etTelefono.getText().toString().trim();
        if (!ValidationUtils.isValidPhone(telefono)) {
            tilTelefono.setError("Teléfono debe tener 10 dígitos");
            return false;
        }
        tilTelefono.setError(null);
        return true;
    }
    
    private boolean validateFechaNacimiento() {
        String fechaStr = etFechaNac.getText().toString().trim();
        if (!ValidationUtils.isValidBirthDate(fechaStr)) {
            tilFechaNac.setError("Fecha no válida o mayor a 120 años");
            return false;
        }
        tilFechaNac.setError(null);
        return true;
    }
    
    private void clearValidationErrors() {
        tilNombre.setError(null);
        tilEmail.setError(null);
        tilTelefono.setError(null);
        tilFechaNac.setError(null);
    }
    
    private ClienteEntity createUpdatedCliente() {
        ClienteEntity updated = new ClienteEntity();
        updated.setId_cliente(clienteOriginal.getId_cliente());
        updated.setNombre(etNombre.getText().toString().trim());
        updated.setEmail(etEmail.getText().toString().trim());
        updated.setTelefono(etTelefono.getText().toString().trim());
        
        // Convertir fecha
        try {
            String fechaStr = etFechaNac.getText().toString().trim();
            if (!fechaStr.isEmpty()) {
                updated.setFecha_nac(dateFormat.parse(fechaStr));
            }
        } catch (Exception e) {
            // Mantener fecha original si hay error
            updated.setFecha_nac(clienteOriginal.getFecha_nac());
        }
        
        updated.setEstado(clienteOriginal.getEstado());
        updated.setCreado_en(clienteOriginal.getCreado_en());
        
        return updated;
    }
    
    private void showDatePicker() {
        if (!isEditing) return;
        
        Calendar calendar = Calendar.getInstance();
        
        // Si hay fecha actual, usarla como inicial
        if (clienteOriginal != null && clienteOriginal.getFecha_nac() != null) {
            calendar.setTime(clienteOriginal.getFecha_nac());
        }
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    etFechaNac.setText(dateFormat.format(selectedDate.getTime()));
                    validateFechaNacimiento();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Establecer límites de fecha
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -13); // Mínimo 13 años
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -120); // Máximo 120 años
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        
        datePickerDialog.show();
    }
    
    private void showConflictDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Conflicto de datos")
                .setMessage(message)
                .setPositiveButton("Recargar", (dialog, which) -> {
                    loadClienteData();
                    setEditMode(false);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    private void showError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }
}