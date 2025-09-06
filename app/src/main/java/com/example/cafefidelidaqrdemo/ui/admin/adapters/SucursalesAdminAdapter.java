package com.example.cafefidelidaqrdemo.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.database.entities.SucursalEntity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

/**
 * Adapter para la gestión administrativa de sucursales
 * Incluye controles para editar, activar/desactivar y eliminar sucursales
 */
public class SucursalesAdminAdapter extends RecyclerView.Adapter<SucursalesAdminAdapter.SucursalAdminViewHolder> {
    
    private List<SucursalEntity> sucursales;
    private OnSucursalClickListener listener;
    
    public SucursalesAdminAdapter(List<SucursalEntity> sucursales, OnSucursalClickListener listener) {
        this.sucursales = sucursales;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public SucursalAdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sucursal_admin, parent, false);
        return new SucursalAdminViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SucursalAdminViewHolder holder, int position) {
        SucursalEntity sucursal = sucursales.get(position);
        holder.bind(sucursal);
    }
    
    @Override
    public int getItemCount() {
        return sucursales != null ? sucursales.size() : 0;
    }
    
    public void updateSucursales(List<SucursalEntity> nuevasSucursales) {
        this.sucursales = nuevasSucursales;
        notifyDataSetChanged();
    }
    
    public interface OnSucursalClickListener {
        void onSucursalClick(SucursalEntity sucursal);
        void onEditarClick(SucursalEntity sucursal);
        void onToggleActivoClick(SucursalEntity sucursal);
        void onVerEnMapaClick(SucursalEntity sucursal);
        void onEliminarClick(SucursalEntity sucursal);
    }
    
    // Force recompilation marker
    
    class SucursalAdminViewHolder extends RecyclerView.ViewHolder {
        
        private MaterialCardView cardView;
        private ImageView imageViewIcon;
        private TextView textViewNombre;
        private TextView textViewDireccion;
        private TextView textViewCiudad;
        private TextView textViewTelefono;
        private TextView textViewHorario;
        private TextView textViewGerente;
        private Chip chipEstado;
        private SwitchMaterial switchActivo;
        private ImageButton buttonEditar;
        private ImageButton buttonMapa;
        private ImageButton buttonEliminar;
        
        public SucursalAdminViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.cardViewSucursal);
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewDireccion = itemView.findViewById(R.id.textViewDireccion);
            textViewCiudad = itemView.findViewById(R.id.textViewCiudad);
            textViewTelefono = itemView.findViewById(R.id.textViewTelefono);
            textViewHorario = itemView.findViewById(R.id.textViewHorario);
            textViewGerente = itemView.findViewById(R.id.textViewGerente);
            chipEstado = itemView.findViewById(R.id.chipEstado);
            switchActivo = itemView.findViewById(R.id.switchActivo);
            buttonEditar = itemView.findViewById(R.id.buttonEditar);
            buttonMapa = itemView.findViewById(R.id.buttonMapa);
            buttonEliminar = itemView.findViewById(R.id.buttonEliminar);
        }
        
        public void bind(SucursalEntity sucursal) {
            // Información básica
            textViewNombre.setText(sucursal.getNombre());
            textViewDireccion.setText(sucursal.getDireccion());
            textViewCiudad.setText("Ciudad no disponible"); // Campo no disponible en entity
            textViewTelefono.setText("Teléfono no disponible"); // Campo no disponible en entity
            String horario = sucursal.getHorario();
        textViewHorario.setText(horario);
            textViewGerente.setText("Gerente no disponible"); // Campo no disponible en entity
            
            // Estado
            configurarEstado(sucursal);
            
            // Switch activo
            switchActivo.setChecked(sucursal.isActiva());
            switchActivo.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleActivoClick(sucursal);
                }
            });
            
            // Listeners de botones
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSucursalClick(sucursal);
                }
            });
            
            buttonEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditarClick(sucursal);
                }
            });
            
            buttonMapa.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVerEnMapaClick(sucursal);
                }
            });
            
            buttonEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEliminarClick(sucursal);
                }
            });
        }
        
        private void configurarEstado(SucursalEntity sucursal) {
            if (sucursal.isActiva()) {
                chipEstado.setText("Activa");
                chipEstado.setChipBackgroundColorResource(android.R.color.holo_green_light);
                cardView.setAlpha(1.0f);
            } else {
                chipEstado.setText("Inactiva");
                chipEstado.setChipBackgroundColorResource(android.R.color.holo_red_light);
                cardView.setAlpha(0.7f);
            }
        }
    }
}