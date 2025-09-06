package com.example.cafefidelidaqrdemo.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.database.models.TopCliente;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

public class TopClientesAdapter extends RecyclerView.Adapter<TopClientesAdapter.TopClienteViewHolder> {
    
    private List<TopCliente> topClientes;
    private OnClienteClickListener listener;
    private DecimalFormat decimalFormat;
    
    public interface OnClienteClickListener {
        void onClienteClick(TopCliente cliente);
        void onClienteInfoClick(TopCliente cliente);
    }
    
    public TopClientesAdapter() {
        this.topClientes = new ArrayList<>();
        this.decimalFormat = new DecimalFormat("#,##0.00");
    }
    
    public void setOnClienteClickListener(OnClienteClickListener listener) {
        this.listener = listener;
    }
    
    public void updateTopClientes(List<TopCliente> nuevosClientes) {
        this.topClientes.clear();
        if (nuevosClientes != null) {
            this.topClientes.addAll(nuevosClientes);
        }
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public TopClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_cliente, parent, false);
        return new TopClienteViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TopClienteViewHolder holder, int position) {
        TopCliente cliente = topClientes.get(position);
        holder.bind(cliente, position + 1);
    }
    
    @Override
    public int getItemCount() {
        return topClientes.size();
    }
    
    public class TopClienteViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvPosicion;
        private TextView tvNombre;
        private TextView tvVisitas;
        private TextView tvCanjes;
        private TextView tvValorTotal;
        private TextView tvSucursalFavorita;
        
        public TopClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvPosicion = itemView.findViewById(R.id.tv_posicion);
            tvNombre = itemView.findViewById(R.id.tv_nombre_cliente);
            tvVisitas = itemView.findViewById(R.id.tv_total_visitas);
            tvCanjes = itemView.findViewById(R.id.tv_total_canjes);
            tvValorTotal = itemView.findViewById(R.id.tv_valor_total);
            tvSucursalFavorita = itemView.findViewById(R.id.tv_sucursal_favorita);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onClienteClick(topClientes.get(getAdapterPosition()));
                }
            });
            
            View btnInfo = itemView.findViewById(R.id.btn_info_cliente);
            if (btnInfo != null) {
                btnInfo.setOnClickListener(v -> {
                    if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onClienteInfoClick(topClientes.get(getAdapterPosition()));
                    }
                });
            }
        }
        
        public void bind(TopCliente cliente, int posicion) {
            tvPosicion.setText(String.valueOf(posicion));
            tvNombre.setText(cliente.nombre != null ? cliente.nombre : "Cliente desconocido");
            tvVisitas.setText(String.valueOf(cliente.totalVisitas));
            tvCanjes.setText(String.valueOf(cliente.totalCanjes));
            tvValorTotal.setText("$" + decimalFormat.format(cliente.valorTotalCanjes));
             tvSucursalFavorita.setText(cliente.sucursalFavorita != null ? cliente.sucursalFavorita : "N/A");
        }
    }
}