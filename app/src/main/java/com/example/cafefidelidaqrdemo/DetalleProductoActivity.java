package com.example.cafefidelidaqrdemo;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.cafefidelidaqrdemo.models.Producto;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetalleProductoActivity extends AppCompatActivity {

    private ImageView ivProducto, ivPopular;
    private TextView tvNombre, tvDescripcion, tvPrecio, tvPrecioOriginal, tvDescuento;
    private TextView tvStock, tvCalorias, tvIngredientes, tvCategoria;
    private TextView tvVegano, tvVegetariano, tvSinLactosa, tvSinGluten;
    private TextView tvPuntosRequeridos;
    private MaterialButton btnAgregarCarrito;
    private Toolbar toolbar;

    private String productoId;
    private Producto producto;
    private DatabaseReference productosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);

        // Obtener ID del producto desde el Intent
        productoId = getIntent().getStringExtra("producto_id");
        if (productoId == null) {
            Toast.makeText(this, "Error: Producto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupFirebase();
        loadProductoDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProducto = findViewById(R.id.iv_producto);
        ivPopular = findViewById(R.id.iv_popular);
        tvNombre = findViewById(R.id.tv_nombre);
        tvDescripcion = findViewById(R.id.tv_descripcion);
        tvPrecio = findViewById(R.id.tv_precio);
        tvPrecioOriginal = findViewById(R.id.tv_precio_original);
        tvDescuento = findViewById(R.id.tv_descuento);
        tvStock = findViewById(R.id.tv_stock);
        tvCalorias = findViewById(R.id.tv_calorias);
        tvIngredientes = findViewById(R.id.tv_ingredientes);
        tvCategoria = findViewById(R.id.tv_categoria);
        tvVegano = findViewById(R.id.tv_vegano);
        tvVegetariano = findViewById(R.id.tv_vegetariano);
        tvSinLactosa = findViewById(R.id.tv_sin_lactosa);
        tvSinGluten = findViewById(R.id.tv_sin_gluten);
        tvPuntosRequeridos = findViewById(R.id.tv_puntos_requeridos);
        btnAgregarCarrito = findViewById(R.id.btn_agregar_carrito);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Detalle del Producto");
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupFirebase() {
        productosRef = FirebaseDatabase.getInstance().getReference("productos");
    }

    private void loadProductoDetails() {
        productosRef.child(productoId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    producto = snapshot.getValue(Producto.class);
                    if (producto != null) {
                        displayProductoDetails();
                    }
                } else {
                    Toast.makeText(DetalleProductoActivity.this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetalleProductoActivity.this, "Error al cargar producto: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProductoDetails() {
        // Configurar imagen del producto
        if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {
            Glide.with(this)
                    .load(producto.getImagenUrl())
                    .placeholder(R.drawable.ic_coffee_placeholder)
                    .error(R.drawable.ic_coffee_placeholder)
                    .into(ivProducto);
        } else {
            ivProducto.setImageResource(R.drawable.ic_coffee_placeholder);
        }

        // Configurar información básica
        tvNombre.setText(producto.getNombre());
        tvDescripcion.setText(producto.getDescripcion());
        tvCategoria.setText("Categoría: " + producto.getCategoria());

        // Configurar precios y descuentos
        if (producto.tieneDescuento()) {
            tvPrecio.setText(String.format("$%.2f", producto.getPrecioConDescuento()));
            tvPrecioOriginal.setText(String.format("$%.2f", producto.getPrecio()));
            tvPrecioOriginal.setPaintFlags(tvPrecioOriginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvPrecioOriginal.setVisibility(View.VISIBLE);
            tvDescuento.setText(String.format("%.0f%% OFF", producto.getDescuento()));
            tvDescuento.setVisibility(View.VISIBLE);
        } else {
            tvPrecio.setText(String.format("$%.2f", producto.getPrecio()));
            tvPrecioOriginal.setVisibility(View.GONE);
            tvDescuento.setVisibility(View.GONE);
        }

        // Configurar stock
        if (producto.estaEnStock()) {
            tvStock.setText(String.format("Stock disponible: %d unidades", producto.getStock()));
            tvStock.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnAgregarCarrito.setEnabled(true);
        } else {
            tvStock.setText("Sin stock");
            tvStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnAgregarCarrito.setEnabled(false);
        }

        // Configurar información nutricional
        if (producto.getCalorias() > 0) {
            tvCalorias.setText(String.format("Calorías: %d", producto.getCalorias()));
            tvCalorias.setVisibility(View.VISIBLE);
        } else {
            tvCalorias.setVisibility(View.GONE);
        }

        // Configurar ingredientes
        if (producto.getIngredientes() != null && !producto.getIngredientes().isEmpty()) {
            tvIngredientes.setText("Ingredientes: " + producto.getIngredientes());
            tvIngredientes.setVisibility(View.VISIBLE);
        } else {
            tvIngredientes.setVisibility(View.GONE);
        }

        // Configurar indicadores dietéticos
        tvVegano.setVisibility(producto.isEsVegano() ? View.VISIBLE : View.GONE);
        tvVegetariano.setVisibility(producto.isEsVegetariano() ? View.VISIBLE : View.GONE);
        tvSinLactosa.setVisibility(!producto.isContieneLactosa() ? View.VISIBLE : View.GONE);
        tvSinGluten.setVisibility(!producto.isContieneGluten() ? View.VISIBLE : View.GONE);

        // Configurar indicador de popularidad
        ivPopular.setVisibility(producto.isEsPopular() ? View.VISIBLE : View.GONE);

        // Configurar puntos requeridos
        if (producto.getPuntosRequeridos() > 0) {
            tvPuntosRequeridos.setText(String.format("⭐ %d puntos requeridos", producto.getPuntosRequeridos()));
            tvPuntosRequeridos.setVisibility(View.VISIBLE);
        } else {
            tvPuntosRequeridos.setVisibility(View.GONE);
        }

        // Configurar botón de agregar al carrito
        btnAgregarCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarAlCarrito();
            }
        });
    }

    private void agregarAlCarrito() {
        if (producto != null && producto.estaEnStock()) {
            // TODO: Implementar lógica de carrito de compras
            Toast.makeText(this, "Producto agregado al carrito", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Producto sin stock", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}