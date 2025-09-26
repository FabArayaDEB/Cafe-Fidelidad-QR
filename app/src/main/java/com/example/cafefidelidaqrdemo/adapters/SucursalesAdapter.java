package com.example.cafefidelidaqrdemo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.models.Sucursal;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SucursalesAdapter extends ListAdapter<SucursalesAdapter.SucursalItem, SucursalesAdapter.SucursalViewHolder> {
    
    private OnSucursalClickListener onSucursalClickListener;
    private OnSucursalLongClickListener onSucursalLongClickListener;
    private final DecimalFormat distanceFormat = new DecimalFormat("#.## km");
    
    public SucursalesAdapter() {
        super(DIFF_CALLBACK);
    }
    
    // Clase para encapsular sucursal con distancia
    public static class SucursalItem {
        private final Sucursal sucursal;
        private final Double distancia; // en kilómetros, null si no disponible
        
        public SucursalItem(Sucursal sucursal, Double distancia) {
            this.sucursal = sucursal;
            this.distancia = distancia;
        }
        
        public Sucursal getSucursal() {
            return sucursal;
        }
        
        public Double getDistancia() {
            return distancia;
        }
        
        public boolean hasDistancia() {
            return distancia != null;
        }
    }
    
    @NonNull
    @Override
    public SucursalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_sucursal, parent, false);
        return new SucursalViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SucursalViewHolder holder, int position) {
        SucursalItem item = getItem(position);
        if (item != null) {
            holder.bind(item);
        }
    }
    
    // Método para actualizar lista con distancias
    public void submitListWithDistance(List<SucursalItem> list) {
        submitList(list);
    }
    
    // Método para actualizar lista sin distancias
    public void submitList(List<SucursalItem> sucursales) {
        List<SucursalItem> items = sucursales.stream()
            .map(sucursal -> new SucursalItem(sucursal.getSucursal(), null))
            .collect(Collectors.toList());
        submitList(items);
    }
    
    // Interfaces para callbacks
    public interface OnSucursalClickListener {
        void onSucursalClick(Sucursal sucursal);
    }
    
    public interface OnSucursalLongClickListener {
        void onSucursalLongClick(Sucursal sucursal);
    }
    
    // Setters para listeners
    public void setOnSucursalClickListener(OnSucursalClickListener listener) {
        this.onSucursalClickListener = listener;
    }
    
    public void setOnSucursalLongClickListener(OnSucursalLongClickListener listener) {
        this.onSucursalLongClickListener = listener;
    }
    
    // ViewHolder
    public class SucursalViewHolder extends RecyclerView.ViewHolder {
        
        private final MaterialCardView cardView;
        private final ImageView imageViewIcon;
        private final TextView textViewNombre;
        private final TextView textViewDireccion;
        private final TextView textViewHorario;
        private final TextView textViewDistancia;
        private final Chip chipEstado;
        private final ImageView imageViewLocation;
        
        public SucursalViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.cardViewSucursal);
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewDireccion = itemView.findViewById(R.id.textViewDireccion);
            textViewHorario = itemView.findViewById(R.id.textViewHorario);
            textViewDistancia = itemView.findViewById(R.id.textViewDistancia);
            chipEstado = itemView.findViewById(R.id.chipEstado);
            imageViewLocation = itemView.findViewById(R.id.imageViewLocation);
        }
        
        public void bind(SucursalItem item) {
            Sucursal sucursal = item.getSucursal();
            
            // Configurar información básica
            textViewNombre.setText(sucursal.getNombre());
            textViewDireccion.setText(sucursal.getDireccion());
            textViewHorario.setText(sucursal.getHorarioApertura() + " - " + sucursal.getHorarioCierre());
            
            // Configurar estado
            configurarEstado(sucursal.isActiva() ? "activo" : "inactivo");
            
            // Configurar distancia
            configurarDistancia(item);
            
            // Configurar icono de sucursal
            configurarIcono(sucursal);
            
            // Configurar apariencia de la tarjeta según estado
            configurarAparienciaTarjeta(sucursal);
            
            // Configurar listeners
            configurarListeners(sucursal);
        }
        
        private void configurarEstado(String estado) {
            if ("activo".equalsIgnoreCase(estado)) {
                chipEstado.setText("Activa");
                chipEstado.setChipBackgroundColorResource(R.color.success_light);
                chipEstado.setTextColor(itemView.getContext().getColor(R.color.success_green));
                chipEstado.setVisibility(View.VISIBLE);
            } else {
                chipEstado.setText("Inactiva");
                chipEstado.setChipBackgroundColorResource(R.color.error_light);
                chipEstado.setTextColor(itemView.getContext().getColor(R.color.error_red));
                chipEstado.setVisibility(View.VISIBLE);
            }
        }
        
        private void configurarDistancia(SucursalItem item) {
            if (item.hasDistancia()) {
                Double distancia = item.getDistancia();
                if (distancia < 1.0) {
                    // Mostrar en metros si es menos de 1 km
                    int metros = (int) (distancia * 1000);
                    textViewDistancia.setText(String.format(Locale.getDefault(), "%d m", metros));
                } else {
                    // Mostrar en kilómetros
                    textViewDistancia.setText(distanceFormat.format(distancia));
                }
                textViewDistancia.setVisibility(View.VISIBLE);
                imageViewLocation.setVisibility(View.VISIBLE);
                
                // Cambiar color según distancia
                if (distancia <= 0.5) {
                    textViewDistancia.setTextColor(itemView.getContext().getColor(R.color.success_green));
                } else if (distancia <= 2.0) {
                    textViewDistancia.setTextColor(itemView.getContext().getColor(R.color.warm_orange));
                } else {
                    textViewDistancia.setTextColor(itemView.getContext().getColor(R.color.on_surface_variant));
                }
            } else {
                textViewDistancia.setVisibility(View.GONE);
                imageViewLocation.setVisibility(View.GONE);
            }
        }
        
        private void configurarIcono(Sucursal sucursal) {
            // Configurar icono según el estado de la sucursal
            if (sucursal.isActiva()) {
                imageViewIcon.setImageResource(R.drawable.ic_store);
                imageViewIcon.setColorFilter(itemView.getContext().getColor(R.color.primary));
            } else {
                imageViewIcon.setImageResource(R.drawable.ic_store_empty);
                imageViewIcon.setColorFilter(itemView.getContext().getColor(R.color.on_surface_variant));
            }
        }
        
        private void configurarAparienciaTarjeta(Sucursal sucursal) {
            if (sucursal.isActiva()) {
                // Sucursal activa - tarjeta normal
                cardView.setCardElevation(4f);
                cardView.setAlpha(1.0f);
                cardView.setStrokeWidth(0);
            } else {
                // Sucursal inactiva - tarjeta atenuada
                cardView.setCardElevation(2f);
                cardView.setAlpha(0.7f);
                cardView.setStrokeWidth(1);
                cardView.setStrokeColor(itemView.getContext().getColor(R.color.color_outline_variant));
            }
        }
        
        private void configurarListeners(Sucursal sucursal) {
            // Click listener
            cardView.setOnClickListener(v -> {
                if (onSucursalClickListener != null) {
                    onSucursalClickListener.onSucursalClick(sucursal);
                }
            });
            
            // Long click listener
            cardView.setOnLongClickListener(v -> {
                if (onSucursalLongClickListener != null) {
                    onSucursalLongClickListener.onSucursalLongClick(sucursal);
                    return true;
                }
                return false;
            });
            
            // Efecto ripple
            cardView.setClickable(true);
            cardView.setFocusable(true);
        }
    }
    
    // DiffUtil callback para optimizar actualizaciones
    private static final DiffUtil.ItemCallback<SucursalItem> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<SucursalItem>() {
            
            @Override
            public boolean areItemsTheSame(@NonNull SucursalItem oldItem, @NonNull SucursalItem newItem) {
                return oldItem.getSucursal().getId() == newItem.getSucursal().getId();
            }
            
            @Override
            public boolean areContentsTheSame(@NonNull SucursalItem oldItem, @NonNull SucursalItem newItem) {
                Sucursal oldSucursal = oldItem.getSucursal();
                Sucursal newSucursal = newItem.getSucursal();
                
                boolean sucursalSame = oldSucursal.getNombre().equals(newSucursal.getNombre()) &&
                    oldSucursal.getDireccion().equals(newSucursal.getDireccion()) &&
                    oldSucursal.getHorarioApertura().equals(newSucursal.getHorarioApertura()) &&
                    oldSucursal.getHorarioCierre().equals(newSucursal.getHorarioCierre()) &&
                    oldSucursal.isActiva() == newSucursal.isActiva() &&
                    Double.compare(oldSucursal.getLatitud(), newSucursal.getLatitud()) == 0 &&
                    Double.compare(oldSucursal.getLongitud(), newSucursal.getLongitud()) == 0;
                
                boolean distanciaSame = (oldItem.getDistancia() == null && newItem.getDistancia() == null) ||
                    (oldItem.getDistancia() != null && newItem.getDistancia() != null &&
                     Double.compare(oldItem.getDistancia(), newItem.getDistancia()) == 0);
                
                return sucursalSame && distanciaSame;
            }
        };
    
    // Métodos de utilidad
    public int getSucursalPosition(int idSucursal) {
        for (int i = 0; i < getItemCount(); i++) {
            SucursalItem item = getItem(i);
            if (item != null && item.getSucursal().getId() == idSucursal) {
                return i;
            }
        }
        return -1;
    }
    
    public Sucursal getSucursalAt(int position) {
        SucursalItem item = getItem(position);
        return item != null ? item.getSucursal() : null;
    }
    
    public boolean isEmpty() {
        return getItemCount() == 0;
    }
    
    public int getActiveSucursalesCount() {
        int count = 0;
        for (int i = 0; i < getItemCount(); i++) {
            SucursalItem item = getItem(i);
            if (item != null && item.getSucursal().isActiva()) {
                count++;
            }
        }
        return count;
    }
}