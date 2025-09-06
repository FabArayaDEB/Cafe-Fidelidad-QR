package com.example.cafefidelidaqrdemo.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.data.entities.ProductoEntity;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

public class ProductosAdminAdapter extends RecyclerView.Adapter<ProductosAdminAdapter.ProductoViewHolder> {
    
    private List<ProductoEntity> productos;
    private OnProductoClickListener listener;
    private DecimalFormat decimalFormat;
    
    public interface OnProductoClickListener {
        void onProductoClick(ProductoEntity producto);
        void onEditarClick(ProductoEntity producto);
        void onEliminarClick(ProductoEntity producto);
        void onToggleActivoClick(ProductoEntity producto);
        void onToggleDisponibilidadClick(ProductoEntity producto);
    }
    
    public ProductosAdminAdapter() {
        this.productos = new ArrayList<>();
        this.decimalFormat = new DecimalFormat("#,##0.00");
    }
    
    public void setOnProductoClickListener(OnProductoClickListener listener) {
        this.listener = listener;
    }
    
    public void updateProductos(List<ProductoEntity> nuevosProductos) {
        this.productos.clear();
        if (nuevosProductos != null) {
            this.productos.addAll(nuevosProductos);
        }
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto_admin, parent, false);
        return new ProductoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        ProductoEntity producto = productos.get(position);
        holder.bind(producto);
    }
    
    @Override
    public int getItemCount() {
        return productos.size();
    }
    
    public class ProductoViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvNombre;
        private TextView tvDescripcion;
        private TextView tvPrecio;
        private TextView tvPuntos;
        private TextView tvCategoria;
        private TextView tvEstado;
        private ImageButton btnEditar;
        private ImageButton btnEliminar;
        private ImageButton btnToggleActivo;
        private ImageButton btnToggleDisponibilidad;
        
        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvNombre = itemView.findViewById(R.id.tv_nombre_producto);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion_producto);
            tvPrecio = itemView.findViewById(R.id.tv_precio_producto);
            tvPuntos = itemView.findViewById(R.id.tv_puntos_producto);
            tvCategoria = itemView.findViewById(R.id.tv_categoria_producto);
            tvEstado = itemView.findViewById(R.id.tv_estado_producto);
            btnEditar = itemView.findViewById(R.id.btn_editar_producto);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_producto);
            btnToggleActivo = itemView.findViewById(R.id.btn_toggle_activo);
            btnToggleDisponibilidad = itemView.findViewById(R.id.btn_toggle_disponibilidad);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onProductoClick(productos.get(getAdapterPosition()));
                }
            });
            
            btnEditar.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onEditarClick(productos.get(getAdapterPosition()));
                }
            });
            
            btnEliminar.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onEliminarClick(productos.get(getAdapterPosition()));
                }
            });

            btnToggleActivo.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onToggleActivoClick(productos.get(getAdapterPosition()));
                }
            });

            btnToggleDisponibilidad.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onToggleDisponibilidadClick(productos.get(getAdapterPosition()));
                }
            });
        }
        
        public void bind(ProductoEntity producto) {
            tvNombre.setText(producto.getNombre() != null ? producto.getNombre() : "Producto sin nombre");
            tvDescripcion.setText(producto.getDescripcion() != null ? producto.getDescripcion() : "Sin descripción");
            tvPrecio.setText("$" + decimalFormat.format(producto.getPrecio()));
            tvCategoria.setText(producto.getCategoria() != null ? producto.getCategoria() : "Sin categoría");
            
            // Estado del producto
            if (producto.isActivo()) {
                tvEstado.setText("Disponible");
                tvEstado.setTextColor(itemView.getContext().getColor(R.color.color_success));
                tvEstado.setBackgroundColor(itemView.getContext().getColor(R.color.color_success_container));
                btnToggleDisponibilidad.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                tvEstado.setText("No disponible");
                tvEstado.setTextColor(itemView.getContext().getColor(R.color.color_error));
                tvEstado.setBackgroundColor(itemView.getContext().getColor(R.color.error_container));
                btnToggleDisponibilidad.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    }
}