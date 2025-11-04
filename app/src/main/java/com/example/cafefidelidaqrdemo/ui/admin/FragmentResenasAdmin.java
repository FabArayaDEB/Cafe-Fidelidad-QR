package com.example.cafefidelidaqrdemo.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
                } else {
                    binding.spinnerProductos.setVisibility(View.GONE);
                    binding.spinnerSucursales.setVisibility(View.VISIBLE);
                    binding.recyclerView.setAdapter(sucursalAdapter);
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
                viewModel.setSelectedProductoId(p != null ? Integer.parseInt(p.getId()) : -1);
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
                viewModel.setSelectedSucursalId(s != null ? Integer.parseInt(s.getId()) : -1);
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
                ArrayAdapter<Producto> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, productos);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerProductos.setAdapter(adapter);
            }
        });

        viewModel.getAllSucursales().observe(getViewLifecycleOwner(), sucursales -> {
            if (sucursales != null) {
                ArrayAdapter<Sucursal> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sucursales);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerSucursales.setAdapter(adapter);
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