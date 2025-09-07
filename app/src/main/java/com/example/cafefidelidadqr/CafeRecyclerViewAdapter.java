package com.example.cafefidelidadqr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CafeRecyclerViewAdapter extends RecyclerView.Adapter<CafeRecyclerViewAdapter.CafeViewHolder> {
    private final RecyclerViewInterface recyclerViewInterface;
    Context context;
    ArrayList<cafeModel> cafeModels;

    public CafeRecyclerViewAdapter(Context context, ArrayList<cafeModel> cafeModels, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.cafeModels = cafeModels;
        this.recyclerViewInterface = recyclerViewInterface;
    }
    @NonNull
    @Override
    public CafeRecyclerViewAdapter.CafeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //aqui se llena el layout revisando cada fila
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);
        return new CafeRecyclerViewAdapter.CafeViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull CafeRecyclerViewAdapter.CafeViewHolder holder, int position) {
        //asignamos los valores a los views que se crearon en recycle_view_row
        //basado en la posicion del recycler view
        holder.nombreCafe.setText(cafeModels.get(position).getNombreCafe());
        holder.precioCafe.setText(cafeModels.get(position).getPrecioCafe());
        holder.imageView.setImageResource(cafeModels.get(position).getImagen());
    }

    @Override
    public int getItemCount() {
        //el recycler view quiere saber el nuemro de items que quieres desplegados
        return cafeModels.size();
    }

    public static class CafeViewHolder extends RecyclerView.ViewHolder {
        //tomando los view del archivo recycler_view_row
        ImageView imageView;
        TextView nombreCafe, precioCafe;


        public CafeViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            nombreCafe = itemView.findViewById(R.id.textView);
            precioCafe = itemView.findViewById(R.id.textView2);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recyclerViewInterface != null) {
                        int pos = getBindingAdapterPosition();

                        if (pos != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }

            }
        }

