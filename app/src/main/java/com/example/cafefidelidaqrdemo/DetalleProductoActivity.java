package com.example.cafefidelidaqrdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.cafefidelidaqrdemo.models.Producto;
import com.example.cafefidelidaqrdemo.models.ResenaProducto;
import com.example.cafefidelidaqrdemo.models.PromedioCalificacion;
import com.example.cafefidelidaqrdemo.repository.ProductoRepository;
import com.example.cafefidelidaqrdemo.repository.ResenasProductoRepository;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;
import com.google.android.material.button.MaterialButton;
// import com.google.firebase.database.DataSnapshot;
// import com.google.firebase.database.DatabaseError;
// import com.google.firebase.database.DatabaseReference;
// import com.google.firebase.database.FirebaseDatabase;
// import com.google.firebase.database.ValueEventListener;

public class DetalleProductoActivity extends AppCompatActivity {

    private ImageView ivProducto, ivPopular;
    private TextView tvNombre, tvDescripcion, tvPrecio, tvPrecioOriginal, tvDescuento;
    private TextView tvStock, tvCalorias, tvIngredientes, tvCategoria;
    private TextView tvVegano, tvVegetariano, tvSinLactosa, tvSinGluten;
    private TextView tvPuntosRequeridos;
    private Toolbar toolbar;

    private String productoId;
    private Producto producto;
    // private DatabaseReference productosRef;

    // Reseñas UI
    private RatingBar rbResena;
    private EditText etComentarioResena;
    private MaterialButton btnEnviarResena;
    private TextView tvPromedioResenas, tvConteoResenas;

    // Repositorios
    private ResenasProductoRepository resenasRepository;
    private ProductoRepository productoRepository;
    private AuthRepository authRepository;
    private int productoIdInt = -1;

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

        try {
            productoIdInt = Integer.parseInt(productoId);
        } catch (NumberFormatException e) {
            productoIdInt = -1;
        }

        initViews();
        setupToolbar();
        // Inicializar repositorios
        productoRepository = ProductoRepository.getInstance(getApplicationContext());
        resenasRepository = ResenasProductoRepository.getInstance(getApplicationContext());
        authRepository = AuthRepository.getInstance();
        authRepository.setContext(getApplicationContext());

        setupResenaUI();
        loadProductoFromRepository();
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

        // Reseñas
        rbResena = findViewById(R.id.rb_resena);
        etComentarioResena = findViewById(R.id.et_comentario_resena);
        btnEnviarResena = findViewById(R.id.btn_enviar_resena);
        tvPromedioResenas = findViewById(R.id.tv_promedio_resenas);
        tvConteoResenas = findViewById(R.id.tv_conteo_resenas);
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

