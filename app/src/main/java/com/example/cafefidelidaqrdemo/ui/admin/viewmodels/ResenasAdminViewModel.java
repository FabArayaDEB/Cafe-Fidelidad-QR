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

    // Paginación
    private static final int PAGE_SIZE = 20;
    private int productoOffset = 0;
    private int sucursalOffset = 0;
    private boolean loadingMoreProducto = false;
    private boolean loadingMoreSucursal = false;
    private final MutableLiveData<Boolean> hasMoreProducto = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> hasMoreSucursal = new MutableLiveData<>(false);

    // Listas acumuladas para la UI
    private final MutableLiveData<java.util.List<ResenaProducto>> resenasProductoPaged = new MutableLiveData<>(new java.util.ArrayList<>());
    private final MutableLiveData<java.util.List<ResenaSucursal>> resenasSucursalPaged = new MutableLiveData<>(new java.util.ArrayList<>());

    public ResenasAdminViewModel(@NonNull Application application) {
        super(application);
        adminRepository = new AdminRepository(application.getApplicationContext());
        resenasProductoRepository = ResenasProductoRepository.getInstance(application.getApplicationContext());
        resenasSucursalRepository = ResenasSucursalRepository.getInstance(application.getApplicationContext());
        clienteRepository = ClienteRepository.getInstance(application.getApplicationContext());

        selectedProductoId.observeForever(id -> {
            if (id != null && id > 0) {
                isLoading.postValue(true);
                productoOffset = 0;
                loadingMoreProducto = false;
                resenasProductoPaged.postValue(new java.util.ArrayList<>());
                resenasProductoRepository.listarResenasPorProducto(id, PAGE_SIZE, productoOffset);
                isLoading.postValue(false);
            }
        });
        selectedSucursalId.observeForever(id -> {
            if (id != null && id > 0) {
                isLoading.postValue(true);
                sucursalOffset = 0;
                loadingMoreSucursal = false;
                resenasSucursalPaged.postValue(new java.util.ArrayList<>());
                resenasSucursalRepository.listarResenasPorSucursal(id, PAGE_SIZE, sucursalOffset);
                isLoading.postValue(false);
            }
        });

        // Resolver nombres de usuarios en cuanto cambien las listas y manejar paginación
        resenasProductoRepository.getResenasLiveData().observeForever(list -> {
            if (list != null) {
                // Merge/replace en acumulado
                java.util.List<ResenaProducto> current = resenasProductoPaged.getValue();
                if (current == null) current = new java.util.ArrayList<>();
                if (loadingMoreProducto) {
                    current.addAll(list);
                } else {
                    current = new java.util.ArrayList<>(list);
                }
                resenasProductoPaged.postValue(current);
                hasMoreProducto.postValue(list.size() == PAGE_SIZE);

                Map<Integer, String> map = new HashMap<>();
                for (ResenaProducto r : current) {
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
                // Merge/replace en acumulado
                java.util.List<ResenaSucursal> current = resenasSucursalPaged.getValue();
                if (current == null) current = new java.util.ArrayList<>();
                if (loadingMoreSucursal) {
                    current.addAll(list);
                } else {
                    current = new java.util.ArrayList<>(list);
                }
                resenasSucursalPaged.postValue(current);
                hasMoreSucursal.postValue(list.size() == PAGE_SIZE);

                Map<Integer, String> map = new HashMap<>();
                for (ResenaSucursal r : current) {
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

    public LiveData<List<ResenaProducto>> getResenasProducto() { return resenasProductoPaged; }
    public LiveData<List<ResenaSucursal>> getResenasSucursal() { return resenasSucursalPaged; }
    public LiveData<Boolean> getHasMoreProducto() { return hasMoreProducto; }
    public LiveData<Boolean> getHasMoreSucursal() { return hasMoreSucursal; }
    public LiveData<Map<Integer, String>> getNombresUsuarios() { return nombresUsuarios; }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public void clearErrorMessage() { errorMessage.postValue(null); }

    public void setSelectedProductoId(int id) { selectedProductoId.postValue(id); }
    public void setSelectedSucursalId(int id) { selectedSucursalId.postValue(id); }

    // Métodos explícitos de refresco (relistan con el ID actual)
    public void refreshProductoResenas() {
        Integer id = selectedProductoId.getValue();
        if (id != null && id > 0) {
            isLoading.postValue(true);
            productoOffset = 0;
            loadingMoreProducto = false;
            resenasProductoPaged.postValue(new java.util.ArrayList<>());
            resenasProductoRepository.listarResenasPorProducto(id, PAGE_SIZE, productoOffset);
            isLoading.postValue(false);
        }
    }

    public void refreshSucursalResenas() {
        Integer id = selectedSucursalId.getValue();
        if (id != null && id > 0) {
            isLoading.postValue(true);
            sucursalOffset = 0;
            loadingMoreSucursal = false;
            resenasSucursalPaged.postValue(new java.util.ArrayList<>());
            resenasSucursalRepository.listarResenasPorSucursal(id, PAGE_SIZE, sucursalOffset);
            isLoading.postValue(false);
        }
    }

    public void loadMoreProducto() {
        Integer id = selectedProductoId.getValue();
        Boolean more = hasMoreProducto.getValue();
        if (id != null && id > 0 && Boolean.TRUE.equals(more)) {
            loadingMoreProducto = true;
            productoOffset += PAGE_SIZE;
            resenasProductoRepository.listarResenasPorProducto(id, PAGE_SIZE, productoOffset);
        }
    }

    public void loadMoreSucursal() {
        Integer id = selectedSucursalId.getValue();
        Boolean more = hasMoreSucursal.getValue();
        if (id != null && id > 0 && Boolean.TRUE.equals(more)) {
            loadingMoreSucursal = true;
            sucursalOffset += PAGE_SIZE;
            resenasSucursalRepository.listarResenasPorSucursal(id, PAGE_SIZE, sucursalOffset);
        }
    }
}
