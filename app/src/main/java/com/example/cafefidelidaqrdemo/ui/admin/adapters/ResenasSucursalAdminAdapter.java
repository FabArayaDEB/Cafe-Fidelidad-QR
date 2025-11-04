package com.example.cafefidelidaqrdemo.ui.admin.adapters;

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
import java.util.List;
import java.util.Locale;

public class ResenasSucursalAdminAdapter extends RecyclerView.Adapter<ResenasSucursalAdminAdapter.ViewHolder> {

    private List<ResenaSucursal> data;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private java.util.Map<Integer, String> userNames = new java.util.HashMap<>();
    private String sucursalNombre = "";

    public ResenasSucursalAdminAdapter(List<ResenaSucursal> data) { this.data = data; }

    public void updateData(List<ResenaSucursal> newData) { this.data = newData; notifyDataSetChanged(); }

    public void setUserNames(java.util.Map<Integer, String> names) {
        this.userNames = names != null ? names : new java.util.HashMap<>();
        notifyDataSetChanged();
    }

    public void setSucursalNombre(String nombre) {
        this.sucursalNombre = nombre != null ? nombre : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resena_admin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ResenaSucursal r = data.get(position);
        String nombreUsuario = userNames.getOrDefault(r.getUsuarioId(), "Usuario #" + r.getUsuarioId());
        holder.title.setText(nombreUsuario);
        holder.context.setText(!sucursalNombre.isEmpty() ? ("Sucursal: " + sucursalNombre) : "");
        long fc = r.getFechaCreacion();
        holder.subtitle.setText(fc > 0 ? sdf.format(new java.util.Date(fc)) : "");
        holder.comment.setText(r.getComentario() != null && !r.getComentario().isEmpty() ? r.getComentario() : "Sin comentario");
        holder.ratingBar.setRating((float) r.getCalificacion());
    }

    @Override
    public int getItemCount() { return data != null ? data.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle, comment, context;
        RatingBar ratingBar;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtTitle);
            subtitle = itemView.findViewById(R.id.txtSubtitle);
            comment = itemView.findViewById(R.id.txtComment);
            context = itemView.findViewById(R.id.txtContext);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}