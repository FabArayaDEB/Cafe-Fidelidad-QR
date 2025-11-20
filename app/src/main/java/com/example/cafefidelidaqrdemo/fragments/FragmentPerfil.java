package com.example.cafefidelidaqrdemo.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.example.cafefidelidaqrdemo.viewmodels.ClienteQRViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FragmentPerfil extends Fragment {
    private FragmentPerfilBinding binding;
    private Context mContext;
    private AuthRepository authRepository;
    private ClienteQRViewModel clienteQRViewModel;
    private Handler refreshHandler;
    private final long REFRESH_INTERVAL_MS = 60_000; // 60 segundos
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (clienteQRViewModel != null) {
                clienteQRViewModel.refreshQR();
            }
            if (refreshHandler != null) {
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        }
    };

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

        // Inicializar ViewModel de QR
        clienteQRViewModel = new ViewModelProvider(this).get(ClienteQRViewModel.class);
        setupObservers();
        
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
        }
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
        
        // Ampliar QR al tocar
        binding.ivQrPersonal.setOnClickListener(v -> {
            Bitmap current = clienteQRViewModel != null ? clienteQRViewModel.qrBitmap.getValue() : null;
            if (current != null) {
                showQrDialog(current);
            } else {
                Toast.makeText(mContext, "QR no disponible aún", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Configurar listener para Mi Cuenta
        binding.layoutMiCuenta.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, DatosPersonalesActivity.class);
            startActivity(intent);
        });
    }

    private void setupObservers() {
        // Observa el bitmap del QR
        clienteQRViewModel.qrBitmap.observe(getViewLifecycleOwner(), bitmap -> {
            if (bitmap != null && binding != null && binding.ivQrPersonal != null) {
                binding.ivQrPersonal.setImageBitmap(bitmap);
            }
        });
        // Observa datos del cliente
        clienteQRViewModel.clienteData.observe(getViewLifecycleOwner(), cliente -> {
            updateClienteInfo(cliente);
            if (binding != null) {
                binding.tvSaludo.setText(clienteQRViewModel.getPersonalizedGreeting());
                binding.tvMcid.setText(clienteQRViewModel.getClienteMcId());
            }
        });
        // Observa errores
        clienteQRViewModel.error.observe(getViewLifecycleOwner(), err -> {
            if (err != null && !err.isEmpty()) {
                Toast.makeText(mContext, err, Toast.LENGTH_SHORT).show();
                clienteQRViewModel.clearError();
            }
        });
    }

    private void showQrDialog(Bitmap bitmap) {
        View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_qr_fullscreen, null);
        ImageView imageView = dialogView.findViewById(R.id.iv_qr_fullscreen);
        imageView.setImageBitmap(bitmap);
        new AlertDialog.Builder(mContext)
                .setView(dialogView)
                .setPositiveButton("Cerrar", (d, w) -> d.dismiss())
                .show();
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
            
            // Mostrar ID generado por ViewModel
            binding.tvMcid.setText(clienteQRViewModel.getClienteMcId());
            
            // Mostrar datos del usuario
            binding.tvNombres.setText(cliente.getNombre());
            binding.tvEmail.setText(cliente.getEmail());
            
            // Mostrar información de fidelidad
            if (binding.tvPuntos != null) {
                binding.tvPuntos.setText(String.valueOf(cliente.getPuntosAcumulados()));
            }
            // Sistema de niveles eliminado: no se muestra nivel
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (refreshHandler == null) refreshHandler = new Handler(Looper.getMainLooper());
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (refreshHandler != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}