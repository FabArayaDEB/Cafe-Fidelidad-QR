package com.example.cafefidelidaqrdemo.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class BeneficioDetailsDialogFragment extends DialogFragment {
    
    private static final String ARG_BENEFICIO = "beneficio";
    
    // Views
    private TextView textNombre;
    private TextView textDescripcion;
    private TextView textTipo;
    private TextView textValor;
    private TextView textVisitasRequeridas;
    private TextView textFechaInicio;
    private TextView textFechaFin;
    private TextView textEstado;
    private TextView textVecesCanjeado;
    private Button buttonCerrar;
    
    // Data
    private BeneficioEntity beneficio;
    private SimpleDateFormat dateFormat;
    
    public static BeneficioDetailsDialogFragment newInstance(BeneficioEntity beneficio) {
        BeneficioDetailsDialogFragment fragment = new BeneficioDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BENEFICIO, beneficio);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        if (getArguments() != null) {
            beneficio = (BeneficioEntity) getArguments().getSerializable(ARG_BENEFICIO);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_beneficio_details, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
        populateData();
    }
    
    private void initViews(View view) {
        textNombre = view.findViewById(R.id.textNombre);
        textDescripcion = view.findViewById(R.id.textDescripcion);
        textTipo = view.findViewById(R.id.textTipo);
        textValor = view.findViewById(R.id.textValor);
        textVisitasRequeridas = view.findViewById(R.id.textVisitasRequeridas);
        textFechaInicio = view.findViewById(R.id.textFechaInicio);
        textFechaFin = view.findViewById(R.id.textFechaFin);
        textEstado = view.findViewById(R.id.textEstado);
        textVecesCanjeado = view.findViewById(R.id.textVecesCanjeado);
        buttonCerrar = view.findViewById(R.id.buttonCerrar);
    }
    
    private void setupClickListeners() {
        buttonCerrar.setOnClickListener(v -> dismiss());
    }
    
    private void populateData() {
        if (beneficio == null) return;
        
        textNombre.setText(beneficio.getNombre());

        textDescripcion.setText("Descripción no disponible");
        
        if (beneficio.getTipo() != null) {
            textTipo.setText(beneficio.getTipo());
        }
        
        // Set valor based on tipo
        if ("DESCUENTO_PORCENTAJE".equals(beneficio.getTipo()) && beneficio.getDescuento_pct() > 0) {
            textValor.setText(String.valueOf(beneficio.getDescuento_pct()) + "%");
        } else if (beneficio.getDescuento_monto() > 0) {
            textValor.setText("$" + String.valueOf(beneficio.getDescuento_monto()));
        } else {
            textValor.setText("N/A");
        }
        
        if (beneficio.getRequisito_visitas() > 0) {
            textVisitasRequeridas.setText(String.valueOf(beneficio.getRequisito_visitas()));
        } else {
            textVisitasRequeridas.setText("N/A");
        }
        
        if (beneficio.getVigencia_ini() > 0) {
            textFechaInicio.setText(dateFormat.format(new java.util.Date(beneficio.getVigencia_ini())));
        } else {
            textFechaInicio.setText("No especificada");
        }
        
        if (beneficio.getVigencia_fin() > 0) {
            textFechaFin.setText(dateFormat.format(new java.util.Date(beneficio.getVigencia_fin())));
        } else {
            textFechaFin.setText("No especificada");
        }
        
        textEstado.setText(beneficio.isActivo() ? "Activo" : "Inactivo");

        textVecesCanjeado.setText("0");
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