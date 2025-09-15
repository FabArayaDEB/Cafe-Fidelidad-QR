package com.example.cafefidelidaqrdemo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.database.entities.UbicacionEntity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class UbicacionAdapter extends RecyclerView.Adapter<UbicacionAdapter.UbicacionViewHolder> {
    private List<UbicacionEntity> ubicaciones;
    private OnUbicacionClickListener clickListener;
    private SimpleDateFormat dateFormat;

    public interface OnUbicacionClickListener {
        void onUbicacionClick(UbicacionEntity ubicacion);
        void onUbicacionLongClick(UbicacionEntity ubicacion);
    }

    public UbicacionAdapter(List<UbicacionEntity> ubicaciones) {
        this.ubicaciones = ubicaciones;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    public void setOnUbicacionClickListener(OnUbicacionClickListener listener) {
        this.clickListener = listener;
    }

    public void updateLocations(List<UbicacionEntity> newUbicaciones) {
        this.ubicaciones = newUbicaciones;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UbicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ubicacion, parent, false);
        return new UbicacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UbicacionViewHolder holder, int position) {
        UbicacionEntity ubicacion = ubicaciones.get(position);
        holder.bind(ubicacion);
    }

    @Override
    public int getItemCount() {
        return ubicaciones != null ? ubicaciones.size() : 0;
    }

    public class UbicacionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCoordenadas;
        private TextView tvFecha;
        private TextView tvPrecision;
        private TextView tvDireccion;
        private TextView tvCiudad;
        private TextView tvSucursal;
        private TextView tvDistancia;
        private View indicadorSincronizado;

        public UbicacionViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvCoordenadas = itemView.findViewById(R.id.tv_coordenadas);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvPrecision = itemView.findViewById(R.id.tv_precision);
            tvDireccion = itemView.findViewById(R.id.tv_direccion);
            tvCiudad = itemView.findViewById(R.id.tv_ciudad);
            tvSucursal = itemView.findViewById(R.id.tv_sucursal);
            tvDistancia = itemView.findViewById(R.id.tv_distancia);
            indicadorSincronizado = itemView.findViewById(R.id.indicador_sincronizado);

            // Configurar click listeners
            itemView.setOnClickListener(v -> {
                if (clickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    clickListener.onUbicacionClick(ubicaciones.get(getAdapterPosition()));
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (clickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    clickListener.onUbicacionLongClick(ubicaciones.get(getAdapterPosition()));
                    return true;
                }
                return false;
            });
        }

        public void bind(UbicacionEntity ubicacion) {
            // Coordenadas
            tvCoordenadas.setText(ubicacion.getCoordenadasString());
            
            // Fecha
            tvFecha.setText(dateFormat.format(ubicacion.getFechaRegistro()));
            
            // Precisión
            String precisionText = String.format(Locale.getDefault(), "±%.1f m", ubicacion.getPrecision());
            tvPrecision.setText(precisionText);
            
            // Dirección
            if (ubicacion.getDireccion() != null && !ubicacion.getDireccion().isEmpty()) {
                tvDireccion.setText(ubicacion.getDireccion());
                tvDireccion.setVisibility(View.VISIBLE);
            } else {
                tvDireccion.setVisibility(View.GONE);
            }
            
            // Ciudad
            if (ubicacion.getCiudad() != null && !ubicacion.getCiudad().isEmpty()) {
                tvCiudad.setText(ubicacion.getCiudad());
                tvCiudad.setVisibility(View.VISIBLE);
            } else {
                tvCiudad.setVisibility(View.GONE);
            }
            
            // Información de sucursal
            if (ubicacion.isEsSucursalCercana()) {
                tvSucursal.setVisibility(View.VISIBLE);
                if (ubicacion.getSucursalId() != null) {
                    tvSucursal.setText("Sucursal ID: " + ubicacion.getSucursalId());
                } else {
                    tvSucursal.setText("Cerca de sucursal");
                }
                
                // Distancia a sucursal
                if (ubicacion.getDistanciaSucursal() != null) {
                    String distanciaText = String.format(Locale.getDefault(), "%.0f m", ubicacion.getDistanciaSucursal());
                    tvDistancia.setText(distanciaText);
                    tvDistancia.setVisibility(View.VISIBLE);
                } else {
                    tvDistancia.setVisibility(View.GONE);
                }
            } else {
                tvSucursal.setVisibility(View.GONE);
                tvDistancia.setVisibility(View.GONE);
            }
            
            // Indicador de sincronización
            if (ubicacion.isSincronizado()) {
                indicadorSincronizado.setBackgroundColor(
                    itemView.getContext().getResources().getColor(android.R.color.holo_green_light)
                );
            } else {
                indicadorSincronizado.setBackgroundColor(
                    itemView.getContext().getResources().getColor(android.R.color.holo_orange_light)
                );
            }
            
            // Configurar color de fondo basado en precisión
            if (ubicacion.getPrecision() <= 10) {
                // Muy buena precisión
                itemView.setBackgroundColor(
                    itemView.getContext().getResources().getColor(android.R.color.white)
                );
            } else if (ubicacion.getPrecision() <= 50) {
                // Buena precisión
                itemView.setBackgroundColor(
                    itemView.getContext().getResources().getColor(android.R.color.background_light)
                );
            } else {
                // Precisión regular
                itemView.setBackgroundColor(
                    itemView.getContext().getResources().getColor(android.R.color.darker_gray)
                );
            }
        }
    }
}