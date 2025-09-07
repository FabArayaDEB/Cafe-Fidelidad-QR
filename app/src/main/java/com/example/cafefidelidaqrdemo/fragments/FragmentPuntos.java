package com.example.cafefidelidaqrdemo.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentPuntos extends Fragment {

    private FragmentUserBinding binding;
    private Context mContext;
    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private PuntosAdapter puntosAdapter;
    private List<TransaccionPuntos> transaccionesList;
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

        firebaseAuth = FirebaseAuth.getInstance();
        
        // Inicializar vistas
        initViews();
        
        // Cargar información de puntos
        loadPuntosInfo();
        
        // Cargar historial de transacciones
        loadTransaccionesHistorial();
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

    private void loadPuntosInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String puntosStr = "" + snapshot.child("puntos").getValue();
                        String nivel = "" + snapshot.child("nivel").getValue();
                        
                        int puntos = 0;
                        try {
                            puntos = Integer.parseInt(puntosStr.equals("null") ? "0" : puntosStr);
                        } catch (NumberFormatException e) {
                            puntos = 0;
                        }
                        
                        if (nivel.equals("null")) {
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(mContext, "Error al cargar información de puntos", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void loadTransaccionesHistorial() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Transacciones")
                .child(firebaseAuth.getUid());
        
        Query query = ref.orderByChild("fecha").limitToLast(50); // Últimas 50 transacciones
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transaccionesList.clear();
                
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        String tipo = "" + ds.child("tipo").getValue();
                        String descripcion = "" + ds.child("descripcion").getValue();
                        String puntosStr = "" + ds.child("puntos").getValue();
                        String fechaStr = "" + ds.child("fecha").getValue();
                        
                        int puntos = Integer.parseInt(puntosStr.equals("null") ? "0" : puntosStr);
                        long fecha = Long.parseLong(fechaStr.equals("null") ? "0" : fechaStr);
                        
                        TransaccionPuntos transaccion = new TransaccionPuntos(tipo, descripcion, puntos, fecha);
                        transaccionesList.add(transaccion);
                    } catch (Exception e) {
                        // Ignorar transacciones con datos inválidos
                    }
                }
                
                // Ordenar por fecha descendente (más recientes primero)
                Collections.reverse(transaccionesList);
                
                if (puntosAdapter != null) {
                    puntosAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Error al cargar historial de transacciones", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Clase para representar una transacción de puntos
    public static class TransaccionPuntos {
        public String tipo;
        public String descripcion;
        public int puntos;
        public long fecha;
        
        public TransaccionPuntos(String tipo, String descripcion, int puntos, long fecha) {
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.puntos = puntos;
            this.fecha = fecha;
        }
    }
    
    // Adapter para el RecyclerView de transacciones
    private class PuntosAdapter extends RecyclerView.Adapter<PuntosAdapter.ViewHolder> {
        private List<TransaccionPuntos> transacciones;
        
        public PuntosAdapter(List<TransaccionPuntos> transacciones) {
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
            TransaccionPuntos transaccion = transacciones.get(position);
            
            String puntosText = (transaccion.puntos > 0 ? "+" : "") + transaccion.puntos + " puntos";
            holder.text1.setText(transaccion.descripcion + " (" + puntosText + ")");
            holder.text2.setText(Contantes.DateTimeFormat(transaccion.fecha));
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