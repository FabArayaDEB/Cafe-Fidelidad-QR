package com.example.cafefidelidaqrdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.adapters.ProductosAdapter;
import com.example.cafefidelidaqrdemo.models.Producto;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;
import com.example.cafefidelidaqrdemo.repository.ProductoRepository;
import androidx.lifecycle.LiveData;
// import com.google.firebase.database.DataSnapshot;
// import com.google.firebase.database.DatabaseError;
// import com.google.firebase.database.DatabaseReference;
// import com.google.firebase.database.FirebaseDatabase;
// import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CatalogoActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProductos;
    private ProductosAdapter productoAdapter;
    private List<Producto> listaProductos;
    private List<Producto> listaProductosFiltrada;
    private EditText etBuscar;
    private Spinner spinnerCategoria, spinnerOrden;
    private ProgressDialog progressDialog;
    private ProductoRepository productoRepository;
    // private DatabaseReference databaseReference;

    // Categorías disponibles
    private String[] categorias = {"Todas", "Café", "Bebidas Frías", "Postres", "Snacks", "Desayunos", "Almuerzos"};
    private String[] opcionesOrden = {"Nombre A-Z", "Nombre Z-A", "Precio Menor", "Precio Mayor", "Más Popular"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Verificar autenticación antes de mostrar contenido
        if (!isUserAuthenticated()) {
            redirectToLogin();
            return;
        }
        
        setContentView(R.layout.activity_catalogo);

        initViews();
        setupRecyclerView();
        setupSpinners();
        setupSearchFilter();
        loadProductos();
    }
    
    /**
     * Verifica si el usuario está autenticado
     */
    private boolean isUserAuthenticated() {
        AuthRepository authRepository = AuthRepository.getInstance();
        return authRepository.isUserLoggedIn();
    }
    
    /**
     * Redirige al usuario a la pantalla de login
     */
    private void redirectToLogin() {
        android.content.Intent intent = new android.content.Intent(this, OpcionesLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        recyclerViewProductos = findViewById(R.id.recycler_productos);
        etBuscar = findViewById(R.id.et_buscar);
        spinnerCategoria = findViewById(R.id.spinner_categoria);
        spinnerOrden = findViewById(R.id.spinner_orden);

        // Configurar toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Catálogo");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar listas
        listaProductos = new ArrayList<>();
        listaProductosFiltrada = new ArrayList<>();

        // Configurar ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando productos...");
        progressDialog.setCancelable(false);

        // Inicializar repositorio de productos (SQLite)
        productoRepository = ProductoRepository.getInstance(getApplicationContext());
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewProductos.setLayoutManager(layoutManager);
        
        productoAdapter = new ProductosAdapter(this, false);
        productoAdapter.setOnProductoClickListener(this::onProductoClick);
        productoAdapter.submitList(listaProductosFiltrada);
        recyclerViewProductos.setAdapter(productoAdapter);
    }

    private void setupSpinners() {
        // Configurar spinner de categorías
        ArrayAdapter<String> adapterCategorias = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, categorias);
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategorias);

        // Configurar spinner de orden
        ArrayAdapter<String> adapterOrden = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, opcionesOrden);
        adapterOrden.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrden.setAdapter(adapterOrden);

        // Listeners para los spinners
        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtrarProductos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerOrden.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ordenarProductos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearchFilter() {
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarProductos();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadProductos() {
        progressDialog.show();

        // Observar productos disponibles publicados por administradores desde SQLite
        LiveData<List<Producto>> liveData = productoRepository.getProductosDisponibles();
        liveData.observe(this, productos -> {
            listaProductos.clear();
            if (productos != null) {
                listaProductos.addAll(productos);
            }
            filtrarProductos();
            progressDialog.dismiss();
        });

        // Observar errores del repositorio para informar a la UI
        productoRepository.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(CatalogoActivity.this, "Error al cargar productos: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrarProductos() {
        String textoBusqueda = etBuscar != null ? etBuscar.getText().toString().toLowerCase().trim() : "";
        String categoriaSeleccionada = (spinnerCategoria != null && spinnerCategoria.getSelectedItem() != null)
                ? spinnerCategoria.getSelectedItem().toString()
                : "Todas";
        
        listaProductosFiltrada.clear();
        
        for (Producto producto : listaProductos) {
            String nombre = producto.getNombre() != null ? producto.getNombre() : "";
            String descripcion = producto.getDescripcion() != null ? producto.getDescripcion() : "";
            String categoria = producto.getCategoria() != null ? producto.getCategoria() : "";

            boolean coincideTexto = textoBusqueda.isEmpty() || 
                    nombre.toLowerCase().contains(textoBusqueda) ||
                    descripcion.toLowerCase().contains(textoBusqueda);
            
            boolean coincideCategoria = "Todas".equals(categoriaSeleccionada) ||
                    categoria.equals(categoriaSeleccionada);
            
            if (coincideTexto && coincideCategoria) {
                listaProductosFiltrada.add(producto);
            }
        }
        
        ordenarProductos();
    }

    private void ordenarProductos() {
        String ordenSeleccionado = (spinnerOrden != null && spinnerOrden.getSelectedItem() != null)
                ? spinnerOrden.getSelectedItem().toString()
                : "Nombre A-Z";
        
        switch (ordenSeleccionado) {
            case "Nombre A-Z":
                Collections.sort(listaProductosFiltrada, new Comparator<Producto>() {
                    @Override
                    public int compare(Producto p1, Producto p2) {
                        String n1 = p1.getNombre() != null ? p1.getNombre() : "";
                        String n2 = p2.getNombre() != null ? p2.getNombre() : "";
                        return n1.compareToIgnoreCase(n2);
                    }
                });
                break;
                
            case "Nombre Z-A":
                Collections.sort(listaProductosFiltrada, new Comparator<Producto>() {
                    @Override
                    public int compare(Producto p1, Producto p2) {
                        String n1 = p1.getNombre() != null ? p1.getNombre() : "";
                        String n2 = p2.getNombre() != null ? p2.getNombre() : "";
                        return n2.compareToIgnoreCase(n1);
                    }
                });
                break;
                
            case "Precio Menor":
                Collections.sort(listaProductosFiltrada, new Comparator<Producto>() {
                    @Override
                    public int compare(Producto p1, Producto p2) {
                        return Double.compare(p1.getPrecio(), p2.getPrecio());
                    }
                });
                break;
                
            case "Precio Mayor":
                Collections.sort(listaProductosFiltrada, new Comparator<Producto>() {
                    @Override
                    public int compare(Producto p1, Producto p2) {
                        return Double.compare(p2.getPrecio(), p1.getPrecio());
                    }
                });
                break;
                
            case "Más Popular":
                // Ordenar por popularidad no está disponible en ProductoEntity
                // Collections.sort(listaProductosFiltrada, new Comparator<ProductoEntity>() {
                //     @Override
                //     public int compare(ProductoEntity p1, ProductoEntity p2) {
                //         return Boolean.compare(p2.isEsPopular(), p1.isEsPopular());
                //     }
                // });
                break;
        }
        
        productoAdapter.submitList(new ArrayList<>(listaProductosFiltrada));
    }

    private void onProductoClick(Producto producto) {
        // Navegar al detalle del producto
        Intent intent = new Intent(this, DetalleProductoActivity.class);
        intent.putExtra("producto_id", producto.getId());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}