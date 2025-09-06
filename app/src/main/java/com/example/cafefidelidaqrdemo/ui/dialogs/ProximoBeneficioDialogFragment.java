package com.example.cafefidelidaqrdemo.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.viewmodel.ProgresoViewModel;

public class ProximoBeneficioDialogFragment extends DialogFragment {
    
    private static final String ARG_PROXIMO_BENEFICIO = "proximo_beneficio";
    
    // Views
    private TextView textNombreBeneficio;
    private TextView textDescripcionBeneficio;
    private TextView textVisitasActuales;
    private TextView textVisitasRequeridas;
    private TextView textVisitasFaltantes;
    private ProgressBar progressBarVisitas;
    private Button buttonCerrar;
    
    // Data
    private ProgresoViewModel.ProximoBeneficio proximoBeneficio;
    
    public static ProximoBeneficioDialogFragment newInstance(ProgresoViewModel.ProximoBeneficio proximoBeneficio) {
        ProximoBeneficioDialogFragment fragment = new ProximoBeneficioDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PROXIMO_BENEFICIO, proximoBeneficio);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            proximoBeneficio = (ProgresoViewModel.ProximoBeneficio) getArguments().getSerializable(ARG_PROXIMO_BENEFICIO);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_proximo_beneficio, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
        populateData();
    }
    
    private void initViews(View view) {
        textNombreBeneficio = view.findViewById(R.id.textNombreBeneficio);
        textDescripcionBeneficio = view.findViewById(R.id.textDescripcionBeneficio);
        textVisitasActuales = view.findViewById(R.id.textVisitasActuales);
        textVisitasRequeridas = view.findViewById(R.id.textVisitasRequeridas);
        textVisitasFaltantes = view.findViewById(R.id.textVisitasFaltantes);
        progressBarVisitas = view.findViewById(R.id.progressBarVisitas);
        buttonCerrar = view.findViewById(R.id.buttonCerrar);
    }
    
    private void setupClickListeners() {
        buttonCerrar.setOnClickListener(v -> dismiss());
    }
    
    private void populateData() {
        if (proximoBeneficio == null) return;
        
        textNombreBeneficio.setText(proximoBeneficio.getNombre());
        textDescripcionBeneficio.setText(proximoBeneficio.getDescripcion());
        textVisitasActuales.setText(String.valueOf(proximoBeneficio.getVisitasActuales()));
        textVisitasRequeridas.setText(String.valueOf(proximoBeneficio.getVisitasRequeridas()));
        
        int visitasFaltantes = proximoBeneficio.getVisitasRequeridas() - proximoBeneficio.getVisitasActuales();
        textVisitasFaltantes.setText(String.valueOf(Math.max(0, visitasFaltantes)));
        
        // Configurar progress bar
        progressBarVisitas.setMax(proximoBeneficio.getVisitasRequeridas());
        progressBarVisitas.setProgress(proximoBeneficio.getVisitasActuales());
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