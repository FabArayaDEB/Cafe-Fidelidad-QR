package com.example.cafefidelidaqrdemo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafefidelidaqrdemo.adapters.ResenasSucursalAdapter;
import com.example.cafefidelidaqrdemo.models.PromedioCalificacion;
import com.example.cafefidelidaqrdemo.models.ResenaSucursal;
import com.example.cafefidelidaqrdemo.models.Sucursal;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;
import com.example.cafefidelidaqrdemo.repository.ResenasSucursalRepository;
import com.example.cafefidelidaqrdemo.repository.SucursalRepository;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;

public class SucursalDetalleActivity extends AppCompatActivity {

    private SucursalRepository sucursalRepository;
    private ResenasSucursalRepository resenasRepository;
    private AuthRepository authRepository;

    private Sucursal sucursalActual;
    private ResenasSucursalAdapter adapter;

    // Vistas
    private TextView tvNombre, tvDireccion, tvTelefono, tvHorario, tvPromedioTexto;
    private RatingBar rbPromedio, rbMiCalificacion;
    private TextInputEditText etComentario;
    private Button btnEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sucursal_detalle);

        // Inicializar repositorios
        sucursalRepository = new SucursalRepository(getApplication());
        resenasRepository = new ResenasSucursalRepository(getApplication());
        authRepository = AuthRepository.getInstance();

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initViews();

        // Obtener ID del Intent
        String idStr = getIntent().getStringExtra("sucursal_id");
        if (idStr != null) {
            cargarDatos(Long.parseLong(idStr));
        } else {
            finish();
        }

        btnEnviar.setOnClickListener(v -> enviarResena());
    }

    private void initViews() {
        tvNombre = findViewById(R.id.tvNombreSucursal);
        tvDireccion = findViewById(R.id.tvDireccion);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvHorario = findViewById(R.id.tvHorario);
        tvPromedioTexto = findViewById(R.id.tvPromedioTexto);
        rbPromedio = findViewById(R.id.rbPromedio);

        rbMiCalificacion = findViewById(R.id.rbMiCalificacion);
        etComentario = findViewById(R.id.etComentario);
        btnEnviar = findViewById(R.id.btnEnviarResena);

        RecyclerView rv = findViewById(R.id.rvResenas);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResenasSucursalAdapter();
        rv.setAdapter(adapter);
    }

    private void cargarDatos(long sucursalId) {
        // 1. Cargar Info Sucursal
        sucursalRepository.getSucursalById(sucursalId, new SucursalRepository.SucursalCallback() {
            @Override
            public void onSuccess(Sucursal sucursal) {
                sucursalActual = sucursal;
                runOnUiThread(() -> {
                    tvNombre.setText(sucursal.getNombre());
                    tvDireccion.setText(sucursal.getDireccion());
                    tvTelefono.setText(sucursal.getTelefono());
                    tvHorario.setText(sucursal.getHorarioApertura() + " - " + sucursal.getHorarioCierre());
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(SucursalDetalleActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });

        cargarResenas(sucursalId);
    }

    private void cargarResenas(long sucursalId) {
        // 2. Cargar Lista de Reseñas
        resenasRepository.obtenerResenas((int) sucursalId).observe(this, resenas -> {
            if (resenas != null) {
                adapter.setResenas(resenas);
            }
        });

        // 3. Cargar Promedio
        resenasRepository.obtenerPromedio((int) sucursalId);
        resenasRepository.getPromedioLiveData().observe(this, promedio -> {
            if (promedio != null) {
                rbPromedio.setRating((float) promedio.getPromedio());
                tvPromedioTexto.setText(String.format("%.1f (%d opiniones)", promedio.getPromedio(), promedio.getCantidad()));
            }
        });
    }

    private void enviarResena() {
        if (sucursalActual == null) return;

        int calificacion = (int) rbMiCalificacion.getRating();
        String comentario = etComentario.getText().toString().trim();

        if (calificacion == 0) {
            Toast.makeText(this, "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener usuario actual
        String uid = authRepository.getCurrentUserUid();
        // OJO: Aquí asumimos que el ID del cliente es numérico para la BD SQLite actual
        // Si tu sistema usa IDs string (UUID), habría que adaptar el modelo ResenaSucursal.
        // Por compatibilidad con lo que tienes, intentaremos convertir o usar un ID dummy si falla.
        int clienteId = 1; // Default dummy
        try {
            // Intentar obtener ID real desde repositorio de cliente si tienes el método
            // O usar el UID si es numérico
            if (uid != null) clienteId = Integer.parseInt(uid.replaceAll("\\D+","")); // Hack temporal si UID es string
        } catch (Exception e) { }

        long now = System.currentTimeMillis();

        // Crear objeto reseña
        ResenaSucursal nuevaResena = new ResenaSucursal();
        nuevaResena.setSucursalId(Integer.parseInt(sucursalActual.getId()));
        nuevaResena.setClienteId(clienteId);
        nuevaResena.setUsuarioId(clienteId); // Por compatibilidad con ambos campos en tu modelo
        nuevaResena.setCalificacion(calificacion);
        nuevaResena.setComentario(comentario);
        nuevaResena.setFechaCreacion(now);
        nuevaResena.setFechaActualizacion(now);

        resenasRepository.crearResena(nuevaResena);

        // Feedback inmediato (Repository usa LiveData para éxito/error, deberías observarlo)
        resenasRepository.getSuccessLiveData().observe(this, msg -> {
            Toast.makeText(this, "¡Reseña publicada!", Toast.LENGTH_SHORT).show();
            etComentario.setText("");
            rbMiCalificacion.setRating(0);
            cargarResenas(Long.parseLong(sucursalActual.getId())); // Recargar
        });
    }
}