package com.example.cafefidelidaqrdemo.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.database.entities.ReporteEntity;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ReportesAdapter extends RecyclerView.Adapter<ReportesAdapter.ReporteViewHolder> {
    
    private List<ReporteEntity> reportes;
    private OnReporteClickListener listener;
    private SimpleDateFormat dateFormat;
    
    public interface OnReporteClickListener {
        void onReporteClick(ReporteEntity reporte);
        void onReporteExportClick(ReporteEntity reporte);
    }
    
    public ReportesAdapter() {
        this.reportes = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }
    
    public void setOnReporteClickListener(OnReporteClickListener listener) {
        this.listener = listener;
    }
    
    public void updateReportes(List<ReporteEntity> nuevosReportes) {
        this.reportes.clear();
        if (nuevosReportes != null) {
            this.reportes.addAll(nuevosReportes);
        }
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ReporteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reporte, parent, false);
        return new ReporteViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ReporteViewHolder holder, int position) {
        ReporteEntity reporte = reportes.get(position);
        holder.bind(reporte);
    }
    
    @Override
    public int getItemCount() {
        return reportes.size();
    }
    
    public class ReporteViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvTitulo;
        private TextView tvTipo;
        private TextView tvFecha;
        private TextView tvSucursal;
        private TextView tvEstado;
        private TextView tvDescripcion;
        
        public ReporteViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvTitulo = itemView.findViewById(R.id.tv_titulo_reporte);
            tvTipo = itemView.findViewById(R.id.tv_tipo_reporte);
            tvFecha = itemView.findViewById(R.id.tv_fecha_reporte);
            tvSucursal = itemView.findViewById(R.id.tv_sucursal_reporte);
            tvEstado = itemView.findViewById(R.id.tv_estado_reporte);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion_reporte);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onReporteClick(reportes.get(getAdapterPosition()));
                }
            });
            
            View btnExport = itemView.findViewById(R.id.btn_export_reporte);
            if (btnExport != null) {
                btnExport.setOnClickListener(v -> {
                    if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onReporteExportClick(reportes.get(getAdapterPosition()));
                    }
                });
            }
        }
        
        public void bind(ReporteEntity reporte) {
            // Usar tipoReporte como título
            tvTitulo.setText(reporte.getTipoReporte() != null ? "Reporte de " + reporte.getTipoReporte() : "Reporte");
            tvTipo.setText(reporte.getTipoReporte() != null ? reporte.getTipoReporte() : "General");
            tvFecha.setText(reporte.getFechaGeneracion() != null ? dateFormat.format(reporte.getFechaGeneracion()) : "N/A");
            tvSucursal.setText(reporte.getSucursalId() != null ? "Sucursal: " + reporte.getSucursalId() : "Todas las sucursales");
            // Usar sincronizado como estado
            tvEstado.setText(reporte.isSincronizado() ? "Sincronizado" : "Pendiente");
            // Usar resumen de métricas como descripción
            tvDescripcion.setText(reporte.getResumenMetricas() != null ? reporte.getResumenMetricas() : "Sin descripción");
        }
    }
}