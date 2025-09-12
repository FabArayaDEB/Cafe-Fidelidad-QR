package com.example.cafefidelidaqrdemo.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.HistorialActivity;
import com.example.cafefidelidaqrdemo.databinding.FragmentPerfilBinding;
import com.example.cafefidelidaqrdemo.OpcionesLoginActivity;
import com.example.cafefidelidaqrdemo.EditarPerfilActivity;
import com.example.cafefidelidaqrdemo.DatosPersonalesActivity;
import com.example.cafefidelidaqrdemo.utils.QRGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FragmentPerfil extends Fragment {
    private FragmentPerfilBinding binding;
    private Context mContext;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    public FragmentPerfil() {
        // Required empty public constructor
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        
        // Configurar listeners
        
        binding.btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, HistorialActivity.class);
            startActivity(intent);
        });
        
        binding.btnLogout.setOnClickListener(v -> {
            // Cerrar sesión
            firebaseAuth.signOut();
            
            // Redirigir a la pantalla de login
            Intent intent = new Intent(mContext, OpcionesLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        
        // Configurar listener para refrescar QR
        binding.ivQrPersonal.setOnClickListener(v -> {
            refreshQRCode();
        });
        
        // Configurar listener para Mi Cuenta
        binding.layoutMiCuenta.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, DatosPersonalesActivity.class);
            startActivity(intent);
        });
        
        // Cargar información del usuario
        loadUserInfo();
    }
    
    private void loadUserInfo() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            
            databaseReference.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombre = snapshot.child("nombre").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String telefono = snapshot.child("telefono").getValue(String.class);
                        String fechaNacimiento = snapshot.child("fechaNacimiento").getValue(String.class);
                        
                        // Configurar saludo personalizado
                        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
                        int hour = Integer.parseInt(sdf.format(new Date()));
                        String greeting;
                        if (hour < 12) {
                            greeting = "¡Buenos días, " + (nombre != null ? nombre : "Usuario") + "!";
                        } else if (hour < 18) {
                            greeting = "¡Buenas tardes, " + (nombre != null ? nombre : "Usuario") + "!";
                        } else {
                            greeting = "¡Buenas noches, " + (nombre != null ? nombre : "Usuario") + "!";
                        }
                        binding.tvSaludo.setText(greeting);
                         
                         // Generar McID único basado en el userId
                         String mcId = "MC" + userId.substring(0, Math.min(6, userId.length())).toUpperCase();
                         binding.tvMcid.setText(mcId);
                         
                         // Mostrar datos del usuario
                         binding.tvNombres.setText(nombre != null ? nombre : "No especificado");
                         binding.tvEmail.setText(email != null ? email : "No especificado");
                        
                        // Mostrar información de fidelidad si está disponible
                        if (binding.tvPuntos != null) {
                            binding.tvPuntos.setText("0"); // Puntos por defecto
                        }
                        if (binding.tvNivel != null) {
                            binding.tvNivel.setText("Bronce"); // Nivel por defecto
                        }
                        
                        // Generar código QR
                        generateClientQR();
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(mContext, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void generateClientQR() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String mcId = "MC" + userId.substring(0, Math.min(6, userId.length())).toUpperCase();
            
            // Obtener datos del usuario desde Firebase para generar QR
            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombre = snapshot.child("nombre").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        
                        // Generar QR con los datos del cliente
                        try {
                            Bitmap qrBitmap = QRGenerator.generateClientQR(
                                userId,
                                nombre != null ? nombre : "Usuario",
                                email != null ? email : currentUser.getEmail(),
                                mcId,
                                0 // Puntos por defecto
                            );
                            if (qrBitmap != null) {
                                binding.ivQrPersonal.setImageBitmap(qrBitmap);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(mContext, "Error al generar código QR", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(mContext, "Error al obtener datos para QR", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void refreshQRCode() {
        // Mostrar indicador de actualización
        binding.ivQrPersonal.setAlpha(0.5f);
        
        // Refrescar QR
        generateClientQR();
        
        Toast.makeText(mContext, "Código QR actualizado", Toast.LENGTH_SHORT).show();
        
        // Restaurar opacidad
        binding.ivQrPersonal.setAlpha(1.0f);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}