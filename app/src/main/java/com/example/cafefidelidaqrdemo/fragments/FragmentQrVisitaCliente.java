package com.example.cafefidelidaqrdemo.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.cafefidelidaqrdemo.ClienteMainActivity;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.databinding.FragmentQrVisitaClienteBinding;
import com.example.cafefidelidaqrdemo.ui.cliente.FragmentProgresoFidelizacion;
import com.example.cafefidelidaqrdemo.utils.Resources;
import com.example.cafefidelidaqrdemo.viewmodels.ClienteQRVisitasViewModel; // Asegúrate de importar el ViewModel correcto

public class FragmentQrVisitaCliente extends Fragment {

    private FragmentQrVisitaClienteBinding binding;
    private Context mContext;
    private ClienteQRVisitasViewModel viewModel;

    public FragmentQrVisitaCliente() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQrVisitaClienteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(ClienteQRVisitasViewModel.class);

        // 2. Configurar UI y Observadores
        setupObservers();
        setupClickListeners();

        // 3. Cargar datos (Solo si no hay datos previos para evitar recargas al rotar)
        if (viewModel.getQrVisitaState().getValue() == null) {
            viewModel.cargarQrVisita();
        }
    }

    private void setupObservers() {
        // Solo un observador para controlar toda la pantalla
        viewModel.getQrVisitaState().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        // Muestra un ProgressBar y oculta el QR mientras carga
                        if (binding.progressBar != null) binding.progressBar.setVisibility(View.VISIBLE);
                        binding.ivQrVisita.setVisibility(View.INVISIBLE);
                        break;

                    case SUCCESS:
                        if (binding.progressBar != null) binding.progressBar.setVisibility(View.GONE);
                        binding.ivQrVisita.setVisibility(View.VISIBLE);

                        // Seteamos el Bitmap
                        if (resource.data != null) {
                            binding.ivQrVisita.setImageBitmap(resource.data);
                        }
                        break;

                    case ERROR:
                        if (binding.progressBar != null) binding.progressBar.setVisibility(View.GONE);
                        binding.ivQrVisita.setVisibility(View.VISIBLE);

                        String errorMsg = resource.message != null ? resource.message : "Error desconocido";
                        Toast.makeText(mContext, errorMsg, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    private void setupClickListeners() {
        // Ampliar QR al tocar
        binding.ivQrVisita.setOnClickListener(v -> {
            // Obtenemos el valor actual del LiveData de forma segura
            Resources<Bitmap> currentState = viewModel.getQrVisitaState().getValue();

            if (currentState != null && currentState.status == Resources.Status.SUCCESS && currentState.data != null) {
                showQrDialog(currentState.data);
            } else {
                Toast.makeText(mContext, "El QR no está listo para visualizar", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnVerPuntos.setOnClickListener( v -> {
            if (getActivity() instanceof ClienteMainActivity) {
                ((ClienteMainActivity) getActivity()).navegarItem(R.id.item_beneficios);
            }
        });
    }

    private void showQrDialog(Bitmap bitmap) {
        // Validamos que el contexto no sea nulo
        if (mContext == null) return;

        View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_qr_fullscreen, null);
        ImageView imageView = dialogView.findViewById(R.id.iv_qr_fullscreen);
        imageView.setImageBitmap(bitmap);

        new AlertDialog.Builder(mContext)
                .setView(dialogView)
                .setPositiveButton("Cerrar", (d, w) -> d.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}