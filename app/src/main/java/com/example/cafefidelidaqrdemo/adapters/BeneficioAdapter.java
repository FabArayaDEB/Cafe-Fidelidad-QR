package com.example.cafefidelidaqrdemo.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.models.Beneficio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BeneficioAdapter extends RecyclerView.Adapter<BeneficioAdapter.BeneficioViewHolder> {
    
    private Context context;
    private List<Beneficio> beneficios;
    private OnBeneficioClickListener listener;
    private SimpleDateFormat dateFormat;
    
    public interface OnBeneficioClickListener {
        void onBeneficioClick(Beneficio beneficio);
        void onUsarBeneficioClick(Beneficio beneficio);
    }
    
    public BeneficioAdapter(Context context, List<Beneficio> beneficios, OnBeneficioClickListener listener) {
        this.context = context;
        this.beneficios = beneficios;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public BeneficioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_beneficio, parent, false);
        return new BeneficioViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BeneficioViewHolder holder, int position) {
        Beneficio beneficio = beneficios.get(position);
        
        // Información básica
        holder.tvNombre.setText(beneficio.getNombre());
        holder.tvDescripcion.setText(beneficio.getDescripcionCompleta());
        
        // Configurar icono según tipo de beneficio
        configurarIconoBeneficio(holder.ivIcono, beneficio.getTipo());
        
        // Configurar valor del beneficio
        configurarValorBeneficio(holder.tvValor, beneficio);
        
        // Configurar fechas
        configurarFechas(holder, beneficio);
        
        // Configurar estado visual
        configurarEstadoVisual(holder, beneficio);
        
        // Configurar botones
        configurarBotones(holder, beneficio);
        
        // Configurar click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBeneficioClick(beneficio);
            }
        });
        
        holder.btnUsar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUsarBeneficioClick(beneficio);
            }
        });
    }
    
    private void configurarIconoBeneficio(ImageView iconView, Beneficio.TipoBeneficio tipo) {
        int iconResource;
        
        switch (tipo) {
            case DESCUENTO_PORCENTAJE:
            case DESCUENTO_FIJO:
                iconResource = R.drawable.ic_discount;
                break;
            case PRODUCTO_GRATIS:
                iconResource = R.drawable.ic_coffee_placeholder;
                break;
            case DOS_POR_UNO:
                iconResource = R.drawable.ic_two_for_one;
                break;
            case PUNTOS_EXTRA:
                iconResource = R.drawable.ic_star;
                break;
            case ENVIO_GRATIS:
                iconResource = R.drawable.ic_delivery;
                break;
            case UPGRADE_PRODUCTO:
                iconResource = R.drawable.ic_upgrade;
                break;
            default:
                iconResource = R.drawable.ic_star;
                break;
        }
        
        iconView.setImageResource(iconResource);
    }
    
    private void configurarValorBeneficio(TextView valorView, Beneficio beneficio) {
        String valor = "";
        
        switch (beneficio.getTipo()) {
            case DESCUENTO_PORCENTAJE:
                valor = "-" + (int)beneficio.getValorDescuentoPorcentaje() + "%";
                break;
            case DESCUENTO_FIJO:
                valor = "-$" + String.format(Locale.getDefault(), "%.0f", beneficio.getValorDescuentoFijo());
                break;
            case PRODUCTO_GRATIS:
                valor = "GRATIS";
                break;
            case DOS_POR_UNO:
                valor = "2x1";
                break;
            case PUNTOS_EXTRA:
                valor = "+PUNTOS";
                break;
            case ENVIO_GRATIS:
                valor = "ENVÍO GRATIS";
                break;
            case UPGRADE_PRODUCTO:
                valor = "UPGRADE";
                break;
        }
        
        valorView.setText(valor);
    }
    
    private void configurarFechas(BeneficioViewHolder holder, Beneficio beneficio) {
        // Fecha de vencimiento
        if (beneficio.getFechaFinVigencia() != null) {
            holder.tvVencimiento.setText("Vence: " + dateFormat.format(beneficio.getFechaFinVigencia()));
            holder.tvVencimiento.setVisibility(View.VISIBLE);
            
            // Verificar si está próximo a vencer (3 días)
            long diasRestantes = (beneficio.getFechaFinVigencia().getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24);
            if (diasRestantes <= 3 && diasRestantes > 0) {
                holder.tvVencimiento.setTextColor(context.getResources().getColor(R.color.warm_orange));
                holder.tvVencimiento.setText("¡Vence pronto! " + dateFormat.format(beneficio.getFechaFinVigencia()));
            } else if (diasRestantes <= 0) {
                holder.tvVencimiento.setTextColor(context.getResources().getColor(R.color.error_red));
                holder.tvVencimiento.setText("Expirado");
            }
        } else {
            holder.tvVencimiento.setVisibility(View.GONE);
        }
        
        // Usos restantes
        if (beneficio.getCantidadMaximaUsos() > 0) {
            int usosRestantes = beneficio.getCantidadMaximaUsos() - beneficio.getCantidadUsosActuales();
            holder.tvUsos.setText("Usos restantes: " + usosRestantes);
            holder.tvUsos.setVisibility(View.VISIBLE);
        } else {
            holder.tvUsos.setVisibility(View.GONE);
        }
    }
    
    private void configurarEstadoVisual(BeneficioViewHolder holder, Beneficio beneficio) {
        switch (beneficio.getEstado()) {
            case DISPONIBLE:
                if (beneficio.esValido()) {
                    holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.card_background));
                    holder.tvEstado.setVisibility(View.GONE);
                    holder.cardView.setAlpha(1.0f);
                } else {
                    // Expirado pero aún marcado como disponible
                    holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.card_background));
                    holder.tvEstado.setText("EXPIRADO");
                    holder.tvEstado.setTextColor(context.getResources().getColor(R.color.error_red));
                    holder.tvEstado.setVisibility(View.VISIBLE);
                    holder.cardView.setAlpha(0.6f);
                }
                break;
                
            case USADO:
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.card_background));
                holder.tvEstado.setText("USADO");
                holder.tvEstado.setTextColor(context.getResources().getColor(R.color.success_green));
                holder.tvEstado.setVisibility(View.VISIBLE);
                holder.cardView.setAlpha(0.7f);
                
                // Mostrar fecha de uso
                if (beneficio.getFechaUltimoCanje() != null) {
                    holder.tvVencimiento.setText("Usado el: " + dateFormat.format(beneficio.getFechaUltimoCanje()));
                    holder.tvVencimiento.setTextColor(context.getResources().getColor(R.color.success_green));
                    holder.tvVencimiento.setVisibility(View.VISIBLE);
                }
                break;
                
            case EXPIRADO:
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.card_background));
                holder.tvEstado.setText("EXPIRADO");
                holder.tvEstado.setTextColor(context.getResources().getColor(R.color.error_red));
                holder.tvEstado.setVisibility(View.VISIBLE);
                holder.cardView.setAlpha(0.5f);
                break;
                
            case BLOQUEADO:
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.card_background));
                holder.tvEstado.setText("BLOQUEADO");
                holder.tvEstado.setTextColor(context.getResources().getColor(R.color.error_red));
                holder.tvEstado.setVisibility(View.VISIBLE);
                holder.cardView.setAlpha(0.5f);
                break;
                
            case PENDIENTE_ACTIVACION:
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.card_background));
                holder.tvEstado.setText("PENDIENTE");
                holder.tvEstado.setTextColor(context.getResources().getColor(R.color.warm_orange));
                holder.tvEstado.setVisibility(View.VISIBLE);
                holder.cardView.setAlpha(0.8f);
                break;
        }
    }
    
    private void configurarBotones(BeneficioViewHolder holder, Beneficio beneficio) {
        switch (beneficio.getEstado()) {
            case DISPONIBLE:
                if (beneficio.esValido()) {
                    holder.btnUsar.setVisibility(View.VISIBLE);
                    holder.btnUsar.setText("Usar Beneficio");
                    holder.btnUsar.setBackgroundColor(context.getResources().getColor(R.color.coffee_primary));
                    holder.btnUsar.setEnabled(true);
                } else {
                    holder.btnUsar.setVisibility(View.GONE);
                }
                break;
                
            case USADO:
            case EXPIRADO:
            case BLOQUEADO:
                holder.btnUsar.setVisibility(View.GONE);
                break;
                
            case PENDIENTE_ACTIVACION:
                holder.btnUsar.setVisibility(View.VISIBLE);
                holder.btnUsar.setText("Pendiente");
                holder.btnUsar.setBackgroundColor(context.getResources().getColor(R.color.warm_orange));
                holder.btnUsar.setEnabled(false);
                break;
        }
    }
    
    @Override
    public int getItemCount() {
        return beneficios.size();
    }
    
    public void updateBeneficios(List<Beneficio> nuevosBeneficios) {
        this.beneficios = nuevosBeneficios;
        notifyDataSetChanged();
    }
    
    public static class BeneficioViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivIcono;
        TextView tvNombre;
        TextView tvDescripcion;
        TextView tvValor;
        TextView tvVencimiento;
        TextView tvUsos;
        TextView tvEstado;
        Button btnUsar;
        
        public BeneficioViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.cardView);
            ivIcono = itemView.findViewById(R.id.iv_icono_beneficio);
            tvNombre = itemView.findViewById(R.id.tv_nombre_beneficio);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion_beneficio);
            tvValor = itemView.findViewById(R.id.tv_valor_beneficio);
            tvVencimiento = itemView.findViewById(R.id.tv_vencimiento);
            tvUsos = itemView.findViewById(R.id.tv_usos_restantes);
            tvEstado = itemView.findViewById(R.id.tv_estado_beneficio);
            btnUsar = itemView.findViewById(R.id.btn_usar_beneficio);
        }
    }
}