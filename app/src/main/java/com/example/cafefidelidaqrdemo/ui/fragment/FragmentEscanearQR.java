package com.example.cafefidelidaqrdemo.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.databinding.FragmentEscanearQrBinding;
import com.example.cafefidelidaqrdemo.ui.viewmodel.QRScannerViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.google.zxing.BarcodeFormat;
import java.util.Arrays;
import java.util.Collection;

/**
 * Fragment para escanear códigos QR de sucursales
 * Maneja permisos de cámara, validación de QR y registro de visitas
 */
public class FragmentEscanearQR extends Fragment {
    
    private static final String TAG = "FragmentEscanearQR";
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    
    private FragmentEscanearQrBinding binding;
    private QRScannerViewModel viewModel;
    private DecoratedBarcodeView barcodeView;
    private boolean isScanning = false;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEscanearQrBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(QRScannerViewModel.class);
        
        // Configurar vista de código de barras
        setupBarcodeView();
        
        // Configurar listeners
        setupListeners();
        
        // Observar ViewModel
        observeViewModel();
        
        // Verificar permisos de cámara
        checkCameraPermission();
    }
    
    private void setupBarcodeView() {
        barcodeView = binding.barcodeScanner;
        
        // Configurar formatos de código de barras (solo QR)
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        
        // Configurar callback de escaneo
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (isScanning && result.getText() != null) {
                    handleQRScanned(result.getText());
                }
            }
        });
    }
    
    private void setupListeners() {
        // Botón de flash
        binding.btnFlash.setOnClickListener(v -> toggleFlash());
        
        // Botón de reintentar
        binding.btnReintentar.setOnClickListener(v -> {
            hideError();
            startScanning();
        });
        
        // Botón de configuración (para permisos)
        binding.btnConfiguracion.setOnClickListener(v -> openAppSettings());
        
        // Botón de cerrar
        binding.btnCerrar.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }
    
    private void observeViewModel() {
        // Observar estado de procesamiento
        viewModel.getEstadoProcesamiento().observe(getViewLifecycleOwner(), estado -> {
            switch (estado) {
                case IDLE:
                    hideLoading();
                    break;
                case PROCESSING:
                    showLoading("Validando QR...");
                    break;
                case SUCCESS:
                    hideLoading();
                    break;
                case ERROR:
                    hideLoading();
                    break;
            }
        });
        
        // Observar mensajes
        viewModel.getMensaje().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null && !mensaje.isEmpty()) {
                showMessage(mensaje);
            }
        });
        
        // Observar errores
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });
        
        // Observar progreso actualizado
        viewModel.getProgresoActualizado().observe(getViewLifecycleOwner(), progreso -> {
            if (progreso != null && !progreso.isEmpty()) {
                showSuccessDialog(progreso);
            }
        });
    }
    
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            requestCameraPermission();
        }
    }
    
    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
            // Mostrar explicación
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Permiso de cámara requerido")
                    .setMessage("Esta aplicación necesita acceso a la cámara para escanear códigos QR de las sucursales.")
                    .setPositiveButton("Conceder", (dialog, which) -> {
                        ActivityCompat.requestPermissions(requireActivity(),
                                new String[]{Manifest.permission.CAMERA},
                                CAMERA_PERMISSION_REQUEST);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        showPermissionDeniedState();
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                showPermissionDeniedState();
            }
        }
    }
    
    private void startScanning() {
        if (barcodeView != null) {
            binding.layoutScanner.setVisibility(View.VISIBLE);
            binding.layoutError.setVisibility(View.GONE);
            binding.layoutPermissionDenied.setVisibility(View.GONE);
            
            barcodeView.resume();
            isScanning = true;
            
            // Mostrar instrucciones
            binding.textInstrucciones.setText("Apunta la cámara hacia el código QR de la sucursal");
        }
    }
    
    private void stopScanning() {
        if (barcodeView != null) {
            barcodeView.pause();
            isScanning = false;
        }
    }
    
    private void handleQRScanned(String qrContent) {
        // Detener escaneo temporalmente
        stopScanning();
        
        // Procesar QR con ViewModel
        viewModel.procesarQR(qrContent);
    }
    
    private void toggleFlash() {
        if (barcodeView != null) {
            // TODO: Implementar toggle flash - método isTorchOn() no existe
            // barcodeView.setTorchOn(!barcodeView.getBarcodeView().getCameraSettings().isTorchOn());
            
            // Actualizar icono del botón
            // boolean flashOn = barcodeView.getBarcodeView().getCameraSettings().isTorchOn();
            // binding.btnFlash.setImageResource(flashOn ? 
            //         R.drawable.ic_flash_off : R.drawable.ic_flash_on);
        }
    }
    
    private void showLoading(String mensaje) {
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.textLoading.setText(mensaje);
        binding.layoutScanner.setVisibility(View.GONE);
    }
    
    private void hideLoading() {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutScanner.setVisibility(View.VISIBLE);
    }
    
    private void showError(String error) {
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.layoutScanner.setVisibility(View.GONE);
        binding.textError.setText(error);
    }
    
    private void hideError() {
        binding.layoutError.setVisibility(View.GONE);
        binding.layoutScanner.setVisibility(View.VISIBLE);
    }
    
    private void showPermissionDeniedState() {
        binding.layoutPermissionDenied.setVisibility(View.VISIBLE);
        binding.layoutScanner.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
    }
    
    private void showMessage(String mensaje) {
        if (getView() != null) {
            Snackbar.make(getView(), mensaje, Snackbar.LENGTH_LONG).show();
        }
    }
    
    private void showSuccessDialog(String progreso) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("¡Visita registrada!")
                .setMessage("Tu visita ha sido registrada exitosamente.\n\n" + progreso)
                .setPositiveButton("Continuar", (dialog, which) -> {
                    // Reiniciar escaneo después de un breve delay
                    binding.getRoot().postDelayed(() -> {
                        if (isAdded()) {
                            startScanning();
                        }
                    }, 1000);
                })
                .setNegativeButton("Salir", (dialog, which) -> {
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                })
                .setCancelable(false)
                .show();
    }
    
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if (barcodeView != null && isScanning) {
                barcodeView.resume();
            }
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (barcodeView != null) {
            barcodeView.pause();
        }
        binding = null;
    }
}