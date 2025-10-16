package com.example.intentopokedex3.view;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.widget.ImageButton;
import com.example.intentopokedex3.R;

public class ActivityTipos extends AppCompatActivity {

    TextView btnPlanta, btnAgua, btnFuego, btnTierra, btnElectrico,
            btnNormal, btnVeneno, btnHada, btnAcero,
            btnBicho, btnHielo, btnRoca, btnLucha, btnSiniestro,
            btnFantasma, btnPsiquico, btnDragon, btnVolador;

    ImageButton btnVolverTipos, logoInicioTipos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipos);

        // 🔹 Botones de tipo (TextView)
        btnPlanta = findViewById(R.id.btnPlanta);
        btnAgua = findViewById(R.id.btnAgua);
        btnFuego = findViewById(R.id.btnFuego);
        btnTierra = findViewById(R.id.btnTierra);
        btnElectrico = findViewById(R.id.btnElectrico);
        btnNormal = findViewById(R.id.btnNormal);
        btnVeneno = findViewById(R.id.btnVeneno);
        btnHada = findViewById(R.id.btnHada);
        btnAcero = findViewById(R.id.btnAcero);
        btnBicho = findViewById(R.id.btnBicho);
        btnHielo = findViewById(R.id.btnHielo);
        btnRoca = findViewById(R.id.btnRoca);
        btnLucha = findViewById(R.id.btnLucha);
        btnSiniestro = findViewById(R.id.btnSiniestro);
        btnFantasma = findViewById(R.id.btnFantasma);
        btnPsiquico = findViewById(R.id.btnPsiquico);
        btnDragon = findViewById(R.id.btnDragon);
        btnVolador = findViewById(R.id.btnVolador);

        // 🔙 Botones superiores e inferiores (ImageButton)
        btnVolverTipos = findViewById(R.id.btnVolverTipos);
        logoInicioTipos = findViewById(R.id.btnLogoInicio);

        // 🔹 Acción del botón Volver
        btnVolverTipos.setOnClickListener(v -> finish());

        // 🔹 Logo inferior → vuelve al inicio
        logoInicioTipos.setOnClickListener(v -> {
            Intent i = new Intent(ActivityTipos.this, ActivityInicio.class);
            startActivity(i);
        });

        // 🌿 Ejemplo: abrir lista según el tipo seleccionado
        asignarAccionTipo(btnPlanta, "planta");
        asignarAccionTipo(btnAgua, "agua");
        asignarAccionTipo(btnFuego, "fuego");
        asignarAccionTipo(btnTierra, "tierra");
        asignarAccionTipo(btnElectrico, "electrico");
        asignarAccionTipo(btnNormal, "normal");
        asignarAccionTipo(btnVeneno, "veneno");
        asignarAccionTipo(btnHada, "hada");
        asignarAccionTipo(btnAcero, "acero");
        asignarAccionTipo(btnBicho, "bicho");
        asignarAccionTipo(btnHielo, "hielo");
        asignarAccionTipo(btnRoca, "roca");
        asignarAccionTipo(btnLucha, "lucha");
        asignarAccionTipo(btnSiniestro, "siniestro");
        asignarAccionTipo(btnFantasma, "fantasma");
        asignarAccionTipo(btnPsiquico, "psiquico");
        asignarAccionTipo(btnDragon, "dragon");
        asignarAccionTipo(btnVolador, "volador");
    }

    // 🔧 Método auxiliar para simplificar el código
    private void asignarAccionTipo(TextView boton, String tipoEspanol) {
        String tipoIngles = traducirTipo(tipoEspanol);

        boton.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityTipos.this, ActivityLista.class);
            intent.putExtra("tipo", tipoIngles);
            startActivity(intent);
        });
    }

    private String traducirTipo(String tipo) {
        switch (tipo) {
            case "planta": return "grass";
            case "fuego": return "fire";
            case "agua": return "water";
            case "tierra": return "ground";
            case "eléctrico": return "electric";
            case "hielo": return "ice";
            case "roca": return "rock";
            case "lucha": return "fighting";
            case "bicho": return "bug";
            case "veneno": return "poison";
            case "psiquico": return "psychic";
            case "siniestro": return "dark";
            case "normal": return "normal";
            case "fantasma": return "ghost";
            case "hada": return "fairy";
            case "dragon": return "dragon";
            case "acero": return "steel";
            case "volador": return "flying";
            default: return tipo;
        }
    }
}
