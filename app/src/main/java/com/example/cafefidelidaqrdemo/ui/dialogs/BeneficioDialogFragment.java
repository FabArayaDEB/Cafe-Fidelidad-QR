package com.example.cafefidelidaqrdemo.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BeneficioDialogFragment extends DialogFragment {
    
    private static final String ARG_BENEFICIO = "beneficio";
    
    // Views
    private TextInputEditText editNombre;
    private TextInputEditText editDescripcion;
    private Spinner spinnerTipo;
    private TextInputEditText editValor;
    private TextInputEditText editVisitasRequeridas;
    private TextInputEditText editFechaInicio;
    private TextInputEditText editFechaFin;
    private Switch switchActivo;
    private Button buttonGuardar;
    private Button buttonCancelar;
    
    // Data
    private BeneficioEntity beneficio;
    private OnBeneficioSavedListener listener;
    private SimpleDateFormat dateFormat;
    
    public interface OnBeneficioSavedListener {
        void onBeneficioSaved(BeneficioEntity beneficio);
    }
    
    public static BeneficioDialogFragment newInstance(@Nullable BeneficioEntity beneficio) {
        BeneficioDialogFragment fragment = new BeneficioDialogFragment();
        Bundle args = new Bundle();
        if (beneficio != null) {
            args.putSerializable(ARG_BENEFICIO, beneficio);
        }
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            beneficio = (BeneficioEntity) getArguments().getSerializable(ARG_BENEFICIO);
        }
        // Inicializar dateFormat
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_beneficio, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            initViews(view);
            
            // Verificar que todas las vistas se inicializaron correctamente
             if (editNombre == null || editDescripcion == null || spinnerTipo == null || 
                 editValor == null || editVisitasRequeridas == null || editFechaInicio == null || 
                 editFechaFin == null || switchActivo == null || buttonGuardar == null || buttonCancelar == null) {
                 Toast.makeText(getContext(), "Error: No se pudieron inicializar las vistas", Toast.LENGTH_LONG).show();
                 dismiss();
                 return;
             }
            
            setupSpinner();
            setupClickListeners();
            
            if (beneficio != null) {
                populateFields();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error crítico al inicializar el diálogo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            dismiss();
        }
    }
    
    private void initViews(View view) {
        editNombre = view.findViewById(R.id.editNombre);
        editDescripcion = view.findViewById(R.id.editDescripcion);
        spinnerTipo = view.findViewById(R.id.spinnerTipo);
        editValor = view.findViewById(R.id.editValor);
        editVisitasRequeridas = view.findViewById(R.id.editVisitasRequeridas);
        editFechaInicio = view.findViewById(R.id.editFechaInicio);
        editFechaFin = view.findViewById(R.id.editFechaFin);
        switchActivo = view.findViewById(R.id.switchActivo);
        buttonGuardar = view.findViewById(R.id.buttonGuardar);
        buttonCancelar = view.findViewById(R.id.buttonCancelar);
    }
    
    private void setupSpinner() {
        String[] tipos = {"DESCUENTO_PORCENTAJE", "DESCUENTO_MONTO", "DOS_POR_UNO", "PREMIO"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tipos
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        buttonGuardar.setOnClickListener(v -> guardarBeneficio());
        buttonCancelar.setOnClickListener(v -> dismiss());
        
        editFechaInicio.setOnClickListener(v -> showDatePicker(true));
        editFechaFin.setOnClickListener(v -> showDatePicker(false));
    }
    
    private void populateFields() {
        if (beneficio == null) return;
        
        editNombre.setText(beneficio.getNombre() != null ? beneficio.getNombre() : "");
        // BeneficioEntity no tiene descripción, usar nombre como descripción por defecto
        editDescripcion.setText(beneficio.getNombre() != null ? beneficio.getNombre() : "");
        
        if (beneficio.getTipo() != null) {
            String[] tipos = {"DESCUENTO_PORCENTAJE", "DESCUENTO_MONTO", "DOS_POR_UNO", "PREMIO"};
            for (int i = 0; i < tipos.length; i++) {
                if (tipos[i].equals(beneficio.getTipo())) {
                    spinnerTipo.setSelection(i);
                    break;
                }
            }
        }
        
        // Set valor based on tipo
        if ("DESCUENTO_PORCENTAJE".equals(beneficio.getTipo()) && beneficio.getDescuento_pct() > 0) {
            editValor.setText(String.valueOf(beneficio.getDescuento_pct()));
        } else if (beneficio.getDescuento_monto() > 0) {
            editValor.setText(String.valueOf(beneficio.getDescuento_monto()));
        }
        
        if (beneficio.getRequisito_visitas() > 0) {
            editVisitasRequeridas.setText(String.valueOf(beneficio.getRequisito_visitas()));
        }
        
        if (beneficio.getVigencia_ini() > 0) {
            editFechaInicio.setText(dateFormat.format(new Date(beneficio.getVigencia_ini())));
        }
        
        if (beneficio.getVigencia_fin() > 0) {
            editFechaFin.setText(dateFormat.format(new Date(beneficio.getVigencia_fin())));
        }
        
        switchActivo.setChecked(beneficio.isActivo());
    }
    
    private void showDatePicker(boolean isStartDate) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(isStartDate ? "Fecha de inicio" : "Fecha de fin")
            .build();
            
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Date selectedDate = new Date(selection);
            String formattedDate = dateFormat.format(selectedDate);
            
            if (isStartDate) {
                editFechaInicio.setText(formattedDate);
            } else {
                editFechaFin.setText(formattedDate);
            }
        });
        
        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }
    
    private void guardarBeneficio() {
        try {
            if (!validateFields()) {
                Toast.makeText(getContext(), "Por favor, corrige los errores en el formulario", Toast.LENGTH_LONG).show();
                return;
            }
            
            if (beneficio == null) {
                beneficio = new BeneficioEntity();
                // Generar ID único para el nuevo beneficio
                beneficio.setId_beneficio(System.currentTimeMillis() + "_" + Math.random());
            }
            
            // Validar que las vistas no sean null
            if (editNombre == null || spinnerTipo == null || editValor == null || 
                editVisitasRequeridas == null || switchActivo == null) {
                Toast.makeText(getContext(), "Error: Vistas no inicializadas", Toast.LENGTH_SHORT).show();
                return;
            }

            beneficio.setNombre(editNombre.getText().toString().trim());
            // BeneficioEntity no tiene setDescripcion, pero podemos usar el nombre
            
            beneficio.setTipo(spinnerTipo.getSelectedItem().toString());
            
            // Configurar regla por defecto si no existe
            if (beneficio.getRegla() == null || beneficio.getRegla().isEmpty()) {
                beneficio.setRegla("{}"); // JSON vacío válido
            }
            
            // Parsing seguro de números
            try {
                double valor = Double.parseDouble(editValor.getText().toString().trim());
                String tipo = spinnerTipo.getSelectedItem().toString();
                
                // Inicializar ambos campos en 0 y luego establecer el correcto
                beneficio.setDescuento_pct(0.0);
                beneficio.setDescuento_monto(0.0);
                
                if (tipo.contains("PORCENTAJE")) {
                    beneficio.setDescuento_pct(valor);
                } else if (tipo.contains("MONTO")) {
                    beneficio.setDescuento_monto(valor);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Error: Valor numérico inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                int visitas = Integer.parseInt(editVisitasRequeridas.getText().toString().trim());
                beneficio.setRequisito_visitas(visitas);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Error: Número de visitas inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            
            beneficio.setEstado(switchActivo.isChecked() ? "activo" : "inactivo");
            
            // Parsing de fechas
            try {
                if (editFechaInicio != null && !editFechaInicio.getText().toString().trim().isEmpty()) {
                    Date fechaInicio = dateFormat.parse(editFechaInicio.getText().toString().trim());
                    if (fechaInicio != null) {
                        beneficio.setVigencia_ini(fechaInicio.getTime());
                    }
                }
                if (editFechaFin != null && !editFechaFin.getText().toString().trim().isEmpty()) {
                    Date fechaFin = dateFormat.parse(editFechaFin.getText().toString().trim());
                    if (fechaFin != null) {
                        beneficio.setVigencia_fin(fechaFin.getTime());
                    }
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error en formato de fecha: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (listener != null) {
                listener.onBeneficioSaved(beneficio);
            }
            
            dismiss();
            
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private boolean validateFields() {
        boolean isValid = true;
        
        // Validar nombre
        if (editNombre.getText().toString().trim().isEmpty()) {
            editNombre.setError("El nombre es requerido");
            isValid = false;
        } else {
            editNombre.setError(null);
        }
        
        // Validar valor
        String valorText = editValor.getText().toString().trim();
        if (valorText.isEmpty()) {
            editValor.setError("El valor es requerido");
            isValid = false;
        } else {
            try {
                double valor = Double.parseDouble(valorText);
                if (valor <= 0) {
                    editValor.setError("El valor debe ser mayor a 0");
                    isValid = false;
                } else {
                    // Validar según el tipo de beneficio
                    String tipo = spinnerTipo.getSelectedItem().toString();
                    if (tipo.contains("PORCENTAJE") && valor > 100) {
                        editValor.setError("El porcentaje no puede ser mayor a 100");
                        isValid = false;
                    } else {
                        editValor.setError(null);
                    }
                }
            } catch (NumberFormatException e) {
                editValor.setError("Valor numérico inválido");
                isValid = false;
            }
        }
        
        // Validar visitas requeridas
        String visitasText = editVisitasRequeridas.getText().toString().trim();
        if (visitasText.isEmpty()) {
            editVisitasRequeridas.setError("Las visitas requeridas son necesarias");
            isValid = false;
        } else {
            try {
                int visitas = Integer.parseInt(visitasText);
                if (visitas <= 0) {
                    editVisitasRequeridas.setError("Debe ser mayor a 0");
                    isValid = false;
                } else {
                    editVisitasRequeridas.setError(null);
                }
            } catch (NumberFormatException e) {
                editVisitasRequeridas.setError("Número de visitas inválido");
                isValid = false;
            }
        }
        
        // Validar fechas si están presentes
        String fechaInicioText = editFechaInicio.getText().toString().trim();
        String fechaFinText = editFechaFin.getText().toString().trim();
        
        if (!fechaInicioText.isEmpty()) {
            try {
                Date fechaInicio = dateFormat.parse(fechaInicioText);
                if (fechaInicio == null) {
                    editFechaInicio.setError("Fecha inválida");
                    isValid = false;
                } else {
                    editFechaInicio.setError(null);
                }
            } catch (Exception e) {
                editFechaInicio.setError("Formato de fecha inválido (dd/MM/yyyy)");
                isValid = false;
            }
        }
        
        if (!fechaFinText.isEmpty()) {
            try {
                Date fechaFin = dateFormat.parse(fechaFinText);
                if (fechaFin == null) {
                    editFechaFin.setError("Fecha inválida");
                    isValid = false;
                } else {
                    editFechaFin.setError(null);
                    
                    // Validar que fecha fin sea posterior a fecha inicio
                    if (!fechaInicioText.isEmpty()) {
                        try {
                            Date fechaInicio = dateFormat.parse(fechaInicioText);
                            if (fechaInicio != null && fechaFin.before(fechaInicio)) {
                                editFechaFin.setError("La fecha fin debe ser posterior a la fecha inicio");
                                isValid = false;
                            }
                        } catch (Exception e) {
                            // Error ya manejado arriba
                        }
                    }
                }
            } catch (Exception e) {
                editFechaFin.setError("Formato de fecha inválido (dd/MM/yyyy)");
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    public void setOnBeneficioSavedListener(OnBeneficioSavedListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}