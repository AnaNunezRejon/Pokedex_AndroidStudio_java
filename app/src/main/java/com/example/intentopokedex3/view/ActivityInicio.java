package com.example.intentopokedex3.view;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.content.Intent;

import com.example.intentopokedex3.R;


public class ActivityInicio extends AppCompatActivity {

    // -------------------------------------------------------------------------
    // Variables de interfaz
    // -------------------------------------------------------------------------
    private ImageButton botonIrATipos;

    // -------------------------------------------------------------------------
    // 🔹 Metodo principal al crear la actividad
    // -------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        // Referencia al boton (fondo clicable)
        botonIrATipos = findViewById(R.id.btnIrATipos);

        // Al pulsar el fondo se abre la pantalla de tipos
        botonIrATipos.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityInicio.this, ActivityTipos.class);
            startActivity(intent);
        });
    }
}
