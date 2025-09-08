package com.example.cafefidelidadqr;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CafeListFragment extends Fragment implements RecyclerViewInterface {

    private ArrayList<cafeModel> cafeModels = new ArrayList<>();
    private int[] cafeImages = {
            R.drawable.nescafe___file, R.drawable.baseline_coffee_24, R.drawable.nescafe___file,
            R.drawable.baseline_coffee_24, R.drawable.nescafe___file, R.drawable.nescafe___file,
            R.drawable.baseline_coffee_24, R.drawable.baseline_coffee_24, R.drawable.baseline_coffee_24,
            R.drawable.baseline_coffee_24, R.drawable.baseline_coffee_24
    };

    private RecyclerView recyclerView;
    private CafeRecyclerViewAdapter adapter;

    public CafeListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cafe_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        setUpCafeModels();

        recyclerView = view.findViewById(R.id.catalogoRecyclerViewFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        

        adapter = new CafeRecyclerViewAdapter(requireContext(), cafeModels, this);
        recyclerView.setAdapter(adapter);
    }

    private void setUpCafeModels() {
        String[] nombresCafes = getResources().getStringArray(R.array.tipos_de_cafes);
        String[] preciosCafes = getResources().getStringArray(R.array.precios_de_cafes);

        cafeModels.clear(); 

        int numberOfItems = Math.min(nombresCafes.length, Math.min(preciosCafes.length, cafeImages.length));

        for (int i = 0; i < numberOfItems; i++) {
              cafeModels.add(new cafeModel(nombresCafes[i], preciosCafes[i], cafeImages[i]));
        }
    }

    @Override
    public void onItemClick(int position) {
        if (cafeModels != null && position >= 0 && position < cafeModels.size()) {
            Intent intent = new Intent(getActivity(), MainActivity2.class);

            intent.putExtra("Nombre", cafeModels.get(position).getNombreCafe());
            intent.putExtra("Precio", cafeModels.get(position).getPrecioCafe());
            intent.putExtra("Imagen", cafeModels.get(position).getImagen());

            startActivity(intent);
        }
    }
}
