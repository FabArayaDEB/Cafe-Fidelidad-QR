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
import androidx.lifecycle.ViewModelProvider;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.HistorialActivity;
import com.example.cafefidelidaqrdemo.models.Cliente;
import com.example.cafefidelidaqrdemo.databinding.FragmentPerfilBinding;
import com.example.cafefidelidaqrdemo.OpcionesLoginActivity;
import com.example.cafefidelidaqrdemo.DatosPersonalesActivity;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FragmentPerfil extends Fragment {
    private FragmentPerfilBinding binding;
    private Context mContext;
    private AuthRepository authRepository;

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
        
        // Inicializar AuthRepository
        authRepository = AuthRepository.getInstance();
        authRepository.setContext(mContext);
        
        // Configurar listeners
        setupClickListeners();
        
        // Cargar información del usuario
        loadUserData();
    }
    
    private void loadUserData() {
        // Cargar datos básicos del usuario
        AuthRepository.LocalUser currentUser = authRepository.getCurrentUser();
        
        if (currentUser != null) {
            // Mostrar información básica del usuario
            if (binding.tvNombres != null) {
                binding.tvNombres.setText(currentUser.name);
            }
            // TODO: Implementar más campos según el layout
        }
        
        // TODO: Implementar carga de QR y otros datos del cliente
        // binding.tvPuntos.setText("0 puntos");
        // binding.tvNivel.setText("Bronce");
    }
    
    private void setupClickListeners() {
        binding.btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, HistorialActivity.class);
            startActivity(intent);
        });
        
        binding.btnLogout.setOnClickListener(v -> {
            // Cerrar sesión usando AuthRepository
            authRepository.logout();
            
            // Redirigir a la pantalla de login
            Intent intent = new Intent(mContext, OpcionesLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        
        // Configurar listener para refrescar QR
        binding.ivQrPersonal.setOnClickListener(v -> {
            // TODO: Implementar generación de QR
            Toast.makeText(mContext, "Funcionalidad de QR en desarrollo", Toast.LENGTH_SHORT).show();
        });
        
        // Configurar listener para Mi Cuenta
        binding.layoutMiCuenta.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, DatosPersonalesActivity.class);
            startActivity(intent);
        });
    }
    
    private void updateClienteInfo(Cliente cliente) {
        if (cliente != null) {
            // Configurar saludo personalizado
            SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
            int hour = Integer.parseInt(sdf.format(new Date()));
            String greeting;
            if (hour < 12) {
                greeting = "¡Buenos días, " + cliente.getNombre() + "!";
            } else if (hour < 18) {
                greeting = "¡Buenas tardes, " + cliente.getNombre() + "!";
            } else {
                greeting = "¡Buenas noches, " + cliente.getNombre() + "!";
            }
            binding.tvSaludo.setText(greeting);
            
            // Mostrar McID
            binding.tvMcid.setText(cliente.getId());
            
            // Mostrar datos del usuario
            binding.tvNombres.setText(cliente.getNombre());
            binding.tvEmail.setText(cliente.getEmail());
            
            // Mostrar información de fidelidad
            if (binding.tvPuntos != null) {
                binding.tvPuntos.setText(String.valueOf(cliente.getPuntosAcumulados()));
            }
            if (binding.tvNivel != null) {
                binding.tvNivel.setText(cliente.getNivel());
            }
        }
    }
    

    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}