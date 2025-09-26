package com.example.cafefidelidaqrdemo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.adapters.ProductosAdapter;
import android.content.Intent;
import com.example.cafefidelidaqrdemo.DetalleProductoActivity;
// import com.example.cafefidelidaqrdemo.activities.DetalleProductoActivity; // TODO: Crear esta actividad
import com.example.cafefidelidaqrdemo.database.models.Producto;
import com.example.cafefidelidaqrdemo.viewmodels.ProductosViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class FragmentProductos extends Fragment {
    
    private ProductosViewModel viewModel;
    private ProductosAdapter adapter;
    
    // UI Components
    private RecyclerView recyclerViewProductos;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearProgressIndicator progressIndicator;
    private MaterialTextView textViewEmpty;
    private MaterialTextView textViewOffline;
    private ChipGroup chipGroupCategorias;
    private ChipGroup chipGroupEstado;
    private FloatingActionButton fabScrollTop;
    private SearchView searchView;
    
    // Estado actual
    private String categoriaSeleccionada = "";
    private String estadoSeleccionado = "";
    private String queryBusqueda = "";
    private List<Producto> productosOriginales = new ArrayList<>();
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_productos, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupViewModel();
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearchView();
        setupFilters();
        setupScrollToTop();
        observeData();
        
        // Cargar datos iniciales
        viewModel.loadProductos();
    }
    
    private void initializeViews(View view) {
        recyclerViewProductos = view.findViewById(R.id.recyclerViewProductos);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        textViewOffline = view.findViewById(R.id.textViewOffline);
        chipGroupCategorias = view.findViewById(R.id.chipGroupCategoria);
        chipGroupEstado = view.findViewById(R.id.chipGroupEstado);
        searchView = view.findViewById(R.id.searchView);
        fabScrollTop = view.findViewById(R.id.fabScrollTop);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProductosViewModel.class);
    }
    
    private void setupRecyclerView() {
        adapter = new ProductosAdapter(getContext(), false); // false para modo cliente
        
        // Configurar GridLayoutManager con 2 columnas
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerViewProductos.setLayoutManager(layoutManager);
        recyclerViewProductos.setAdapter(adapter);
        
        // Listener para clicks en productos
        adapter.setOnProductoClickListener(new ProductosAdapter.OnProductoClickListener() {
            @Override
            public void onProductoClick(Producto producto) {
                // Abrir detalle del producto
                Intent intent = new Intent(getActivity(), DetalleProductoActivity.class);
                intent.putExtra("producto_id", producto.getId());
                startActivity(intent);
            }
        });
        
        // Listener para scroll
        recyclerViewProductos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                // Mostrar/ocultar botón de scroll to top
                if (layoutManager.findFirstVisibleItemPosition() > 5) {
                    fabScrollTop.show();
                } else {
                    fabScrollTop.hide();
                }
            }
        });
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshProductos();
        });
        
        swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.coffee_secondary,
            R.color.coffee_accent
        );
    }
    
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryBusqueda = query != null ? query.trim() : "";
                applyFilters();
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                queryBusqueda = newText != null ? newText.trim() : "";
                applyFilters();
                return true;
            }
        });
        
        searchView.setOnCloseListener(() -> {
            queryBusqueda = "";
            applyFilters();
            return false;
        });
    }
    
    private void setupFilters() {
        // Configurar filtros de categoría
        setupCategoriaFilters();
        
        // Configurar filtros de estado
        setupEstadoFilters();
    }
    
    private void setupCategoriaFilters() {
        // Chip "Todas las categorías"
        Chip chipTodas = new Chip(getContext());
        chipTodas.setText("Todas");
        chipTodas.setCheckable(true);
        chipTodas.setChecked(true);
        chipTodas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                categoriaSeleccionada = "";
                uncheckOtherCategoriaChips(chipTodas);
                applyFilters();
            }
        });
        chipGroupCategorias.addView(chipTodas);
        
        // Chips para categorías específicas
        String[] categorias = {"Café", "Bebidas", "Postres", "Snacks", "Desayunos"};
        for (String categoria : categorias) {
            Chip chip = new Chip(getContext());
            chip.setText(categoria);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    categoriaSeleccionada = categoria;
                    uncheckOtherCategoriaChips(chip);
                    applyFilters();
                }
            });
            chipGroupCategorias.addView(chip);
        }
    }
    
    private void setupEstadoFilters() {
        // Chip "Todos"
        Chip chipTodos = new Chip(getContext());
        chipTodos.setText("Todos");
        chipTodos.setCheckable(true);
        chipTodos.setChecked(true);
        chipTodos.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                estadoSeleccionado = "";
                uncheckOtherEstadoChips(chipTodos);
                applyFilters();
            }
        });
        chipGroupEstado.addView(chipTodos);
        
        // Chip "Disponibles"
        Chip chipDisponibles = new Chip(getContext());
        chipDisponibles.setText("Disponibles");
        chipDisponibles.setCheckable(true);
        chipDisponibles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                estadoSeleccionado = "activo";
                uncheckOtherEstadoChips(chipDisponibles);
                applyFilters();
            }
        });
        chipGroupEstado.addView(chipDisponibles);
        
        // Chip "No disponibles"
        Chip chipNoDisponibles = new Chip(getContext());
        chipNoDisponibles.setText("No disponibles");
        chipNoDisponibles.setCheckable(true);
        chipNoDisponibles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                estadoSeleccionado = "inactivo";
                uncheckOtherEstadoChips(chipNoDisponibles);
                applyFilters();
            }
        });
        chipGroupEstado.addView(chipNoDisponibles);
    }
    
    private void setupScrollToTop() {
        fabScrollTop.setOnClickListener(v -> {
            recyclerViewProductos.smoothScrollToPosition(0);
        });
        fabScrollTop.hide();
    }
    
    private void observeData() {
        // Observar lista de productos
        viewModel.getProductos().observe(getViewLifecycleOwner(), productos -> {
            if (productos != null) {
                productosOriginales = new ArrayList<>(productos);
                applyFilters();
            }
        });
        
        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                swipeRefreshLayout.setRefreshing(isLoading);
            }
        });
        
        // Observar errores
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
                viewModel.clearError();
            }
        });
        
        // Observar estado offline
        viewModel.getIsOffline().observe(getViewLifecycleOwner(), isOffline -> {
            if (isOffline != null) {
                textViewOffline.setVisibility(isOffline ? View.VISIBLE : View.GONE);
            }
        });
    }
    
    private void applyFilters() {
        List<Producto> productosFiltrados = new ArrayList<>();
        
        for (Producto producto : productosOriginales) {
            boolean pasaFiltroCategoria = categoriaSeleccionada.isEmpty() || 
                producto.getCategoria().equalsIgnoreCase(categoriaSeleccionada);
            
            boolean pasaFiltroEstado = estadoSeleccionado.isEmpty() || 
                (estadoSeleccionado.equalsIgnoreCase("disponible") && producto.isDisponible()) ||
                (estadoSeleccionado.equalsIgnoreCase("no disponible") && !producto.isDisponible());
            
            boolean pasaFiltroBusqueda = queryBusqueda.isEmpty() || 
                producto.getNombre().toLowerCase().contains(queryBusqueda.toLowerCase()) ||
                producto.getDescripcion().toLowerCase().contains(queryBusqueda.toLowerCase()) ||
                producto.getCategoria().toLowerCase().contains(queryBusqueda.toLowerCase());
            
            if (pasaFiltroCategoria && pasaFiltroEstado && pasaFiltroBusqueda) {
                productosFiltrados.add(producto);
            }
        }
        
        adapter.submitList(productosFiltrados);
        
        // Mostrar mensaje si no hay productos
        if (productosFiltrados.isEmpty()) {
            if (categoriaSeleccionada.isEmpty() && estadoSeleccionado.isEmpty() && queryBusqueda.isEmpty()) {
                textViewEmpty.setText("No hay productos disponibles");
            } else {
                textViewEmpty.setText("No se encontraron productos con los filtros aplicados");
            }
            textViewEmpty.setVisibility(View.VISIBLE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
        }
    }
    
    private void uncheckOtherCategoriaChips(Chip selectedChip) {
        for (int i = 0; i < chipGroupCategorias.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategorias.getChildAt(i);
            if (chip != selectedChip) {
                chip.setChecked(false);
            }
        }
    }
    
    private void uncheckOtherEstadoChips(Chip selectedChip) {
        for (int i = 0; i < chipGroupEstado.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupEstado.getChildAt(i);
            if (chip != selectedChip) {
                chip.setChecked(false);
            }
        }
    }
    
    private void showProductoDetails(Producto producto) {
        String message = String.format(
            "Producto: %s\nCategoría: %s\nPrecio: $%.2f\nEstado: %s",
            producto.getNombre(),
            producto.getCategoria(),
            producto.getPrecio(),
            producto.isDisponible() ? "Disponible" : "No disponible"
        );
        
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
    
    private void showError(String error) {
        if (getView() != null) {
            Snackbar.make(getView(), error, Snackbar.LENGTH_LONG)
                .setAction("Reintentar", v -> viewModel.refreshProductos())
                .show();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refrescar datos si es necesario
        if (productosOriginales.isEmpty()) {
            viewModel.loadProductos();
        }
    }
}