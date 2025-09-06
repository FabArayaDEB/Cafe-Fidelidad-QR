package com.example.cafefidelidaqrdemo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.database.entities.ProductoEntity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductosAdapter extends ListAdapter<ProductoEntity, ProductosAdapter.ProductoViewHolder> {
    
    private OnProductoClickListener onProductoClickListener;
    private OnProductoLongClickListener onProductoLongClickListener;
    
    public ProductosAdapter() {
        super(DIFF_CALLBACK);
    }
    
    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        ProductoEntity producto = getItem(position);
        holder.bind(producto);
    }
    
    public void setOnProductoClickListener(OnProductoClickListener listener) {
        this.onProductoClickListener = listener;
    }
    
    public void setOnProductoLongClickListener(OnProductoLongClickListener listener) {
        this.onProductoLongClickListener = listener;
    }
    
    class ProductoViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView imageViewProducto;
        private final TextView textViewNombre;
        private final TextView textViewCategoria;
        private final TextView textViewPrecio;
        private final Chip chipEstado;
        private final View viewDisponibilidad;
        
        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.cardViewProducto);
            imageViewProducto = itemView.findViewById(R.id.imageViewProducto);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewCategoria = itemView.findViewById(R.id.textViewCategoria);
            textViewPrecio = itemView.findViewById(R.id.textViewPrecio);
            chipEstado = itemView.findViewById(R.id.chipEstado);
            viewDisponibilidad = itemView.findViewById(R.id.viewDisponibilidad);
            
            // Configurar listeners
            cardView.setOnClickListener(v -> {
                if (onProductoClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onProductoClickListener.onProductoClick(getItem(position));
                    }
                }
            });
            
            cardView.setOnLongClickListener(v -> {
                if (onProductoLongClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onProductoLongClickListener.onProductoLongClick(getItem(position));
                        return true;
                    }
                }
                return false;
            });
        }
        
        public void bind(ProductoEntity producto) {
            // Configurar nombre
            textViewNombre.setText(producto.getNombre());
            
            // Configurar categoría
            textViewCategoria.setText(producto.getCategoria());
            
            // Configurar precio
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
            textViewPrecio.setText(currencyFormat.format(producto.getPrecio()));
            
            // Configurar estado
            boolean isDisponible = "activo".equalsIgnoreCase(producto.getEstado());
            
            if (isDisponible) {
                chipEstado.setText("Disponible");
                chipEstado.setChipBackgroundColorResource(R.color.success_light);
                chipEstado.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success_green));
                viewDisponibilidad.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.success));
            } else {
                chipEstado.setText("No disponible");
                chipEstado.setChipBackgroundColorResource(R.color.error_light);
                chipEstado.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.error_red));
                viewDisponibilidad.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.error));
            }
            
            // Configurar imagen del producto basada en la categoría
            configureProductImage(producto.getCategoria());
            
            // Configurar apariencia de la tarjeta según disponibilidad
            configureCardAppearance(isDisponible);
        }
        
        private void configureProductImage(String categoria) {
            int imageResource;
            
            switch (categoria.toLowerCase()) {
                case "café":
                case "cafe":
                    imageResource = R.drawable.ic_coffee;
                    break;
                case "bebidas":
                    imageResource = R.drawable.ic_local_drink;
                    break;
                case "postres":
                    imageResource = R.drawable.ic_cake;
                    break;
                case "snacks":
                    imageResource = R.drawable.ic_fastfood;
                    break;
                case "desayunos":
                    imageResource = R.drawable.ic_breakfast;
                    break;
                default:
                    imageResource = R.drawable.ic_restaurant;
                    break;
            }
            
            imageViewProducto.setImageResource(imageResource);
            
            // Configurar tint del icono
            imageViewProducto.setColorFilter(
                ContextCompat.getColor(itemView.getContext(), R.color.primary)
            );
        }
        
        private void configureCardAppearance(boolean isDisponible) {
            if (isDisponible) {
                // Producto disponible - apariencia normal
                cardView.setAlpha(1.0f);
                cardView.setCardElevation(4f);
                textViewNombre.setAlpha(1.0f);
                textViewCategoria.setAlpha(1.0f);
                textViewPrecio.setAlpha(1.0f);
                imageViewProducto.setAlpha(1.0f);
            } else {
                // Producto no disponible - apariencia atenuada
                cardView.setAlpha(0.7f);
                cardView.setCardElevation(2f);
                textViewNombre.setAlpha(0.6f);
                textViewCategoria.setAlpha(0.6f);
                textViewPrecio.setAlpha(0.6f);
                imageViewProducto.setAlpha(0.5f);
            }
        }
    }
    
    // DiffUtil para optimizar actualizaciones de la lista
    private static final DiffUtil.ItemCallback<ProductoEntity> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<ProductoEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull ProductoEntity oldItem, @NonNull ProductoEntity newItem) {
                return oldItem.getIdProducto().equals(newItem.getIdProducto());
            }
            
            @Override
            public boolean areContentsTheSame(@NonNull ProductoEntity oldItem, @NonNull ProductoEntity newItem) {
                return oldItem.getNombre().equals(newItem.getNombre()) &&
                       oldItem.getCategoria().equals(newItem.getCategoria()) &&
                       oldItem.getPrecio().equals(newItem.getPrecio()) &&
                       oldItem.getEstado().equals(newItem.getEstado());
            }
        };
    
    // Interfaces para callbacks
    public interface OnProductoClickListener {
        void onProductoClick(ProductoEntity producto);
    }
    
    public interface OnProductoLongClickListener {
        void onProductoLongClick(ProductoEntity producto);
    }
}