package com.example.cafefidelidaqrdemo.ui.admin.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.models.Producto;
import com.example.cafefidelidaqrdemo.models.Sucursal;
import com.example.cafefidelidaqrdemo.models.ResenaProducto;
import com.example.cafefidelidaqrdemo.models.ResenaSucursal;
import com.example.cafefidelidaqrdemo.repository.AdminRepository;
import com.example.cafefidelidaqrdemo.repository.ResenasProductoRepository;
import com.example.cafefidelidaqrdemo.repository.ResenasSucursalRepository;
import com.example.cafefidelidaqrdemo.repository.ClienteRepository;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ResenasAdminViewModel extends AndroidViewModel {

    private final AdminRepository adminRepository;
    private final ResenasProductoRepository resenasProductoRepository;
    private final ResenasSucursalRepository resenasSucursalRepository;
    private final ClienteRepository clienteRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    private final MutableLiveData<Integer> selectedProductoId = new MutableLiveData<>(-1);
    private final MutableLiveData<Integer> selectedSucursalId = new MutableLiveData<>(-1);
    private final MutableLiveData<Map<Integer, String>> nombresUsuarios = new MutableLiveData<>(new HashMap<>());

    public ResenasAdminViewModel(@NonNull Application application) {
        super(application);
        adminRepository = new AdminRepository(application.getApplicationContext());
        resenasProductoRepository = ResenasProductoRepository.getInstance(application.getApplicationContext());
        resenasSucursalRepository = ResenasSucursalRepository.getInstance(application.getApplicationContext());
        clienteRepository = ClienteRepository.getInstance(application.getApplicationContext());

        selectedProductoId.observeForever(id -> {
            if (id != null && id > 0) {
                isLoading.postValue(true);
                // Cargar primeras 50 reseñas, sin desplazamiento
                resenasProductoRepository.listarResenasPorProducto(id, 50, 0);
                isLoading.postValue(false);
            }
        });
        selectedSucursalId.observeForever(id -> {
            if (id != null && id > 0) {
                isLoading.postValue(true);
                // Cargar primeras 50 reseñas, sin desplazamiento
                resenasSucursalRepository.listarResenasPorSucursal(id, 50, 0);
                isLoading.postValue(false);
            }
        });

        // Resolver nombres de usuarios en cuanto cambien las listas
        resenasProductoRepository.getResenasLiveData().observeForever(list -> {
            if (list != null) {
                Map<Integer, String> map = new HashMap<>();
                for (ResenaProducto r : list) {
                    int uid = r.getUsuarioId();
                    if (uid > 0 && !map.containsKey(uid)) {
                        com.example.cafefidelidaqrdemo.models.Cliente c = clienteRepository.getClienteByIdSync(uid);
                        map.put(uid, c != null && c.getNombre() != null && !c.getNombre().isEmpty() ? c.getNombre() : ("Usuario #" + uid));
                    }
                }
                nombresUsuarios.postValue(map);
            }
        });
        resenasSucursalRepository.getResenasLiveData().observeForever(list -> {
            if (list != null) {
                Map<Integer, String> map = new HashMap<>();
                for (ResenaSucursal r : list) {
                    int uid = r.getUsuarioId();
                    if (uid > 0 && !map.containsKey(uid)) {
                        com.example.cafefidelidaqrdemo.models.Cliente c = clienteRepository.getClienteByIdSync(uid);
                        map.put(uid, c != null && c.getNombre() != null && !c.getNombre().isEmpty() ? c.getNombre() : ("Usuario #" + uid));
                    }
                }
                nombresUsuarios.postValue(map);
            }
        });
    }

    public LiveData<List<Producto>> getAllProductos() { return adminRepository.getAllProductos(); }
    public LiveData<List<Sucursal>> getAllSucursales() { return adminRepository.getAllSucursales(); }

    public LiveData<List<ResenaProducto>> getResenasProducto() { return resenasProductoRepository.getResenasLiveData(); }
    public LiveData<List<ResenaSucursal>> getResenasSucursal() { return resenasSucursalRepository.getResenasLiveData(); }
    public LiveData<Map<Integer, String>> getNombresUsuarios() { return nombresUsuarios; }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public void clearErrorMessage() { errorMessage.postValue(null); }

    public void setSelectedProductoId(int id) { selectedProductoId.postValue(id); }
    public void setSelectedSucursalId(int id) { selectedSucursalId.postValue(id); }
}