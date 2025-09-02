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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;

public class FragmentQR extends Fragment {

    private FragmentChatBinding binding;
    private Context mContext;
    private FirebaseAuth firebaseAuth;
    private Button btnEscanearQR;
    private TextView tvInstrucciones, tvUltimoEscaneo;
    private static final int CAMERA_PERMISSION_REQUEST = 100;

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
        try {
            // Formato esperado: "CAFE_FIDELIDAD:MESA_X:MONTO_Y" o "CAFE_FIDELIDAD:COMPRA:MONTO_Y"
            if (contenidoQR.startsWith("CAFE_FIDELIDAD:")) {
                String[] partes = contenidoQR.split(":");
                if (partes.length >= 3) {
                    String tipo = partes[1]; // MESA_X o COMPRA
                    double monto = Double.parseDouble(partes[2]);
                    
                    // Calcular puntos
                    int puntosGanados = Contantes.calcularPuntos(monto);
                    
                    // Registrar transacción
                    registrarTransaccion(tipo, monto, puntosGanados);
                } else {
                    Toast.makeText(mContext, "Código QR inválido", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, "Este no es un código QR de Café Fidelidad", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Error al procesar código QR: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    private void registrarTransaccion(String tipo, double monto, int puntosGanados) {
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
        
        // Actualizar puntos del usuario
        actualizarPuntosUsuario(puntosGanados, monto);
        
        Toast.makeText(mContext, 
                "¡Felicidades! Has ganado " + puntosGanados + " puntos por tu compra de $" + 
                String.format("%.2f", monto), 
                Toast.LENGTH_LONG).show();
    }
    
    private void actualizarPuntosUsuario(int puntosGanados, double montoCompra) {
        String uid = firebaseAuth.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int puntosActuales = 0;
                double totalComprasActual = 0;
                
                try {
                    String puntosStr = "" + snapshot.child("puntos").getValue();
                    puntosActuales = Integer.parseInt(puntosStr.equals("null") ? "0" : puntosStr);
                    
                    String totalStr = "" + snapshot.child("totalCompras").getValue();
                    totalComprasActual = Double.parseDouble(totalStr.equals("null") ? "0" : totalStr);
                } catch (Exception e) {
                    // Usar valores por defecto
                }
                
                int nuevosPuntos = puntosActuales + puntosGanados;
                double nuevoTotal = totalComprasActual + montoCompra;
                String nuevoNivel = Contantes.calcularNivel(nuevosPuntos);
                
                HashMap<String, Object> updates = new HashMap<>();
                updates.put("puntos", nuevosPuntos);
                updates.put("totalCompras", nuevoTotal);
                updates.put("nivel", nuevoNivel);
                updates.put("ultimaVisita", System.currentTimeMillis());
                
                userRef.updateChildren(updates);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Error al actualizar puntos", Toast.LENGTH_SHORT).show();
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