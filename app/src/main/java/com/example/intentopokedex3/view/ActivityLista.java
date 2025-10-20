package com.example.intentopokedex3.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.intentopokedex3.R;
import com.example.intentopokedex3.controller.PokedexApi;
import com.example.intentopokedex3.model.Pokemon;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityLista extends AppCompatActivity {

    private GridLayout gridPokemons;
    private ImageButton botonVolverLista, botonLogoInicio;
    private EditText campoBuscar;
    private ListView listaSugerencias;

    private String tipoSeleccionado;
    private int desplazamiento = 0;
    private final int LIMITE = 30;
    private boolean cargando = false;
    private ArrayList<Pokemon> listaTotal = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        gridPokemons = findViewById(R.id.gridPokemons);
        botonVolverLista = findViewById(R.id.btnVolverLista);
        botonLogoInicio = findViewById(R.id.btnLogoInicioLista);
        campoBuscar = findViewById(R.id.buscarPokemon);
        listaSugerencias = findViewById(R.id.listaSugerencias);

        tipoSeleccionado = getIntent().getStringExtra("tipo");
        if (tipoSeleccionado == null) tipoSeleccionado = "fire";

        configurarBotones();
        configurarBuscador();
        cargarPokemonsPorTipo(tipoSeleccionado);
    }

    private void configurarBotones() {
        botonVolverLista.setOnClickListener(v -> finish());
        botonLogoInicio.setOnClickListener(v -> {
            Intent i = new Intent(ActivityLista.this, ActivityInicio.class);
            startActivity(i);
        });
    }

    private void configurarBuscador() {
        ArrayList<String> nombresPokemon = new ArrayList<>();
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombresPokemon);
        listaSugerencias.setAdapter(adaptador);

        // Variable para recordar la última búsqueda
        final String[] ultimaBusqueda = {""};

        campoBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String texto = s.toString().trim().toLowerCase();
                ultimaBusqueda[0] = texto; // guardamos lo último que el usuario escribió

                if (texto.length() >= 1) {
                    new Thread(() -> {
                        ArrayList<Pokemon> lista = PokedexApi.buscarPorNombre(texto);

                        // ✅ antes de actualizar la UI, comprobamos si el usuario sigue con ese texto
                        runOnUiThread(() -> {
                            if (!texto.equals(ultimaBusqueda[0])) {
                                // el usuario ya cambió el texto, así que descartamos esta respuesta
                                return;
                            }

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

            Intent i = new Intent(ActivityLista.this, ActivityDetalle.class);
            i.putExtra("nombrePokemon", nombre);
            i.putExtra("numeroPokemon", numero);
            i.putExtra("imagenUrl",
                    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + numero + ".png");
            startActivity(i);

            listaSugerencias.setVisibility(View.GONE);
        });
    }


    private void cargarPokemonsPorTipo(String tipo) {
        cargarMasPokemons();
    }

    private void cargarMasPokemons() {
        if (cargando) return;
        cargando = true;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // 🔹 Llamamos al nuevo método sin “Sync”
            ArrayList<Pokemon> nuevos = PokedexApi.obtenerPokemonPorTipo(tipoSeleccionado, desplazamiento, LIMITE);

            handler.post(() -> {
                mostrarPokemons(nuevos);
                desplazamiento += LIMITE;
                cargando = false;
            });
        });
    }

    private void mostrarPokemons(ArrayList<Pokemon> lista) {
        if (desplazamiento == 0) {
            gridPokemons.removeAllViews();
            listaTotal.clear();
        }

        listaTotal.addAll(lista);

        for (Pokemon p : lista) {
            LinearLayout tarjeta = new LinearLayout(this);
            tarjeta.setOrientation(LinearLayout.VERTICAL);
            tarjeta.setPadding(10, 10, 10, 10);
            tarjeta.setBackgroundResource(R.drawable.fondo_item_pokemon);
            tarjeta.setElevation(8f);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels / 2.2);
            params.setMargins(10, 10, 10, 10);
            tarjeta.setLayoutParams(params);

            // Imagen
            ImageView imagen = new ImageView(this);
            imagen.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 300));
            imagen.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // Descarga sin interfaz → en hilo aparte
            new Thread(() -> {
                Bitmap bmp = PokedexApi.descargarImagen(p.getImagenUrl());
                runOnUiThread(() -> {
                    if (bmp != null) imagen.setImageBitmap(bmp);
                });
            }).start();

            // Nombre
            TextView nombre = new TextView(this);
            nombre.setText("#" + p.getNumero() + " " + p.getNombre().toUpperCase());
            nombre.setTextSize(16);
            nombre.setPadding(0, 8, 0, 8);
            nombre.setGravity(android.view.Gravity.CENTER);
            nombre.setTextColor(getResources().getColor(android.R.color.white));

            tarjeta.addView(imagen);
            tarjeta.addView(nombre);

            tarjeta.setOnClickListener(v -> {
                Intent i = new Intent(ActivityLista.this, ActivityDetalle.class);
                i.putExtra("nombrePokemon", p.getNombre());
                i.putExtra("numeroPokemon", String.valueOf(p.getNumero()));
                i.putExtra("imagenUrl", p.getImagenUrl());
                startActivity(i);
            });

            gridPokemons.addView(tarjeta);
        }

        agregarBotonVerMas();
    }

    private void agregarBotonVerMas() {
        gridPokemons.post(() -> {
            View botonExistente = gridPokemons.findViewWithTag("btnVerMas");
            if (botonExistente != null) gridPokemons.removeView(botonExistente);

            Button botonVerMas = new Button(this);
            botonVerMas.setTag("btnVerMas");
            botonVerMas.setText("Ver más Pokémon");
            botonVerMas.setBackgroundResource(R.drawable.rounded_search_background_white);
            botonVerMas.setTextColor(Color.BLACK);
            botonVerMas.setPadding(20, 10, 20, 10);
            botonVerMas.setOnClickListener(v -> cargarMasPokemons());

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = GridLayout.LayoutParams.MATCH_PARENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(20, 30, 20, 40);
            params.columnSpec = GridLayout.spec(0, 2);
            botonVerMas.setLayoutParams(params);

            gridPokemons.addView(botonVerMas);
        });
    }
}
