package com.example.intentopokedex3.view;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.intentopokedex3.R;
import com.example.intentopokedex3.controller.PokedexApi;
import com.example.intentopokedex3.model.Pokemon;

import java.util.ArrayList;

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

        // 🔹 Referencias UI
        btnVolverTipos = findViewById(R.id.btnVolverTipos);
        logoInicioTipos = findViewById(R.id.btnLogoInicio);
        EditText inputBuscarTipos = findViewById(R.id.inputBuscarTipos);

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

        // 🔙 Botón volver
        btnVolverTipos.setOnClickListener(v -> finish());

        // 🏠 Logo inferior → volver al inicio
        logoInicioTipos.setOnClickListener(v -> {
            Intent i = new Intent(ActivityTipos.this, ActivityInicio.class);
            startActivity(i);
        });

        // 🌿 Asignar acciones a los botones de tipo
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

        // 🔍 BUSCADOR
        ListView listaSugerencias = findViewById(R.id.listaSugerencias);
        ArrayList<String> nombresPokemon = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombresPokemon);
        listaSugerencias.setAdapter(adapter);

        // Escucha el texto del buscador
        inputBuscarTipos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String texto = s.toString().trim().toLowerCase();

                if (texto.length() >= 1) {
                    PokedexApi.buscarPorNombre(texto, lista -> {
                        runOnUiThread(() -> {
                            nombresPokemon.clear();
                            for (Pokemon p : lista) {
                                nombresPokemon.add("#" + p.getNumero() + " " + p.getNombre().toUpperCase());
                            }
                            adapter.notifyDataSetChanged();
                            listaSugerencias.setVisibility(View.VISIBLE);
                        });
                    });
                } else {
                    listaSugerencias.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Cuando el usuario pulsa sobre un Pokémon de la lista
        listaSugerencias.setOnItemClickListener((parent, view, position, id) -> {
            String textoSeleccionado = nombresPokemon.get(position);
            // Ejemplo: "#1 BULBASAUR"
            String[] partes = textoSeleccionado.split(" ");
            String numero = partes[0].replace("#", "");
            String nombre = partes[1].toLowerCase();

            Intent i = new Intent(ActivityTipos.this, ActivityDetalle.class);
            i.putExtra("nombrePokemon", nombre);
            i.putExtra("numeroPokemon", numero);
            i.putExtra("imagenUrl", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + numero + ".png");
            startActivity(i);

            listaSugerencias.setVisibility(View.GONE);
        });
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
