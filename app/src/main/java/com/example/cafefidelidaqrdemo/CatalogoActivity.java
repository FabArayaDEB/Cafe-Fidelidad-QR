package com.example.cafefidelidaqrdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
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

import com.example.cafefidelidaqrdemo.adapters.ProductoAdapter;
import com.example.cafefidelidaqrdemo.models.Producto;
import com.example.cafefidelidaqrdemo.data.repositories.AuthRepository;
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
    private ProductoAdapter productoAdapter;
    private List<Producto> listaProductos;
    private List<Producto> listaProductosFiltrada;
    private EditText etBuscar;
    private Spinner spinnerCategoria, spinnerOrden;
    private ProgressDialog progressDialog;
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

        // Referencia a Firebase
        // databaseReference = FirebaseDatabase.getInstance().getReference("Productos");
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewProductos.setLayoutManager(layoutManager);
        
        productoAdapter = new ProductoAdapter(this, listaProductosFiltrada);
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
        
        // databaseReference.addValueEventListener(new ValueEventListener() {
        //     @Override
        //     public void onDataChange(@NonNull DataSnapshot snapshot) {
        //         productos.clear();
        //         for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
        //             Producto producto = dataSnapshot.getValue(Producto.class);
        //             if (producto != null && producto.isActivo()) {
        //                 listaProductos.add(producto);
        //             }
        //         }
        //         
        //         filtrarProductos();
        //         progressDialog.dismiss();
        //     }
        //
        //     @Override
        //     public void onCancelled(@NonNull DatabaseError error) {
        //         progressDialog.dismiss();
        //         Toast.makeText(CatalogoActivity.this, 
        //                 "Error al cargar productos: " + error.getMessage(), 
        //                 Toast.LENGTH_SHORT).show();
        //     }
        // });
        
        // Método deshabilitado - Firebase removido
        progressDialog.dismiss();
        Toast.makeText(this, "Carga de productos deshabilitada", Toast.LENGTH_SHORT).show();
    }

    private void filtrarProductos() {
        String textoBusqueda = etBuscar.getText().toString().toLowerCase().trim();
        String categoriaSeleccionada = spinnerCategoria.getSelectedItem().toString();
        
        listaProductosFiltrada.clear();
        
        for (Producto producto : listaProductos) {
            boolean coincideTexto = textoBusqueda.isEmpty() || 
                    producto.getNombre().toLowerCase().contains(textoBusqueda) ||
                    producto.getDescripcion().toLowerCase().contains(textoBusqueda);
            
            boolean coincideCategoria = categoriaSeleccionada.equals("Todas") ||
                    producto.getCategoria().equals(categoriaSeleccionada);
            
            if (coincideTexto && coincideCategoria) {
                listaProductosFiltrada.add(producto);
            }
        }
        
        ordenarProductos();
    }

    private void ordenarProductos() {
        String ordenSeleccionado = spinnerOrden.getSelectedItem().toString();
        
        switch (ordenSeleccionado) {
            case "Nombre A-Z":
                Collections.sort(listaProductosFiltrada, new Comparator<Producto>() {
                    @Override
                    public int compare(Producto p1, Producto p2) {
                        return p1.getNombre().compareToIgnoreCase(p2.getNombre());
                    }
                });
                break;
                
            case "Nombre Z-A":
                Collections.sort(listaProductosFiltrada, new Comparator<Producto>() {
                    @Override
                    public int compare(Producto p1, Producto p2) {
                        return p2.getNombre().compareToIgnoreCase(p1.getNombre());
                    }
                });
                break;
                
            case "Precio Menor":
                Collections.sort(listaProductosFiltrada, new Comparator<Producto>() {
                    @Override
                    public int compare(Producto p1, Producto p2) {
                        return Double.compare(p1.getPrecioConDescuento(), p2.getPrecioConDescuento());
                    }
                });
                break;
                
            case "Precio Mayor":
                Collections.sort(listaProductosFiltrada, new Comparator<Producto>() {
                    @Override
                    public int compare(Producto p1, Producto p2) {
                        return Double.compare(p2.getPrecioConDescuento(), p1.getPrecioConDescuento());
                    }
                });
                break;
                
            case "Más Popular":
                Collections.sort(listaProductosFiltrada, new Comparator<Producto>() {
                    @Override
                    public int compare(Producto p1, Producto p2) {
                        return Boolean.compare(p2.isEsPopular(), p1.isEsPopular());
                    }
                });
                break;
        }
        
        productoAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}