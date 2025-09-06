package com.example.cafefidelidaqrdemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.models.HistorialItem;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Adaptador para mostrar la lista de historial de visitas y canjes
 */
public class HistorialAdapter extends ListAdapter<HistorialItem, HistorialAdapter.HistorialViewHolder> {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    
    private OnItemClickListener onItemClickListener;
    private Context context;
    
    public interface OnItemClickListener {
        void onItemClick(HistorialItem item);
        void onItemLongClick(HistorialItem item);
    }
    
    public HistorialAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial, parent, false);
        return new HistorialViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        HistorialItem item = getItem(position);
        holder.bind(item);
    }
    
    class HistorialViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvTitulo;
        private TextView tvSubtitulo;
        private TextView tvFecha;
        private TextView tvEstado;
        private ImageView ivTipo;
        private ImageView ivEstado;
        private TextView tvInfoAdicional;
        private View indicadorSync;
        
        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvSubtitulo = itemView.findViewById(R.id.tv_subtitulo);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvEstado = itemView.findViewById(R.id.tv_estado);
            ivTipo = itemView.findViewById(R.id.iv_tipo);
            ivEstado = itemView.findViewById(R.id.iv_estado);
            tvInfoAdicional = itemView.findViewById(R.id.tv_info_adicional);
            indicadorSync = itemView.findViewById(R.id.indicador_sync);
            
            // Configurar click listeners
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(getItem(position));
                    }
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemLongClick(getItem(position));
                        return true;
                    }
                }
                return false;
            });
        }
        
        public void bind(HistorialItem item) {
            // Título y subtítulo
            tvTitulo.setText(item.getTitulo());
            tvSubtitulo.setText(item.getSubtitulo());
            
            // Fecha
            if (item.getFechaHora() != null) {
                tvFecha.setText(DATE_FORMAT.format(item.getFechaHora()));
            } else {
                tvFecha.setText("Fecha no disponible");
            }
            
            // Estado de sincronización
            tvEstado.setText(item.getTextoEstado());
            
            // Icono de tipo (visita o canje)
            if (item.getTipo() == HistorialItem.Tipo.VISITA) {
                ivTipo.setImageResource(R.drawable.ic_location); // Icono de ubicación para visitas
                ivTipo.setColorFilter(ContextCompat.getColor(context, R.color.color_visita));
            } else {
                ivTipo.setImageResource(R.drawable.ic_gift); // Icono de regalo para canjes
                ivTipo.setColorFilter(ContextCompat.getColor(context, R.color.color_canje));
            }
            
            // Icono y color de estado de sincronización
            configurarEstadoSync(item);
            
            // Información adicional
            String infoAdicional = item.getInfoAdicional();
            if (infoAdicional != null && !infoAdicional.trim().isEmpty()) {
                tvInfoAdicional.setVisibility(View.VISIBLE);
                tvInfoAdicional.setText(infoAdicional);
            } else {
                tvInfoAdicional.setVisibility(View.GONE);
            }
            
            // Indicador visual de sincronización
            if (item.isPendienteSync()) {
                indicadorSync.setVisibility(View.VISIBLE);
                indicadorSync.setBackgroundColor(ContextCompat.getColor(context, R.color.color_pendiente));
            } else if (item.isError()) {
                indicadorSync.setVisibility(View.VISIBLE);
                indicadorSync.setBackgroundColor(ContextCompat.getColor(context, R.color.color_error));
            } else {
                indicadorSync.setVisibility(View.GONE);
            }
        }
        
        private void configurarEstadoSync(HistorialItem item) {
            switch (item.getEstadoSync()) {
                case "PENDIENTE":
                    ivEstado.setImageResource(R.drawable.ic_pending);
                    ivEstado.setColorFilter(ContextCompat.getColor(context, R.color.color_pendiente));
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.color_pendiente));
                    break;
                    
                case "ENVIADO":
                    ivEstado.setImageResource(R.drawable.ic_check_circle);
                    ivEstado.setColorFilter(ContextCompat.getColor(context, R.color.color_enviado));
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.color_enviado));
                    break;
                    
                case "ERROR":
                    ivEstado.setImageResource(R.drawable.ic_error);
                    ivEstado.setColorFilter(ContextCompat.getColor(context, R.color.color_error));
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.color_error));
                    break;
                    
                default:
                    ivEstado.setImageResource(R.drawable.ic_redeem);
                    ivEstado.setColorFilter(ContextCompat.getColor(context, R.color.color_texto_secundario));
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.color_texto_secundario));
                    break;
            }
        }
    }
    
    private static final DiffUtil.ItemCallback<HistorialItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<HistorialItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull HistorialItem oldItem, @NonNull HistorialItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull HistorialItem oldItem, @NonNull HistorialItem newItem) {
            return oldItem.equals(newItem) &&
                    oldItem.getEstadoSync().equals(newItem.getEstadoSync()) &&
                    oldItem.getTipo() == newItem.getTipo();
        }
    };
}