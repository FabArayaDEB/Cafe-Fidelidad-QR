package com.example.cafefidelidaqrdemo;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.cafefidelidaqrdemo.databinding.ActivityEditarDatosBinding;
// import com.google.android.gms.tasks.OnFailureListener;
// import com.google.android.gms.tasks.OnSuccessListener;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.database.DataSnapshot;
// import com.google.firebase.database.DatabaseError;
// import com.google.firebase.database.DatabaseReference;
// import com.google.firebase.database.FirebaseDatabase;
// import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditarPerfilActivity extends AppCompatActivity {

    private ActivityEditarDatosBinding binding;
    // private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditarDatosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Actualizando perfil de fidelidad");
        progressDialog.setCanceledOnTouchOutside(false);

        // loadUserInfo(); // Deshabilitado - sin Firebase
        Toast.makeText(this, "Funcionalidad de edición de perfil temporalmente deshabilitada", Toast.LENGTH_LONG).show();

        binding.ImgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // updateUserInfo(); // Deshabilitado - sin Firebase
                Toast.makeText(EditarPerfilActivity.this, "Funcionalidad de actualización temporalmente deshabilitada", Toast.LENGTH_LONG).show();
            }
        });

        // Configurar DatePicker para fecha de nacimiento
        binding.etFechaNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    /*
    private void loadUserInfo() {
        // DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // ref.child(firebaseAuth.getUid())
        //         .addValueEventListener(new ValueEventListener() {
        //             @Override
        //             public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String nombres = "" + snapshot.child("names").getValue();
                        String imagen = "" + snapshot.child("imagen").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String telefono = "" + snapshot.child("telefono").getValue();
                        String fechaNacimiento = "" + snapshot.child("fechaNacimiento").getValue();
                        String puntos = "" + snapshot.child("puntos").getValue();
                        String nivel = "" + snapshot.child("nivel").getValue();
                        String totalCompras = "" + snapshot.child("totalCompras").getValue();

                        binding.tvNombres.setText(nombres.equals("null") ? "" : nombres);
                        binding.etTelefono.setText(telefono.equals("null") ? "" : telefono);
                        binding.etFechaNacimiento.setText(fechaNacimiento.equals("null") ? "" : fechaNacimiento);

                        Glide.with(EditarPerfilActivity.this)
                                .load(imagen)
                                .placeholder(R.drawable.ic_account)
                                .into(binding.ivPerfil);
                    }

        //             @Override
        //             public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditarPerfilActivity.this, 
                            "Error al cargar información: " + error.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }
    */

    /*
    private void updateUserInfo() {
        String nombres = binding.tvNombres.getText().toString().trim();
        String telefono = binding.etTelefono.getText().toString().trim();
        String fechaNacimiento = binding.etFechaNacimiento.getText().toString().trim();
        
        if (nombres.isEmpty()) {
            binding.tvNombres.setError("Ingrese su nombre");
            binding.tvNombres.requestFocus();
            return;
        }

        progressDialog.setMessage("Actualizando información del perfil...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("names", nombres);
        hashMap.put("telefono", telefono);
        hashMap.put("fechaNacimiento", fechaNacimiento);
        // Actualizar fecha de última modificación
        hashMap.put("ultimaModificacion", System.currentTimeMillis());

        // DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        // reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(EditarPerfilActivity.this, 
                            "Perfil actualizado correctamente", 
                            Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(EditarPerfilActivity.this, 
                            "Error al actualizar perfil: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }
    */

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Formatear la fecha como DD/MM/AAAA
                        String fecha = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                        binding.etFechaNacimiento.setText(fecha);
                    }
                },
                year, month, day
        );
        
        // Establecer fecha máxima como hoy (no permitir fechas futuras)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
}