package com.example.cafefidelidaqrdemo.ui.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;
import com.example.cafefidelidaqrdemo.ui.adapters.MisBeneficiosAdapter;
import com.example.cafefidelidaqrdemo.ui.viewmodels.MisBeneficiosViewModel;
import com.example.cafefidelidaqrdemo.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment para mostrar los beneficios disponibles del cliente
 * Permite solicitar OTP para canje de beneficios
 */
public class FragmentMisBeneficios extends Fragment {
    
    private static final String TAG = "FragmentMisBeneficios";
    
    // Views
    private RecyclerView recyclerViewBeneficios;
    private TextView textViewNoBeneficios;
    private MaterialCardView cardViewOtp;
    private TextView textViewOtpCodigo;
    private TextView textViewTiempoRestante;
    private TextView textViewBeneficioSeleccionado;
    private Button buttonSolicitarNuevo;
    private Button buttonCancelarOtp;
    
    // ViewModel y Adapter
    private MisBeneficiosViewModel viewModel;
    private MisBeneficiosAdapter adapter;
    
    // Datos
    private SessionManager sessionManager;
    private String clienteId;
    private CountDownTimer countDownTimer;
    private BeneficioEntity beneficioSeleccionado;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        clienteId = sessionManager.getClienteId();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mis_beneficios, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initViewModel();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
        
