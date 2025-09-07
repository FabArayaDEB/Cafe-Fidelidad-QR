package com.example.cafefidelidadqr;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        String nombreCafe = getIntent().getStringExtra("Nombre");
        String precioCafe = getIntent().getStringExtra("Precio");
        int imagenCafe = getIntent().getIntExtra("Imagen", 0);

        TextView nombreTextView = findViewById(R.id.nombreTextView);
        TextView precioTextView = findViewById(R.id.precioTextView);
        ImageView imagenImageView = findViewById(R.id.CafeImagen);

        nombreTextView.setText(nombreCafe);
        precioTextView.setText(precioCafe);
        imagenImageView.setImageResource(imagenCafe);

        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}