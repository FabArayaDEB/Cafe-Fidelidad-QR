package com.example.cafefidelidaqrdemo.fragment;

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

import com.example.cafefidelidaqrdemo.Contantes;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.databinding.FragmentChatBinding;
import com.example.cafefidelidaqrdemo.security.QRSecurityManager;
import com.example.cafefidelidaqrdemo.security.SecureNetworkManager;
import com.example.cafefidelidaqrdemo.offline.OfflineManager;
import com.example.cafefidelidaqrdemo.database.entities.TransaccionEntity;
import com.example.cafefidelidaqrdemo.utils.PerformanceMonitor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import io.jsonwebtoken.Claims;

public class FragmentQR extends Fragment {

    private FragmentChatBinding binding;
    private Context mContext;
    private FirebaseAuth firebaseAuth;
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

        firebaseAuth = FirebaseAuth.getInstance();
        
        // Inicializar gestores de seguridad y offline
        qrSecurityManager = QRSecurityManager.getInstance();
        networkManager = SecureNetworkManager.getInstance();
        offlineManager = OfflineManager.getInstance(getContext());
        
        // Inicializar vistas
        initViews();
        
        // Configurar botón de escaneo
        btnEscanearQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()) {
                    iniciarEscaneoQR();
                } else {
                    requestCameraPermission();
                }
            }
        });
        
        // Cargar último escaneo
        loadUltimoEscaneo();
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
            
            // Obtener usuario actual
            String uid = firebaseAuth.getUid();
            if (uid == null) {
                PerformanceMonitor.endMeasurement("procesar_codigo_qr", processingStart);
                Toast.makeText(mContext, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Crear entidad de transacción para offline-first
            long createTransactionStart = PerformanceMonitor.startMeasurement("crear_transaccion_entity");
            TransaccionEntity transaccion = new TransaccionEntity();
            transaccion.setId(UUID.randomUUID().toString());
            transaccion.setUserId(uid);
            transaccion.setSucursalId(sucursalId);
            transaccion.setMesaId(mesaId);
            transaccion.setTipo("ganancia");
            transaccion.setMonto(monto);
            transaccion.setPuntos(Contantes.calcularPuntos(monto));
            transaccion.setFecha(System.currentTimeMillis());
            transaccion.setQrTimestamp(timestamp);
            transaccion.setDescripcion("Transacción QR - Sucursal: " + sucursalId + " Mesa: " + mesaId);
            PerformanceMonitor.endMeasurement("crear_transaccion_entity", createTransactionStart);
            
            // Registrar transacción usando arquitectura offline-first
            offlineManager.registrarTransaccion(transaccion, new OfflineManager.TransaccionCallback() {
                @Override
                public void onExito(String transaccionId) {
                    getActivity().runOnUiThread(() -> {
                        PerformanceMonitor.endMeasurement("procesar_codigo_qr", processingStart);
                        Toast.makeText(mContext, "¡Transacción registrada! Puntos ganados: " + transaccion.getPuntos(), 
                                Toast.LENGTH_LONG).show();
                        // Actualizar puntos del usuario
                        actualizarPuntosUsuario(transaccion.getPuntos(), transaccion.getMonto());
                    });
                }
                
                @Override
                public void onError(String error) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(mContext, "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
            
        } catch (Exception e) {
            Toast.makeText(mContext, "Error al procesar código QR: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Registra transacción con validaciones de seguridad mejoradas
     */
    private void registrarTransaccionSegura(String sucursalId, String mesaId, double monto, 
                                           int puntosGanados, long qrTimestamp, String jwtToken) {
        String uid = firebaseAuth.getUid();
        long timestamp = System.currentTimeMillis();
        
        // Validar token JWT
        Claims claims = qrSecurityManager.validateJWTToken(jwtToken);
        if (claims == null) {
            Toast.makeText(mContext, "Token de autenticación inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Generar hash seguro para la transacción
        String transactionHash = qrSecurityManager.generateSecureHash(
            uid + sucursalId + mesaId + monto + timestamp);
        
        // Crear objeto de transacción con datos de seguridad
        HashMap<String, Object> transaccion = new HashMap<>();
        transaccion.put("sucursalId", sucursalId);
        transaccion.put("mesaId", mesaId);
        transaccion.put("monto", monto);
        transaccion.put("puntos", puntosGanados);
        transaccion.put("fecha", timestamp);
        transaccion.put("qrTimestamp", qrTimestamp);
        transaccion.put("hash", transactionHash);
        transaccion.put("jwtUsed", true);
        transaccion.put("descripcion", "Escaneo QR Seguro - Sucursal: " + sucursalId + 
                                     " Mesa: " + mesaId + " - $" + String.format("%.2f", monto));
        
        // Guardar en Firebase con referencia única
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("TransaccionesSeguras")
                .child(uid)
                .child(transactionHash);
        
        ref.setValue(transaccion)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(mContext, 
                            "¡Transacción segura registrada! +" + puntosGanados + " puntos", 
                            Toast.LENGTH_LONG).show();
                    
                    // Actualizar puntos del usuario
                     actualizarPuntosUsuario(puntosGanados, monto);
                     
                     // Registrar también en el sistema legacy para compatibilidad
                     registrarTransaccionLegacy(mesaId, monto, puntosGanados);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(mContext, 
                            "Error al registrar transacción: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }
    
    /**
     * Método legacy para compatibilidad con sistema anterior
     */
    private void registrarTransaccionLegacy(String tipo, double monto, int puntosGanados) {
        String uid = firebaseAuth.getUid();
        long timestamp = System.currentTimeMillis();
        
        // Crear registro de transacción
        HashMap<String, Object> transaccion = new HashMap<>();
        transaccion.put("tipo", "ganancia");
        transaccion.put("descripcion", "Compra en " + tipo + " - $" + String.format("%.2f", monto));
        transaccion.put("puntos", puntosGanados);
        transaccion.put("monto", monto);
        transaccion.put("fecha", timestamp);
        
        // Guardar transacción
        DatabaseReference transaccionRef = FirebaseDatabase.getInstance()
                .getReference("Transacciones")
                .child(uid)
                .push();
        
        transaccionRef.setValue(transaccion);
        
        Toast.makeText(mContext, 
                "¡Felicidades! Has ganado " + puntosGanados + " puntos por tu compra de $" + 
                String.format("%.2f", monto), 
                Toast.LENGTH_LONG).show();
    }
    
    private void actualizarPuntosUsuario(int puntosGanados, double montoCompra) {
        long updateStart = PerformanceMonitor.startMeasurement("actualizar_puntos_usuario_qr");
        
        String uid = firebaseAuth.getUid();
        
        // Usar OfflineManager para actualización offline-first
        offlineManager.obtenerUsuario(uid, new OfflineManager.UsuarioCallback() {
            @Override
            public void onExito(com.example.cafefidelidaqrdemo.database.entities.UsuarioEntity usuario) {
                if (usuario != null) {
                    long calculationStart = PerformanceMonitor.startMeasurement("calcular_nuevos_valores");
                    
                    // Calcular nuevos valores
                    int nuevosPuntos = usuario.getPuntos() + puntosGanados;
                    double nuevasCompras = usuario.getTotalCompras() + montoCompra;
                    String nuevoNivel = Contantes.calcularNivel(nuevosPuntos);
                    
                    PerformanceMonitor.endMeasurement("calcular_nuevos_valores", calculationStart);
                    
                    // Actualizar usuario usando OfflineManager
                    usuario.setPuntos(nuevosPuntos);
                    usuario.setTotalCompras(nuevasCompras);
                    usuario.setNivel(nuevoNivel);
                    usuario.setLastSync(System.currentTimeMillis());
                    usuario.setNeedsSync(true);
                    
                    offlineManager.actualizarUsuario(usuario, new OfflineManager.UsuarioCallback() {
                        @Override
                        public void onExito(com.example.cafefidelidaqrdemo.database.entities.UsuarioEntity usuarioActualizado) {
                            PerformanceMonitor.endMeasurement("actualizar_puntos_usuario_qr", updateStart);
                            // Puntos actualizados correctamente offline-first
                        }
                        
                        @Override
                        public void onError(String error) {
                            PerformanceMonitor.endMeasurement("actualizar_puntos_usuario_qr", updateStart);
                            Toast.makeText(mContext, "Error actualizando puntos: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    PerformanceMonitor.endMeasurement("actualizar_puntos_usuario_qr", updateStart);
                }
            }
            
            @Override
            public void onError(String error) {
                PerformanceMonitor.endMeasurement("actualizar_puntos_usuario_qr", updateStart);
                Toast.makeText(mContext, "Error obteniendo datos del usuario: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadUltimoEscaneo() {
        String uid = firebaseAuth.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Transacciones")
                .child(uid);
        
        ref.orderByChild("fecha").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (tvUltimoEscaneo != null) {
                            if (snapshot.exists()) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String descripcion = "" + ds.child("descripcion").getValue();
                                    String fechaStr = "" + ds.child("fecha").getValue();
                                    
                                    try {
                                        long fecha = Long.parseLong(fechaStr);
                                        String fechaFormateada = Contantes.DateTimeFormat(fecha);
                                        tvUltimoEscaneo.setText("Último escaneo: " + descripcion + " - " + fechaFormateada);
                                    } catch (Exception e) {
                                        tvUltimoEscaneo.setText("Último escaneo: " + descripcion);
                                    }
                                    break;
                                }
                            } else {
                                tvUltimoEscaneo.setText("Aún no has escaneado ningún código QR");
                            }
                        }
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Ignorar errores
                    }
                });
    }
}