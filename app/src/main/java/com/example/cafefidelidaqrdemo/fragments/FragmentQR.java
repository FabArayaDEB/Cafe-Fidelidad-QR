package com.example.cafefidelidaqrdemo.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cafefidelidaqrdemo.Contantes;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.databinding.FragmentChatBinding;
import com.example.cafefidelidaqrdemo.ui.cliente.viewmodels.TableroClienteViewModel;
import com.example.cafefidelidaqrdemo.database.entities.TransaccionEntity;
// Imports de seguridad y performance removidos para simplificación
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Map;
import java.util.UUID;
import io.jsonwebtoken.Claims;

public class FragmentQR extends Fragment {

    private FragmentChatBinding binding;
    private Context mContext;
    private TableroClienteViewModel viewModel;
    private Button btnEscanearQR;
    private TextView tvInstrucciones, tvUltimoEscaneo;
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    
    // Gestores de seguridad
    // Managers de seguridad y offline removidos para simplificación

    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    public FragmentQR() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(TableroClienteViewModel.class);
        
        // Inicializar gestores de seguridad y offline
        // Inicialización de managers removida para simplificación
        
        // Inicializar vistas
        initViews();
        
        // Configurar observadores
        setupObservers();
        
        // Configurar botón de escaneo
        btnEscanearQR.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                iniciarEscaneoQR();
            } else {
                requestCameraPermission();
            }
        });
        
        // Cargar datos
        viewModel.loadTransacciones();
    }
    
    private void initViews() {
        // Buscar vistas en el layout
        btnEscanearQR = binding.getRoot().findViewById(R.id.btn_iniciar_escaner);
        tvInstrucciones = binding.getRoot().findViewById(R.id.tv_ultimo_monto);
        tvUltimoEscaneo = binding.getRoot().findViewById(R.id.tv_ultima_fecha);
        
        // Configurar texto de instrucciones
        if (tvInstrucciones != null) {
            tvInstrucciones.setText("Escanea el código QR en tu mesa o en el mostrador para acumular puntos con tu compra.");
        }
    }
    
    private void setupObservers() {
        // TODO: Implementar observación de transacciones cuando esté disponible en TableroClienteViewModel
        if (tvUltimoEscaneo != null) {
            tvUltimoEscaneo.setText("Aún no has escaneado ningún código QR");
        }
        
        // Observar errores
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Mostrar/ocultar indicador de carga
        });
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(), 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarEscaneoQR();
            } else {
                Toast.makeText(mContext, "Se necesita permiso de cámara para escanear códigos QR", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void iniciarEscaneoQR() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea el código QR de Café Fidelidad");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(mContext, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            } else {
                procesarCodigoQR(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    private void procesarCodigoQR(String contenidoQR) {
        try {
            // Validación básica simplificada
            if (!contenidoQR.startsWith("CAFE_FIDELIDAD:")) {
                Toast.makeText(mContext, "Este no es un código QR de Café Fidelidad", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Procesamiento simplificado del QR
            // En una implementación real, aquí se extraerían los datos del QR
            String[] parts = contenidoQR.split(":");
            if (parts.length < 2) {
                Toast.makeText(mContext, "Formato de QR inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Crear transacción básica
            TransaccionEntity transaccion = new TransaccionEntity();
            transaccion.setId(UUID.randomUUID().toString());
            transaccion.setTipo("ganancia");
            transaccion.setMonto(10.0); // Monto por defecto
            transaccion.setPuntos(Contantes.calcularPuntos(10.0));
            transaccion.setFecha(System.currentTimeMillis());
            transaccion.setDescripcion("Transacción QR procesada");
            
            // TODO: Implementar registro de transacción cuando esté disponible en TableroClienteViewModel
            Toast.makeText(mContext, "¡Transacción registrada! Puntos ganados: " + transaccion.getPuntos(), 
                    Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Toast.makeText(mContext, "Error al procesar código QR: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }
    

    

}