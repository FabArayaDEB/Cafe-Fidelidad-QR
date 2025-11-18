package com.example.cafefidelidaqrdemo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.models.ResenaSucursal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResenasSucursalAdapter extends RecyclerView.Adapter<ResenasSucursalAdapter.ViewHolder> {

    private List<ResenaSucursal> resenas = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public void setResenas(List<ResenaSucursal> resenas) {
        this.resenas = resenas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usamos item_resena_admin porque ya tiene la estructura necesaria
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resena_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ResenaSucursal resena = resenas.get(position);
        holder.ratingBar.setRating(resena.getCalificacion());
        holder.tvComentario.setText(resena.getComentario());

        // Aquí deberías buscar el nombre del usuario si es posible,
        // por ahora mostramos ID o "Cliente"
        holder.tvUsuario.setText("Cliente #" + resena.getClienteId());

        holder.tvFecha.setText(dateFormat.format(new Date(resena.getFechaCreacion())));

        // Ocultar botón de eliminar ya que es vista de cliente
        //holder.itemView.findViewById(R.id.btn_eliminar_resena).setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return resenas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RatingBar ratingBar;
        TextView tvUsuario, tvFecha, tvComentario;

        ViewHolder(View itemView) {
            super(itemView);
            // --- CAMBIOS: Usar los IDs correctos de item_resena_admin.xml ---
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvUsuario = itemView.findViewById(R.id.txtTitle);      // Antes tv_usuario_resena
            tvFecha = itemView.findViewById(R.id.txtSubtitle);     // Antes tv_fecha_resena
            tvComentario = itemView.findViewById(R.id.txtComment); // Antes tv_comentario_resena

            // Ocultar el texto de "Producto/Sucursal" ya que estamos DENTRO de la sucursal
            TextView txtContext = itemView.findViewById(R.id.txtContext);
            if(txtContext != null) txtContext.setVisibility(View.GONE);
        }
    }
}