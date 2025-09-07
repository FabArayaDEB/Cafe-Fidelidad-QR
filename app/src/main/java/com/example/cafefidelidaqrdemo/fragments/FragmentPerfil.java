package com.example.cafefidelidaqrdemo.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.HistorialActivity;
import com.example.cafefidelidaqrdemo.databinding.FragmentPerfilBinding;
import com.example.cafefidelidaqrdemo.OpcionesLoginActivity;
import com.example.cafefidelidaqrdemo.EditarPerfilActivity;
import com.example.cafefidelidaqrdemo.Contantes;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class FragmentPerfil extends Fragment {
    private FragmentPerfilBinding binding;
    private Context mContext;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    public FragmentPerfil() {
        // Required empty public constructor
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        
        // Configurar listeners para los botones
        binding.UpdateUserData.setOnClickListener(v -> {
            startActivity(new Intent(mContext, EditarPerfilActivity.class));
        });
        
        binding.btnHistorial.setOnClickListener(v -> {
            startActivity(new Intent(mContext, HistorialActivity.class));
        });
        
        binding.btnLogout.setOnClickListener(v -> {
            // Cerrar sesión de Firebase
            firebaseAuth.signOut();
            
            // Cerrar sesión de Google también
            GoogleSignIn.getClient(mContext, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
            
            startActivity(new Intent(mContext, OpcionesLoginActivity.class));
            getActivity().finishAffinity();
        });
        
        // Cargar información del usuario
        loadUserInfo();
    }
    
    private void loadUserInfo() {
        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }
        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // Usuario no existe en la base de datos, crear datos por defecto
                            crearDatosUsuarioPorDefecto();
                            return;
                        }
                        
                        String nombres = snapshot.child("names").getValue() != null ? 
            snapshot.child("names").getValue().toString() : "Usuario";
        String email = snapshot.child("email").getValue() != null ? 
            snapshot.child("email").getValue().toString() : "";
        String telefono = snapshot.child("telefono").getValue() != null ? 
            snapshot.child("telefono").getValue().toString() : "";
        String fechaNacimiento = snapshot.child("fechaNacimiento").getValue() != null ? 
            snapshot.child("fechaNacimiento").getValue().toString() : "";
        String proveedor = snapshot.child("proveedor").getValue() != null ? 
            snapshot.child("proveedor").getValue().toString() : "";
                        Object registroObj = snapshot.child("date").getValue();
                        String imagen = snapshot.child("imagen").getValue() != null ? 
                            snapshot.child("imagen").getValue().toString() : "";
                        
                        // Información específica del programa de fidelidad
                        Object puntosObj = snapshot.child("puntos").getValue();
                        String nivel = snapshot.child("nivel").getValue() != null ? 
                            snapshot.child("nivel").getValue().toString() : "";
                        Object totalComprasObj = snapshot.child("totalCompras").getValue();
                        Object ultimaVisitaObj = snapshot.child("ultimaVisita").getValue();

                        // Procesar fecha de registro
                        long registroTimestamp = 0;
                        if (registroObj != null) {
                            try {
                                registroTimestamp = Long.parseLong(registroObj.toString());
                            } catch (NumberFormatException e) {
                                registroTimestamp = System.currentTimeMillis();
                            }
                        } else {
                            registroTimestamp = System.currentTimeMillis();
                        }
                        String date = Contantes.DateFormat(registroTimestamp);
                        
                        // Procesar puntos
                        int puntos = 0;
                        if (puntosObj != null) {
                            try {
                                puntos = Integer.parseInt(puntosObj.toString());
                            } catch (NumberFormatException e) {
                                puntos = 0;
                            }
                        }
                        
                        // Procesar total de compras
                        double totalCompras = 0;
                        if (totalComprasObj != null) {
                            try {
                                totalCompras = Double.parseDouble(totalComprasObj.toString());
                            } catch (NumberFormatException e) {
                                totalCompras = 0;
                            }
                        }
                        
                        // Procesar última visita
                        String ultimaVisita = "No disponible";
                        if (ultimaVisitaObj != null) {
                            try {
                                long ultimaVisitaTimestamp = Long.parseLong(ultimaVisitaObj.toString());
                                ultimaVisita = Contantes.DateFormat(ultimaVisitaTimestamp);
                            } catch (NumberFormatException e) {
                                ultimaVisita = "No disponible";
                            }
                        }
                        
                        // Calcular nivel si no existe
                        if (nivel.isEmpty()) {
                            nivel = Contantes.calcularNivel(puntos);
                        }

                        // Establecer valores en la UI
                        binding.tvNombres.setText(nombres);
                        binding.tvEmail.setText(email);
                        if (binding.tvTelefono != null) {
                            binding.tvTelefono.setText(telefono.isEmpty() ? "No especificado" : telefono);
                        }
                        if (binding.tvFechaNacimiento != null) {
                            binding.tvFechaNacimiento.setText(fechaNacimiento.isEmpty() ? "No especificado" : fechaNacimiento);
                        }
                        binding.tvRegistro.setText("Miembro desde: " + date);
                        
                        // Información del programa de fidelidad
                        if (binding.tvPuntos != null) {
                            binding.tvPuntos.setText(String.valueOf(puntos));
                        }
                        if (binding.tvNivel != null) {
                            binding.tvNivel.setText(nivel);
                        }
                        if (binding.tvComprasTotales != null) {
                            binding.tvComprasTotales.setText("$" + String.format("%.2f", totalCompras));
                        }
                        if (binding.tvUltimaVisita != null) {
                            binding.tvUltimaVisita.setText(ultimaVisita);
                        }
                        
                        // Mostrar puntos para siguiente nivel
                        if (binding.tvPuntosSiguiente != null) {
                            int puntosParaSiguiente = Contantes.puntosParaSiguienteNivel(puntos);
                            if (puntosParaSiguiente > 0) {
                                binding.tvPuntosSiguiente.setText(String.valueOf(puntosParaSiguiente));
                            } else {
                                binding.tvPuntosSiguiente.setText("0");
                            }
                        }

                        Glide.with(mContext)
                                .load(imagen)
                                .placeholder(R.drawable.ic_account)
                                .into(binding.itemPerfil);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Manejar error
                    }
                });
    }
    
    private void crearDatosUsuarioPorDefecto() {
        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }
        
        String uid = firebaseAuth.getUid();
        String email = firebaseAuth.getCurrentUser().getEmail();
        String nombre = firebaseAuth.getCurrentUser().getDisplayName();
        
        if (nombre == null || nombre.isEmpty()) {
            nombre = "Usuario";
        }
        
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("names", nombre);
        userData.put("email", email != null ? email : "");
        userData.put("telefono", "");
        userData.put("fechaNacimiento", "");
        userData.put("proveedor", "Email");
        userData.put("date", System.currentTimeMillis());
        userData.put("imagen", "");
        userData.put("puntos", 0);
        userData.put("nivel", "Bronce");
        userData.put("totalCompras", 0.0);
        userData.put("ultimaVisita", System.currentTimeMillis());
        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    // Datos creados exitosamente, recargar información
                    loadUserInfo();
                })
                .addOnFailureListener(e -> {
                    // Error al crear datos
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}