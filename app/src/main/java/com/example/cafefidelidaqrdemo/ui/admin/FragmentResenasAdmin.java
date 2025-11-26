package com.example.cafefidelidaqrdemo.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.databinding.FragmentResenasAdminBinding;
import com.example.cafefidelidaqrdemo.models.Producto;
import com.example.cafefidelidaqrdemo.models.Sucursal;
import com.example.cafefidelidaqrdemo.models.ResenaProducto;
import com.example.cafefidelidaqrdemo.models.ResenaSucursal;
import com.example.cafefidelidaqrdemo.ui.admin.adapters.ResenasProductoAdminAdapter;
import com.example.cafefidelidaqrdemo.ui.admin.adapters.ResenasSucursalAdminAdapter;
import com.example.cafefidelidaqrdemo.ui.admin.viewmodels.ResenasAdminViewModel;
import com.google.android.material.tabs.TabLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class FragmentResenasAdmin extends Fragment {

    private FragmentResenasAdminBinding binding;
    private ResenasAdminViewModel viewModel;

    private ResenasProductoAdminAdapter productoAdapter;
    private ResenasSucursalAdminAdapter sucursalAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentResenasAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        setupViewModel();
        setupTabs();
        setupLists();
        setupObservers();

        // Configurar SwipeRefresh
        binding.swipeRefresh.setOnRefreshListener(() -> {
            TabLayout.Tab tab = binding.tabLayout.getTabAt(binding.tabLayout.getSelectedTabPosition());
            if (tab != null && tab.getPosition() == 0) {
                viewModel.refreshProductoResenas();
            } else {
                viewModel.refreshSucursalResenas();
            }
        });

        // Cerrar el indicador al terminar la carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding != null) {
                binding.swipeRefresh.setRefreshing(Boolean.TRUE.equals(isLoading));
            }
        });

        // Botón Cargar más según pestaña actual
        binding.btnCargarMas.setOnClickListener(v -> {
            TabLayout.Tab tab = binding.tabLayout.getTabAt(binding.tabLayout.getSelectedTabPosition());
            if (tab != null && tab.getPosition() == 0) {
                viewModel.loadMoreProducto();
            } else {
                viewModel.loadMoreSucursal();
            }
        });
    }

    private void setupToolbar() {
        binding.toolbar.setTitle("Reseñas");
        binding.toolbar.setNavigationOnClickListener(v -> volverAlMenuPrincipal());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ResenasAdminViewModel.class);
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Productos"), true);
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Sucursales"));
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    binding.spinnerProductos.setVisibility(View.VISIBLE);
                    binding.spinnerSucursales.setVisibility(View.GONE);
                    binding.recyclerView.setAdapter(productoAdapter);
                    Boolean more = viewModel.getHasMoreProducto().getValue();
                    binding.btnCargarMas.setVisibility(Boolean.TRUE.equals(more) ? View.VISIBLE : View.GONE);
                } else {
                    binding.spinnerProductos.setVisibility(View.GONE);
                    binding.spinnerSucursales.setVisibility(View.VISIBLE);
                    binding.recyclerView.setAdapter(sucursalAdapter);
                    Boolean more = viewModel.getHasMoreSucursal().getValue();
                    binding.btnCargarMas.setVisibility(Boolean.TRUE.equals(more) ? View.VISIBLE : View.GONE);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupLists() {
        productoAdapter = new ResenasProductoAdminAdapter(new ArrayList<>());
        sucursalAdapter = new ResenasSucursalAdminAdapter(new ArrayList<>());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(productoAdapter);

        binding.spinnerProductos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Producto p = (Producto) parent.getItemAtPosition(position);
                int pid = -1;
                if (p != null && p.getId() != null) {
                    try { pid = Integer.parseInt(p.getId()); } catch (NumberFormatException ignored) { pid = -1; }
                }
                viewModel.setSelectedProductoId(pid);
                if (p != null) {
                    productoAdapter.setProductoNombre(p.getNombre());
                } else {
                    productoAdapter.setProductoNombre("");
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spinnerSucursales.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Sucursal s = (Sucursal) parent.getItemAtPosition(position);
                int sid = -1;
                if (s != null && s.getId() != null) {
                    try { sid = Integer.parseInt(s.getId()); } catch (NumberFormatException ignored) { sid = -1; }
                }
                viewModel.setSelectedSucursalId(sid);
                if (s != null) {
                    sucursalAdapter.setSucursalNombre(s.getNombre());
                } else {
                    sucursalAdapter.setSucursalNombre("");
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupObservers() {
        // Productos y sucursales para spinners
        viewModel.getAllProductos().observe(getViewLifecycleOwner(), productos -> {
            if (productos != null) {
                ArrayAdapter<Producto> adapter = new ArrayAdapter<Producto>(requireContext(), android.R.layout.simple_spinner_item, productos) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        TextView tv = (TextView) v.findViewById(android.R.id.text1);
                        if (tv == null && v instanceof TextView) {
                            tv = (TextView) v;
                        }
                        Producto p = getItem(position);
                        if (p != null && tv != null) {
                            tv.setText(p.getId() + " - " + p.getNombre());
                        }
                        return v;
                    }

                    @Override
                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v = super.getDropDownView(position, convertView, parent);
                        TextView tv = (TextView) v.findViewById(android.R.id.text1);
                        if (tv == null && v instanceof TextView) {
                            tv = (TextView) v;
                        }
                        Producto p = getItem(position);
                        if (p != null && tv != null) {
                            tv.setText(p.getId() + " - " + p.getNombre());
                        }
                        return v;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerProductos.setAdapter(adapter);
                // Seleccionar el primero por defecto y cargar reseñas
                if (!productos.isEmpty()) {
                    Producto p0 = productos.get(0);
                    productoAdapter.setProductoNombre(p0.getNombre());
                    binding.spinnerProductos.post(() -> binding.spinnerProductos.setSelection(0));
                    try {
                        int id = Integer.parseInt(p0.getId());
                        viewModel.setSelectedProductoId(id);
                    } catch (Exception ignored) {}
                }
            }
        });

        viewModel.getAllSucursales().observe(getViewLifecycleOwner(), sucursales -> {
            if (sucursales != null) {
                ArrayAdapter<Sucursal> adapter = new ArrayAdapter<Sucursal>(requireContext(), android.R.layout.simple_spinner_item, sucursales) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        TextView tv = (TextView) v.findViewById(android.R.id.text1);
                        if (tv == null && v instanceof TextView) {
                            tv = (TextView) v;
                        }
                        Sucursal s = getItem(position);
                        if (s != null && tv != null) {
                            tv.setText(s.getId() + " - " + s.getNombre());
                        }
                        return v;
                    }

                    @Override
                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v = super.getDropDownView(position, convertView, parent);
                        TextView tv = (TextView) v.findViewById(android.R.id.text1);
                        if (tv == null && v instanceof TextView) {
                            tv = (TextView) v;
                        }
                        Sucursal s = getItem(position);
                        if (s != null && tv != null) {
                            tv.setText(s.getId() + " - " + s.getNombre());
                        }
                        return v;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerSucursales.setAdapter(adapter);
                // Seleccionar la primera por defecto y cargar reseñas
                if (!sucursales.isEmpty()) {
                    Sucursal s0 = sucursales.get(0);
                    sucursalAdapter.setSucursalNombre(s0.getNombre());
                    binding.spinnerSucursales.post(() -> binding.spinnerSucursales.setSelection(0));
                    try {
                        int id = Integer.parseInt(s0.getId());
                        viewModel.setSelectedSucursalId(id);
                    } catch (Exception ignored) {}
                }
            }
        });

        // Reseñas por producto
        viewModel.getResenasProducto().observe(getViewLifecycleOwner(), resenas -> {
            List<ResenaProducto> list = resenas != null ? resenas : new ArrayList<>();
            productoAdapter.updateData(list);
            binding.emptyView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Reseñas por sucursal
        viewModel.getResenasSucursal().observe(getViewLifecycleOwner(), resenas -> {
            List<ResenaSucursal> list = resenas != null ? resenas : new ArrayList<>();
            sucursalAdapter.updateData(list);
            binding.emptyView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Nombres de usuarios para títulos
        viewModel.getNombresUsuarios().observe(getViewLifecycleOwner(), map -> {
            if (map != null) {
                productoAdapter.setUserNames(map);
                sucursalAdapter.setUserNames(map);
            }
        });

        // Loading y errores
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), err -> {
            if (err != null && !err.isEmpty()) {
                Toast.makeText(getContext(), err, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });

        // Control de visibilidad del botón Cargar más
        viewModel.getHasMoreProducto().observe(getViewLifecycleOwner(), more -> {
            TabLayout.Tab tab = binding.tabLayout.getTabAt(binding.tabLayout.getSelectedTabPosition());
            if (tab != null && tab.getPosition() == 0) {
                binding.btnCargarMas.setVisibility(Boolean.TRUE.equals(more) ? View.VISIBLE : View.GONE);
            }
        });
        viewModel.getHasMoreSucursal().observe(getViewLifecycleOwner(), more -> {
            TabLayout.Tab tab = binding.tabLayout.getTabAt(binding.tabLayout.getSelectedTabPosition());
            if (tab != null && tab.getPosition() == 1) {
                binding.btnCargarMas.setVisibility(Boolean.TRUE.equals(more) ? View.VISIBLE : View.GONE);
            }
        });
    }

    

    private void volverAlMenuPrincipal() {
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
