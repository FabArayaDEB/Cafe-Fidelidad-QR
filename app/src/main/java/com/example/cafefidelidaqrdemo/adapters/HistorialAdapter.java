package com.example.cafefidelidaqrdemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.models.Canje;
import com.example.cafefidelidaqrdemo.models.Visita;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para mostrar el historial de visitas y canjes del usuario
 */
public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {
    
    private static final int TYPE_VISITA = 0;
    private static final int TYPE_CANJE = 1;
    
    private Context context;
    private List<Object> historialItems;
    private SimpleDateFormat dateFormat;
    
    public HistorialAdapter(Context context) {
        this.context = context;
        this.historialItems = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }
    
    @Override
    public int getItemViewType(int position) {
        Object item = historialItems.get(position);
        if (item instanceof Visita) {
            return TYPE_VISITA;
        } else if (item instanceof Canje) {
            return TYPE_CANJE;
        }
        return TYPE_VISITA; // Default
    }
    
    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_VISITA) {
            view = LayoutInflater.from(context).inflate(R.layout.item_historial_visita, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_historial_canje, parent, false);
        }
        return new HistorialViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        Object item = historialItems.get(position);
        
        if (item instanceof Visita) {
            bindVisita(holder, (Visita) item);
        } else if (item instanceof Canje) {
            bindCanje(holder, (Canje) item);
        }
    }
    
    private void bindVisita(HistorialViewHolder holder, Visita visita) {
        if (holder.textTipo != null) {
            holder.textTipo.setText("Visita");
        }
        if (holder.textDescripcion != null) {
            holder.textDescripcion.setText("Visita registrada");
        }
        if (holder.textFecha != null) {
            holder.textFecha.setText(dateFormat.format(new Date(visita.getFechaVisita())));
        }
        if (holder.textPuntos != null) {
            holder.textPuntos.setText("+" + visita.getPuntosGanados() + " puntos");
        }
    }
    
    private void bindCanje(HistorialViewHolder holder, Canje canje) {
        if (holder.textTipo != null) {
            holder.textTipo.setText("Canje");
        }
        if (holder.textDescripcion != null) {
            holder.textDescripcion.setText("Beneficio canjeado");
        }
        if (holder.textFecha != null) {
            holder.textFecha.setText(dateFormat.format(new Date(canje.getFechaCanje())));
        }
        if (holder.textPuntos != null) {
            holder.textPuntos.setText("-" + canje.getPuntosUsados() + " puntos");
        }
    }
    
    @Override
    public int getItemCount() {
        return historialItems.size();
    }
    
    public void updateHistorial(List<Object> newItems) {
        this.historialItems.clear();
        if (newItems != null) {
            this.historialItems.addAll(newItems);
        }
        notifyDataSetChanged();
    }
    
    public static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView textTipo;
        TextView textDescripcion;
        TextView textFecha;
        TextView textPuntos;
        
        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            textTipo = itemView.findViewById(R.id.textTipo);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
            textFecha = itemView.findViewById(R.id.textFecha);
            textPuntos = itemView.findViewById(R.id.textPuntos);
        }
    }
}