        // Cargar beneficios disponibles
        viewModel.cargarBeneficiosDisponibles(clienteId);
    }
    
    private void initViews(View view) {
        recyclerViewBeneficios = view.findViewById(R.id.recyclerViewBeneficios);
        textViewNoBeneficios = view.findViewById(R.id.textViewNoBeneficios);
        cardViewOtp = view.findViewById(R.id.cardViewOtp);
        textViewOtpCodigo = view.findViewById(R.id.textViewOtpCodigo);
        textViewTiempoRestante = view.findViewById(R.id.textViewTiempoRestante);
        textViewBeneficioSeleccionado = view.findViewById(R.id.textViewBeneficioSeleccionado);
        buttonSolicitarNuevo = view.findViewById(R.id.buttonSolicitarNuevo);
        buttonCancelarOtp = view.findViewById(R.id.buttonCancelarOtp);
        
        // Inicialmente ocultar card de OTP
        cardViewOtp.setVisibility(View.GONE);
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(MisBeneficiosViewModel.class);
    }
    
    private void setupRecyclerView() {
        adapter = new MisBeneficiosAdapter(new ArrayList<>(), this::onBeneficioClick);
        recyclerViewBeneficios.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewBeneficios.setAdapter(adapter);
    }
    
    private void setupObservers() {
        // Observar beneficios disponibles
        viewModel.getBeneficiosDisponibles().observe(getViewLifecycleOwner(), beneficios -> {
            if (beneficios != null && !beneficios.isEmpty()) {
                adapter.updateBeneficios(beneficios);
                recyclerViewBeneficios.setVisibility(View.VISIBLE);
                textViewNoBeneficios.setVisibility(View.GONE);
            } else {
                recyclerViewBeneficios.setVisibility(View.GONE);
                textViewNoBeneficios.setVisibility(View.VISIBLE);
            }
        });
        
        // Observar OTP actual
        viewModel.getOtpActual().observe(getViewLifecycleOwner(), otp -> {
            if (otp != null && !otp.isEmpty()) {
                textViewOtpCodigo.setText(formatearOtp(otp));
                cardViewOtp.setVisibility(View.VISIBLE);
            }
        });
        
        // Observar tiempo restante
        viewModel.getTiempoRestante().observe(getViewLifecycleOwner(), tiempoRestante -> {
            if (tiempoRestante != null && tiempoRestante > 0) {
                iniciarTemporizador(tiempoRestante);
            } else {
                detenerTemporizador();
                textViewTiempoRestante.setText("00:00");
            }
        });
        
        // Observar validez del OTP
        viewModel.getOtpValido().observe(getViewLifecycleOwner(), valido -> {
            if (valido != null) {
                if (!valido) {
                    // OTP expirado o usado
                    cardViewOtp.setVisibility(View.GONE);
                    detenerTemporizador();
                }
            }
        });
        
        // Observar estado del canje
        viewModel.getEstadoCanje().observe(getViewLifecycleOwner(), estado -> {
            if (estado != null) {
                manejarEstadoCanje(estado);
            }
        });
        
        // Observar mensajes de error
        viewModel.getMensajeError().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null && !mensaje.isEmpty()) {
                mostrarError(mensaje);
            }
        });
    }
    
    private void setupClickListeners() {
        buttonSolicitarNuevo.setOnClickListener(v -> {
            if (beneficioSeleccionado != null) {
                solicitarNuevoOtp();
            }
        });
        
        buttonCancelarOtp.setOnClickListener(v -> {
            cancelarOtp();
        });
    }
    
    private void onBeneficioClick(BeneficioEntity beneficio) {
        beneficioSeleccionado = beneficio;
        mostrarDialogoConfirmacionCanje(beneficio);
    }
    
    private void mostrarDialogoConfirmacionCanje(BeneficioEntity beneficio) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Canjear Beneficio")
            .setMessage("¿Deseas canjear el beneficio \"" + beneficio.getNombre() + "\"?\n\n" +
                       "Se generará un código OTP válido por 60 segundos.")
            .setPositiveButton("Canjear", (dialog, which) -> {
                solicitarOtp(beneficio);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void solicitarOtp(BeneficioEntity beneficio) {
        String sucursalId = sessionManager.getSucursalId(); // Obtener sucursal actual
        textViewBeneficioSeleccionado.setText(beneficio.getNombre());
        viewModel.solicitarOtp(clienteId, beneficio.getId_beneficio(), sucursalId);
    }
    
    private void solicitarNuevoOtp() {
        if (beneficioSeleccionado != null) {
            String sucursalId = sessionManager.getSucursalId();
            viewModel.solicitarNuevoOtp(clienteId, beneficioSeleccionado.getId_beneficio(), sucursalId);
        }
    }
    
    private void cancelarOtp() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancelar OTP")
            .setMessage("¿Estás seguro de que deseas cancelar el código OTP?")
            .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                cardViewOtp.setVisibility(View.GONE);
                detenerTemporizador();
                beneficioSeleccionado = null;
                viewModel.limpiarEstado();
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void manejarEstadoCanje(String estado) {
        switch (estado) {
            case "OTP_GENERADO":
                Toast.makeText(getContext(), "Código OTP generado. Muéstralo al cajero.", Toast.LENGTH_SHORT).show();
                break;
            case "OTP_ACTIVO":
                Toast.makeText(getContext(), "Ya tienes un código OTP activo.", Toast.LENGTH_SHORT).show();
                break;
            case "OTP_EXPIRADO":
                Toast.makeText(getContext(), "El código OTP ha expirado.", Toast.LENGTH_SHORT).show();
                buttonSolicitarNuevo.setVisibility(View.VISIBLE);
                break;
            case "CANJE_COMPLETADO":
                Toast.makeText(getContext(), "¡Beneficio canjeado exitosamente!", Toast.LENGTH_LONG).show();
                cardViewOtp.setVisibility(View.GONE);
                viewModel.cargarBeneficiosDisponibles(clienteId); // Recargar lista
                break;
            case "ERROR_CONEXION":
                buttonSolicitarNuevo.setVisibility(View.VISIBLE);
                break;
            case "ERROR_DOBLE_CANJE":
                cardViewOtp.setVisibility(View.GONE);
                viewModel.cargarBeneficiosDisponibles(clienteId); // Recargar lista
                break;
        }
    }
    
    private void mostrarError(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
    }
    
    private String formatearOtp(String otp) {
        if (otp.length() == 6) {
            return otp.substring(0, 3) + " " + otp.substring(3);
        }
        return otp;
    }
    
    private void iniciarTemporizador(long tiempoRestanteMs) {
        detenerTemporizador(); // Detener temporizador anterior si existe
        
        countDownTimer = new CountDownTimer(tiempoRestanteMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long segundos = millisUntilFinished / 1000;
                long minutos = segundos / 60;
                segundos = segundos % 60;
                
                String tiempoFormateado = String.format("%02d:%02d", minutos, segundos);
                textViewTiempoRestante.setText(tiempoFormateado);
                
                // Cambiar color cuando quedan menos de 10 segundos
                if (millisUntilFinished < 10000) {
                    textViewTiempoRestante.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    textViewTiempoRestante.setTextColor(getResources().getColor(android.R.color.black));
                }
            }
            
            @Override
            public void onFinish() {
                textViewTiempoRestante.setText("00:00");
                textViewTiempoRestante.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                buttonSolicitarNuevo.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "El código OTP ha expirado", Toast.LENGTH_SHORT).show();
            }
        };
        
        countDownTimer.start();
        buttonSolicitarNuevo.setVisibility(View.GONE);
    }
    
    private void detenerTemporizador() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detenerTemporizador();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        detenerTemporizador();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Verificar si hay un OTP activo al volver al fragment
        if (viewModel != null && clienteId != null) {
            viewModel.verificarOtpActivo(clienteId);
        }
    }
}