package com.example.notas_app_sqlite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.notas_app_sqlite.databinding.ActivityMainBinding;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NotasDB db;
    private NotasAdapter cardAdapter;
    private NotasTableAdapter tableAdapter;
    private SharedPreferences sharedPreferences;
    
    private boolean isTableView = false;
    
    // Constantes para SharedPreferences
    private static final String PREF_NAME = "notas_preferences";
    private static final String KEY_VIEW_TYPE = "view_type";
    private static final String VIEW_CARDS = "cards";
    private static final String VIEW_TABLE = "table";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Configurar toolbar
        setSupportActionBar(binding.toolbar);
        
        // Inicializar base de datos
        db = new NotasDB(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        
        // Inicializar adaptadores
        List<Nota> notas = db.getAllNotas();
        cardAdapter = new NotasAdapter(notas, this);
        tableAdapter = new NotasTableAdapter(notas, this);
        
        // Configurar RecyclerView
        binding.notasRv.setLayoutManager(new LinearLayoutManager(this));
        
        // Restaurar vista guardada
        String savedViewType = sharedPreferences.getString(KEY_VIEW_TYPE, VIEW_CARDS);
        isTableView = savedViewType.equals(VIEW_TABLE);
        updateView();
        
        // Configurar FAB para agregar nota
        binding.FABAgregarNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AgregarNotaActivity.class);
                startActivity(intent);
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem viewCardsItem = menu.findItem(R.id.action_view_cards);
        MenuItem viewTableItem = menu.findItem(R.id.action_view_table);
        
        if (viewCardsItem != null) {
            viewCardsItem.setVisible(isTableView);
        }
        if (viewTableItem != null) {
            viewTableItem.setVisible(!isTableView);
        }
        
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_view_cards) {
            isTableView = false;
            saveViewPreference(VIEW_CARDS);
            updateView();
            invalidateOptionsMenu();
            return true;
        } else if (itemId == R.id.action_view_table) {
            isTableView = true;
            saveViewPreference(VIEW_TABLE);
            updateView();
            invalidateOptionsMenu();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void updateView() {
        if (isTableView) {
            // Mostrar header de tabla
            binding.tableHeader.getRoot().setVisibility(View.VISIBLE);
            binding.notasRv.setAdapter(tableAdapter);
        } else {
            // Ocultar header de tabla
            binding.tableHeader.getRoot().setVisibility(View.GONE);
            binding.notasRv.setAdapter(cardAdapter);
        }
    }
    
    private void saveViewPreference(String viewType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_VIEW_TYPE, viewType);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar datos cuando se regresa a la actividad
        List<Nota> notas = db.getAllNotas();
        cardAdapter.updateData(notas);
        tableAdapter.updateData(notas);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cerrar la base de datos cuando se destruye la actividad
        if (db != null) {
            db.close();
        }
    }
}