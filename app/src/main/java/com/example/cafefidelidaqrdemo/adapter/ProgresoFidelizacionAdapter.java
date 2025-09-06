package com.example.cafefidelidaqrdemo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.model.Beneficio;
import com.example.cafefidelidaqrdemo.viewmodel.ProgresoViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para mostrar el progreso hacia próximos beneficios
 */
public class ProgresoFidelizacionAdapter extends RecyclerView.Adapter<ProgresoFidelizacionAdapter.ProgresoViewHolder> {
    
    private List<ProgresoViewModel.ProximoBeneficio> proximosBeneficios;
    
    public ProgresoFidelizacionAdapter() {
        this.proximosBeneficios = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public ProgresoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_progreso_beneficio, parent, false);
        return new ProgresoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProgresoViewHolder holder, int position) {
        ProgresoViewModel.ProximoBeneficio proximoBeneficio = proximosBeneficios.get(position);
        holder.bind(proximoBeneficio);
    }
    
    @Override
    public int getItemCount() {
        return proximosBeneficios.size();
    }
    
    public void updateProximosBeneficios(List<ProgresoViewModel.ProximoBeneficio> nuevosProximosBeneficios) {
        this.proximosBeneficios.clear();
        if (nuevosProximosBeneficios != null) {
            this.proximosBeneficios.addAll(nuevosProximosBeneficios);
        }
        notifyDataSetChanged();
    }
    
    static class ProgresoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNombreBeneficio;
        private final TextView tvDescripcionBeneficio;
        private final TextView tvProgresoTexto;
        private final ProgressBar progressBar;
        private final TextView tvVisitasRequeridas;
        private final TextView tvVisitasFaltantes;
        
        public ProgresoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreBeneficio = itemView.findViewById(R.id.tv_nombre_beneficio);
            tvDescripcionBeneficio = itemView.findViewById(R.id.tv_descripcion_beneficio);
            tvProgresoTexto = itemView.findViewById(R.id.tv_progreso_texto);
            progressBar = itemView.findViewById(R.id.progress_bar);
            tvVisitasRequeridas = itemView.findViewById(R.id.tv_visitas_requeridas);
            tvVisitasFaltantes = itemView.findViewById(R.id.tv_visitas_faltantes);
        }
        
        public void bind(ProgresoViewModel.ProximoBeneficio proximoBeneficio) {
            Beneficio beneficio = proximoBeneficio.getBeneficio();
            
            // Información del beneficio
            tvNombreBeneficio.setText(beneficio.getNombre());
            tvDescripcionBeneficio.setText(beneficio.getDescripcion());
            
            // Progreso
            int progreso = (int) (proximoBeneficio.getProgreso() * 100);
            progressBar.setProgress(progreso);
            tvProgresoTexto.setText(progreso + "%");
            
            // Visitas
            tvVisitasRequeridas.setText("Visitas requeridas: " + proximoBeneficio.getVisitasRequeridas());
            
            if (proximoBeneficio.getVisitasFaltantes() > 0) {
                tvVisitasFaltantes.setText("Te faltan " + proximoBeneficio.getVisitasFaltantes() + " visitas");
                tvVisitasFaltantes.setVisibility(View.VISIBLE);
            } else {
                tvVisitasFaltantes.setText("¡Beneficio disponible!");
                tvVisitasFaltantes.setVisibility(View.VISIBLE);
            }
        }
    }
}