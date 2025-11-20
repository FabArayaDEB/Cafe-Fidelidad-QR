package com.example.cafefidelidaqrdemo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.models.Cliente;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para mostrar la lista de clientes en el panel de administración
 */
public class ClientesAdminAdapter extends RecyclerView.Adapter<ClientesAdminAdapter.ClienteViewHolder> {
    
    private List<Cliente> clientes;
    private OnClienteActionListener listener;
    
    public interface OnClienteActionListener {
        void onClienteClick(Cliente cliente);
        void onEditarClick(Cliente cliente);
        void onToggleEstadoClick(Cliente cliente);
        void onEliminarClick(Cliente cliente);
    }
    
    public ClientesAdminAdapter(List<Cliente> clientes, OnClienteActionListener listener) {
        this.clientes = clientes;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cliente_admin, parent, false);
        return new ClienteViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
        Cliente cliente = clientes.get(position);
        holder.bind(cliente);
    }
    
    @Override
    public int getItemCount() {
        return clientes.size();
    }
    
    public void updateClientes(List<Cliente> nuevosClientes) {
        this.clientes = nuevosClientes;
        notifyDataSetChanged();
    }
    
    class ClienteViewHolder extends RecyclerView.ViewHolder {
        
        private MaterialCardView cardView;
        private ImageView iconCliente;
        private TextView textNombre;
        private TextView textEmail;
        private TextView textTelefono;
        private TextView textPuntos;
        private TextView textEstado;
        private TextView textFechaCreacion;
        private MaterialButton btnEditar;
        private MaterialButton btnToggleEstado;
        private MaterialButton btnEliminar;
        
        public ClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.cardCliente);
            iconCliente = itemView.findViewById(R.id.iconCliente);
            textNombre = itemView.findViewById(R.id.textNombre);
            textEmail = itemView.findViewById(R.id.textEmail);
            textTelefono = itemView.findViewById(R.id.textTelefono);
            textPuntos = itemView.findViewById(R.id.textPuntos);
            textEstado = itemView.findViewById(R.id.textEstado);
            textFechaCreacion = itemView.findViewById(R.id.textFechaCreacion);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnToggleEstado = itemView.findViewById(R.id.btnToggleEstado);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
        
        public void bind(Cliente cliente) {
            // Información básica
            textNombre.setText(cliente.getNombre());
            textEmail.setText(cliente.getEmail());
            textTelefono.setText(cliente.getTelefono());
            textPuntos.setText(String.format("%d sellos", cliente.getPuntosAcumulados()));
            
            // Estado
            if (cliente.isActivo()) {
                textEstado.setText("Activo");
                textEstado.setTextColor(itemView.getContext().getColor(R.color.color_success));
                iconCliente.setImageResource(R.drawable.ic_person);
                iconCliente.setColorFilter(itemView.getContext().getColor(R.color.color_success));
            } else {
                textEstado.setText("Inactivo");
                textEstado.setTextColor(itemView.getContext().getColor(R.color.color_error));
                iconCliente.setImageResource(R.drawable.ic_person_off);
                iconCliente.setColorFilter(itemView.getContext().getColor(R.color.color_error));
            }
            
            // Fecha de creación
            if (cliente.getFechaCreacion() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String fecha = sdf.format(new Date(cliente.getFechaCreacion()));
                textFechaCreacion.setText("Registrado: " + fecha);
            } else {
                textFechaCreacion.setText("Fecha no disponible");
            }
            
            // Configurar botones
            btnToggleEstado.setText(cliente.isActivo() ? "Desactivar" : "Activar");
            btnToggleEstado.setIcon(itemView.getContext().getDrawable(
                    cliente.isActivo() ? R.drawable.ic_toggle_off : R.drawable.ic_toggle_on));
            
            // Click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClienteClick(cliente);
                }
            });
            
            btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditarClick(cliente);
                }
            });
            
            btnToggleEstado.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleEstadoClick(cliente);
                }
            });
            
            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEliminarClick(cliente);
                }
            });
        }
    }
}