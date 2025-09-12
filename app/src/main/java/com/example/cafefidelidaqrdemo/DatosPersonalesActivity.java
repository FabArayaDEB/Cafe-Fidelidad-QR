package com.example.cafefidelidaqrdemo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DatosPersonalesActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private TextView tvNombre, tvTelefono, tvFechaNacimiento;
    private LinearLayout layoutNombre, layoutTelefono, layoutFechaNacimiento;
    private Toolbar toolbar;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_personales);
        
        // Inicializar vistas
        initViews();

        // Inicializar Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Configurar toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Datos Personales");
        }

        // Configurar listener del botón de retroceso
        toolbar.setNavigationOnClickListener(v -> finish());

        // Cargar datos del usuario
        loadUserData();
        
        // Configurar listeners para edición
        setupEditListeners();
    }

    private void loadUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            
            databaseReference.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombre = snapshot.child("nombre").getValue(String.class);
                        String telefono = snapshot.child("telefono").getValue(String.class);
                        String fechaNacimiento = snapshot.child("fechaNacimiento").getValue(String.class);
                        
                        // Mostrar datos en las vistas
                        tvNombre.setText(nombre != null ? nombre : "Dato no proporcionado");
                        tvTelefono.setText(telefono != null ? telefono : "Dato no proporcionado");
                        tvFechaNacimiento.setText(fechaNacimiento != null ? fechaNacimiento : "13/11/2002");
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DatosPersonalesActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvNombre = findViewById(R.id.tv_nombre_value);
        tvTelefono = findViewById(R.id.tv_telefono_value);
        tvFechaNacimiento = findViewById(R.id.tv_fecha_nacimiento_value);
        layoutNombre = findViewById(R.id.layout_nombre);
        layoutTelefono = findViewById(R.id.layout_telefono);
        layoutFechaNacimiento = findViewById(R.id.layout_fecha_nacimiento);
    }
    
    private void setupEditListeners() {
          // Listener para editar nombre
          layoutNombre.setOnClickListener(v -> showEditDialog("Nombre", tvNombre.getText().toString(), "nombre"));
          
          // Listener para editar teléfono
          layoutTelefono.setOnClickListener(v -> showEditDialog("Teléfono", tvTelefono.getText().toString(), "telefono"));
          
          // Listener para editar fecha de nacimiento
          layoutFechaNacimiento.setOnClickListener(v -> showDatePickerDialog());
      }
    
    private void showEditDialog(String title, String currentValue, String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar " + title);
        
        final EditText input = new EditText(this);
        input.setText(currentValue.equals("Dato no proporcionado") ? "" : currentValue);
        builder.setView(input);
        
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newValue = input.getText().toString().trim();
            if (!newValue.isEmpty()) {
                updateUserData(field, newValue);
            }
        });
        
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }
    
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    updateUserData("fechaNacimiento", date);
                }, year, month, day);
        
        datePickerDialog.show();
    }
    
    private void updateUserData(String field, String value) {
         FirebaseUser currentUser = firebaseAuth.getCurrentUser();
         if (currentUser != null) {
             String userId = currentUser.getUid();
             
             Map<String, Object> updates = new HashMap<>();
             updates.put(field, value);
             
             databaseReference.child(userId).updateChildren(updates)
                     .addOnSuccessListener(aVoid -> {
                         // Actualizar la UI inmediatamente
                         updateUIField(field, value);
                         Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                     })
                     .addOnFailureListener(e -> {
                         Toast.makeText(this, "Error al actualizar datos", Toast.LENGTH_SHORT).show();
                     });
         }
     }
     
     private void updateUIField(String field, String value) {
          switch (field) {
              case "nombre":
                  tvNombre.setText(value);
                  break;
              case "telefono":
                  tvTelefono.setText(value);
                  break;
              case "fechaNacimiento":
                  tvFechaNacimiento.setText(value);
                  break;
          }
      }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}