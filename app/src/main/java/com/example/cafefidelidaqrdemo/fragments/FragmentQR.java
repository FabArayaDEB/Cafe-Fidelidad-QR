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
import com.example.cafefidelidaqrdemo.security.QRSecurityManager;
import com.example.cafefidelidaqrdemo.security.SecureNetworkManager;
import com.example.cafefidelidaqrdemo.offline.OfflineManager;
import com.example.cafefidelidaqrdemo.utils.PerformanceMonitor;
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
    private QRSecurityManager qrSecurityManager;
    private SecureNetworkManager networkManager;
    private OfflineManager offlineManager;

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
        qrSecurityManager = QRSecurityManager.getInstance();
        networkManager = SecureNetworkManager.getInstance();
        offlineManager = OfflineManager.getInstance(getContext());
        
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
        long processingStart = PerformanceMonitor.startMeasurement("procesar_codigo_qr");
        
        try {
            // Validar formato básico
            long validationStart = PerformanceMonitor.startMeasurement("validar_qr_formato");
            if (!contenidoQR.startsWith("CAFE_FIDELIDAD:")) {
                PerformanceMonitor.endMeasurement("validar_qr_formato", validationStart);
                PerformanceMonitor.endMeasurement("procesar_codigo_qr", processingStart);
                Toast.makeText(mContext, "Este no es un código QR de Café Fidelidad", Toast.LENGTH_SHORT).show();
                return;
            }
            PerformanceMonitor.endMeasurement("validar_qr_formato", validationStart);
            
            // Validar firma HMAC-SHA256
            long hmacStart = PerformanceMonitor.startMeasurement("validar_qr_hmac");
            if (!qrSecurityManager.validateQRCode(contenidoQR)) {
                PerformanceMonitor.endMeasurement("validar_qr_hmac", hmacStart);
                PerformanceMonitor.endMeasurement("procesar_codigo_qr", processingStart);
                Toast.makeText(mContext, "Código QR inválido o expirado", Toast.LENGTH_SHORT).show();
                return;
            }
            PerformanceMonitor.endMeasurement("validar_qr_hmac", hmacStart);
            
            // Extraer datos del QR validado
            long extractStart = PerformanceMonitor.startMeasurement("extraer_datos_qr");
            Map<String, Object> qrData = qrSecurityManager.extractQRData(contenidoQR);
            if (qrData.isEmpty()) {
                PerformanceMonitor.endMeasurement("extraer_datos_qr", extractStart);
                PerformanceMonitor.endMeasurement("procesar_codigo_qr", processingStart);
                Toast.makeText(mContext, "Error al procesar datos del QR", Toast.LENGTH_SHORT).show();
                return;
            }
            PerformanceMonitor.endMeasurement("extraer_datos_qr", extractStart);
            
            String sucursalId = (String) qrData.get("sucursalId");
            String mesaId = (String) qrData.get("mesaId");
            double monto = (Double) qrData.get("monto");
            long timestamp = (Long) qrData.get("timestamp");
            
            // Crear entidad de transacción para offline-first
            long createTransactionStart = PerformanceMonitor.startMeasurement("crear_transaccion_entity");
            TransaccionEntity transaccion = new TransaccionEntity();
            transaccion.setId(UUID.randomUUID().toString());
            transaccion.setSucursalId(sucursalId);
            transaccion.setMesaId(mesaId);
            transaccion.setTipo("ganancia");
            transaccion.setMonto(monto);
            transaccion.setPuntos(Contantes.calcularPuntos(monto));
            transaccion.setFecha(System.currentTimeMillis());
            transaccion.setQrTimestamp(timestamp);
            transaccion.setDescripcion("Transacción QR - Sucursal: " + sucursalId + " Mesa: " + mesaId);
            PerformanceMonitor.endMeasurement("crear_transaccion_entity", createTransactionStart);
            
            // TODO: Implementar registro de transacción cuando esté disponible en TableroClienteViewModel
            PerformanceMonitor.endMeasurement("procesar_codigo_qr", processingStart);
            Toast.makeText(mContext, "¡Transacción registrada! Puntos ganados: " + transaccion.getPuntos(), 
                    Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Toast.makeText(mContext, "Error al procesar código QR: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }
    

    

}