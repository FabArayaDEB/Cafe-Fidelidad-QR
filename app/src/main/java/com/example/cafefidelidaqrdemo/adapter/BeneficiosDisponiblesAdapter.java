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
 * Adapter para mostrar beneficios disponibles del cliente
 * Diseñado para mostrar beneficios que el cliente puede usar
 */
public class BeneficiosDisponiblesAdapter extends RecyclerView.Adapter<BeneficiosDisponiblesAdapter.BeneficioViewHolder> {
    
    private List<Beneficio> beneficios;
    private final OnBeneficioClickListener listener;
    private final SimpleDateFormat dateFormat;
    
    public interface OnBeneficioClickListener {
        void onBeneficioClick(Beneficio beneficio);
    }
    
    public BeneficiosDisponiblesAdapter(List<Beneficio> beneficios, OnBeneficioClickListener listener) {
        this.beneficios = beneficios;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public BeneficioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_beneficio_disponible, parent, false);
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
        private final ImageView iconBeneficio;
        private final TextView textNombre;
        private final TextView textDescripcion;
        private final TextView textValor;
        private final TextView textVigencia;
        private final Chip chipTipo;
        private final MaterialButton btnUsar;
        private final View indicadorDisponible;
        
        public BeneficioViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.cardBeneficio);
            iconBeneficio = itemView.findViewById(R.id.iconBeneficio);
            textNombre = itemView.findViewById(R.id.textNombre);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
            textValor = itemView.findViewById(R.id.textValor);
            textVigencia = itemView.findViewById(R.id.textVigencia);
            chipTipo = itemView.findViewById(R.id.chipTipo);
            btnUsar = itemView.findViewById(R.id.btnUsar);
            indicadorDisponible = itemView.findViewById(R.id.indicadorDisponible);
        }
        
        public void bind(Beneficio beneficio) {
            Context context = itemView.getContext();
            
            // Información básica
            textNombre.setText(beneficio.getNombre());
            textDescripcion.setText(beneficio.getDescripcion() != null ? 
                beneficio.getDescripcion() : "Beneficio disponible para usar");
            
            // Configurar tipo y valor
            setupTipoAndValue(beneficio, context);
            
            // Configurar vigencia
            setupVigencia(beneficio, context);
            
            // Configurar apariencia de disponible
            setupAvailableAppearance(context);
            
            // Configurar click listeners
            setupClickListeners(beneficio);
        }
        
        private void setupTipoAndValue(Beneficio beneficio, Context context) {
            // Configurar chip de tipo
            String tipoText = getTipoDisplayName(beneficio.getTipo());
            chipTipo.setText(tipoText);
            chipTipo.setChipBackgroundColorResource(getTipoColor(beneficio.getTipo()));
            chipTipo.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            
            // Configurar icono
            int iconRes = getTipoIcon(beneficio.getTipo());
            iconBeneficio.setImageResource(iconRes);
            iconBeneficio.setColorFilter(ContextCompat.getColor(context, getTipoColor(beneficio.getTipo())));
            
            // Configurar valor
            String valorText = getValorDisplayText(beneficio);
            textValor.setText(valorText);
            textValor.setTextColor(ContextCompat.getColor(context, getTipoColor(beneficio.getTipo())));
        }
        
        private void setupVigencia(Beneficio beneficio, Context context) {
            if (beneficio.getFechaFin() != null) {
                Date now = new Date();
                long diasRestantes = (beneficio.getFechaFin().getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
                
                if (diasRestantes > 7) {
                    textVigencia.setText(String.format("Válido hasta %s", dateFormat.format(beneficio.getFechaFin())));
                    textVigencia.setTextColor(ContextCompat.getColor(context, R.color.color_on_surface_variant));
                } else if (diasRestantes > 0) {
                    textVigencia.setText(String.format("¡Expira en %d días!", diasRestantes));
                    textVigencia.setTextColor(ContextCompat.getColor(context, R.color.color_warning));
                } else {
                    textVigencia.setText("¡Expira hoy!");
                    textVigencia.setTextColor(ContextCompat.getColor(context, R.color.color_error));
                }
                textVigencia.setVisibility(View.VISIBLE);
            } else {
                textVigencia.setVisibility(View.GONE);
            }
        }
        
        private void setupAvailableAppearance(Context context) {
            // Card con apariencia destacada para beneficios disponibles
            cardView.setCardElevation(8f);
            cardView.setStrokeColor(ContextCompat.getColor(context, R.color.color_primary));
            cardView.setStrokeWidth(2);
            
            // Indicador visual de disponible
            indicadorDisponible.setVisibility(View.VISIBLE);
            indicadorDisponible.setBackgroundColor(ContextCompat.getColor(context, R.color.color_success));
            
            // Botón de usar destacado
            btnUsar.setBackgroundColor(ContextCompat.getColor(context, R.color.color_primary));
            btnUsar.setTextColor(ContextCompat.getColor(context, R.color.color_on_primary));
        }
        
        private void setupClickListeners(Beneficio beneficio) {
            // Click en toda la card
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBeneficioClick(beneficio);
                }
            });
            
            // Click en botón usar
            btnUsar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBeneficioClick(beneficio);
                }
            });
        }
        
        private String getTipoDisplayName(Beneficio.TipoBeneficio tipo) {
            switch (tipo) {
                case DESCUENTO_PORCENTAJE:
                    return "Descuento";
                case DESCUENTO_MONTO:
                    return "Descuento";
                case DOS_POR_UNO:
                    return "2x1";
                case PRODUCTO_GRATIS:
                    return "Gratis";
                default:
                    return "Beneficio";
            }
        }
        
        private int getTipoIcon(Beneficio.TipoBeneficio tipo) {
            switch (tipo) {
                case DESCUENTO_PORCENTAJE:
                    return R.drawable.ic_discount;
                case DESCUENTO_MONTO:
                    return R.drawable.ic_discount;
                case DOS_POR_UNO:
                    return R.drawable.ic_redeem;
                case PRODUCTO_GRATIS:
                    return R.drawable.ic_redeem;
                default:
                    return R.drawable.ic_redeem;
            }
        }
        
        private int getTipoColor(Beneficio.TipoBeneficio tipo) {
            switch (tipo) {
                case DESCUENTO_PORCENTAJE:
                    return R.color.color_primary;
                case DESCUENTO_MONTO:
                    return R.color.color_secondary;
                case DOS_POR_UNO:
                    return R.color.color_tertiary;
                case PRODUCTO_GRATIS:
                    return R.color.color_success;
                default:
                    return R.color.color_primary;
            }
        }
        
        private String getValorDisplayText(Beneficio beneficio) {
            switch (beneficio.getTipo()) {
                case DESCUENTO_PORCENTAJE:
                    return String.format("%.0f%% OFF", beneficio.getValor());
                case DESCUENTO_MONTO:
                    return String.format("$%.0f OFF", beneficio.getValor());
                case DOS_POR_UNO:
                    return "¡2x1!";
                case PRODUCTO_GRATIS:
                    return "¡GRATIS!";
                default:
                    return "¡Disponible!";
            }
        }
    }
}