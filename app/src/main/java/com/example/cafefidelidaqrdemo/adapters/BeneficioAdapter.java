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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.models.Beneficio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BeneficioAdapter extends RecyclerView.Adapter<BeneficioAdapter.BeneficioViewHolder> {
    
    private final Context context;
    private List<Beneficio> beneficios;
    private final OnBeneficioClickListener listener;
    private final SimpleDateFormat dateFormat;

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
        holder.tvDescripcion.setText(beneficio.getDescripcion());
        
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
    
    private void configurarIconoBeneficio(ImageView iconView, String tipo) {
        int iconResource;
        String safeTipo = tipo != null ? tipo : "beneficio";
        switch (safeTipo) {
            case "descuento":
                iconResource = R.drawable.ic_coffee_placeholder;
                break;
            case "dos_por_uno":
                iconResource = R.drawable.ic_coffee_placeholder;
                break;
            default:
                iconResource = R.drawable.ic_coffee_placeholder;
                break;
        }
        iconView.setImageResource(iconResource);
    }

    private void configurarValorBeneficio(TextView valorView, Beneficio beneficio) {
        String tipo = beneficio.getTipo();
        String valor;
        if (tipo == null) {
            valor = "Beneficio";
        } else {
            switch (tipo) {
                case "descuento":
                    valor = "Descuento $";
                    break;
                case "dos_por_uno":
                    valor = "2x1";
                    break;
                default:
                    valor = "Beneficio";
                    break;
            }
        }
        valorView.setText(valor);
    }

    private void configurarFechas(BeneficioViewHolder holder, Beneficio beneficio) {
        if (beneficio.getFechaFinVigencia() != null) {
            holder.tvVencimiento.setText("Vence: " + dateFormat.format(beneficio.getFechaFinVigencia()));
            holder.tvVencimiento.setVisibility(View.VISIBLE);
            long diasRestantes = (beneficio.getFechaFinVigencia().getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24);
            if (diasRestantes <= 3 && diasRestantes > 0) {
                holder.tvVencimiento.setTextColor(ContextCompat.getColor(context, R.color.warm_orange));
                holder.tvVencimiento.setText("Vence pronto: " + dateFormat.format(beneficio.getFechaFinVigencia()));
            } else if (diasRestantes <= 0) {
                holder.tvVencimiento.setTextColor(ContextCompat.getColor(context, R.color.error_red));
                holder.tvVencimiento.setText("Expirado");
            }
        } else {
            holder.tvVencimiento.setVisibility(View.GONE);
        }
        if (beneficio.getCantidadMaximaUsos() > 0) {
            int usosRestantes = beneficio.getCantidadMaximaUsos() - beneficio.getCantidadUsosActuales();
            holder.tvUsos.setText(context.getString(R.string.usos_restantes, usosRestantes));
            holder.tvUsos.setVisibility(View.VISIBLE);
        } else {
            holder.tvUsos.setVisibility(View.GONE);
        }
    }

    private void configurarEstadoVisual(BeneficioViewHolder holder, Beneficio beneficio) {
        if ("disponible".equals(beneficio.getEstado())) {
            // if (beneficio.isVigente()) { // Método no existe en models.Beneficio
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background));
            holder.tvEstado.setVisibility(View.GONE);
            holder.cardView.setAlpha(1.0f);
            // } else {
            //     holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background));
            //     holder.tvEstado.setText("Expirado");
            //     holder.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.error_red));
            //     holder.tvEstado.setVisibility(View.VISIBLE);
            //     holder.cardView.setAlpha(0.6f);
            // }
        } else if ("usado".equals(beneficio.getEstado())) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background));
            holder.tvEstado.setText("Usado");
            holder.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.success_green));
            holder.tvEstado.setVisibility(View.VISIBLE);
            holder.cardView.setAlpha(0.7f);
            // if (beneficio.getFechaFin() != null) { // Método getFechaFin no existe
            //     holder.tvVencimiento.setText(context.getString(R.string.usado_el, dateFormat.format(beneficio.getFechaFin()))); // Método getFechaFin no existe
            // }
            holder.tvVencimiento.setText("Usado");
            holder.tvVencimiento.setTextColor(ContextCompat.getColor(context, R.color.success_green));
            holder.tvVencimiento.setVisibility(View.VISIBLE);
        } else if ("expirado".equals(beneficio.getEstado())) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background));
            holder.tvEstado.setText("Expirado");
            holder.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.error_red));
            holder.tvEstado.setVisibility(View.VISIBLE);
            holder.cardView.setAlpha(0.5f);
        } else if ("bloqueado".equals(beneficio.getEstado())) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background));
            holder.tvEstado.setText("Bloqueado");
            holder.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.error_red));
            holder.tvEstado.setVisibility(View.VISIBLE);
            holder.cardView.setAlpha(0.5f);
        } else if ("pendiente_activacion".equals(beneficio.getEstado())) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background));
            holder.tvEstado.setText("Pendiente");
            holder.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.warm_orange));
            holder.tvEstado.setVisibility(View.VISIBLE);
            holder.cardView.setAlpha(0.8f);
        }
    }

    private void configurarBotones(BeneficioViewHolder holder, Beneficio beneficio) {
        String estado = beneficio.getEstado();
        if (estado == null) {
            estado = beneficio.isActivo() ? "disponible" : "expirado";
        }
        switch (estado) {
            case "disponible":
                // if (beneficio.isVigente()) { // Método no existe
                holder.btnUsar.setVisibility(View.VISIBLE);
                holder.btnUsar.setText(context.getString(R.string.usar_beneficio));
                holder.btnUsar.setBackgroundColor(ContextCompat.getColor(context, R.color.coffee_primary));
                holder.btnUsar.setEnabled(true);
                // } else {
                //     holder.btnUsar.setVisibility(View.GONE);
                // }
                break;
            case "usado":
            case "expirado":
            case "bloqueado":
                holder.btnUsar.setVisibility(View.GONE);
                break;
            case "pendiente_activacion":
                holder.btnUsar.setVisibility(View.VISIBLE);
                holder.btnUsar.setText("Pendiente");
                holder.btnUsar.setBackgroundColor(ContextCompat.getColor(context, R.color.warm_orange));
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