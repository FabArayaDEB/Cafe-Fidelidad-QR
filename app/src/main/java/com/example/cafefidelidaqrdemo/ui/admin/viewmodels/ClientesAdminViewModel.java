package com.example.cafefidelidaqrdemo.ui.admin.viewmodels;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.models.Cliente;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientesAdminViewModel extends AndroidViewModel {
    private static final String TAG = "ClientesAdminViewModel";
    private CafeFidelidadDB database;
    private ExecutorService executor;
    
    private MutableLiveData<List<Cliente>> clientesLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> successLiveData = new MutableLiveData<>();

    public ClientesAdminViewModel(@NonNull Application application) {
        super(application);
        database = CafeFidelidadDB.getInstance(application);
        executor = Executors.newFixedThreadPool(2);
        cargarClientes();
    }

    public LiveData<List<Cliente>> getClientesLiveData() {
        return clientesLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<String> getSuccessLiveData() {
        return successLiveData;
    }

    public void cargarClientes() {
        isLoadingLiveData.setValue(true);
        executor.execute(() -> {
            try {
                List<Cliente> clientes = database.obtenerTodosLosClientes();
                clientesLiveData.postValue(clientes);
            } catch (Exception e) {
                errorLiveData.postValue("Error al cargar clientes: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void cargarClientesActivos() {
        isLoadingLiveData.setValue(true);
        executor.execute(() -> {
            try {
                List<Cliente> clientes = database.obtenerClientesActivos();
                clientesLiveData.postValue(clientes);
            } catch (Exception e) {
                errorLiveData.postValue("Error al cargar clientes activos: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void agregarCliente(Cliente cliente) {
        Log.d(TAG, "Iniciando agregarCliente para: " + cliente.getNombre());
        executor.execute(() -> {
            try {
                Log.d(TAG, "Ejecutando inserción en hilo de fondo");
                long id = database.insertarCliente(cliente);
                Log.d(TAG, "Resultado de inserción - ID: " + id);
                if (id > 0) {
                    Log.d(TAG, "Cliente agregado exitosamente, recargando lista");
                    successLiveData.postValue("Cliente agregado exitosamente");
                    cargarClientes();
                } else {
                    Log.e(TAG, "Error: ID de inserción es <= 0");
                    errorLiveData.postValue("Error al agregar cliente");
                }
            } catch (Exception e) {
                Log.e(TAG, "Excepción al agregar cliente: " + e.getMessage(), e);
                errorLiveData.postValue("Error al agregar cliente: " + e.getMessage());
            }
        });
    }

    public void actualizarCliente(Cliente cliente) {
        executor.execute(() -> {
            try {
                int rowsAffected = database.actualizarCliente(cliente);
                if (rowsAffected > 0) {
                    successLiveData.postValue("Cliente actualizado exitosamente");
                    cargarClientes();
                } else {
                    errorLiveData.postValue("Error al actualizar cliente");
                }
            } catch (Exception e) {
                errorLiveData.postValue("Error al actualizar cliente: " + e.getMessage());
            }
        });
    }

    public void eliminarCliente(int clienteId) {
        executor.execute(() -> {
            try {
                int rowsAffected = database.eliminarCliente(clienteId);
                if (rowsAffected > 0) {
                    successLiveData.postValue("Cliente eliminado exitosamente");
                    cargarClientes();
                } else {
                    errorLiveData.postValue("Error al eliminar cliente");
                }
            } catch (Exception e) {
                errorLiveData.postValue("Error al eliminar cliente: " + e.getMessage());
            }
        });
    }

    public void cambiarEstadoCliente(Cliente cliente) {
        cliente.setActivo(!cliente.isActivo());
        actualizarCliente(cliente);
    }

    public void buscarClientes(String query) {
        if (query == null || query.trim().isEmpty()) {
            cargarClientes();
            return;
        }

        isLoadingLiveData.setValue(true);
        executor.execute(() -> {
            try {
                List<Cliente> todosLosClientes = database.obtenerTodosLosClientes();
                List<Cliente> clientesFiltrados = todosLosClientes.stream()
                        .filter(cliente -> 
                            cliente.getNombre().toLowerCase().contains(query.toLowerCase()) ||
                            cliente.getEmail().toLowerCase().contains(query.toLowerCase()) ||
                            (cliente.getTelefono() != null && cliente.getTelefono().contains(query))
                        )
                        .collect(java.util.stream.Collectors.toList());
                
                clientesLiveData.postValue(clientesFiltrados);
            } catch (Exception e) {
                errorLiveData.postValue("Error al buscar clientes: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}