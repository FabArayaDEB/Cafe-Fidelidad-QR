package com.example.cafefidelidaqrdemo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.cafefidelidaqrdemo.model.ActividadReciente;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardViewModel extends ViewModel {
    private MutableLiveData<List<ActividadReciente>> actividadReciente;
    private MutableLiveData<Integer> contadorProductos;
    private MutableLiveData<Integer> contadorBeneficios;
    private MutableLiveData<Integer> contadorClientes;
    private MutableLiveData<Double> ventasHoy;

    public AdminDashboardViewModel() {
        actividadReciente = new MutableLiveData<>();
        contadorProductos = new MutableLiveData<>();
        contadorBeneficios = new MutableLiveData<>();
        contadorClientes = new MutableLiveData<>();
        ventasHoy = new MutableLiveData<>();
        
        // Inicializar con datos por defecto
        actividadReciente.setValue(new ArrayList<>());
        contadorProductos.setValue(0);
        contadorBeneficios.setValue(0);
        contadorClientes.setValue(0);
        ventasHoy.setValue(0.0);
    }

    public LiveData<List<ActividadReciente>> getActividadReciente() {
        return actividadReciente;
    }

    public LiveData<Integer> getContadorProductos() {
        return contadorProductos;
    }

    public LiveData<Integer> getContadorBeneficios() {
        return contadorBeneficios;
    }

    public LiveData<Integer> getContadorClientes() {
        return contadorClientes;
    }

    public LiveData<Double> getVentasHoy() {
        return ventasHoy;
    }

    public void actualizarContadores() {
        // Aquí se implementaría la lógica para actualizar los contadores
        // Por ahora solo valores de ejemplo
        contadorProductos.setValue(25);
        contadorBeneficios.setValue(8);
        contadorClientes.setValue(150);
        ventasHoy.setValue(1250.50);
    }

    public void cargarActividadReciente() {
        // Aquí se implementaría la lógica para cargar actividad reciente
        List<ActividadReciente> actividades = new ArrayList<>();
        // Agregar actividades de ejemplo si es necesario
        actividadReciente.setValue(actividades);
    }
}