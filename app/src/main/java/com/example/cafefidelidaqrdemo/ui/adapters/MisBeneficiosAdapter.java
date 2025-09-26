package com.example.cafefidelidaqrdemo.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.example.cafefidelidaqrdemo.database.models.Beneficio;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para mostrar beneficios disponibles para canje
 */
public class MisBeneficiosAdapter extends RecyclerView.Adapter<MisBeneficiosAdapter.BeneficioViewHolder> {
    
    private List<Beneficio> beneficios;
    private OnBeneficioClickListener listener;
    private SimpleDateFormat dateFormat;
    
    public interface OnBeneficioClickListener {
        void onBeneficioClick(Beneficio beneficio);
    }
    
    public MisBeneficiosAdapter(List<Beneficio> beneficios, OnBeneficioClickListener listener) {
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
    
    public void updateBeneficios(List<Beneficio> nuevosBeneficios) {
        this.beneficios = nuevosBeneficios;
        notifyDataSetChanged();
    }
    
    class BeneficioViewHolder extends RecyclerView.ViewHolder {
        
        private MaterialCardView cardView;
        private TextView textNombre;
        private Chip chipTipo;
        private TextView textDescripcion;
        private TextView textValor;
        private TextView textVigencia;
        private MaterialButton btnUsar;
        private ImageView iconBeneficio;
        
        public BeneficioViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.cardBeneficio);
            textNombre = itemView.findViewById(R.id.textNombre);
            chipTipo = itemView.findViewById(R.id.chipTipo);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
            textValor = itemView.findViewById(R.id.textValor);
            textVigencia = itemView.findViewById(R.id.textVigencia);
            btnUsar = itemView.findViewById(R.id.btnUsar);
            iconBeneficio = itemView.findViewById(R.id.iconBeneficio);
        }
        
        public void bind(Beneficio beneficio) {
            // Nombre del beneficio
            textNombre.setText(beneficio.getNombre());
            
            // Tipo de beneficio
            chipTipo.setText(formatearTipo(beneficio.getTipo()));
            
            // Descripción - usar regla como descripción
            if (beneficio.getRegla() != null && !beneficio.getRegla().isEmpty()) {
                textDescripcion.setText(beneficio.getRegla());
                textDescripcion.setVisibility(View.VISIBLE);
            } else {
                textDescripcion.setVisibility(View.GONE);
            }
            
            // Valor del descuento
            String valor = formatearValor(beneficio);
            textValor.setText(valor);
            
            // Vigencia
            String vigencia = formatearVigencia(beneficio);
            textVigencia.setText(vigencia);
            
            // Icono según tipo
            configurarIcono(beneficio);
            
            // Estado del beneficio
            configurarEstado(beneficio);
            
            // Click listener para usar beneficio
            btnUsar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBeneficioClick(beneficio);
                }
            });
            
            // Click listener para la card completa
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBeneficioClick(beneficio);
                }
            });
        }
        
        private String formatearTipo(String tipo) {
            if (tipo == null) return "Beneficio";
            
            switch (tipo.toUpperCase()) {
                case "DESCUENTO_PORCENTAJE":
                    return "Descuento %";
                case "DESCUENTO_MONTO":
                    return "Descuento $";
                case "2X1":
                    return "2x1";
                case "PREMIO":
                    return "Premio";
                case "PRODUCTO_GRATIS":
                    return "Producto Gratis";
                default:
                    return tipo;
            }
        }
        
        private String formatearValor(Beneficio beneficio) {
            if (beneficio.getDescuento_pct() > 0) {
                return beneficio.getDescuento_pct() + "% OFF";
            } else if (beneficio.getDescuento_monto() > 0) {
                return "$" + String.format("%.0f", beneficio.getDescuento_monto()) + " OFF";
            } else if ("2X1".equals(beneficio.getTipo())) {
                return "2x1";
            } else if ("PREMIO".equals(beneficio.getTipo()) || "PRODUCTO_GRATIS".equals(beneficio.getTipo())) {
                return "GRATIS";
            }
            return "BENEFICIO";
        }
        
        private void configurarIcono(Beneficio beneficio) {
            int iconRes = R.drawable.ic_redeem; // icono por defecto
            
            if (beneficio.getTipo() != null) {
                switch (beneficio.getTipo().toUpperCase()) {
                    case "DESCUENTO_PORCENTAJE":
                    case "DESCUENTO_MONTO":
                        iconRes = R.drawable.ic_discount;
                        break;
                    case "2X1":
                        iconRes = R.drawable.ic_redeem;
                        break;
                    case "PREMIO":
                    case "PRODUCTO_GRATIS":
                        iconRes = R.drawable.ic_celebration;
                        break;
                }
            }
            
            iconBeneficio.setImageResource(iconRes);
        }
        
        private String formatearVigencia(Beneficio beneficio) {
            if (beneficio.getVigencia_fin() > 0) {
                Date fechaFin = new Date(beneficio.getVigencia_fin());
                Date hoy = new Date();
                
                long diasRestantes = (beneficio.getVigencia_fin() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
                
                if (diasRestantes < 0) {
                    return "Expirado";
                } else if (diasRestantes == 0) {
                    return "Expira hoy";
                } else if (diasRestantes <= 7) {
                    return "Expira en " + diasRestantes + " días";
                } else {
                    return "Válido hasta " + dateFormat.format(fechaFin);
                }
            }
            return "Sin vencimiento";
        }
        
        private void configurarEstado(Beneficio beneficio) {
            if (!beneficio.isActivo()) {
                btnUsar.setEnabled(false);
                btnUsar.setText("NO DISPONIBLE");
                btnUsar.setBackgroundTintList(itemView.getContext().getColorStateList(android.R.color.darker_gray));
                cardView.setAlpha(0.6f);
            } else if (beneficio.getVigencia_fin() > 0 && beneficio.getVigencia_fin() < System.currentTimeMillis()) {
                btnUsar.setEnabled(false);
                btnUsar.setText("EXPIRADO");
                btnUsar.setBackgroundTintList(itemView.getContext().getColorStateList(android.R.color.holo_red_dark));
                cardView.setAlpha(0.6f);
            } else {
                // Beneficio disponible
                btnUsar.setEnabled(true);
                btnUsar.setText("USAR BENEFICIO");
                btnUsar.setBackgroundTintList(itemView.getContext().getColorStateList(R.color.color_success));
                cardView.setAlpha(1.0f);
                
                // Verificar si está próximo a vencer
                if (beneficio.getVigencia_fin() > 0) {
                    long diasRestantes = (beneficio.getVigencia_fin() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
                    if (diasRestantes <= 3) {
                        // Cambiar color a warning si está próximo a vencer
                        btnUsar.setBackgroundTintList(itemView.getContext().getColorStateList(R.color.color_warning));
                    }
                }
            }
        }
    }
}