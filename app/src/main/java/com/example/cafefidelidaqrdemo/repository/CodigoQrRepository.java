package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.models.CodigoQr;
import com.example.cafefidelidaqrdemo.utils.Resources;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CodigoQrRepository {

    private static final String TAG = "CodigoQrRepository";
    private final CafeFidelidadDB database;
    private final Executor executor;
    private final Context context;

    public CodigoQrRepository(Context context) {
        this.context = context;
        this.database = CafeFidelidadDB.getInstance(context);
        // Solamente 1 hilo para asegurar la actualizacion del qr anterior en la base de datos antes de la creacion de uno nuevo
        this.executor = Executors.newFixedThreadPool(1);
    }

    // Insertar nuevo codigo qr en la base de datos
    // en todos los metodos retornamos live data para hacer uso de lambda en el ViewModel
    public LiveData<Resources<Integer>> insertarCodigoQrRepo(CodigoQr codigoQr, String idCliente) {
        Log.d(TAG, "Insertando Codigo Qr desde Repo...");
        MutableLiveData<Resources<Integer>> result = new MutableLiveData<>();

        result.setValue(Resources.loading(null));

        executor.execute(() -> {
            try {
                Log.d(TAG, "Verificacion: " + codigoQr.getContentQr());

                if (codigoQr.getContentQr() == null || codigoQr.getContentQr().trim().isEmpty()) {
                    result.postValue(Resources.error("El codigo QR no puede estar vacio", null));
                    return;
                }
                if (codigoQr.getGenerationTime() < 0) {
                    result.postValue(Resources.error("Tiempo de generación inválido", null));
                    return;
                }
                if (idCliente == null || idCliente.trim().isEmpty()) {
                    result.postValue(Resources.error("Cliente inválido", null));
                    return;
                }
                if (database.existeCodigoQr(codigoQr.getContentQr(), idCliente)) {
                    result.postValue(Resources.error("El código QR ya existe", null));
                    return;
                }

                Log.d(TAG, "Insertando en DB...");
                long res = database.insertarCodigoQr(codigoQr, idCliente);

                Integer id = (int) res;

                if (res > 0) {
                    Log.d(TAG, "Éxito. ID: " + id);
                    result.postValue(Resources.success(id));
                } else {
                    Log.d(TAG, "Fallo en DB.");
                    result.postValue(Resources.error("Error al insertar en base de datos", null));
                }

            } catch (Exception e) {
                Log.e(TAG, "Excepción: " + e.getMessage());
                result.postValue(Resources.error("Error desconocido: " + e.getMessage(), null));
            }
        });
        return result;
    }

    // Actualiza el estado del codigo qr antiguo en la base de datos
    public void cambiarEstadoQrRepo(String contenidoAntiguo) {
        Log.d(TAG, "Cambiando estado de QR antiguo.");

        CodigoQr codigoQrAntiguo = database.obtenerCodigoQrPorContent(contenidoAntiguo);

        if (codigoQrAntiguo != null) {
            codigoQrAntiguo.setEstado("INACTIVO");
            int FilasAfectadas = database.actualizarCodigoQr(codigoQrAntiguo);

            if (FilasAfectadas > 0) {
                Log.d(TAG, "Éxito.");
            }
        }
    }

    // Obtiene el codigo qr de la base de datos por su content para la creacion de un nuevo QR
    public LiveData<Resources<CodigoQr>> obtenerCodigoQrRepo(String contentQr) {
        Log.d(TAG, "Obtener Codigo Qr Repo: " + contentQr);
        MutableLiveData<Resources<CodigoQr>> result = new MutableLiveData<>();

        result.setValue(Resources.loading(null));

        executor.execute(() -> {
            try {
                Log.d(TAG, "Verificacion: " + contentQr);

                if (contentQr == null || contentQr.trim().isEmpty()) {
                    result.postValue(Resources.error("El código QR no puede estar vacío.", null));
                    return;
                }

                Log.d(TAG, "Consultando DB...");
                CodigoQr qr = database.obtenerCodigoQrPorContent(contentQr);

                if (qr != null) {
                    Log.d(TAG, "Éxito. ID: " + qr.getIdCodigoQr());
                    result.postValue(Resources.success(qr));
                } else {
                    Log.d(TAG, "Fallo en DB.");
                    result.postValue(Resources.error("No se encontró el código QR.", null));
                }
            } catch (Exception e) {
                Log.e(TAG, "Excepción: " + e.getMessage());
                result.postValue(Resources.error("Error al obtener datos: " + e.getMessage(), null));
            }
        });
        return result;
    }

    // Verificar si el mismo cliente porta el mismo codigo QR en con el mismo content en la base de datos
    public LiveData<Resources<Integer>> verificarExistenciaDeQr(String contentQr, String idCliente) {
        Log.d(TAG, "Verificar existencia de qr: " + contentQr);
        MutableLiveData<Resources<Integer>> result = new MutableLiveData<>();

        result.setValue(Resources.loading(null));

        executor.execute(() -> {
            try {
                Log.d(TAG, "Verificacion: " + contentQr);

                if (contentQr == null || contentQr.trim().isEmpty()) {
                    result.postValue(Resources.error("El código QR no puede estar vacío.", null));
                    return;
                }
                if (idCliente == null || idCliente.trim().isEmpty()) {
                    result.postValue(Resources.error("El id del cliente es inválido.", null));
                    return;
                }

                Log.d(TAG, "Verificando en DB...");

                int res = database.existeCodigoQr(contentQr, idCliente) ? 1 : 0;

                Log.d(TAG, "Éxito. Resultado: " + res);
                result.postValue(Resources.success(res));

            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Excepción: " + nfe.getMessage());
                result.postValue(Resources.error("El ID del cliente no es numérico.", null));
            } catch (Exception e) {
                Log.e(TAG, "Excepción: " + e.getMessage());
                result.postValue(Resources.error("Error de verificación Creacion QR: " + e.getMessage(), null));
            }
        });
        return result;
    }
}