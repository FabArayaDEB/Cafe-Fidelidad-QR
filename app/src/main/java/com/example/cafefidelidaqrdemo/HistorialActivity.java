package com.example.cafefidelidaqrdemo;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafefidelidaqrdemo.adapters.HistorialAdapter;
import com.example.cafefidelidaqrdemo.databinding.ActivityHistorialBinding;
import com.example.cafefidelidaqrdemo.models.Canje;
import com.example.cafefidelidaqrdemo.models.Visita;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.database.DataSnapshot;
// import com.google.firebase.database.DatabaseError;
// import com.google.firebase.database.DatabaseReference;
// import com.google.firebase.database.FirebaseDatabase;
// import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private ActivityHistorialBinding binding;
    // private FirebaseAuth firebaseAuth;
    private HistorialAdapter historialAdapter;
    private List<Object> historialItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // firebaseAuth = FirebaseAuth.getInstance();
        historialItems = new ArrayList<>();

        setupRecyclerView();
        loadHistorial();

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupRecyclerView() {
        historialAdapter = new HistorialAdapter(this);
        binding.recyclerViewHistorial.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewHistorial.setAdapter(historialAdapter);
    }

    private void loadHistorial() {
        /*
        // String userId = firebaseAuth.getCurrentUser().getUid();
        
        // Cargar visitas
        // DatabaseReference visitasRef = FirebaseDatabase.getInstance().getReference("Visitas");
        // visitasRef.orderByChild("userId").equalTo(userId)
        //         .addValueEventListener(new ValueEventListener() {
        //             @Override
        //             public void onDataChange(@NonNull DataSnapshot snapshot) {
        //                 // Limpiar visitas anteriores
        //                 historialItems.removeIf(item -> item instanceof Visita);
        //                 
        //                 for (DataSnapshot visitaSnapshot : snapshot.getChildren()) {
        //                     Visita visita = visitaSnapshot.getValue(Visita.class);
        */
        
        // Historial deshabilitado sin Firebase
        historialItems.clear();
        // historialAdapter.updateHistorial(historialItems); // Método no existe
        // historialAdapter.submitList(historialItems); // Requiere List<HistorialItem>
        // Historial deshabilitado
        /*
        //                     if (visita != null) {
        //                         historialItems.add(visita);
        //                     }
        //                 }
        //                 
        //                 sortAndUpdateHistorial();
        //             }
        //
        //             @Override
        //             public void onCancelled(@NonNull DatabaseError error) {
        //                 // Manejar error
        //             }
        //         });

        // Cargar canjes
        // DatabaseReference canjesRef = FirebaseDatabase.getInstance().getReference("Canjes");
        // canjesRef.orderByChild("userId").equalTo(userId)
        //         .addValueEventListener(new ValueEventListener() {
        //             @Override
        //             public void onDataChange(@NonNull DataSnapshot snapshot) {
        //                 // Limpiar canjes anteriores
        //                 historialItems.removeIf(item -> item instanceof Canje);
        //                 
        //                 for (DataSnapshot canjeSnapshot : snapshot.getChildren()) {
        //                     Canje canje = canjeSnapshot.getValue(Canje.class);
        //                     if (canje != null) {
        //                         historialItems.add(canje);
        //                     }
        //                 }
        //                 
        //                 sortAndUpdateHistorial();
        //             }
        //
        //             @Override
        //             public void onCancelled(@NonNull DatabaseError error) {
        //                 // Manejar error
        //             }
        //         });
        */
    }

    private void sortAndUpdateHistorial() {
        // Ordenar por fecha (más reciente primero)
        Collections.sort(historialItems, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                long fecha1 = 0;
                long fecha2 = 0;
                
                if (o1 instanceof Visita) {
                    fecha1 = ((Visita) o1).getFechaVisita();
                } else if (o1 instanceof Canje) {
                    fecha1 = ((Canje) o1).getFechaCanje();
                }
                
                if (o2 instanceof Visita) {
                    fecha2 = ((Visita) o2).getFechaVisita();
                } else if (o2 instanceof Canje) {
                    fecha2 = ((Canje) o2).getFechaCanje();
                }
                
                return Long.compare(fecha2, fecha1); // Orden descendente
            }
        });
        
        historialAdapter.notifyDataSetChanged();
        
        // Mostrar/ocultar mensaje de historial vacío
        if (historialItems.isEmpty()) {
            binding.textViewEmptyHistorial.setVisibility(View.VISIBLE);
            binding.recyclerViewHistorial.setVisibility(View.GONE);
        } else {
            binding.textViewEmptyHistorial.setVisibility(View.GONE);
            binding.recyclerViewHistorial.setVisibility(View.VISIBLE);
        }
    }
}