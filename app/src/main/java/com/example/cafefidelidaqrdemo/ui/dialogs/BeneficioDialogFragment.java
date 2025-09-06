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
import com.example.cafefidelidaqrdemo.model.Beneficio;
import com.example.cafefidelidaqrdemo.model.Beneficio.TipoBeneficio;
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
    private Beneficio beneficio;
    private OnBeneficioSavedListener listener;
    private SimpleDateFormat dateFormat;
    
    public interface OnBeneficioSavedListener {
        void onBeneficioSaved(Beneficio beneficio);
    }
    
    public static BeneficioDialogFragment newInstance(@Nullable Beneficio beneficio) {
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
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        if (getArguments() != null) {
            beneficio = (Beneficio) getArguments().getSerializable(ARG_BENEFICIO);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_beneficio, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupSpinner();
        setupClickListeners();
        
        if (beneficio != null) {
            populateFields();
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
        ArrayAdapter<TipoBeneficio> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            TipoBeneficio.values()
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
        editNombre.setText(beneficio.getNombre());
        editDescripcion.setText(beneficio.getDescripcion());
        
        if (beneficio.getTipo() != null) {
            spinnerTipo.setSelection(beneficio.getTipo().ordinal());
        }
        
        editValor.setText(String.valueOf(beneficio.getValor()));
        // editVisitasRequeridas.setText(String.valueOf(beneficio.getVisitasRequeridas())); // Método no existe
        
        if (beneficio.getFechaInicioVigencia() != null) {
            editFechaInicio.setText(dateFormat.format(beneficio.getFechaInicioVigencia()));
        }
        
        if (beneficio.getFechaFinVigencia() != null) {
            editFechaFin.setText(dateFormat.format(beneficio.getFechaFinVigencia()));
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
        if (!validateFields()) {
            return;
        }
        
        if (beneficio == null) {
            beneficio = new Beneficio();
        }
        
        // Populate beneficio with form data
        beneficio.setNombre(editNombre.getText().toString().trim());
        beneficio.setDescripcion(editDescripcion.getText().toString().trim());
        beneficio.setTipo((TipoBeneficio) spinnerTipo.getSelectedItem());
        beneficio.setValor(Double.parseDouble(editValor.getText().toString()));
        // beneficio.setVisitasRequeridas(Integer.parseInt(editVisitasRequeridas.getText().toString())); // Método no existe
        beneficio.setActivo(switchActivo.isChecked());
        
        // Set dates if provided
        try {
            if (!editFechaInicio.getText().toString().isEmpty()) {
                beneficio.setFechaInicioVigencia(dateFormat.parse(editFechaInicio.getText().toString()));
            }
            if (!editFechaFin.getText().toString().isEmpty()) {
                beneficio.setFechaFinVigencia(dateFormat.parse(editFechaFin.getText().toString()));
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error en formato de fecha", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (listener != null) {
            listener.onBeneficioSaved(beneficio);
        }
        
        dismiss();
    }
    
    private boolean validateFields() {
        if (editNombre.getText().toString().trim().isEmpty()) {
            editNombre.setError("El nombre es requerido");
            return false;
        }
        
        if (editDescripcion.getText().toString().trim().isEmpty()) {
            editDescripcion.setError("La descripción es requerida");
            return false;
        }
        
        try {
            Double.parseDouble(editValor.getText().toString());
        } catch (NumberFormatException e) {
            editValor.setError("Valor inválido");
            return false;
        }
        
        try {
            Integer.parseInt(editVisitasRequeridas.getText().toString());
        } catch (NumberFormatException e) {
            editVisitasRequeridas.setError("Número de visitas inválido");
            return false;
        }
        
        return true;
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