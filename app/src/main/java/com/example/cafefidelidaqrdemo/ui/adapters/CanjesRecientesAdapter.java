package com.example.cafefidelidaqrdemo.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.database.entities.CanjeEntity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para mostrar canjes recientes en el tablero del cliente
 */
public class CanjesRecientesAdapter extends RecyclerView.Adapter<CanjesRecientesAdapter.CanjeViewHolder> {
    
    private List<CanjeEntity> canjes;
    private OnCanjeClickListener listener;
    private SimpleDateFormat dateFormat;
    
    public interface OnCanjeClickListener {
        void onCanjeClick(CanjeEntity canje);
    }
    
    public CanjesRecientesAdapter(List<CanjeEntity> canjes, OnCanjeClickListener listener) {
        this.canjes = canjes;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public CanjeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_canje_reciente, parent, false);
        return new CanjeViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CanjeViewHolder holder, int position) {
        CanjeEntity canje = canjes.get(position);
        holder.bind(canje);
    }
    
    @Override
    public int getItemCount() {
        return canjes != null ? canjes.size() : 0;
    }
    
    public void updateCanjes(List<CanjeEntity> nuevosCanjes) {
        this.canjes = nuevosCanjes;
        notifyDataSetChanged();
    }
    
    class CanjeViewHolder extends RecyclerView.ViewHolder {
        
        private MaterialCardView cardView;
        private TextView textBeneficio;
        private TextView textFecha;
        private TextView textEstado;
        private Chip chipTipo;
        
        public CanjeViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.cardViewCanje);
            textBeneficio = itemView.findViewById(R.id.textBeneficio);
            textFecha = itemView.findViewById(R.id.textFecha);
            textEstado = itemView.findViewById(R.id.textEstado);
            chipTipo = itemView.findViewById(R.id.chipTipo);
            
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCanjeClick(canjes.get(position));
                    }
                }
            });
        }
        
        public void bind(CanjeEntity canje) {
            textBeneficio.setText(canje.getId_beneficio() != null ? 
                canje.getId_beneficio() : "Beneficio");
            
            if (canje.getFecha_solicitud() > 0) {
                textFecha.setText(dateFormat.format(new Date(canje.getFecha_solicitud())));
            } else {
                textFecha.setText("Fecha no disponible");
            }
            
            textEstado.setText(canje.getEstado() != null ? canje.getEstado() : "Pendiente");
            
            // Ocultar chip de tipo ya que CanjeEntity no tiene este campo
            chipTipo.setVisibility(View.GONE);
        }
    }
}