package com.example.cafefidelidaqrdemo;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cafefidelidaqrdemo.models.Sucursal;
import com.example.cafefidelidaqrdemo.models.ResenaSucursal;
import com.example.cafefidelidaqrdemo.repository.SucursalRepository;
import com.example.cafefidelidaqrdemo.repository.ResenasSucursalRepository;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;
import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.Glide;

public class DetalleSucursalActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvNombre, tvDireccion, tvTelefono, tvHorario;
    private ImageView ivImagenSucursal;

    // Reseñas UI
    private RatingBar rbResena;
    private EditText etComentarioResena;
    private MaterialButton btnEnviarResena;
    private TextView tvPromedioResenas, tvConteoResenas;

    private String sucursalIdStr;
    private int sucursalIdInt = -1;

    // Repositorios
    private SucursalRepository sucursalRepository;
    private ResenasSucursalRepository resenasRepository;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_sucursal);

        sucursalIdStr = getIntent().getStringExtra("sucursal_id");
        if (sucursalIdStr == null || sucursalIdStr.trim().isEmpty()) {
            Toast.makeText(this, "Error: Sucursal no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            sucursalIdInt = Integer.parseInt(sucursalIdStr);
        } catch (NumberFormatException e) {
            sucursalIdInt = -1;
        }

        initViews();
        setupToolbar();

        sucursalRepository = new SucursalRepository(getApplicationContext());
        resenasRepository = ResenasSucursalRepository.getInstance(getApplicationContext());
        authRepository = AuthRepository.getInstance();
        authRepository.setContext(getApplicationContext());

        setupResenaUI();
        loadSucursal();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvNombre = findViewById(R.id.tv_nombre_sucursal);
        tvDireccion = findViewById(R.id.tv_direccion_sucursal);
        tvTelefono = findViewById(R.id.tv_telefono_sucursal);
        tvHorario = findViewById(R.id.tv_horario_sucursal);
        ivImagenSucursal = findViewById(R.id.image_sucursal);

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
            getSupportActionBar().setTitle("Detalle de Sucursal");
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadSucursal() {
        if (sucursalIdInt <= 0) {
            Toast.makeText(this, "ID de sucursal inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        sucursalRepository.getSucursalById(sucursalIdInt).observe(this, s -> {
            if (s != null) {
                displaySucursalDetails(s);
                resenasRepository.obtenerPromedio(sucursalIdInt);
            } else {
                Toast.makeText(DetalleSucursalActivity.this, "Sucursal no encontrada", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupResenaUI() {
        resenasRepository.getPromedioLiveData().observe(this, promedio -> {
            if (promedio != null) {
                tvPromedioResenas.setText(String.format(java.util.Locale.getDefault(), "Promedio: %.1f", promedio.getPromedio()));
                tvConteoResenas.setText(String.format(java.util.Locale.getDefault(), "(%d reseñas)", promedio.getCantidad()));
            }
        });

        resenasRepository.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.trim().isEmpty()) {
                Toast.makeText(DetalleSucursalActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        resenasRepository.getSuccessLiveData().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(DetalleSucursalActivity.this, msg, Toast.LENGTH_SHORT).show();
                if (sucursalIdInt > 0) {
                    resenasRepository.obtenerPromedio(sucursalIdInt);
                }
                rbResena.setRating(0f);
                etComentarioResena.setText("");
            }
        });

        if (btnEnviarResena != null) {
            btnEnviarResena.setOnClickListener(v -> {
                if (sucursalIdInt <= 0) {
                    Toast.makeText(DetalleSucursalActivity.this, "Sucursal inválida", Toast.LENGTH_SHORT).show();
                    return;
                }

                int calificacion = Math.round(rbResena != null ? rbResena.getRating() : 0f);
                if (calificacion < 1 || calificacion > 5) {
                    Toast.makeText(DetalleSucursalActivity.this, "Selecciona una calificación de 1 a 5", Toast.LENGTH_SHORT).show();
                    return;
                }

                String comentario = etComentarioResena != null ? etComentarioResena.getText().toString().trim() : "";

                AuthRepository.LocalUser user = authRepository.getCurrentUser();
                int usuarioIdInt = 0;
                if (user != null && user.uid != null) {
                    try {
                        usuarioIdInt = Integer.parseInt(user.uid);
                    } catch (NumberFormatException e) {
                        try {
                            com.example.cafefidelidaqrdemo.utils.SessionManager sm = new com.example.cafefidelidaqrdemo.utils.SessionManager(DetalleSucursalActivity.this);
                            String emailSesion = sm.getUserEmail();
                            if (emailSesion != null && !emailSesion.trim().isEmpty()) {
                                com.example.cafefidelidaqrdemo.repository.ClienteRepository clienteRepo = new com.example.cafefidelidaqrdemo.repository.ClienteRepository(DetalleSucursalActivity.this);
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
                    Toast.makeText(DetalleSucursalActivity.this, "Debes iniciar sesión con un cliente válido para reseñar", Toast.LENGTH_SHORT).show();
                    return;
                }

                long now = System.currentTimeMillis();
                ResenaSucursal resena = new ResenaSucursal(0, sucursalIdInt, usuarioIdInt, calificacion, comentario, now, now);
                resenasRepository.crearResena(resena);
            });
        }
    }

    private void displaySucursalDetails(Sucursal sucursal) {
        tvNombre.setText(sucursal.getNombre());
        tvDireccion.setText("Dirección: " + (sucursal.getDireccion() != null ? sucursal.getDireccion() : "Sin dirección"));
        tvTelefono.setText("Teléfono: " + (sucursal.getTelefono() != null ? sucursal.getTelefono() : "No disponible"));
        String horario = (sucursal.getHorarioApertura() != null ? sucursal.getHorarioApertura() : "?") +
                " - " + (sucursal.getHorarioCierre() != null ? sucursal.getHorarioCierre() : "?");
        tvHorario.setText("Horario: " + horario);

        String url = sucursal.getImagenUrl();
        if (ivImagenSucursal != null) {
            if (url != null && !url.trim().isEmpty()) {
                Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.ic_store)
                        .error(R.drawable.ic_store_empty)
                        .centerCrop()
                        .into(ivImagenSucursal);
            } else {
                ivImagenSucursal.setImageResource(sucursal.isActiva() ? R.drawable.ic_store : R.drawable.ic_store_empty);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
