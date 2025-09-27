package com.example.cafefidelidaqrdemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.models.Producto;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Adapter unificado para productos que maneja tanto la vista de cliente como la de administrador
 */
public class ProductosAdapter extends ListAdapter<Producto, RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_CLIENT = 0;
    private static final int VIEW_TYPE_ADMIN = 1;
    
    private Context context;
    private boolean isAdminMode;
    private OnProductoClickListener onProductoClickListener;
    private OnProductoAdminActionListener onProductoAdminActionListener;
    private DecimalFormat decimalFormat;
    
    public ProductosAdapter(Context context, boolean isAdminMode) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.isAdminMode = isAdminMode;
        this.decimalFormat = new DecimalFormat("#,##0.00");
    }
    
    @Override
    public int getItemViewType(int position) {
        return isAdminMode ? VIEW_TYPE_ADMIN : VIEW_TYPE_CLIENT;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        if (viewType == VIEW_TYPE_ADMIN) {
            View view = inflater.inflate(R.layout.item_producto_admin, parent, false);
            return new AdminViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_producto, parent, false);
            return new ClientViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Producto producto = getItem(position);
        
        if (holder instanceof AdminViewHolder) {
            ((AdminViewHolder) holder).bind(producto);
        } else if (holder instanceof ClientViewHolder) {
            ((ClientViewHolder) holder).bind(producto);
        }
    }
    
    // ViewHolder para vista de cliente
    class ClientViewHolder extends RecyclerView.ViewHolder {
        private CardView cardProducto;
        private ImageView ivProducto, ivPopular;
        private TextView tvNombre, tvDescripcion, tvPrecio, tvPrecioOriginal, tvDescuento, tvStock;
        private TextView tvPuntosRequeridos;
        
        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            cardProducto = itemView.findViewById(R.id.card_producto);
            ivProducto = itemView.findViewById(R.id.iv_producto);
            ivPopular = itemView.findViewById(R.id.iv_popular);
            tvNombre = itemView.findViewById(R.id.tv_nombre);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion);
            tvPrecio = itemView.findViewById(R.id.tv_precio);
            tvPrecioOriginal = itemView.findViewById(R.id.tv_precio_original);
            tvDescuento = itemView.findViewById(R.id.tv_descuento);
            tvStock = itemView.findViewById(R.id.tv_stock);
            tvPuntosRequeridos = itemView.findViewById(R.id.tv_puntos_requeridos);
            
            itemView.setOnClickListener(v -> {
                if (onProductoClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onProductoClickListener.onProductoClick(getItem(getAdapterPosition()));
                }
            });
        }
        
        public void bind(Producto producto) {
            // Configurar nombre y descripción
            tvNombre.setText(producto.getNombre());
            tvDescripcion.setText(producto.getDescripcion());
            
            // Configurar imagen del producto (placeholder por ahora)
            ivProducto.setImageResource(R.drawable.ic_coffee_placeholder);
            
            // Configurar precio
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
            tvPrecio.setText(currencyFormat.format(producto.getPrecio()));
            
            // Configurar stock
            tvStock.setText(String.format("Stock: %d", producto.getStockDisponible()));
            
            // Configurar puntos requeridos
            if (producto.getPuntosRequeridos() > 0) {
                tvPuntosRequeridos.setText(String.format("⭐ %d pts", producto.getPuntosRequeridos()));
                tvPuntosRequeridos.setVisibility(View.VISIBLE);
            } else {
                tvPuntosRequeridos.setVisibility(View.GONE);
            }
            
            // Ocultar elementos de descuento por ahora
            tvPrecioOriginal.setVisibility(View.GONE);
            tvDescuento.setVisibility(View.GONE);
            ivPopular.setVisibility(View.GONE);
        }
    }
    
    // ViewHolder para vista de administrador
    class AdminViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombre, tvDescripcion, tvPrecio, tvPuntos, tvCategoria, tvEstado;
        private ImageButton btnEditar, btnEliminar, btnToggleActivo, btnToggleDisponibilidad;
        
        public AdminViewHolder(@NonNull View itemView) {
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
            
            setupClickListeners();
        }
        
        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                if (onProductoAdminActionListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onProductoAdminActionListener.onProductoClick(getItem(getAdapterPosition()));
                }
            });
            
            btnEditar.setOnClickListener(v -> {
                if (onProductoAdminActionListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onProductoAdminActionListener.onEditarClick(getItem(getAdapterPosition()));
                }
            });
            
            btnEliminar.setOnClickListener(v -> {
                if (onProductoAdminActionListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onProductoAdminActionListener.onEliminarClick(getItem(getAdapterPosition()));
                }
            });
            
            btnToggleActivo.setOnClickListener(v -> {
                if (onProductoAdminActionListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onProductoAdminActionListener.onToggleActivoClick(getItem(getAdapterPosition()));
                }
            });
            
            btnToggleDisponibilidad.setOnClickListener(v -> {
                if (onProductoAdminActionListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onProductoAdminActionListener.onToggleDisponibilidadClick(getItem(getAdapterPosition()));
                }
            });
        }
        
        public void bind(Producto producto) {
            // Configurar información básica
            tvNombre.setText(producto.getNombre());
            tvDescripcion.setText(producto.getDescripcion());
            tvCategoria.setText(producto.getCategoria());
            
            // Configurar precio
            tvPrecio.setText(String.format("$%s", decimalFormat.format(producto.getPrecio())));
            
            // Configurar puntos
            tvPuntos.setText(String.format("%d pts", producto.getPuntosRequeridos()));
            
            // Configurar estado
            String estado = producto.getEstado();
            tvEstado.setText(estado);
            
            // Configurar color del estado
            if ("activo".equalsIgnoreCase(estado) || "disponible".equalsIgnoreCase(estado)) {
                tvEstado.setBackgroundColor(ContextCompat.getColor(context, R.color.color_success_container));
                tvEstado.setTextColor(ContextCompat.getColor(context, R.color.color_success));
            } else {
                tvEstado.setBackgroundColor(ContextCompat.getColor(context, R.color.color_error_container));
                tvEstado.setTextColor(ContextCompat.getColor(context, R.color.color_error));
            }
        }
    }
    
    // DiffUtil para optimizar actualizaciones
    private static final DiffUtil.ItemCallback<Producto> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<Producto>() {
            @Override
            public boolean areItemsTheSame(@NonNull Producto oldItem, @NonNull Producto newItem) {
                return oldItem.getId().equals(newItem.getId());
            }
            
            @Override
            public boolean areContentsTheSame(@NonNull Producto oldItem, @NonNull Producto newItem) {
                return oldItem.getNombre().equals(newItem.getNombre()) &&
                       oldItem.getCategoria().equals(newItem.getCategoria()) &&
                       Double.compare(oldItem.getPrecio(), newItem.getPrecio()) == 0 &&
                       oldItem.getEstado().equals(newItem.getEstado()) &&
                       oldItem.getPuntosRequeridos() == newItem.getPuntosRequeridos() &&
                       oldItem.getStockDisponible() == newItem.getStockDisponible();
            }
        };
    
    // Interfaces para callbacks
    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
    }
    
    public interface OnProductoAdminActionListener {
        void onProductoClick(Producto producto);
        void onEditarClick(Producto producto);
        void onEliminarClick(Producto producto);
        void onToggleActivoClick(Producto producto);
        void onToggleDisponibilidadClick(Producto producto);
    }
    
    // Setters para listeners
    public void setOnProductoClickListener(OnProductoClickListener listener) {
        this.onProductoClickListener = listener;
    }
    
    public void setOnProductoAdminActionListener(OnProductoAdminActionListener listener) {
        this.onProductoAdminActionListener = listener;
    }
    
    // Método para cambiar entre modo cliente y administrador
    public void setAdminMode(boolean isAdminMode) {
        if (this.isAdminMode != isAdminMode) {
            this.isAdminMode = isAdminMode;
            notifyDataSetChanged();
        }
    }
    
    public boolean isAdminMode() {
        return isAdminMode;
    }
}