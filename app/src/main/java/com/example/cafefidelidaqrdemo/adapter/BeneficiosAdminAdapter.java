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
import com.example.cafefidelidaqrdemo.model.Beneficio;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.materialswitch.MaterialSwitch;

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
        void onToggleActiveBeneficio(Beneficio beneficio);
        void onEditarBeneficio(Beneficio beneficio);
        void onEliminarBeneficio(Beneficio beneficio);
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
        private final Chip chipTipo;
        private final TextView textVigencia;
        private final TextView textUsos;
        private final TextView textClientesBeneficiados;
        private final MaterialSwitch switchActivo;
        private final MaterialButton btnEditar;
        private final MaterialButton btnEliminar;
        
        public BeneficioViewHolder(@NonNull View itemView) {
            super(itemView);
            this.cardView = itemView.findViewById(R.id.cardView);
            textNombre = itemView.findViewById(R.id.textNombre);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
            chipTipo = itemView.findViewById(R.id.chipTipo);
            textVigencia = itemView.findViewById(R.id.textVigencia);
            textUsos = itemView.findViewById(R.id.textContadorBeneficios); // Cambiado de textUsos
            textClientesBeneficiados = itemView.findViewById(R.id.textClientesBeneficiados);
            switchActivo = itemView.findViewById(R.id.switchActivo);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
        
        public void bind(Beneficio beneficio) {
            Context context = itemView.getContext();
            
            // Información básica
            textNombre.setText(beneficio.getNombre());
            textDescripcion.setText(beneficio.getDescripcion() != null ? 
                beneficio.getDescripcion() : "Sin descripción");
            
            // Tipo
            setupTipo(beneficio);
            
            // Vigencia
            setupVigencia(beneficio);
            
            // Estado activo/inactivo
            setupEstadoSwitch(beneficio);
            
            // Configurar botones
            setupButtons(beneficio);
            
            // Estadísticas
            setupEstadisticas(beneficio);
        }
        
        private void setupTipo(Beneficio beneficio) {
            String tipoText = getTipoDisplayName(beneficio.getTipo());
            chipTipo.setText(tipoText);
        }
        
        private void setupVigencia(Beneficio beneficio) {
            if (beneficio.getFechaInicioVigencia() != null && beneficio.getFechaFinVigencia() != null) {
                String vigenciaText = String.format("Vigencia: %s - %s",
                    dateFormat.format(beneficio.getFechaInicioVigencia()),
                    dateFormat.format(beneficio.getFechaFinVigencia()));
                textVigencia.setText(vigenciaText);
                textVigencia.setVisibility(View.VISIBLE);
                
                // Verificar si está vigente
                Date now = new Date();
                if (now.before(beneficio.getFechaInicioVigencia()) || now.after(beneficio.getFechaFinVigencia())) {
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
        
        private void setupEstadoSwitch(Beneficio beneficio) {
            switchActivo.setChecked(beneficio.isActivo());
            switchActivo.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleActiveBeneficio(beneficio);
                }
            });
        }
        
        private void setupEstadisticas(Beneficio beneficio) {
            // Mostrar estadísticas de uso
            int usos = beneficio.getCantidadUsosActuales();
            textUsos.setText(String.format("Usos: %d", usos));
            
            // Mostrar clientes beneficiados si está disponible
            int clientesBeneficiados = beneficio.getVecesCanjeado();
            textClientesBeneficiados.setText(String.format("Clientes: %d", clientesBeneficiados));
            textClientesBeneficiados.setVisibility(View.VISIBLE);
        }
        
        private void setupButtons(Beneficio beneficio) {
            btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditarBeneficio(beneficio);
                }
            });
            
            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEliminarBeneficio(beneficio);
                }
            });
        }
        

        
        private String getTipoDisplayName(Beneficio.TipoBeneficio tipo) {
            switch (tipo) {
                case DESCUENTO_PORCENTAJE:
                    return "Descuento %";
                case DESCUENTO_MONTO:
                    return "Descuento Fijo";
                case DOS_POR_UNO:
                    return "2x1";
                case PREMIO:
                    return "Premio";
                default:
                    return "Desconocido";
            }
        }
        

    }
}