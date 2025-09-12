package com.example.cafefidelidaqrdemo.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.databinding.ActivityQrscannerBinding;
import com.example.cafefidelidaqrdemo.utils.QRGenerator;
import com.example.cafefidelidaqrdemo.viewmodels.QRScannerViewModel;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

import java.text.DecimalFormat;

public class QRScannerActivity extends AppCompatActivity {
    
    private ActivityQrscannerBinding binding;
    private QRScannerViewModel viewModel;
    private QRGenerator.ClienteQRData currentClientData;
    private boolean isFlashOn = false;
    
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrscannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        viewModel = new ViewModelProvider(this).get(QRScannerViewModel.class);
        
        setupToolbar();
        setupObservers();
        setupClickListeners();
        
        if (checkCameraPermission()) {
            startScanning();
        } else {
            requestCameraPermission();
        }
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupClickListeners() {
        // Botón de flash
        binding.btnFlash.setOnClickListener(v -> toggleFlash());
        
        // Botón de entrada manual
        binding.manualEntryButton.setOnClickListener(v -> showManualEntryDialog());
        
        // Botón de registrar compra
        binding.btnRegisterPurchase.setOnClickListener(v -> showPurchaseDialog());
    }
    
    private void setupObservers() {
        // Observar resultados del ViewModel
        viewModel.getScanResult().observe(this, result -> {
            if (result != null) {
                if (result.isSuccess()) {
                    binding.tvInstructions.setText("Cliente verificado: " + result.getClienteData().getNombre());
                    currentClientData = result.getClienteData();
                    showClientInfo(result.getClienteData());
                } else {
                    binding.tvInstructions.setText("Error: " + result.getErrorMessage());
                    currentClientData = null;
                    hideClientInfo();
                    Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_LONG).show();
                    // Reanudar escaneo después de un error
                    resumeScanning();
                }
            }
        });
        
        viewModel.getPurchaseResult().observe(this, success -> {
            if (success != null) {
                if (success) {
                    Toast.makeText(this, "Compra registrada exitosamente", Toast.LENGTH_SHORT).show();
                    clearClientInfo();
                    resumeScanning();
                } else {
                    Toast.makeText(this, "Error al registrar la compra", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, "Permiso de cámara requerido para escanear QR", 
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    private void startScanning() {
        binding.barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null) {
                    // Pausar escaneo mientras se procesa
                    binding.barcodeScanner.pause();
                    
                    // Procesar el código QR
                    viewModel.processQRCode(result.getText());
                }
            }
        });
        
        binding.barcodeScanner.resume();
    }
    
    private void resumeScanning() {
        binding.barcodeScanner.resume();
    }
    
    private void toggleFlash() {
        isFlashOn = !isFlashOn;
        if (isFlashOn) {
            binding.barcodeScanner.setTorchOn();
            binding.btnFlash.setImageResource(R.drawable.ic_flash_off);
        } else {
            binding.barcodeScanner.setTorchOff();
            binding.btnFlash.setImageResource(R.drawable.ic_flash_on);
        }
    }
    
    private void showClientInfo(QRGenerator.ClienteQRData clienteData) {
        binding.clientInfoLayout.getRoot().setVisibility(View.VISIBLE);
        
        // Mostrar información del cliente
        binding.clientInfoLayout.tvClientName.setText(clienteData.getNombre());
        binding.clientInfoLayout.tvClientEmail.setText(clienteData.getEmail());
        binding.clientInfoLayout.tvClientPoints.setText(String.valueOf(clienteData.getPuntos()));
        binding.clientInfoLayout.tvClientLevel.setText(clienteData.getNivel() != null ? clienteData.getNivel() : "Regular");
        
        binding.btnRegisterPurchase.setVisibility(View.VISIBLE);
    }
    
    private void hideClientInfo() {
        binding.clientInfoLayout.getRoot().setVisibility(View.GONE);
        binding.btnRegisterPurchase.setVisibility(View.GONE);
    }
    
    private void clearClientInfo() {
        currentClientData = null;
        
        // Limpiar los campos del layout incluido
        binding.clientInfoLayout.tvClientName.setText("");
        binding.clientInfoLayout.tvClientEmail.setText("");
        binding.clientInfoLayout.tvClientPoints.setText("");
        binding.clientInfoLayout.tvClientLevel.setText("");
        
        hideClientInfo();
        binding.tvInstructions.setText("Apunta la cámara hacia el código QR del cliente");
    }
    
    private void showManualEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Entrada Manual de QR");
        
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Ingresa el código QR del cliente");
        builder.setView(input);
        
        builder.setPositiveButton("Procesar", (dialog, which) -> {
            String qrContent = input.getText().toString().trim();
            if (!qrContent.isEmpty()) {
                viewModel.processQRCode(qrContent);
            } else {
                Toast.makeText(this, "Por favor ingresa un código QR válido", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }
    
    private void showPurchaseDialog() {
        if (currentClientData == null) {
            Toast.makeText(this, "No hay cliente seleccionado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registrar Compra");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_purchase, null);
        android.widget.EditText amountInput = dialogView.findViewById(R.id.etAmount);
        android.widget.EditText descriptionInput = dialogView.findViewById(R.id.etDescription);
        
        builder.setView(dialogView);
        
        builder.setPositiveButton("Registrar", (dialog, which) -> {
            String amountStr = amountInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa el monto", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(this, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                viewModel.registerPurchase(currentClientData, amount, description);
                
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null && binding.barcodeScanner != null) {
            binding.barcodeScanner.resume();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (binding != null && binding.barcodeScanner != null) {
            binding.barcodeScanner.pause();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}