package com.example.cafefidelidaqrdemo.ui.cliente;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.adapters.BeneficiosDisponiblesAdapter;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.managers.BeneficioManager;
import com.example.cafefidelidaqrdemo.models.Beneficio;
import com.example.cafefidelidaqrdemo.models.Visita;
import com.example.cafefidelidaqrdemo.viewmodels.MisBeneficiosViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment actualizado con sistema de sellos digitales (tarjeta virtual de fidelización)
 */
public class FragmentProgresoFidelizacion extends Fragment {

    private static final int TOTAL_SELLOS = 7; // Cantidad total para canjear un beneficio

    // Views
    private LinearLayout sellosContainer;
    private TextView textProgresoNumero;
    private TextView textProgresoDescripcion;
    private Button btnCanjear;
    private RecyclerView recyclerBeneficiosDisponibles;

    // Lógica y datos
    private CafeFidelidadDB db;
    private BeneficioManager beneficioManager;
    private MisBeneficiosViewModel beneficioViewModel;
    private BeneficiosDisponiblesAdapter beneficiosAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progreso_fidelizacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sellosContainer = view.findViewById(R.id.sellosContainer);
        textProgresoNumero = view.findViewById(R.id.textProgresoNumero);
        textProgresoDescripcion = view.findViewById(R.id.textProgresoDescripcion);
        btnCanjear = view.findViewById(R.id.btnCanjear);
        recyclerBeneficiosDisponibles = view.findViewById(R.id.recyclerViewBeneficiosDisponibles);

        // Inicializaciones
        db = CafeFidelidadDB.getInstance(getContext());
        beneficioManager = new BeneficioManager(requireContext());
        beneficioViewModel = new ViewModelProvider(this).get(MisBeneficiosViewModel.class);

        // Configurar RecyclerView
        beneficiosAdapter = new BeneficiosDisponiblesAdapter(new ArrayList<>(), this::onBeneficioClick);
        recyclerBeneficiosDisponibles.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerBeneficiosDisponibles.setAdapter(beneficiosAdapter);

        // Cargar progreso inicial
        actualizarProgreso();

        // Cargar lista de beneficios
        beneficioViewModel.refreshBeneficios();
        beneficioViewModel.getBeneficiosDisponibles().observe(getViewLifecycleOwner(), beneficios -> {
            if (beneficios != null) beneficiosAdapter.updateBeneficios(beneficios);
        });

        // Acción de canje
        btnCanjear.setOnClickListener(v -> realizarCanje());
    }

    /**
     * Actualiza la interfaz mostrando los sellos y el progreso del cliente.
     */
    private void actualizarProgreso() {
        int clienteId = 1; // Temporal hasta implementar sesión real
        List<Visita> visitasCliente = db.obtenerVisitasPorCliente(clienteId);

        int sellosActuales = visitasCliente.size();
        boolean beneficioDisponible = beneficioManager.verificarBeneficioPorSellos(visitasCliente, TOTAL_SELLOS);
        int faltantes = beneficioManager.obtenerSellosRestantes(visitasCliente, TOTAL_SELLOS);

        // Actualizar textos
        textProgresoNumero.setText(sellosActuales + "/" + TOTAL_SELLOS);
        textProgresoDescripcion.setText(
                beneficioDisponible
                        ? "🎉 ¡Puedes canjear tu café gratis!"
                        : "☕ Te faltan " + faltantes + " visitas para tu próximo beneficio"
        );

        btnCanjear.setEnabled(beneficioDisponible);

        // Dibujar sellos visuales
        sellosContainer.removeAllViews();
        for (int i = 0; i < TOTAL_SELLOS; i++) {
            ImageView sello = new ImageView(getContext());

            // Si el cliente completó todos los sellos, el último se muestra dorado
            if (beneficioDisponible && i == TOTAL_SELLOS - 1) {
                sello.setImageResource(R.drawable.ic_cafe_dorado);
            } else if (i < sellosActuales) {
                sello.setImageResource(R.drawable.ic_cafe_lleno);
            } else {
                sello.setImageResource(R.drawable.ic_cafe_vacio);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
            params.setMargins(8, 0, 8, 0);
            sello.setLayoutParams(params);
            sellosContainer.addView(sello);
        }
    }

    /**
     * Realiza el canje del beneficio si el usuario completó los sellos.
     */
    private void realizarCanje() {
        int clienteId = 1; // Temporal
        List<Visita> visitasCliente = db.obtenerVisitasPorCliente(clienteId);

        boolean beneficioDisponible = beneficioManager.verificarBeneficioPorSellos(visitasCliente, TOTAL_SELLOS);
        if (beneficioDisponible) {
            Toast.makeText(getContext(), "🎉 ¡Café gratis canjeado con éxito!", Toast.LENGTH_SHORT).show();

            // Registrar canje en la base de datos
            beneficioManager.registrarCanjePorSellos(clienteId, TOTAL_SELLOS);

            // Reiniciar visitas después del canje
            db.reiniciarVisitasCliente(clienteId);

            // Refrescar progreso visual
            actualizarProgreso();
        } else {
            Toast.makeText(getContext(), "⚠️ Aún no completas los sellos necesarios", Toast.LENGTH_SHORT).show();
        }
    }

    private void onBeneficioClick(Beneficio beneficio) {
        Toast.makeText(getContext(), "Beneficio: " + beneficio.getNombre(), Toast.LENGTH_SHORT).show();
    }
}
