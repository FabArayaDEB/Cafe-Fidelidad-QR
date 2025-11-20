package com.example.cafefidelidaqrdemo.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.models.Cliente;
import com.example.cafefidelidaqrdemo.repository.ClienteRepository;
import com.example.cafefidelidaqrdemo.utils.QRGenerator;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * Actividad de escaneo de QR para administradores.
 * Permite identificar al cliente mediante su código QR.
 */
public class QRScannerActivity extends AppCompatActivity {

    private static final int REQ_CAMERA = 1001;

    private DecoratedBarcodeView barcodeScanner;
    private FloatingActionButton btnFlash;
    private MaterialButton manualEntryButton;
    private MaterialButton btnRegisterPurchase;

    private View clientInfoLayout;
    private TextView tvClientName;
    private TextView tvClientEmail;
    private TextView tvClientPoints;

    private boolean torchOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        barcodeScanner = findViewById(R.id.barcodeScanner);
        btnFlash = findViewById(R.id.btnFlash);
        manualEntryButton = findViewById(R.id.manualEntryButton);
        btnRegisterPurchase = findViewById(R.id.btnRegisterPurchase);

        clientInfoLayout = findViewById(R.id.clientInfoLayout);
        tvClientName = findViewById(R.id.tvClientName);
        tvClientEmail = findViewById(R.id.tvClientEmail);
        tvClientPoints = findViewById(R.id.tvClientPoints);

        setupActions();
        ensureCameraPermission();
    }

    private void setupActions() {
        if (btnFlash != null && barcodeScanner != null) {
            btnFlash.setOnClickListener(v -> toggleFlash());
        }

        if (manualEntryButton != null) {
            manualEntryButton.setOnClickListener(v -> promptManualEntry());
        }

        if (btnRegisterPurchase != null) {
            btnRegisterPurchase.setOnClickListener(v -> {
                Toast.makeText(this, "Registrar compra: próximamente", Toast.LENGTH_SHORT).show();
            });
        }

        if (barcodeScanner != null) {
            barcodeScanner.decodeContinuous(new BarcodeCallback() {
                @Override
                public void barcodeResult(BarcodeResult result) {
                    if (result == null || result.getText() == null) return;
                    // Pausar para evitar lecturas múltiples mientras procesamos
                    barcodeScanner.pause();
                    processQR(result.getText());
                }
            });
        }
    }

    private void ensureCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
        } else {
            startScanning();
        }
    }

    private void startScanning() {
        if (barcodeScanner != null) {
            barcodeScanner.resume();
        }
    }

    private void toggleFlash() {
        torchOn = !torchOn;
        if (torchOn) {
            barcodeScanner.setTorchOn();
        } else {
            barcodeScanner.setTorchOff();
        }
    }

    private void processQR(String qrContent) {
        // Sanitizar y normalizar entrada
        String raw = qrContent == null ? "" : qrContent.trim();
        if (raw.isEmpty()) {
            Toast.makeText(this, "QR vacío", Toast.LENGTH_SHORT).show();
            resumeScanningDelayed();
            return;
        }

        // Intentar parseo del formato esperado (CLIENTE:...)
        QRGenerator.ClienteQRData data = QRGenerator.parseClientQR(raw);

        String clienteId = null;
        String nombre = null;
        String email = null;

        if (data != null) {
            clienteId = data.getClienteId();
            nombre = data.getNombre();
            email = data.getEmail();
        } else {
            // Fallback: intentar heurística mediante QRValidator
            if (com.example.cafefidelidaqrdemo.utils.QRValidator.isClienteQR(raw)) {
                clienteId = com.example.cafefidelidaqrdemo.utils.QRValidator.extractClienteId(raw);
            }
        }

        // Buscar cliente por ID (preferente) o por email
        ClienteRepository repo = ClienteRepository.getInstance(this.getApplicationContext());
        Cliente cliente = null;
        try {
            if (clienteId != null && !clienteId.isEmpty()) {
                int id = Integer.parseInt(clienteId.replaceAll("[^0-9]", ""));
                cliente = repo.getClienteByIdSync(id);
            }
        } catch (NumberFormatException ignored) { }

        if (cliente == null && email != null && !email.isEmpty()) {
            cliente = repo.getClienteByEmailSync(email);
        }

        if (cliente == null && data == null) {
            Toast.makeText(this, "QR inválido o no reconocido", Toast.LENGTH_LONG).show();
            resumeScanningDelayed();
            return;
        }

        if (cliente == null) {
            // Mostrar los datos básicos del QR aunque no esté en base local
            showClientInfo(nombre, email, "0");
            Toast.makeText(this, "Cliente no encontrado en base local", Toast.LENGTH_SHORT).show();
        } else {
            String puntos = String.valueOf(cliente.getPuntosAcumulados());
            showClientInfo(cliente.getNombre(), cliente.getEmail(), puntos);
        }

        // Mostrar acciones disponibles tras identificación
        if (btnRegisterPurchase != null) {
            btnRegisterPurchase.setVisibility(View.VISIBLE);
        }
        // Mantener en pausa hasta que el administrador decida continuar
    }

    private void showClientInfo(String nombre, String email, String puntos) {
        if (clientInfoLayout != null) {
            clientInfoLayout.setVisibility(View.VISIBLE);
        }
        if (tvClientName != null) tvClientName.setText(nombre != null ? nombre : "");
        if (tvClientEmail != null) tvClientEmail.setText(email != null ? email : "");
        if (tvClientPoints != null) tvClientPoints.setText(puntos != null ? puntos : "0");
    }

    private void promptManualEntry() {
        // Entrada manual básica: pedir email o ID mediante un diálogo simple
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("ID o Email del cliente");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Entrada Manual")
                .setMessage("Introduce el ID numérico o el email del cliente")
                .setView(input)
                .setPositiveButton("Buscar", (d, w) -> {
                    String value = input.getText().toString().trim();
                    buscarClienteManual(value);
                })
                .setNegativeButton("Cancelar", (d, w) -> resumeScanningDelayed())
                .show();
    }

    private void buscarClienteManual(String value) {
        if (value.isEmpty()) {
            Toast.makeText(this, "Valor vacío", Toast.LENGTH_SHORT).show();
            resumeScanningDelayed();
            return;
        }

        ClienteRepository repo = ClienteRepository.getInstance(this.getApplicationContext());
        Cliente cliente = null;
        try {
            int id = Integer.parseInt(value);
            cliente = repo.getClienteByIdSync(id);
        } catch (NumberFormatException nfe) {
            cliente = repo.getClienteByEmailSync(value);
        }

        if (cliente != null) {
            showClientInfo(cliente.getNombre(), cliente.getEmail(), String.valueOf(cliente.getPuntosAcumulados()));
        } else {
            Toast.makeText(this, "Cliente no encontrado", Toast.LENGTH_SHORT).show();
        }
        // Pausar mantiene, ofrecer continuar
    }

    private void resumeScanningDelayed() {
        // Pequeño retraso para evitar doble lectura inmediata
        barcodeScanner.postDelayed(() -> {
            if (barcodeScanner != null) barcodeScanner.resume();
        }, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeScanner != null) barcodeScanner.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeScanner != null) barcodeScanner.pause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}