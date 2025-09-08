package com.example.cafefidelidadqr;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

// MainActivity ya no necesita implementar RecyclerViewInterface
public class MainActivity extends AppCompatActivity {

    // Las variables y métodos relacionados con RecyclerView se han movido a CafeListFragment
    // ArrayList<cafeModel> cafeModels = new ArrayList<>();
    // int[] cafeImages = {R.drawable.nescafe___file, ... };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Cargar el CafeListFragment
        if (savedInstanceState == null) { // Solo añadir el fragmento si no se está restaurando de un estado previo
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            CafeListFragment cafeListFragment = new CafeListFragment();
            // R.id.fragment_container_view es el ID del FragmentContainerView en activity_main.xml
            fragmentTransaction.replace(R.id.fragment_container_view, cafeListFragment);
            fragmentTransaction.commit();
        }

        // El listener para WindowInsets se aplica al View raíz de la Activity (R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // El método setUpCafeModels() se ha movido a CafeListFragment
    /*
    private void setUpCafeModels(){
        ...
    }
    */

    // El método onItemClick(int position) se ha movido a CafeListFragment
    /*
    @Override
    public void onItemClick(int position) {
        ...
    }
    */
}
