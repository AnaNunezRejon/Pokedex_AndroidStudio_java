package com.example.intentopokedex3.view;

import android.content.Intent;
import android.graphics.Color;
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
    private ImageButton btnVolverLista, btnLogoInicioLista;
    private EditText buscarPokemon;
    private String tipoSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        // 🔹 Referencias UI
        gridPokemons = findViewById(R.id.gridPokemons);
        btnVolverLista = findViewById(R.id.btnVolverLista);
        btnLogoInicioLista = findViewById(R.id.btnLogoInicioLista);
        buscarPokemon = findViewById(R.id.buscarPokemon);

        // ✅ Recuperar tipo seleccionado
        tipoSeleccionado = getIntent().getStringExtra("tipo");
        if (tipoSeleccionado == null) tipoSeleccionado = "fire";

        // 🔙 Botón volver
        btnVolverLista.setOnClickListener(v -> finish());

        // 🏠 Logo inferior → volver al inicio
        btnLogoInicioLista.setOnClickListener(v -> {
            Intent i = new Intent(ActivityLista.this, ActivityInicio.class);
            startActivity(i);
        });

        // 🔍 BUSCADOR FLOTANTE
        ListView listaSugerencias = findViewById(R.id.listaSugerencias);
        ArrayList<String> nombresPokemon = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombresPokemon);
        listaSugerencias.setAdapter(adapter);

        buscarPokemon.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String texto = s.toString().trim().toLowerCase();

                if (!texto.isEmpty()) {
                    PokedexApi.buscarPorNombre(texto, lista -> runOnUiThread(() -> {
                        nombresPokemon.clear();
                        for (Pokemon p : lista) {
                            nombresPokemon.add("#" + p.getNumero() + " " + p.getNombre().toUpperCase());
                        }
                        adapter.notifyDataSetChanged();
                        listaSugerencias.setVisibility(View.VISIBLE);
                    }));
                } else {
                    listaSugerencias.setVisibility(View.GONE);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 🔹 Click en sugerencia → Ir a detalle
        listaSugerencias.setOnItemClickListener((parent, view, position, id) -> {
            String textoSeleccionado = nombresPokemon.get(position);
            // Ejemplo: "#1 BULBASAUR"
            String[] partes = textoSeleccionado.split(" ");
            String numero = partes[0].replace("#", "");
            String nombre = partes[1].toLowerCase();

            Intent i = new Intent(ActivityLista.this, ActivityDetalle.class);
            i.putExtra("nombrePokemon", nombre);
            i.putExtra("numeroPokemon", numero);
            // 🔸 URL estándar de imagen
            i.putExtra("imagenUrl", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + numero + ".png");
            startActivity(i);

            listaSugerencias.setVisibility(View.GONE);
        });

        // ⚙️ Cargar lista principal
        cargarPokemonsPorTipo(tipoSeleccionado);
    }

    private int offset = 0;  // Control de posición
    private final int LIMITE = 30;
    private boolean cargando = false; // Evita doble clics
    private String tipoActual;
    private ArrayList<Pokemon> listaTotal = new ArrayList<>();

    private void cargarPokemonsPorTipo(String tipo) {
        tipoActual = tipo;
        cargarMasPokemons(); // Primera carga
    }

    private void cargarMasPokemons() {
        if (cargando) return;
        cargando = true;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            ArrayList<Pokemon> nuevos = PokedexApi.obtenerPokemonPorTipoSync(tipoActual, offset, LIMITE);
            handler.post(() -> {
                mostrarPokemons(nuevos);
                offset += LIMITE;
                cargando = false;
            });
        });
    }

    private void mostrarPokemons(ArrayList<Pokemon> lista) {
        // Si es la primera vez, limpiamos
        if (offset == 0) {
            gridPokemons.removeAllViews();
            listaTotal.clear();
        }

        listaTotal.addAll(lista);

        for (Pokemon p : lista) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(10, 10, 10, 10);
            card.setBackgroundResource(R.drawable.fondo_item_pokemon);
            card.setElevation(8f);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels / 2.2);
            params.setMargins(10, 10, 10, 10);
            card.setLayoutParams(params);

            // Imagen
            ImageView imagen = new ImageView(this);
            imagen.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 300));
            imagen.setScaleType(ImageView.ScaleType.FIT_CENTER);

            PokedexApi.descargarImagen(p.getImagenUrl(), imagen::setImageBitmap);

            // Nombre
            TextView nombre = new TextView(this);
            nombre.setText("#" + p.getNumero() + " " + p.getNombre().toUpperCase());
            nombre.setTextSize(16);
            nombre.setPadding(0, 8, 0, 8);
            nombre.setGravity(android.view.Gravity.CENTER);
            nombre.setTextColor(getResources().getColor(android.R.color.white));

            card.addView(imagen);
            card.addView(nombre);

            card.setOnClickListener(v -> {
                Intent i = new Intent(ActivityLista.this, ActivityDetalle.class);
                i.putExtra("nombrePokemon", p.getNombre());
                i.putExtra("numeroPokemon", String.valueOf(p.getNumero()));
                i.putExtra("imagenUrl", p.getImagenUrl());
                startActivity(i);
            });

            gridPokemons.addView(card);
        }

        // 🔹 Añadir o actualizar el botón “Ver más”
        gridPokemons.post(() -> {
            // Buscar si hay un botón anterior (por ID generado)
            View botonExistente = gridPokemons.findViewWithTag("btnVerMas");
            if (botonExistente != null) gridPokemons.removeView(botonExistente);

            Button btnVerMas = new Button(this);
            btnVerMas.setTag("btnVerMas"); // ✅ usamos tag en lugar de R.id
            btnVerMas.setText("Ver más Pokémon");
            btnVerMas.setBackgroundResource(R.drawable.rounded_search_background_white);
            btnVerMas.setTextColor(Color.BLACK);
            btnVerMas.setPadding(20, 10, 20, 10);
            btnVerMas.setOnClickListener(v -> cargarMasPokemons());

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = GridLayout.LayoutParams.MATCH_PARENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(20, 30, 20, 40);
            params.columnSpec = GridLayout.spec(0, 2);
            btnVerMas.setLayoutParams(params);

            gridPokemons.addView(btnVerMas);
        });
    }
}
