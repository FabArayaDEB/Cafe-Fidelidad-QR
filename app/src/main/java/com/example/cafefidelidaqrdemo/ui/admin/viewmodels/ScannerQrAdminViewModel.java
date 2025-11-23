package com.example.cafefidelidaqrdemo.ui.admin.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafefidelidaqrdemo.models.CodigoQr;
import com.example.cafefidelidaqrdemo.repository.ScannerQrAdminRepository;
import com.example.cafefidelidaqrdemo.utils.Resources;
import com.example.cafefidelidaqrdemo.utils.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerQrAdminViewModel extends AndroidViewModel {
    private final MutableLiveData<Resources<CodigoQr>> _scanState = new MutableLiveData<>();
    private final ScannerQrAdminRepository repository;


    public ScannerQrAdminViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ScannerQrAdminRepository(application);
    }

    public LiveData<Resources<CodigoQr>> getScannerQrVisitasState() {
        return _scanState;
    }

    //Reseteamos el estado del MutableLiveData cada vez que se llama al encender la camara
    public void resetearEstado() {
        _scanState.setValue(null);
    }

    //Recibimos la respuesta del repo respecto a la lectura del QR y devolvemos un objeto de
    // QR con los estados modificados para mostrar en pantalla
    public void procesarQRVisita(String contentQr) {
        if (_scanState.getValue() != null && _scanState.getValue().status == Resources.Status.LOADING) {
            return;
        }

        _scanState.setValue(Resources.loading(null));

        LiveData<Resources<CodigoQr>> source = repository.verificarYCanjearQr(contentQr);

        source.observeForever(new androidx.lifecycle.Observer<Resources<CodigoQr>>() {
            @Override
            public void onChanged(Resources<CodigoQr> result) {
                _scanState.setValue(result);
                source.removeObserver(this);
            }
        });
    }
}
