package com.example.notas_app_sqlite;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.notas_app_sqlite.databinding.ActivityAgregarNotaBinding;

public class AgregarNotaActivity extends AppCompatActivity {

    private ActivityAgregarNotaBinding binding;
    private NotasDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAgregarNotaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar base de datos
        db = new NotasDB(this);

        // Configurar botón guardar
        binding.btnGuardarNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = binding.etTitulo.getText().toString().trim();
                String descripcion = binding.etDescripcion.getText().toString().trim();

                if (!titulo.isEmpty() && !descripcion.isEmpty()) {
                    guardarNota(titulo, descripcion);
                } else {
                    Toast.makeText(getApplicationContext(), 
                        "Por favor, completa todos los campos", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Configurar botón cancelar (opcional)
        if (binding.btnCancelar != null) {
            binding.btnCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // Cerrar la actividad sin guardar
                }
            });
        }
    }

    private void guardarNota(String titulo, String descripcion) {
        try {
            // Crear nueva nota (sin ID, se asigna automáticamente)
            Nota nota = new Nota(0, titulo, descripcion);
            
            // Insertar en la base de datos
            db.insertNota(nota);
            
            // Mostrar mensaje de éxito
            Toast.makeText(getApplicationContext(), 
                "Nota guardada exitosamente", 
                Toast.LENGTH_SHORT).show();
            
            // Regresar a MainActivity
            Intent intent = new Intent(AgregarNotaActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            
        } catch (Exception e) {
            // Manejar errores
            Toast.makeText(getApplicationContext(), 
                "Error al guardar la nota: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cerrar la base de datos
        if (db != null) {
            db.close();
        }
    }

    // Método para validar entrada (opcional)
    private boolean validarEntrada(String titulo, String descripcion) {
        if (titulo == null || titulo.trim().isEmpty()) {
            binding.etTitulo.setError("El título es requerido");
            binding.etTitulo.requestFocus();
            return false;
        }

        if (descripcion == null || descripcion.trim().isEmpty()) {
            binding.etDescripcion.setError("La descripción es requerida");
            binding.etDescripcion.requestFocus();
            return false;
        }

        if (titulo.length() > 100) {
            binding.etTitulo.setError("El título no puede exceder 100 caracteres");
            binding.etTitulo.requestFocus();
            return false;
        }

        if (descripcion.length() > 500) {
            binding.etDescripcion.setError("La descripción no puede exceder 500 caracteres");
            binding.etDescripcion.requestFocus();
            return false;
        }

        return true;
    }

    // Método alternativo para guardar con validación mejorada
    private void guardarNotaConValidacion(String titulo, String descripcion) {
        if (!validarEntrada(titulo, descripcion)) {
            return;
        }

        try {
            Nota nota = new Nota(0, titulo.trim(), descripcion.trim());
            db.insertNota(nota);
            
            Toast.makeText(this, "Nota guardada exitosamente", Toast.LENGTH_SHORT).show();
            
            // Limpiar campos después de guardar (opcional)
            binding.etTitulo.setText("");
            binding.etDescripcion.setText("");
            
            // Regresar a MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}