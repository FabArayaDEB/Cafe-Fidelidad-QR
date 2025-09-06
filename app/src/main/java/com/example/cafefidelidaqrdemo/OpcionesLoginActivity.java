package com.example.cafefidelidaqrdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cafefidelidaqrdemo.databinding.ActivityOpcionesLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class OpcionesLoginActivity extends AppCompatActivity {

    private ActivityOpcionesLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressDialog progressDialog;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOpcionesLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        
        // Configurar ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Iniciando sesión");
        progressDialog.setCanceledOnTouchOutside(false);

        // Configurar Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configurar listeners
        binding.typeEmail.setOnClickListener(v -> {
            startActivity(new Intent(OpcionesLoginActivity.this, LoginActivity.class));
        });

        binding.typeGoogle.setOnClickListener(v -> {
            signInWithGoogle();
        });
    }

    private void signInWithGoogle() {
        progressDialog.setMessage("Conectando con Google...");
        progressDialog.show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                // Google Sign In was successful, authenticate with Firebase
                com.google.android.gms.auth.api.signin.GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed
                progressDialog.dismiss();
                Toast.makeText(this, "Error al iniciar sesión con Google: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        progressDialog.setMessage("Autenticando con Firebase...");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success - verificar si el usuario existe en la base de datos
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkUserExistsInDatabase(user);
                    } else {
                        // Sign in failed
                        progressDialog.dismiss();
                        Toast.makeText(OpcionesLoginActivity.this, 
                            "Error de autenticación: " + task.getException().getMessage(), 
                            Toast.LENGTH_LONG).show();
                        task.getException().printStackTrace();
                    }
                });
    }
    
    private void checkUserExistsInDatabase(FirebaseUser user) {
        progressDialog.setMessage("Configurando tu perfil...");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Usuario ya existe, sincronizar con SQLite local
                            HashMap<String, Object> existingUserData = new HashMap<>();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                existingUserData.put(child.getKey(), child.getValue());
                            }
                            
                            OfflineManager offlineManager = OfflineManager.getInstance(OpcionesLoginActivity.this);
                            offlineManager.saveUserToLocal(existingUserData, new OfflineManager.SaveUserCallback() {
                                @Override
                                public void onSuccess() {
                                    runOnUiThread(() -> {
                                        progressDialog.dismiss();
                                        startActivity(new Intent(OpcionesLoginActivity.this, MainActivity.class));
                                        finish();
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    runOnUiThread(() -> {
                                        progressDialog.dismiss();
                                        startActivity(new Intent(OpcionesLoginActivity.this, MainActivity.class));
                                        finish();
                                    });
                                }
                            });
                        } else {
                            // Usuario nuevo, crear datos por defecto
                            createUserDataForGoogleSignIn(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(OpcionesLoginActivity.this, 
                            "Error al verificar datos de usuario", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void createUserDataForGoogleSignIn(FirebaseUser user) {
        progressDialog.setMessage("Creando tu programa de fidelidad...");
        
        String uid = user.getUid();
        String name = user.getDisplayName() != null ? user.getDisplayName() : "Usuario";
        String email = user.getEmail() != null ? user.getEmail() : "";
        String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
        long currentTime = System.currentTimeMillis();
        
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("names", name);
        userData.put("email", email);
        userData.put("proveedor", "google");
        userData.put("estado", "activo");
        userData.put("imagen", photoUrl);
        userData.put("date", currentTime);
        userData.put("puntos", Contantes.PUNTOS_BIENVENIDA); // Puntos de bienvenida
        userData.put("nivel", Contantes.NIVEL_BRONCE); // Nivel inicial
        userData.put("totalCompras", 0.0);
        userData.put("ultimaVisita", currentTime);
        userData.put("telefono", ""); // Campo para RF-02
        userData.put("fechaNacimiento", ""); // Campo para RF-02
        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).setValue(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Guardar también en SQLite local usando OfflineManager
                        OfflineManager offlineManager = OfflineManager.getInstance(OpcionesLoginActivity.this);
                        offlineManager.saveUserToLocal(userData, new OfflineManager.SaveUserCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(OpcionesLoginActivity.this, 
                                        "¡Bienvenido! Has recibido " + Contantes.PUNTOS_BIENVENIDA + " puntos de bienvenida", 
                                        Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(OpcionesLoginActivity.this, MainActivity.class));
                                    finish();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(OpcionesLoginActivity.this, 
                                        "Usuario registrado pero error al guardar localmente: " + error, 
                                        Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(OpcionesLoginActivity.this, MainActivity.class));
                                    finish();
                                });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(OpcionesLoginActivity.this, 
                            "Error al crear perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}