package com.example.cafefidelidadqr.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.cafefidelidadqr.EditarPerfilActivity;
import com.example.cafefidelidadqr.OpcionesLoginActivity;
import com.example.cafefidelidadqr.R;
import com.example.cafefidelidadqr.databinding.FragmentProfBinding;
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

    private FragmentProfBinding binding;
    private Context mContext;
    private FirebaseAuth firebaseAuth;

    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    public FragmentPerfil() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        firebaseAuth = FirebaseAuth.getInstance();
        
        loadUserInfo();
        
        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                GoogleSignIn.getClient(mContext, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
                startActivity(new Intent(mContext, OpcionesLoginActivity.class));
                getActivity().finish();
            }
        });
        
        binding.UpdateUserData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Abrir actividad para editar perfil
                startActivity(new Intent(mContext, EditarPerfilActivity.class));
            }
        });
    }

    private void loadUserInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String uid = "" + snapshot.child("uid").getValue();
                            String nombres = "" + snapshot.child("nombres").getValue();
                            String email = "" + snapshot.child("email").getValue();
                            String proveedor = "" + snapshot.child("proveedor").getValue();
                            String fechaRegistro = "" + snapshot.child("fechaRegistro").getValue();
                            String imagen = "" + snapshot.child("imagen").getValue();

                            // Configurar información del usuario
                            binding.tvNombres.setText(nombres);
                            binding.tvEmail.setText(email);
                            binding.tvRegistro.setText(fechaRegistro);
                            
                            // Cargar imagen de perfil
                            try {
                                if (!imagen.equals("null") && !imagen.isEmpty()) {
                                    Glide.with(mContext)
                                            .load(imagen)
                                            .placeholder(R.drawable.ic_account)
                                            .into(binding.itemPerfil);
                                } else {
                                    binding.itemPerfil.setImageResource(R.drawable.ic_account);
                                }
                            } catch (Exception e) {
                                binding.itemPerfil.setImageResource(R.drawable.ic_account);
                            }
                        } else {
                            // Si no existen datos del usuario, crear datos por defecto
                            crearDatosUsuarioPorDefecto();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Manejar error
                    }
                });
    }

    private void crearDatosUsuarioPorDefecto() {
        String fechaRegistro = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
        
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", firebaseAuth.getUid());
        hashMap.put("nombres", firebaseAuth.getCurrentUser().getDisplayName() != null ? 
                   firebaseAuth.getCurrentUser().getDisplayName() : "Usuario");
        hashMap.put("email", firebaseAuth.getCurrentUser().getEmail());
        hashMap.put("proveedor", "Firebase");
        hashMap.put("estado", "online");
        hashMap.put("imagen", "");
        hashMap.put("fechaRegistro", fechaRegistro);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .setValue(hashMap)
                .addOnSuccessListener(aVoid -> {
                    // Datos creados exitosamente
                })
                .addOnFailureListener(e -> {
                    // Error al crear datos
                });
    }
}