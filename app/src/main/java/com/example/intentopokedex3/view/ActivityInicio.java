package com.example.intentopokedex3.view;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.content.Intent;

import com.example.intentopokedex3.R;

public class ActivityInicio extends AppCompatActivity {

    ImageButton btnIrATipos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        btnIrATipos = findViewById(R.id.btnIrATipos);

        // Al pulsar el fondo, abre la ActivityTipos
        btnIrATipos.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityInicio.this, ActivityTipos.class);
            startActivity(intent);
        });
    }
}