    private void loadProductoFromRepository() {
        if (productoIdInt <= 0) {
            Toast.makeText(this, "ID de producto inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        productoRepository.getProductoById(productoIdInt).observe(this, p -> {
            if (p != null) {
                producto = p;
                displayProductoDetails();
                // Cargar promedio de reseñas
                resenasRepository.obtenerPromedio(productoIdInt);
            } else {
                Toast.makeText(DetalleProductoActivity.this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupResenaUI() {
        // Observar promedio
        resenasRepository.getPromedioLiveData().observe(this, promedio -> {
            if (promedio != null) {
                tvPromedioResenas.setText(String.format(java.util.Locale.getDefault(), "Promedio: %.1f", promedio.getPromedio()));
                tvConteoResenas.setText(String.format(java.util.Locale.getDefault(), "(%d reseñas)", promedio.getCantidad()));
            }
        });

        // Observar errores/exitos
        resenasRepository.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.trim().isEmpty()) {
                Toast.makeText(DetalleProductoActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
        resenasRepository.getSuccessLiveData().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(DetalleProductoActivity.this, msg, Toast.LENGTH_SHORT).show();
                // Refrescar promedio
                if (productoIdInt > 0) {
                    resenasRepository.obtenerPromedio(productoIdInt);
                }
                // Limpiar formulario
                rbResena.setRating(0f);
                etComentarioResena.setText("");
            }
        });

        // Enviar reseña
        if (btnEnviarResena != null) {
            btnEnviarResena.setOnClickListener(v -> {
                if (productoIdInt <= 0) {
                    Toast.makeText(DetalleProductoActivity.this, "Producto inválido", Toast.LENGTH_SHORT).show();
                    return;
                }

                int calificacion = Math.round(rbResena != null ? rbResena.getRating() : 0f);
                if (calificacion < 1 || calificacion > 5) {
                    Toast.makeText(DetalleProductoActivity.this, "Selecciona una calificación de 1 a 5", Toast.LENGTH_SHORT).show();
                    return;
                }

                String comentario = etComentarioResena != null ? etComentarioResena.getText().toString().trim() : "";

                // Obtener usuario actual (acepta IDs no numéricos de usuarios locales demo)
                AuthRepository.LocalUser user = authRepository.getCurrentUser();
                int usuarioIdInt = 0;
                if (user != null && user.uid != null) {
                    try {
                        // Intentar parsear ID directamente
                        usuarioIdInt = Integer.parseInt(user.uid);
                    } catch (NumberFormatException e) {
                        // Si el UID no es numérico (p.ej. "user_001"), intentar resolver por email de sesión en SQLite
                        try {
                            com.example.cafefidelidaqrdemo.utils.SessionManager sm = new com.example.cafefidelidaqrdemo.utils.SessionManager(DetalleProductoActivity.this);
                            String emailSesion = sm.getUserEmail();
                            if (emailSesion != null && !emailSesion.trim().isEmpty()) {
                                com.example.cafefidelidaqrdemo.repository.ClienteRepository clienteRepo = new com.example.cafefidelidaqrdemo.repository.ClienteRepository(DetalleProductoActivity.this);
                                com.example.cafefidelidaqrdemo.models.Cliente c = clienteRepo.getClienteByEmailSync(emailSesion.trim());
                                if (c != null && c.getId() != null) {
                                    try {
                                        usuarioIdInt = Integer.parseInt(c.getId());
                                    } catch (NumberFormatException ignored) {
                                        usuarioIdInt = 0;
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                            usuarioIdInt = 0;
                        }
                    }
                }
                if (usuarioIdInt <= 0) {
                    Toast.makeText(DetalleProductoActivity.this, "Debes iniciar sesión con un cliente válido para reseñar", Toast.LENGTH_SHORT).show();
                    return;
                }

                long now = System.currentTimeMillis();
                ResenaProducto resena = new ResenaProducto(0, productoIdInt, usuarioIdInt, calificacion, comentario, now, now);
                resenasRepository.crearResena(resena);
            });
        }
    }

    private void displayProductoDetails() {
        // Configurar imagen del producto - ProductoEntity no tiene imagen
        ivProducto.setImageResource(R.drawable.ic_coffee_placeholder);

        // Configurar información básica
        tvNombre.setText(producto.getNombre());
        tvDescripcion.setText(producto.getDescripcion());
        tvCategoria.setText("Categoría: " + producto.getCategoria());

        // Configurar precios - ProductoEntity no tiene descuentos
        tvPrecio.setText(String.format("$%.2f", producto.getPrecio()));
        tvPrecioOriginal.setVisibility(View.GONE);
        tvDescuento.setVisibility(View.GONE);

        // Configurar stock - usando disponibilidad
        if (producto.isDisponible()) {
            tvStock.setText("Disponible");
            tvStock.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvStock.setText("No disponible");
            tvStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // Configurar información nutricional - No disponible en ProductoEntity
        tvCalorias.setVisibility(View.GONE);

        // Configurar ingredientes - No disponible en ProductoEntity
        tvIngredientes.setVisibility(View.GONE);

        // Configurar indicadores dietéticos - No disponible en ProductoEntity
        tvVegano.setVisibility(View.GONE);
        tvVegetariano.setVisibility(View.GONE);
        tvSinLactosa.setVisibility(View.GONE);
        tvSinGluten.setVisibility(View.GONE);

        // Configurar indicador de popularidad - No disponible en ProductoEntity
        ivPopular.setVisibility(View.GONE);

        // Configurar puntos requeridos - ocultar por ahora ya que no está en el modelo
        tvPuntosRequeridos.setVisibility(View.GONE);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}