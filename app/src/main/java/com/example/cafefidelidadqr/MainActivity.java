package com.example.cafefidelidadqr;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
// import androidx.core.view.ViewCompat; // Removed as potentially unused and to ensure ViewCompat.setOnApplyWindowInsetsListener below is correctly referenced
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecyclerViewInterface {

    ArrayList<cafeModel> cafeModels = new ArrayList<>();
    int[] cafeImages = {R.drawable.nescafe___file, R.drawable.baseline_coffee_24, R.drawable.nescafe___file, R.drawable.baseline_coffee_24, R.drawable.nescafe___file, R.drawable.nescafe___file, R.drawable.baseline_coffee_24, R.drawable.baseline_coffee_24, R.drawable.baseline_coffee_24, R.drawable.baseline_coffee_24, R.drawable.baseline_coffee_24};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.catalogoRecyclerView);
        setUpCafeModels();

        CafeRecyclerViewAdapter adapter = new CafeRecyclerViewAdapter(this, cafeModels, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Ensuring ViewCompat is used from androidx.core.view.ViewCompat
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setUpCafeModels(){
        String[] nombresCafes = getResources().getStringArray(R.array.tipos_de_cafes);
        String[] preciosCafes = getResources().getStringArray(R.array.precios_de_cafes);

        for (int i = 0; i < nombresCafes.length; i++) {
            cafeModels.add(new cafeModel(nombresCafes[i],
                    preciosCafes[i],
                    cafeImages[i]));
        }
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(MainActivity.this, MainActivity2.class);

        intent.putExtra("Nombre", cafeModels.get(position).getNombreCafe());
        intent.putExtra("Precio", cafeModels.get(position).getPrecioCafe());
        intent.putExtra("Imagen", cafeModels.get(position).getImagen());

        startActivity(intent);

    }
}
