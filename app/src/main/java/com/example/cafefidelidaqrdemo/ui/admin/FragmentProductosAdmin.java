package com.example.cafefidelidaqrdemo.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.databinding.FragmentProductosAdminBinding;
import com.example.cafefidelidaqrdemo.databinding.DialogProductoBinding;
import com.example.cafefidelidaqrdemo.database.entities.ProductoEntity;
import com.example.cafefidelidaqrdemo.adapters.ProductosAdapter;
import com.example.cafefidelidaqrdemo.ui.admin.viewmodels.ProductosAdminViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment para administración CRUD de productos
 * Permite crear, editar, activar/desactivar y eliminar productos
 */
public class FragmentProductosAdmin extends Fragment {
    
    private FragmentProductosAdminBinding binding;
    private ProductosAdminViewModel viewModel;
    private ProductosAdapter adapter;
    private List<ProductoEntity> productosList = new ArrayList<>();
    private boolean mostrarSoloActivos = true;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductosAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        setupRecyclerView();
        setupUI();
        setupObservers();
        setupClickListeners();
        setupSearchView();
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_productos_admin, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_toggle_filter) {
            toggleFiltroActivos();
            return true;
        } else if (id == R.id.action_export) {
            exportarProductos();
            return true;
        } else if (id == R.id.action_import) {
            importarProductos();
            return true;
        } else if (id == R.id.action_sync) {
            sincronizarProductos();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProductosAdminViewModel.class);
    }
    
    private void setupRecyclerView() {
        adapter = new ProductosAdapter(getContext(), true); // true para modo administrador
        adapter.setOnProductoAdminActionListener(new ProductosAdapter.OnProductoAdminActionListener() {
            @Override
            public void onProductoClick(ProductoEntity producto) {
                mostrarDetalleProducto(producto);
            }
            
            @Override
            public void onEditarClick(ProductoEntity producto) {
                mostrarDialogoEditarProducto(producto);
            }
            
            @Override
            public void onToggleActivoClick(ProductoEntity producto) {
                toggleEstadoProducto(producto);
            }
            
            @Override
            public void onToggleDisponibilidadClick(ProductoEntity producto) {
                toggleDisponibilidadProducto(producto);
            }
            
            @Override
            public void onEliminarClick(ProductoEntity producto) {
                confirmarEliminacionProducto(producto);
            }
        });
        
        binding.recyclerViewProductos.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewProductos.setAdapter(adapter);
        
        // Agregar divisores entre elementos
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                binding.recyclerViewProductos.getContext(),
                LinearLayoutManager.VERTICAL);
        binding.recyclerViewProductos.addItemDecoration(dividerItemDecoration);
    }
    
    private void setupUI() {
        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener(v -> volverAlMenuPrincipal());
        
        // Configurar estado inicial
        actualizarTextoFiltro();
    }
    
    private void setupObservers() {
        // Observar lista de productos
        if (mostrarSoloActivos) {
            viewModel.getProductosActivos().observe(getViewLifecycleOwner(), this::actualizarListaProductos);
        } else {
            viewModel.getAllProductos().observe(getViewLifecycleOwner(), this::actualizarListaProductos);
        }
        
        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Estado de carga manejado por el ViewModel
        });
        
        // Observar mensajes de error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });
        
        // Observar mensajes de éxito
        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearSuccessMessage();
            }
        });
        
        // Observar estadísticas - Elementos de UI no disponibles
        // TODO: Agregar elementos de contador al layout si es necesario
    }
    
    private void setupClickListeners() {
        // Botón agregar producto
        binding.fabAgregarProducto.setOnClickListener(v -> mostrarDialogoNuevoProducto());
        
        // Botón filtro
        binding.btnFiltros.setOnClickListener(v -> toggleFiltroActivos());
    }
    
    private void setupSearchView() {
        binding.etBuscarProducto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    // Mostrar todos los productos según el filtro actual
                    if (mostrarSoloActivos) {
                        viewModel.getProductosActivos().observe(getViewLifecycleOwner(), FragmentProductosAdmin.this::actualizarListaProductos);
                    } else {
                        viewModel.getAllProductos().observe(getViewLifecycleOwner(), FragmentProductosAdmin.this::actualizarListaProductos);
                    }
                } else {
                    // Realizar búsqueda
                    viewModel.buscarProductos(query).observe(getViewLifecycleOwner(), FragmentProductosAdmin.this::actualizarListaProductos);
                }
            }
        });
    }
    
    private void actualizarListaProductos(List<ProductoEntity> productos) {
        if (productos != null) {
            productosList.clear();
            productosList.addAll(productos);
            adapter.submitList(productos);
            
            // Mostrar/ocultar mensaje de lista vacía
            if (productos.isEmpty()) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewProductos.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.recyclerViewProductos.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void toggleFiltroActivos() {
        mostrarSoloActivos = !mostrarSoloActivos;
        actualizarTextoFiltro();
        
        // Cambiar observador según el filtro
        if (mostrarSoloActivos) {
            viewModel.getProductosActivos().observe(getViewLifecycleOwner(), this::actualizarListaProductos);
        } else {
            viewModel.getAllProductos().observe(getViewLifecycleOwner(), this::actualizarListaProductos);
        }
    }
    
    private void actualizarTextoFiltro() {
        binding.btnFiltros.setText(
                mostrarSoloActivos ? "Mostrar Todos" : "Solo Activos"
        );
    }
    
    private void mostrarDialogoNuevoProducto() {
        DialogProductoBinding dialogBinding = DialogProductoBinding.inflate(getLayoutInflater());
        
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Nuevo Producto")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Crear", null)
                .setNegativeButton("Cancelar", null)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validarFormularioProducto(dialogBinding)) {
                    ProductoEntity nuevoProducto = crearProductoDesdeFormulario(dialogBinding);
                    viewModel.crearProducto(nuevoProducto);
                    dialog.dismiss();
                }
            });
        });
        
        // Configurar campos del formulario
        configurarFormularioProducto(dialogBinding, null);
        
        dialog.show();
    }
    
    private void mostrarDialogoEditarProducto(ProductoEntity producto) {
        DialogProductoBinding dialogBinding = DialogProductoBinding.inflate(getLayoutInflater());
        
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Editar Producto")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validarFormularioProducto(dialogBinding)) {
                    ProductoEntity productoEditado = crearProductoDesdeFormulario(dialogBinding);
                    productoEditado.setId_producto(producto.getId_producto());
                    // Mantener campos del producto original
                    productoEditado.setVersion(producto.getVersion());
                    // productoEditado.setFechaCreacion(producto.getFechaCreacion()); // TODO: database.entities.ProductoEntity no tiene este método
                    viewModel.actualizarProducto(productoEditado);
                    dialog.dismiss();
                }
            });
        });
        
        // Llenar campos con datos del producto
        configurarFormularioProducto(dialogBinding, producto);
        
        dialog.show();
    }
    
    private void configurarFormularioProducto(DialogProductoBinding dialogBinding, ProductoEntity producto) {
        if (producto != null) {
            // Modo edición - llenar campos
            dialogBinding.editNombre.setText(producto.getNombre());
            dialogBinding.editDescripcion.setText(producto.getDescripcion());
            dialogBinding.editPrecio.setText(String.valueOf(producto.getPrecio()));
            // dialogBinding.editPuntos.setText(String.valueOf(producto.getPuntosRequeridos())); // Campo no disponible en ProductoEntity
            dialogBinding.editCategoria.setText(producto.getCategoria());
            dialogBinding.switchDisponible.setChecked(producto.isActivo());
        } else {
            // Modo creación - valores por defecto
            dialogBinding.switchDisponible.setChecked(true);
            dialogBinding.editPuntos.setText("0");
        }
        
        // Configurar formato de precio
        dialogBinding.editPrecio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                // Validar que sea un número válido
                String text = s.toString();
                if (!text.isEmpty()) {
                    try {
                        double precio = Double.parseDouble(text);
                        if (precio < 0) {
                            dialogBinding.editPrecio.setError("El precio no puede ser negativo");
                        }
                    } catch (NumberFormatException e) {
                        dialogBinding.editPrecio.setError("Ingrese un precio válido");
                    }
                }
            }
        });
    }
    
    private boolean validarFormularioProducto(DialogProductoBinding dialogBinding) {
        boolean esValido = true;
        
        // Validar nombre
        String nombre = dialogBinding.editNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            dialogBinding.editNombre.setError("El nombre es obligatorio");
            esValido = false;
        }
        
        // Validar precio
        String precioStr = dialogBinding.editPrecio.getText().toString().trim();
        if (precioStr.isEmpty()) {
            dialogBinding.editPrecio.setError("El precio es obligatorio");
            esValido = false;
        } else {
            try {
                double precio = Double.parseDouble(precioStr);
                if (precio < 0) {
                    dialogBinding.editPrecio.setError("El precio no puede ser negativo");
                    esValido = false;
                }
            } catch (NumberFormatException e) {
                dialogBinding.editPrecio.setError("Ingrese un precio válido");
                esValido = false;
            }
        }
        
        // Validar puntos requeridos
        String puntosStr = dialogBinding.editPuntos.getText().toString().trim();
        if (puntosStr.isEmpty()) {
            dialogBinding.editPuntos.setError("Los puntos son obligatorios");
            esValido = false;
        } else {
            try {
                int puntos = Integer.parseInt(puntosStr);
                if (puntos < 0) {
                    dialogBinding.editPuntos.setError("Los puntos no pueden ser negativos");
                    esValido = false;
                }
            } catch (NumberFormatException e) {
                dialogBinding.editPuntos.setError("Ingrese puntos válidos");
                esValido = false;
            }
        }
        
        return esValido;
    }
    
    private ProductoEntity crearProductoDesdeFormulario(DialogProductoBinding dialogBinding) {
        ProductoEntity producto = new ProductoEntity();
        
        // Generar ID único para el producto usando solo timestamp
        producto.setId_producto(String.valueOf(System.currentTimeMillis()));
        
        producto.setNombre(dialogBinding.editNombre.getText().toString().trim());
        producto.setDescripcion(dialogBinding.editDescripcion.getText().toString().trim());
        producto.setPrecio(Double.parseDouble(dialogBinding.editPrecio.getText().toString().trim()));
        producto.setCategoria(dialogBinding.editCategoria.getText().toString().trim());
        producto.setEstado(dialogBinding.switchDisponible.isChecked() ? "activo" : "inactivo");
        
        // Valores por defecto para campos requeridos
        producto.setStockDisponible(0); // Stock inicial
        producto.setVersion(1); // Versión inicial
        producto.setNeedsSync(true); // Necesita sincronización
        producto.setSynced(false); // No sincronizado aún
        producto.setLastSync(System.currentTimeMillis());
        
        return producto;
    }
    
    private void mostrarDetalleProducto(ProductoEntity producto) {
        // Crear diálogo con información detallada del producto
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        
        String detalles = String.format(
                "Nombre: %s\n" +
                "Descripción: %s\n" +
                "Precio: %s\n" +
                "Stock: %d\n" +
                "Categoría: %s\n" +
                "Estado: %s\n" +
                "Creado: %s\n" +
                "Modificado: %s",
                producto.getNombre(),
                producto.getDescripcion(),
                NumberFormat.getCurrencyInstance(new Locale("es", "CO")).format(producto.getPrecio()),
                "N/A", // Stock no disponible
                producto.getCategoria(),
                producto.isActivo() ? "Activo" : "Inactivo",
                "N/A", // Fecha creación no disponible
                "N/A" // Fecha modificación no disponible
        );
        
        builder.setTitle("Detalle del Producto")
                .setMessage(detalles)
                .setPositiveButton("Cerrar", null)
                .setNeutralButton("Editar", (dialog, which) -> mostrarDialogoEditarProducto(producto))
                .show();
    }
    
    private void toggleEstadoProducto(ProductoEntity producto) {
        String accion = producto.isActivo() ? "desactivar" : "activar";
        String mensaje = String.format("¿Está seguro que desea %s el producto '%s'?", accion, producto.getNombre());
        
        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar acción")
                .setMessage(mensaje)
                .setPositiveButton("Sí", (dialog, which) -> {
                    if (producto.isActivo()) {
                        viewModel.desactivarProducto(Long.parseLong(producto.getId_producto()), "Desactivado por administrador");
                    } else {
                        viewModel.activarProducto(Long.parseLong(producto.getId_producto()));
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    
    private void confirmarEliminacionProducto(ProductoEntity producto) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Producto")
                .setMessage(String.format("¿Está seguro que desea eliminar el producto '%s'?\n\nEsta acción no se puede deshacer.", producto.getNombre()))
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.eliminarProducto(Long.parseLong(producto.getId_producto()));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    private void exportarProductos() {
        viewModel.exportarProductos();
        Toast.makeText(getContext(), "Exportando productos...", Toast.LENGTH_SHORT).show();
    }
    
    private void importarProductos() {
        // Implementar importación de productos
        Toast.makeText(getContext(), "Función de importación en desarrollo", Toast.LENGTH_SHORT).show();
    }
    
    private void sincronizarProductos() {
        viewModel.sincronizarConServidor();
        Toast.makeText(getContext(), "Sincronizando con servidor...", Toast.LENGTH_SHORT).show();
    }
    
    private void toggleDisponibilidadProducto(ProductoEntity producto) {
        // Cambiar el estado de disponibilidad del producto
        boolean nuevoEstado = !producto.isActivo();
        producto.setActivo(nuevoEstado);
        
        // Actualizar en el ViewModel
        viewModel.actualizarProducto(producto);
        
        String mensaje = nuevoEstado ? "Producto marcado como disponible" : "Producto marcado como no disponible";
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
    
    private void volverAlMenuPrincipal() {
        // Navegar de vuelta al dashboard de administración
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}