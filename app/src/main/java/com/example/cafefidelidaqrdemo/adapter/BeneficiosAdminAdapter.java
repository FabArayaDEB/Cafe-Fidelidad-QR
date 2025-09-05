package com.example.cafefidelidaqrdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.models.Beneficio;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para mostrar lista de beneficios en modo administrador
 * Permite acciones de editar, eliminar, activar/desactivar y ver detalles
 */
public class BeneficiosAdminAdapter extends RecyclerView.Adapter<BeneficiosAdminAdapter.BeneficioViewHolder> {
    
    private List<Beneficio> beneficios;
    private final OnBeneficioActionListener listener;
    private final SimpleDateFormat dateFormat;
    
    public interface OnBeneficioActionListener {
        void onEditBeneficio(Beneficio beneficio);
        void onDeleteBeneficio(Beneficio beneficio);
        void onToggleActiveBeneficio(Beneficio beneficio);
        void onViewBeneficioDetails(Beneficio beneficio);
        void onDuplicateBeneficio(Beneficio beneficio);
    }
    
    public BeneficiosAdminAdapter(List<Beneficio> beneficios, OnBeneficioActionListener listener) {
        this.beneficios = beneficios;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public BeneficioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_beneficio_admin, parent, false);
        return new BeneficioViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BeneficioViewHolder holder, int position) {
        Beneficio beneficio = beneficios.get(position);
        holder.bind(beneficio);
    }
    
    @Override
    public int getItemCount() {
        return beneficios != null ? beneficios.size() : 0;
    }
    
    public void updateBeneficios(List<Beneficio> newBeneficios) {
        this.beneficios = newBeneficios;
        notifyDataSetChanged();
    }
    
    class BeneficioViewHolder extends RecyclerView.ViewHolder {
        
        private final MaterialCardView cardView;
        private final TextView textNombre;
        private final TextView textDescripcion;
        private final TextView textTipo;
        private final TextView textValor;
        private final TextView textVigencia;
        private final TextView textSucursales;
        private final Chip chipEstado;
        private final ImageView iconTipo;
        private final MaterialButton btnEditar;
        private final MaterialButton btnEliminar;
        private final MaterialButton btnToggleActive;
        private final MaterialButton btnDetalles;
        private final MaterialButton btnDuplicar;
        
        public BeneficioViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.cardBeneficio);
            textNombre = itemView.findViewById(R.id.textNombre);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
            textTipo = itemView.findViewById(R.id.textTipo);
            textValor = itemView.findViewById(R.id.textValor);
            textVigencia = itemView.findViewById(R.id.textVigencia);
            textSucursales = itemView.findViewById(R.id.textSucursales);
            chipEstado = itemView.findViewById(R.id.chipEstado);
            iconTipo = itemView.findViewById(R.id.iconTipo);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            btnToggleActive = itemView.findViewById(R.id.btnToggleActive);
            btnDetalles = itemView.findViewById(R.id.btnDetalles);
            btnDuplicar = itemView.findViewById(R.id.btnDuplicar);
        }
        
        public void bind(Beneficio beneficio) {
            Context context = itemView.getContext();
            
            // Información básica
            textNombre.setText(beneficio.getNombre());
            textDescripcion.setText(beneficio.getDescripcion() != null ? 
                beneficio.getDescripcion() : "Sin descripción");
            
            // Tipo y valor
            setupTipoAndValue(beneficio, context);
            
            // Vigencia
            setupVigencia(beneficio);
            
            // Sucursales
            setupSucursales(beneficio);
            
            // Estado
            setupEstado(beneficio, context);
            
            // Configurar botones
            setupButtons(beneficio);
            
            // Configurar card según estado
            setupCardAppearance(beneficio, context);
        }
        
        private void setupTipoAndValue(Beneficio beneficio, Context context) {
            String tipoText = getTipoDisplayName(beneficio.getTipo());
            textTipo.setText(tipoText);
            
            // Configurar icono según tipo
            int iconRes = getTipoIcon(beneficio.getTipo());
            iconTipo.setImageResource(iconRes);
            
            // Configurar valor según tipo
            String valorText = getValorDisplayText(beneficio);
            textValor.setText(valorText);
        }
        
        private void setupVigencia(Beneficio beneficio) {
            if (beneficio.getFechaInicio() != null && beneficio.getFechaFin() != null) {
                String vigenciaText = String.format("Vigencia: %s - %s",
                    dateFormat.format(beneficio.getFechaInicio()),
                    dateFormat.format(beneficio.getFechaFin()));
                textVigencia.setText(vigenciaText);
                textVigencia.setVisibility(View.VISIBLE);
                
                // Verificar si está vigente
                Date now = new Date();
                if (now.before(beneficio.getFechaInicio()) || now.after(beneficio.getFechaFin())) {
                    textVigencia.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_error));
                } else {
                    textVigencia.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_success));
                }
            } else {
                textVigencia.setText("Sin vigencia definida");
                textVigencia.setVisibility(View.VISIBLE);
                textVigencia.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_warning));
            }
        }
        
        private void setupSucursales(Beneficio beneficio) {
            if (beneficio.getSucursalesAplicables() != null && !beneficio.getSucursalesAplicables().isEmpty()) {
                if (beneficio.getSucursalesAplicables().size() == 1) {
                    textSucursales.setText("Sucursal: " + beneficio.getSucursalesAplicables().get(0));
                } else {
                    textSucursales.setText(String.format("Sucursales: %d aplicables", 
                        beneficio.getSucursalesAplicables().size()));
                }
                textSucursales.setVisibility(View.VISIBLE);
            } else {
                textSucursales.setText("Todas las sucursales");
                textSucursales.setVisibility(View.VISIBLE);
            }
        }
        
        private void setupEstado(Beneficio beneficio, Context context) {
            if (beneficio.isActivo()) {
                chipEstado.setText("Activo");
                chipEstado.setChipBackgroundColorResource(R.color.color_success);
                chipEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            } else {
                chipEstado.setText("Inactivo");
                chipEstado.setChipBackgroundColorResource(R.color.color_error);
                chipEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            }
        }
        
        private void setupButtons(Beneficio beneficio) {
            btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditBeneficio(beneficio);
                }
            });
            
            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteBeneficio(beneficio);
                }
            });
            
            btnToggleActive.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleActiveBeneficio(beneficio);
                }
            });
            
            btnDetalles.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewBeneficioDetails(beneficio);
                }
            });
            
            btnDuplicar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDuplicateBeneficio(beneficio);
                }
            });
            
            // Configurar texto del botón toggle
            btnToggleActive.setText(beneficio.isActivo() ? "Desactivar" : "Activar");
            btnToggleActive.setIconResource(beneficio.isActivo() ? 
                R.drawable.ic_visibility_off : R.drawable.ic_visibility);
        }
        
        private void setupCardAppearance(Beneficio beneficio, Context context) {
            if (!beneficio.isActivo()) {
                // Beneficio inactivo - apariencia atenuada
                cardView.setAlpha(0.7f);
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_surface_variant));
            } else {
                // Beneficio activo - apariencia normal
                cardView.setAlpha(1.0f);
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_surface));
            }
            
            // Verificar si está vigente
            if (beneficio.getFechaInicio() != null && beneficio.getFechaFin() != null) {
                Date now = new Date();
                if (now.before(beneficio.getFechaInicio()) || now.after(beneficio.getFechaFin())) {
                    // No vigente - borde rojo
                    cardView.setStrokeColor(ContextCompat.getColor(context, R.color.color_error));
                    cardView.setStrokeWidth(2);
                } else {
                    // Vigente - sin borde especial
                    cardView.setStrokeWidth(0);
                }
            }
        }
        
        private String getTipoDisplayName(Beneficio.TipoBeneficio tipo) {
            switch (tipo) {
                case DESCUENTO_PORCENTAJE:
                    return "Descuento %";
                case DESCUENTO_MONTO:
                    return "Descuento Fijo";
                case DOS_POR_UNO:
                    return "2x1";
                case PRODUCTO_GRATIS:
                    return "Producto Gratis";
                default:
                    return "Desconocido";
            }
        }
        
        private int getTipoIcon(Beneficio.TipoBeneficio tipo) {
            switch (tipo) {
                case DESCUENTO_PORCENTAJE:
                    return R.drawable.ic_percent;
                case DESCUENTO_MONTO:
                    return R.drawable.ic_money_off;
                case DOS_POR_UNO:
                    return R.drawable.ic_looks_two;
                case PRODUCTO_GRATIS:
                    return R.drawable.ic_card_giftcard;
                default:
                    return R.drawable.ic_help;
            }
        }
        
        private String getValorDisplayText(Beneficio beneficio) {
            switch (beneficio.getTipo()) {
                case DESCUENTO_PORCENTAJE:
                    return String.format("%.0f%%", beneficio.getValor());
                case DESCUENTO_MONTO:
                    return String.format("$%.2f", beneficio.getValor());
                case DOS_POR_UNO:
                    return "2x1";
                case PRODUCTO_GRATIS:
                    return "Gratis";
                default:
                    return String.valueOf(beneficio.getValor());
            }
        }
    }
}