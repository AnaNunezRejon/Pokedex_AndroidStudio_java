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

    // -------------------------------------------------------------------------
    // Variables de interfaz
    // -------------------------------------------------------------------------
    private TextView btnPlanta, btnAgua, btnFuego, btnTierra, btnElectrico,
            btnNormal, btnVeneno, btnHada, btnAcero,
            btnBicho, btnHielo, btnRoca, btnLucha, btnSiniestro,
            btnFantasma, btnPsiquico, btnDragon, btnVolador;

    private ImageButton botonVolver, botonLogoInicio;
    private EditText campoBuscar;
    private ListView listaSugerencias;

    // -------------------------------------------------------------------------
    // 🔹 Metodo principal al crear la pantalla
    // -------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipos);

        // Referencias de la interfaz
        botonVolver = findViewById(R.id.btnVolverTipos);
        botonLogoInicio = findViewById(R.id.btnLogoInicio);
        campoBuscar = findViewById(R.id.inputBuscarTipos);
        listaSugerencias = findViewById(R.id.listaSugerencias);

        // Inicializar botones de tipo
        inicializarBotonesTipos();

        // Configurar botones principales
        configurarBotones();

        // Configurar buscador de Pokemon
        configurarBuscador();
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo que inicializa todos los botones de tipo
    // -------------------------------------------------------------------------
    private void inicializarBotonesTipos() {
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

        // Asignar acción a cada botón
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

    // -------------------------------------------------------------------------
    // 🔹 Metodo que configura los botones volver y logo inicio
    // -------------------------------------------------------------------------
    private void configurarBotones() {
        botonVolver.setOnClickListener(v -> finish());

        botonLogoInicio.setOnClickListener(v -> {
            Intent i = new Intent(ActivityTipos.this, ActivityInicio.class);
            startActivity(i);
        });
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo que configura el buscador flotante (versión sin interfaces)
    // -------------------------------------------------------------------------
    private void configurarBuscador() {
        ArrayList<String> nombresPokemon = new ArrayList<>();
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombresPokemon);
        listaSugerencias.setAdapter(adaptador);

        // Variable para guardar el último texto buscado
        final String[] ultimaBusqueda = {""};

        campoBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String texto = s.toString().trim().toLowerCase();
                ultimaBusqueda[0] = texto; // Guardamos la búsqueda actual

                if (texto.length() >= 1) {
                    new Thread(() -> {
                        ArrayList<Pokemon> lista = PokedexApi.buscarPorNombre(texto);

                        runOnUiThread(() -> {
                            // ✅ Si el texto del campo cambió mientras la API respondía, no actualizar
                            if (!texto.equals(ultimaBusqueda[0])) return;

                            nombresPokemon.clear();
                            for (Pokemon p : lista) {
                                nombresPokemon.add("#" + p.getNumero() + " " + p.getNombre().toUpperCase());
                            }

                            adaptador.notifyDataSetChanged();
                            listaSugerencias.setVisibility(View.VISIBLE);
                        });
                    }).start();
                } else {
                    nombresPokemon.clear();
                    adaptador.notifyDataSetChanged();
                    listaSugerencias.setVisibility(View.GONE);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        listaSugerencias.setOnItemClickListener((parent, view, position, id) -> {
            String textoSeleccionado = nombresPokemon.get(position);
            String[] partes = textoSeleccionado.split(" ");
            String numero = partes[0].replace("#", "");
            String nombre = partes[1].toLowerCase();

            Intent i = new Intent(ActivityTipos.this, ActivityDetalle.class);
            i.putExtra("nombrePokemon", nombre);
            i.putExtra("numeroPokemon", numero);
            i.putExtra("imagenUrl",
                    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + numero + ".png");
            startActivity(i);

            listaSugerencias.setVisibility(View.GONE);
        });
    }


    // -------------------------------------------------------------------------
    // 🔹 Metodo que asigna la acción a cada botón de tipo
    // -------------------------------------------------------------------------
    private void asignarAccionTipo(TextView boton, String tipoEspanol) {
        String tipoIngles = traducirTipo(tipoEspanol);

        boton.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityTipos.this, ActivityLista.class);
            intent.putExtra("tipo", tipoIngles);
            startActivity(intent);
        });
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo auxiliar que traduce los nombres de tipo al inglés
    // -------------------------------------------------------------------------
    private String traducirTipo(String tipo) {
        switch (tipo) {
            case "planta": return "grass";
            case "fuego": return "fire";
            case "agua": return "water";
            case "tierra": return "ground";
            case "electrico": return "electric";
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
