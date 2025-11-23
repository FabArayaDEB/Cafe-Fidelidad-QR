package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;
import android.graphics.Bitmap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.cafefidelidaqrdemo.models.CodigoQr;
import com.example.cafefidelidaqrdemo.repository.CodigoQrRepository;
import com.example.cafefidelidaqrdemo.utils.QRGenerator;
import com.example.cafefidelidaqrdemo.utils.Resources;
import com.example.cafefidelidaqrdemo.utils.SessionManager;
import com.google.zxing.WriterException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClienteQRVisitasViewModel extends AndroidViewModel {

    private final CodigoQrRepository codigoQrRepository;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    // Constante de validez (24 horas)
    private static final long QR_VALIDITY_MS = 86400000;

    private final MediatorLiveData<Resources<Bitmap>> qrVisitaState = new MediatorLiveData<>();

    public ClienteQRVisitasViewModel(@NonNull Application application) {
        super(application);
        this.codigoQrRepository = new CodigoQrRepository(application);
        this.sessionManager = new SessionManager(application);
        this.executor = Executors.newSingleThreadExecutor();
    }

    // metodo poara que el fragment pueda observar el live data
    public LiveData<Resources<Bitmap>> getQrVisitaState() {
        return qrVisitaState;
    }

    // Metodo para cargar el QR de las preferencias compartidas o la creacion de un nuevo QR.
    // todo depende del tiempo que haya transcurrido desde la ultima creaiacion de un QR
    public void cargarQrVisita() {
        qrVisitaState.setValue(Resources.loading(null));

        //Extrae el contenido del QR de las preferencias compartidas y el tiempo de cracion
        //del QR de las preferencias compartidas
        String contentQrSharedPreferences = sessionManager.getQRCodeContent();
        long tiempoTranscurrido = System.currentTimeMillis() - sessionManager.getLastGenerationTime();

        String userId = sessionManager.getUserId();

        if (userId == null || userId.isEmpty()) {
            qrVisitaState.setValue(Resources.error("Usuario no identificado (ID Nulo)", null));
            return;
        }

        // Validamos que el QR aun no haya expirado
        if (contentQrSharedPreferences != null && tiempoTranscurrido < QR_VALIDITY_MS) {
            // utilizar el QR guardado en las preferencias compartidas
            procesarQrExistente(contentQrSharedPreferences);
        } else {
            // creamos el nuevo QR y pasamos el QR para cambiar su estado
            iniciarCreacionNuevoQr(userId, contentQrSharedPreferences); // Pasamos el String
        }
    }

    //En caso de que aun no pasen las 24 horas se utiliza el mismo QR
    private void procesarQrExistente(String content) {
        LiveData<Resources<CodigoQr>> repoSource = codigoQrRepository.obtenerCodigoQrRepo(content);

        qrVisitaState.addSource(repoSource, resource -> {
            if (resource.status == Resources.Status.LOADING) return;

            qrVisitaState.removeSource(repoSource);

            if (resource.status == Resources.Status.SUCCESS && resource.data != null) {
                // Metodo para cambiar el estado del MutableLiveData con el bitMap del QR nuevo o reutilizado
                generarYEmitirBitmap(content, String.valueOf(resource.data.getIdCodigoQr()), false);
            } else {
                qrVisitaState.setValue(Resources.error(resource.message != null ? resource.message : "Error al recuperar QR", null));
            }
        });
    }

    //Se crea el nuevo contenido random para el QR y se verifica si existe en la DB
    private void iniciarCreacionNuevoQr(String userId, String contenidoAntiguo) {
        String nuevoContent = QRGenerator.generarcontenidoQrVisitaAleatorio();

        LiveData<Resources<Integer>> verificacionSource = codigoQrRepository.verificarExistenciaDeQr(nuevoContent, userId);

        qrVisitaState.addSource(verificacionSource, resource -> {
            if (resource.status == Resources.Status.LOADING) return;
            qrVisitaState.removeSource(verificacionSource);

            if (resource.status == Resources.Status.SUCCESS) {
                boolean existe = resource.data != null && resource.data == 1;
                if (existe) {
                    iniciarCreacionNuevoQr(userId, contenidoAntiguo); // Reintento recursivo
                } else {
                    insertarNuevoQrEnDb(nuevoContent, userId, contenidoAntiguo);
                }
            } else {
                qrVisitaState.setValue(Resources.error(resource.message, null));
            }
        });
    }

    //Se cambia el estado del anterior QR y se guarda el nuevo en la DB
    private void insertarNuevoQrEnDb(String content, String userId, String contenidoAntiguo) {
        CodigoQr nuevoQr = new CodigoQr(userId, content, System.currentTimeMillis());
        nuevoQr.setEstado("activo");

        // Cambiar estado de QR existente
        if (contenidoAntiguo != null) {
            codigoQrRepository.cambiarEstadoQrRepo(contenidoAntiguo);
        }

        LiveData<Resources<Integer>> insertSource = codigoQrRepository.insertarCodigoQrRepo(nuevoQr, userId);

        qrVisitaState.addSource(insertSource, resource -> {
            if (resource.status == Resources.Status.LOADING) return;
            qrVisitaState.removeSource(insertSource);

            if (resource.status == Resources.Status.SUCCESS && resource.data != null) {
                String idGenerado = String.valueOf(resource.data);
                Toast.makeText(getApplication(), "Nuevo QR creado... ", Toast.LENGTH_LONG).show();
                generarYEmitirBitmap(content, idGenerado, true);
            } else {
                qrVisitaState.setValue(Resources.error(resource.message, null));
            }
        });
    }

    //Se genera el QR y se guarda en el mutable live data que observa el fragment
    private void generarYEmitirBitmap(String content, String idQr, boolean guardarEnPrefs) {
        executor.execute(() -> {
            try {
                Bitmap bitmap = QRGenerator.generarQrVisita(content, idQr);

                // Guardamos el nuevo QR el las preferencias compartidas
                if (guardarEnPrefs) {
                    sessionManager.saveQRCode(content);
                }

                // IMPORTANTE: Como estamos en un hilo de fondo, usamos postValue
                qrVisitaState.postValue(Resources.success(bitmap));

            } catch (WriterException e) {
                qrVisitaState.postValue(Resources.error("Error visualizando QR: " + e.getMessage(), null));
            } catch (Exception e) {
                qrVisitaState.postValue(Resources.error("Error inesperado: " + e.getMessage(), null));
            }
        });
    }
}