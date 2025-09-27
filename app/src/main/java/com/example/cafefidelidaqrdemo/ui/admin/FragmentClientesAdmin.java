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
import com.example.cafefidelidaqrdemo.models.Cliente;
import com.example.cafefidelidaqrdemo.databinding.FragmentClientesAdminBinding;
import com.example.cafefidelidaqrdemo.databinding.DialogClienteBinding;
import com.example.cafefidelidaqrdemo.adapters.ClientesAdminAdapter;
import com.example.cafefidelidaqrdemo.ui.admin.viewmodels.ClientesAdminViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment para administración CRUD de clientes
 * Permite crear, editar, activar/desactivar y eliminar clientes
 */
public class FragmentClientesAdmin extends Fragment {
    
    private FragmentClientesAdminBinding binding;
    private ClientesAdminViewModel viewModel;
    private ClientesAdminAdapter adapter;
    private List<Cliente> clientesList = new ArrayList<>();
    private boolean mostrarSoloActivos = true;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentClientesAdminBinding.inflate(inflater, container, false);
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
        inflater.inflate(R.menu.menu_admin_clientes, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_toggle_filtro) {
            toggleFiltroActivos();
            return true;
        } else if (id == R.id.action_exportar) {
            exportarClientes();
            return true;
        } else if (id == R.id.action_importar) {
            importarClientes();
            return true;
        } else if (id == R.id.action_sincronizar) {
            sincronizarClientes();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ClientesAdminViewModel.class);
    }
    
    private void setupRecyclerView() {
        adapter = new ClientesAdminAdapter(clientesList, new ClientesAdminAdapter.OnClienteActionListener() {
            @Override
            public void onClienteClick(Cliente cliente) {
                mostrarDetalleCliente(cliente);
            }
            
            @Override
            public void onEditarClick(Cliente cliente) {
                mostrarDialogoEditarCliente(cliente);
            }
            
            @Override
            public void onToggleEstadoClick(Cliente cliente) {
                toggleEstadoCliente(cliente);
            }
            
            @Override
            public void onEliminarClick(Cliente cliente) {
                confirmarEliminacionCliente(cliente);
            }
        });
        
        binding.recyclerViewClientes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewClientes.setAdapter(adapter);
        binding.recyclerViewClientes.addItemDecoration(
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }
    
    private void setupUI() {
        binding.textViewTitulo.setText("Administración de Clientes");
        actualizarTextoFiltro();
    }
    
    private void setupObservers() {
        viewModel.getClientesLiveData().observe(getViewLifecycleOwner(), clientes -> {
            if (clientes != null) {
                actualizarListaClientes(clientes);
            }
        });
        
        viewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.fabNuevoCliente.setEnabled(!isLoading);
            }
        });
        
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
        
        viewModel.getSuccessLiveData().observe(getViewLifecycleOwner(), success -> {
            if (success != null && !success.isEmpty()) {
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupClickListeners() {
        binding.fabNuevoCliente.setOnClickListener(v -> mostrarDialogoNuevoCliente());
        
        binding.buttonFiltro.setOnClickListener(v -> toggleFiltroActivos());
    }
    
    private void setupSearchView() {
        binding.searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarClientes(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void actualizarListaClientes(List<Cliente> clientes) {
        clientesList.clear();
        
        if (mostrarSoloActivos) {
            for (Cliente cliente : clientes) {
                if (cliente.isActivo()) {
                    clientesList.add(cliente);
                }
            }
        } else {
            clientesList.addAll(clientes);
        }
        
        adapter.notifyDataSetChanged();
        
        // Actualizar contador
        binding.textViewContador.setText(String.format("Total: %d clientes", clientesList.size()));
        
        // Mostrar/ocultar vista vacía
        if (clientesList.isEmpty()) {
            binding.recyclerViewClientes.setVisibility(View.GONE);
            binding.textViewEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewClientes.setVisibility(View.VISIBLE);
            binding.textViewEmpty.setVisibility(View.GONE);
        }
    }
    
    private void filtrarClientes(String query) {
        viewModel.buscarClientes(query);
    }
    
    private void toggleFiltroActivos() {
        mostrarSoloActivos = !mostrarSoloActivos;
        actualizarTextoFiltro();
        
        // Recargar datos con el nuevo filtro
        if (mostrarSoloActivos) {
            viewModel.cargarClientesActivos();
        } else {
            viewModel.cargarClientes();
        }
    }
    
    private void actualizarTextoFiltro() {
        binding.buttonFiltro.setText(mostrarSoloActivos ? "Mostrar todos" : "Solo activos");
    }
    
    private void mostrarDialogoNuevoCliente() {
        DialogClienteBinding dialogBinding = DialogClienteBinding.inflate(getLayoutInflater());
        
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogBinding.getRoot())
                .create();
        
        dialogBinding.tvTituloDialog.setText("Nuevo Cliente");
        
        dialogBinding.btnCancelar.setOnClickListener(v -> dialog.dismiss());
        
        dialogBinding.btnGuardar.setOnClickListener(v -> {
            if (validarFormularioCliente(dialogBinding)) {
                Cliente nuevoCliente = crearClienteDesdeFormulario(dialogBinding);
                viewModel.agregarCliente(nuevoCliente);
                dialog.dismiss();
            }
        });
        
        dialog.show();
    }
    
    private void mostrarDialogoEditarCliente(Cliente cliente) {
        DialogClienteBinding dialogBinding = DialogClienteBinding.inflate(getLayoutInflater());
        
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogBinding.getRoot())
                .create();
        
        dialogBinding.tvTituloDialog.setText("Editar Cliente");
        
        // Configurar formulario con datos del cliente
        configurarFormularioCliente(dialogBinding, cliente);
        
        dialogBinding.btnCancelar.setOnClickListener(v -> dialog.dismiss());
        
        dialogBinding.btnGuardar.setOnClickListener(v -> {
            if (validarFormularioCliente(dialogBinding)) {
                Cliente clienteActualizado = crearClienteDesdeFormulario(dialogBinding);
                clienteActualizado.setId(cliente.getId());
                viewModel.actualizarCliente(clienteActualizado);
                dialog.dismiss();
            }
        });
        
        dialog.show();
    }
    
    private void configurarFormularioCliente(DialogClienteBinding dialogBinding, Cliente cliente) {
        dialogBinding.editNombre.setText(cliente.getNombre());
        dialogBinding.editEmail.setText(cliente.getEmail());
        dialogBinding.editTelefono.setText(cliente.getTelefono());
        dialogBinding.editPassword.setText(cliente.getPassword());
        dialogBinding.editPuntos.setText(String.valueOf(cliente.getPuntosAcumulados()));
        dialogBinding.switchActivo.setChecked(cliente.isActivo());
    }
    
    private boolean validarFormularioCliente(DialogClienteBinding dialogBinding) {
        String nombre = dialogBinding.editNombre.getText().toString().trim();
        String email = dialogBinding.editEmail.getText().toString().trim();
        String telefono = dialogBinding.editTelefono.getText().toString().trim();
        String password = dialogBinding.editPassword.getText().toString().trim();
        
        if (nombre.isEmpty()) {
            dialogBinding.layoutNombre.setError("El nombre es requerido");
            return false;
        }
        dialogBinding.layoutNombre.setError(null);
        
        if (email.isEmpty()) {
            dialogBinding.layoutEmail.setError("El email es requerido");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            dialogBinding.layoutEmail.setError("Email inválido");
            return false;
        }
        dialogBinding.layoutEmail.setError(null);
        
        if (telefono.isEmpty()) {
            dialogBinding.layoutTelefono.setError("El teléfono es requerido");
            return false;
        }
        dialogBinding.layoutTelefono.setError(null);
        
        if (password.isEmpty()) {
            dialogBinding.layoutPassword.setError("La contraseña es requerida");
            return false;
        }
        if (password.length() < 6) {
            dialogBinding.layoutPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return false;
        }
        dialogBinding.layoutPassword.setError(null);
        
        return true;
    }
    
    private Cliente crearClienteDesdeFormulario(DialogClienteBinding dialogBinding) {
        Cliente cliente = new Cliente();
        cliente.setNombre(dialogBinding.editNombre.getText().toString().trim());
        cliente.setEmail(dialogBinding.editEmail.getText().toString().trim());
        cliente.setTelefono(dialogBinding.editTelefono.getText().toString().trim());
        cliente.setPassword(dialogBinding.editPassword.getText().toString().trim());
        
        String puntosText = dialogBinding.editPuntos.getText().toString().trim();
        int puntos = puntosText.isEmpty() ? 0 : Integer.parseInt(puntosText);
        cliente.setPuntosAcumulados(puntos);
        
        cliente.setActivo(dialogBinding.switchActivo.isChecked());
        cliente.setFechaCreacion(System.currentTimeMillis());
        cliente.setFechaActualizacion(System.currentTimeMillis());
        
        return cliente;
    }
    
    private void mostrarDetalleCliente(Cliente cliente) {
        // Crear diálogo con información detallada del cliente
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        
        String detalles = String.format(
                "Nombre: %s\n" +
                "Email: %s\n" +
                "Teléfono: %s\n" +
                "Puntos acumulados: %d\n" +
                "Total visitas: %d\n" +
                "Estado: %s",
                cliente.getNombre(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getPuntosAcumulados(),
                cliente.getTotalVisitas(),
                cliente.isActivo() ? "Activo" : "Inactivo"
        );
        
        builder.setTitle("Detalle del Cliente")
                .setMessage(detalles)
                .setPositiveButton("Cerrar", null)
                .setNeutralButton("Editar", (dialog, which) -> mostrarDialogoEditarCliente(cliente))
                .show();
    }
    
    private void toggleEstadoCliente(Cliente cliente) {
        viewModel.cambiarEstadoCliente(cliente);
    }
    
    private void confirmarEliminacionCliente(Cliente cliente) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de que desea eliminar al cliente " + cliente.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.eliminarCliente(Integer.parseInt(cliente.getId()));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    private void exportarClientes() {
        Toast.makeText(getContext(), "Función de exportar en desarrollo", Toast.LENGTH_SHORT).show();
    }
    
    private void importarClientes() {
        Toast.makeText(getContext(), "Función de importar en desarrollo", Toast.LENGTH_SHORT).show();
    }
    
    private void sincronizarClientes() {
        viewModel.cargarClientes();
        Toast.makeText(getContext(), "Sincronizando clientes...", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}