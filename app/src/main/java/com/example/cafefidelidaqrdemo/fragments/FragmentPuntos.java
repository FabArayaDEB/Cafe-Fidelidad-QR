package com.example.cafefidelidaqrdemo.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cafefidelidaqrdemo.Contantes;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.databinding.FragmentUserBinding;
import com.example.cafefidelidaqrdemo.ui.cliente.viewmodels.TableroClienteViewModel;
import com.example.cafefidelidaqrdemo.database.entities.TransaccionEntity;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;

import java.util.ArrayList;
import java.util.List;

public class FragmentPuntos extends Fragment {

    private FragmentUserBinding binding;
    private Context mContext;
    private TableroClienteViewModel viewModel;
    private RecyclerView recyclerView;
    private PuntosAdapter puntosAdapter;
    private List<TransaccionEntity> transaccionesList;
    private TextView tvPuntosActuales, tvNivelActual, tvPuntosParaSiguiente;

    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    public FragmentPuntos() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(TableroClienteViewModel.class);
        
        // Inicializar vistas
        initViews();
        
        // Configurar observadores
        setupObservers();
        
        // Cargar datos
        String clienteId = "cliente_demo"; // TODO: Obtener ID real del cliente autenticado
        viewModel.cargarTableroCliente(clienteId);
    }
    
    private void initViews() {
        // Buscar vistas en el layout
        tvPuntosActuales = binding.getRoot().findViewById(R.id.tv_puntos_actuales);
        tvNivelActual = binding.getRoot().findViewById(R.id.tv_nivel_actual);
        tvPuntosParaSiguiente = binding.getRoot().findViewById(R.id.tv_puntos_siguiente_nivel);
        recyclerView = binding.getRoot().findViewById(R.id.recyclerView_transacciones);
        
        // Configurar RecyclerView
        if (recyclerView != null) {
            transaccionesList = new ArrayList<>();
            puntosAdapter = new PuntosAdapter(transaccionesList);
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            recyclerView.setAdapter(puntosAdapter);
        }
    }
    
    private void setupObservers() {
        // Observar datos del cliente
        viewModel.getClienteData().observe(getViewLifecycleOwner(), this::updateClienteInfo);
        
        // TODO: Implementar observación de transacciones cuando esté disponible en TableroClienteViewModel
        
        // Observar errores
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Mostrar/ocultar indicador de carga
        });
    }

    private void updateClienteInfo(ClienteEntity cliente) {
        if (cliente == null) return;
        
        int puntos = cliente.getPuntos();
        String nivel = cliente.getNivel();
        
        if (nivel == null || nivel.isEmpty()) {
            nivel = Contantes.calcularNivel(puntos);
        }
        
        // Actualizar UI
        if (tvPuntosActuales != null) {
            tvPuntosActuales.setText("Puntos Actuales: " + puntos);
        }
        if (tvNivelActual != null) {
            tvNivelActual.setText("Nivel: " + nivel);
        }
        if (tvPuntosParaSiguiente != null) {
            int puntosParaSiguiente = Contantes.puntosParaSiguienteNivel(puntos);
            if (puntosParaSiguiente > 0) {
                tvPuntosParaSiguiente.setText("Faltan " + puntosParaSiguiente + " puntos para el siguiente nivel");
            } else {
                tvPuntosParaSiguiente.setText("¡Has alcanzado el nivel máximo!");
            }
        }
    }
    
    private void updateTransacciones(List<TransaccionEntity> transacciones) {
        if (transacciones == null) return;
        
        transaccionesList.clear();
        transaccionesList.addAll(transacciones);
        
        if (puntosAdapter != null) {
            puntosAdapter.notifyDataSetChanged();
        }
    }
    
    // Adapter para el RecyclerView de transacciones
    private class PuntosAdapter extends RecyclerView.Adapter<PuntosAdapter.ViewHolder> {
        private List<TransaccionEntity> transacciones;
        
        public PuntosAdapter(List<TransaccionEntity> transacciones) {
            this.transacciones = transacciones;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TransaccionEntity transaccion = transacciones.get(position);
            
            String puntosText = (transaccion.getPuntos() > 0 ? "+" : "") + transaccion.getPuntos() + " puntos";
            holder.text1.setText(transaccion.getDescripcion() + " (" + puntosText + ")");
            holder.text2.setText(Contantes.DateTimeFormat(transaccion.getFecha()));
        }
        
        @Override
        public int getItemCount() {
            return transacciones.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            
            ViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}