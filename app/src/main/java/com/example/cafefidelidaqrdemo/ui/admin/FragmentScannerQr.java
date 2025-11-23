package com.example.cafefidelidaqrdemo.ui.admin;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.databinding.FragmentScannerQrBinding;
import com.example.cafefidelidaqrdemo.ui.admin.viewmodels.ScannerQrAdminViewModel;
import com.example.cafefidelidaqrdemo.utils.Resources;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@androidx.camera.core.ExperimentalGetImage
public class FragmentScannerQr extends Fragment {

    private static final String TAG = "FragmentScannerQr";
    private FragmentScannerQrBinding binding;
    private ScannerQrAdminViewModel viewModel;

    // Variables de CameraX
    private ExecutorService cameraExecutor;
    private boolean isScanning = false;
    // Verificador de permisos de camara
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(getContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                }
            });

    public FragmentScannerQr() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentScannerQrBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ScannerQrAdminViewModel.class);
        // Tarea pesada de la camara que se ejecute en un hilo separado
        cameraExecutor = Executors.newSingleThreadExecutor();

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        // Observamos el estado del escaner para recibir la respuesta del estado del QR escaneado
        viewModel.getScannerQrVisitasState().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    //desabilitar botones | aun no implementado
                    break;
                case SUCCESS:
                    if (resource.data != null) {
                        isScanning = false;
                        // se muestra la carta de confirmacion
                        mostrarExito(String.valueOf(resource.data.getIdCodigoQr()));
                    }
                    break;
                case ERROR:
                    isScanning = false;
                    String errorMsg = resource.message != null ? resource.message : "Error desconocido";
                    String idVisual = (resource.data != null) ? String.valueOf(resource.data.getIdCodigoQr()) : "---";
                    // se muestra la carta de error
                    mostrarError(errorMsg, idVisual);
                    break;
            }
        });
    }

    // Configuración de listeners
    private void setupListeners() {
        binding.btnIniciarEscaner.setOnClickListener(v -> {
            binding.cardResultadoScan.setVisibility(View.GONE);
            viewModel.resetearEstado();
            isScanning = true;
            // Verificamos los permisos de la camara
            checkCameraPermissionAndStart();
        });

        binding.btnRegresar.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
    }

    // Verificacion de permisos
    private void checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            // Aqui se solicitan los permisos de la camara si estos no existen
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // Instaciar camara
    private void startCamera() {
        binding.cameraPreviewFrame.setVisibility(View.VISIBLE);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        // Cuando la instancia de la camara este lista de llama al objeto de la camara
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error al iniciar cámara: " + e.getMessage());
                Toast.makeText(getContext(), "Erroe al mostrar la camara", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    // Configuracion de la camara
    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        // definimos que muestre en pantalla lo que la camara ve
        preview.setSurfaceProvider(binding.cameraPreviewFrame.getSurfaceProvider());

        // definimos de que manera la camara recibira y procesara la imagen
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::procesarImagen);

        // definimos que se usara la camara trasera por defecto
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();
            // Confuguramos la camara
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception exc) {
            Log.e(TAG, "Fallo al vincular casos de uso", exc);
        }
    }

    // Procesar imagen de cada fotograma que detecta la camara
    private void procesarImagen(androidx.camera.core.ImageProxy imageProxy) {
        if (!isScanning) {
            imageProxy.close();
            return;
        }

        // tranformamos la imagen recibida a una que pueda ser procesada por ML Kit
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                    // se le dice en que tipo de formato va a escanear los objetos
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build();
            BarcodeScanner scanner = BarcodeScanning.getClient(options);

            // Cuando llegue un fotograma desde la camara se procesa y se convierten los datos del QR en String
            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();

                            if (rawValue != null && isScanning) {
                                isScanning = false;

                                requireActivity().runOnUiThread(() -> {
                                    // Se ocupan los deatos obtenidos (Content y id) para procesor el QR
                                    viewModel.procesarQRVisita(rawValue);
                                });
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error ML Kit: " + e.getMessage()))
                    .addOnCompleteListener(task -> imageProxy.close()); // IMPORTANTE: Cerrar siempre
        } else {
            imageProxy.close();
        }
    }

    private void mostrarExito(String idQr) {
        mostrarTarjeta(true, "¡Visita Registrada!", "CANJEADO", idQr);
    }

    private void mostrarError(String mensajeError, String idQr) {
        mostrarTarjeta(false, mensajeError, "ERROR", idQr);
    }

    private void mostrarTarjeta(boolean esExito, String mensaje, String estado, String id) {
        // Hacemos visible la tarjeta
        binding.cardResultadoScan.setVisibility(View.VISIBLE);

        // Seteamos textos
        binding.tvResultadoMensaje.setText(mensaje);
        binding.tvResultadoEstado.setText(estado);
        binding.tvResultadoId.setText("#" + id);

        // Cambiamos colores e iconos según el resultado
        if (esExito) {
            binding.ivResultadoIcono.setImageResource(R.drawable.ic_check_circle);
            binding.ivResultadoIcono.setColorFilter(Color.parseColor("#2E7D32")); // Verde
            binding.tvResultadoMensaje.setTextColor(Color.parseColor("#2E7D32"));
            binding.tvResultadoEstado.setTextColor(Color.parseColor("#2E7D32"));
            binding.btnIniciarEscaner.setText("Escanear Otro");
        } else {
            binding.ivResultadoIcono.setImageResource(R.drawable.ic_error);
            binding.ivResultadoIcono.setColorFilter(Color.parseColor("#C62828")); // Rojo
            binding.tvResultadoMensaje.setTextColor(Color.parseColor("#C62828"));
            binding.tvResultadoEstado.setTextColor(Color.parseColor("#C62828"));
            binding.btnIniciarEscaner.setText("Reintentar");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Evitar fugas de memoria con Binding
        if (cameraExecutor != null) {
            cameraExecutor.shutdown(); // Liberar hilo de cámara
        }
    }
}