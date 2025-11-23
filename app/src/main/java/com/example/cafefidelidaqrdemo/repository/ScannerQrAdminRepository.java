package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import android.util.Log;

import androidx.camera.core.processing.SurfaceProcessorNode;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.models.CodigoQr;
import com.example.cafefidelidaqrdemo.utils.Resources;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerQrAdminRepository {
    private static final String TAG = "ScannerQrAdminRepository";
    private final CafeFidelidadDB database;
    private final ExecutorService executorService;
    private Context context;

    public ScannerQrAdminRepository(Context context) {
        this.context = context;
        this.database = CafeFidelidadDB.getInstance(context);
        this.executorService = Executors.newFixedThreadPool(1);
    }

    // Método para verificar y canjear un QR
    // Verificar el estado y si ha sido escaneado
    // canjear el estado del QR a INACTIVO cuando este sea leido por la camara
    public LiveData<Resources<CodigoQr>> verificarYCanjearQr(String formatoQr) {
        MutableLiveData<Resources<CodigoQr>> result = new MutableLiveData<>();
        result.postValue(Resources.loading(null));

        executorService.execute(() -> {
            try {
                Log.d(TAG, "Validando datos de formato QR...");
                // Separacion de los datos del foramto de QR leido por la camara
                String[] datos = formatoQr.split("\\|");
                if (datos == null || datos.length != 2) {
                    result.postValue(Resources.error("Formato QR inválido", null));
                    Log.d(TAG, "Formato QR inválido");
                    return; // IMPORTANTE: Detener ejecución
                }

                String contentQr = datos[0];
                int idQr = Integer.parseInt(datos[1]);

                Log.d(TAG, "Formato valido.");
                Log.d(TAG, "Buscando QR en BD...");
                // Verificacion del estado del QR
                CodigoQr codigoQr = database.obtenerCodigoQrPorIdAndContent(idQr, contentQr);

                if (codigoQr == null) {
                    result.postValue(Resources.error("QR no encontrado en sistema", null));
                    Log.d(TAG, "QR no encontrado en sistema");
                    return;
                }

                Log.d(TAG, "QR encontrado en sistema.");
                Log.d(TAG, "Verificando QR...");
                if (codigoQr.isScanned()) {
                    result.postValue(Resources.error("El QR ya fue utilizado anteriormente", codigoQr));
                    Log.d(TAG, "El QR ya fue utilizado anteriormente");
                    return;
                }

                if ("INACTIVO".equals(codigoQr.getEstado())) {
                    result.postValue(Resources.error("El QR está inactivo o cancelado", codigoQr));
                    Log.d(TAG, "El QR está inactivo o cancelado");
                    return;
                }

                Log.d(TAG, "Verificacion de datos completa");
                Log.d(TAG, "Actualizando estado del QR...");
                codigoQr.setIsScanned(true);
                codigoQr.setEstado("INACTIVO"); // "USADO"

                // Actualizacion del estado del QR una vez ya escaneado
                int filas = database.actualizarCodigoQr(codigoQr);

                if (filas > 0) {
                    // ÉXITO REAL
                    Log.d(TAG,"Actualización realizada con éxito");
                    result.postValue(Resources.success(codigoQr));
                } else {
                    Log.d(TAG,"Error al actualizar el QR");
                    result.postValue(Resources.error("Error al guardar cambios en BD", null));
                }

            } catch (Exception e) {
                Log.d(TAG, "Error: " + e.getMessage());
                result.postValue(Resources.error("Error: " + e.getMessage(), null));
            }
        });
        return result;
    }
}
