package com.example.cafefidelidaqrdemo.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cafefidelidaqrdemo.DetalleProductoActivity;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.models.Producto;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private Context context;
    private List<Producto> listaProductos;

    public ProductoAdapter(Context context, List<Producto> listaProductos) {
        this.context = context;
        this.listaProductos = listaProductos;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.bind(producto);
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public class ProductoViewHolder extends RecyclerView.ViewHolder {
        private CardView cardProducto;
        private ImageView ivProducto, ivPopular;
        private TextView tvNombre, tvDescripcion, tvPrecio, tvPrecioOriginal, tvDescuento, tvStock;

        public ProductoViewHolder(@NonNull View itemView) {
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
        }

        public void bind(Producto producto) {
            // Configurar nombre y descripción
            tvNombre.setText(producto.getNombre());
            tvDescripcion.setText(producto.getDescripcion());

            // Configurar imagen del producto
            if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {
                Glide.with(context)
                        .load(producto.getImagenUrl())
                        .placeholder(R.drawable.ic_coffee_placeholder)
                        .error(R.drawable.ic_coffee_placeholder)
                        .into(ivProducto);
            } else {
                ivProducto.setImageResource(R.drawable.ic_coffee_placeholder);
            }

            // Configurar precios y descuentos
            if (producto.tieneDescuento()) {
                // Mostrar precio con descuento
                tvPrecio.setText(String.format("$%.2f", producto.getPrecioConDescuento()));
                
                // Mostrar precio original tachado
                tvPrecioOriginal.setText(String.format("$%.2f", producto.getPrecio()));
                tvPrecioOriginal.setPaintFlags(tvPrecioOriginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvPrecioOriginal.setVisibility(View.VISIBLE);
                
                // Mostrar porcentaje de descuento
                tvDescuento.setText(String.format("%.0f%% OFF", producto.getDescuento()));
                tvDescuento.setVisibility(View.VISIBLE);
            } else {
                // Solo mostrar precio normal
                tvPrecio.setText(String.format("$%.2f", producto.getPrecio()));
                tvPrecioOriginal.setVisibility(View.GONE);
                tvDescuento.setVisibility(View.GONE);
            }

            // Configurar indicador de popularidad
            if (producto.isEsPopular()) {
                ivPopular.setVisibility(View.VISIBLE);
            } else {
                ivPopular.setVisibility(View.GONE);
            }

            // Configurar stock
            if (producto.estaEnStock()) {
                tvStock.setText(String.format("Stock: %d", producto.getStock()));
                tvStock.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                cardProducto.setAlpha(1.0f);
            } else {
                tvStock.setText("Sin stock");
                tvStock.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                cardProducto.setAlpha(0.6f);
            }

            // Click listener para ver detalles del producto
            cardProducto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetalleProductoActivity.class);
                    intent.putExtra("producto_id", producto.getId());
                    context.startActivity(intent);
                }
            });
        }
    }

    // Método para actualizar la lista de productos
    public void updateProductos(List<Producto> nuevosProductos) {
        this.listaProductos = nuevosProductos;
        notifyDataSetChanged();
    }

    // Método para filtrar productos
    public void filtrarPorCategoria(String categoria) {
        // Este método puede ser implementado si se necesita filtrado adicional
        notifyDataSetChanged();
    }
}