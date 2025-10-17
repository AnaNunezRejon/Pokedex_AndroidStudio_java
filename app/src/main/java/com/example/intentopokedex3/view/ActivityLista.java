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

/**
 * Pantalla que muestra la lista de Pokemon filtrados por tipo.
 * Permite buscar Pokemon por nombre y cargar mas resultados por bloques.
 *
 *
 * FLUJO LISTA:
 * onCreate()
 *    ↳ carga el layout activity_lista.xml
 *    ↳ obtiene referencias (gridPokemons, buscarPokemon, botones)
 *    ↳ recupera el tipo recibido desde ActivityTipos
 *    ↳ configura navegacion:
 *         • btnVolverLista → finish()
 *         • btnLogoInicioLista → abre ActivityInicio
 *    ↳ configura el buscador (igual que en Tipos)
 *         • buscarPokemon.addTextChangedListener()
 *              → llama a PokedexApi.buscarPorNombre()
 *              → actualiza listaSugerencias
 *         • onItemClick → abre ActivityDetalle con datos del pokemon
 *    ↳ carga inicial de pokemons:
 *         cargarPokemonsPorTipo(tipoSeleccionado)
 *              ↳ llama a cargarMasPokemons()
 *
 * cargarMasPokemons()
 *    ↳ crea un hilo (ExecutorService)
 *    ↳ llama a PokedexApi.obtenerPokemonPorTipoSync(tipoActual, offset, LIMITE)
 *         ↳ devuelve un ArrayList<Pokemon> con los datos de 30 pokemons
 *    ↳ al terminar, muestra los resultados en mostrarPokemons()
 *    ↳ aumenta offset + 30 para futuras cargas
 *
 * mostrarPokemons()
 *    ↳ limpia el grid si es la primera carga
 *    ↳ por cada Pokemon crea una tarjeta (LinearLayout)
 *         • Imagen → PokedexApi.descargarImagen()
 *         • Nombre → TextView con numero y nombre
 *         • onClick → abre ActivityDetalle con los datos
 *    ↳ añade un boton "Ver mas Pokemon"
 *         • ocupa todo el ancho
 *         • al pulsarlo → vuelve a ejecutar cargarMasPokemons()
 *
 * 🔹 Objetivo:
 *    Muestra todos los pokemons del tipo elegido.
 *    Tiene buscador, paginacion manual y acceso a los detalles.
 */
public class ActivityLista extends AppCompatActivity {

    // -------------------------------------------------------------------------
    // Variables de la interfaz
    // -------------------------------------------------------------------------
    private GridLayout gridPokemons;
    private ImageButton botonVolverLista, botonLogoInicio;
    private EditText campoBuscar;
    private ListView listaSugerencias;

    // -------------------------------------------------------------------------
    // Variables de control de datos
    // -------------------------------------------------------------------------
    private String tipoSeleccionado;
    private int desplazamiento = 0;
    private final int LIMITE = 30;
    private boolean cargando = false;
    private ArrayList<Pokemon> listaTotal = new ArrayList<>();

    // -------------------------------------------------------------------------
    // 🔹 Metodo principal al crear la actividad
    // -------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        // Referencias UI
        gridPokemons = findViewById(R.id.gridPokemons);
        botonVolverLista = findViewById(R.id.btnVolverLista);
        botonLogoInicio = findViewById(R.id.btnLogoInicioLista);
        campoBuscar = findViewById(R.id.buscarPokemon);
        listaSugerencias = findViewById(R.id.listaSugerencias);

        // Recuperar el tipo de Pokemon recibido desde ActivityTipos
        tipoSeleccionado = getIntent().getStringExtra("tipo");
        if (tipoSeleccionado == null) tipoSeleccionado = "fire";

        // Configurar botones
        configurarBotones();

        // Configurar buscador
        configurarBuscador();

        // Cargar lista inicial de Pokemon del tipo seleccionado
        cargarPokemonsPorTipo(tipoSeleccionado);
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo que configura los botones principales
    // -------------------------------------------------------------------------
    private void configurarBotones() {
        // Boton volver a la pantalla anterior
        botonVolverLista.setOnClickListener(v -> finish());

        // Boton logo inferior para ir al inicio
        botonLogoInicio.setOnClickListener(v -> {
            Intent i = new Intent(ActivityLista.this, ActivityInicio.class);
            startActivity(i);
        });
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo que configura el buscador de Pokemon
    // -------------------------------------------------------------------------
    private void configurarBuscador() {
        ArrayList<String> nombresPokemon = new ArrayList<>();
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombresPokemon);
        listaSugerencias.setAdapter(adaptador);

        // Detecta texto escrito en el campo de busqueda
        campoBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String texto = s.toString().trim().toLowerCase();

                if (!texto.isEmpty()) {
                    // Llamada a la API -> busca coincidencias
                    PokedexApi.buscarPorNombre(texto, lista -> runOnUiThread(() -> {
                        nombresPokemon.clear();
                        for (Pokemon p : lista) {
                            nombresPokemon.add("#" + p.getNumero() + " " + p.getNombre().toUpperCase());
                        }
                        adaptador.notifyDataSetChanged();
                        listaSugerencias.setVisibility(View.VISIBLE);
                    }));
                } else {
                    listaSugerencias.setVisibility(View.GONE);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        // Cuando el usuario pulsa un Pokemon sugerido
        listaSugerencias.setOnItemClickListener((parent, view, position, id) -> {
            String textoSeleccionado = nombresPokemon.get(position);
            // Ejemplo: "#1 BULBASAUR"
            String[] partes = textoSeleccionado.split(" ");
            String numero = partes[0].replace("#", "");
            String nombre = partes[1].toLowerCase();

            // Se abre la pantalla de detalle con el Pokemon seleccionado
            Intent i = new Intent(ActivityLista.this, ActivityDetalle.class);
            i.putExtra("nombrePokemon", nombre);
            i.putExtra("numeroPokemon", numero);
            i.putExtra("imagenUrl", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + numero + ".png");
            startActivity(i);

            listaSugerencias.setVisibility(View.GONE);
        });
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo principal que carga Pokemon segun el tipo
    // -------------------------------------------------------------------------
    /**
     * Este metodo se llama al abrir la pantalla o al pulsar "Ver mas".
     * Llama a {@link #cargarMasPokemons()} que maneja el proceso asincrono.
     */
    private void cargarPokemonsPorTipo(String tipo) {
        cargarMasPokemons(); // primera carga
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo que carga un bloque adicional de Pokemon
    // -------------------------------------------------------------------------
    /**
     * Llama al metodo {@link PokedexApi#obtenerPokemonPorTipoSync(String, int, int)}
     * pasandole el tipo, desplazamiento y limite actual.
     * Luego envia los resultados a {@link #mostrarPokemons(ArrayList)}.
     */
    private void cargarMasPokemons() {
        if (cargando) return; // evita doble clic
        cargando = true;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            ArrayList<Pokemon> nuevos = PokedexApi.obtenerPokemonPorTipoSync(tipoSeleccionado, desplazamiento, LIMITE);
            handler.post(() -> {
                mostrarPokemons(nuevos);
                desplazamiento += LIMITE;
                cargando = false;
            });
        });
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo que muestra los Pokemon en pantalla
    // -------------------------------------------------------------------------
    /**
     * Este metodo crea dinamicamente las "tarjetas" con los datos de cada Pokemon.
     * Se llama desde {@link #cargarMasPokemons()} una vez que la API devuelve resultados.
     */
    private void mostrarPokemons(ArrayList<Pokemon> lista) {
        if (desplazamiento == 0) {
            gridPokemons.removeAllViews();
            listaTotal.clear();
        }

        listaTotal.addAll(lista);

        for (Pokemon p : lista) {
            // Tarjeta contenedora
            LinearLayout tarjeta = new LinearLayout(this);
            tarjeta.setOrientation(LinearLayout.VERTICAL);
            tarjeta.setPadding(10, 10, 10, 10);
            tarjeta.setBackgroundResource(R.drawable.fondo_item_pokemon);
            tarjeta.setElevation(8f);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels / 2.2);
            params.setMargins(10, 10, 10, 10);
            tarjeta.setLayoutParams(params);

            // Imagen del Pokemon
            ImageView imagen = new ImageView(this);
            imagen.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 300));
            imagen.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // Descarga de imagen asincrona (llama a PokedexApi.descargarImagen)
            PokedexApi.descargarImagen(p.getImagenUrl(), imagen::setImageBitmap);

            // Nombre del Pokemon
            TextView nombre = new TextView(this);
            nombre.setText("#" + p.getNumero() + " " + p.getNombre().toUpperCase());
            nombre.setTextSize(16);
            nombre.setPadding(0, 8, 0, 8);
            nombre.setGravity(android.view.Gravity.CENTER);
            nombre.setTextColor(getResources().getColor(android.R.color.white));

            // Añadimos imagen y nombre a la tarjeta
            tarjeta.addView(imagen);
            tarjeta.addView(nombre);

            // Al pulsar la tarjeta abrimos la pantalla de detalle
            tarjeta.setOnClickListener(v -> {
                Intent i = new Intent(ActivityLista.this, ActivityDetalle.class);
                i.putExtra("nombrePokemon", p.getNombre());
                i.putExtra("numeroPokemon", String.valueOf(p.getNumero()));
                i.putExtra("imagenUrl", p.getImagenUrl());
                startActivity(i);
            });

            gridPokemons.addView(tarjeta);
        }

        // Añadimos o actualizamos el boton "Ver mas Pokemon"
        agregarBotonVerMas();
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo que agrega el boton "Ver mas Pokemon"
    // -------------------------------------------------------------------------
    /**
     * Este metodo crea el boton "Ver mas Pokemon" al final del GridLayout.
     * Al pulsarlo, se llama de nuevo a {@link #cargarMasPokemons()}.
     */
    private void agregarBotonVerMas() {
        gridPokemons.post(() -> {
            // Elimina boton anterior si existe
            View botonExistente = gridPokemons.findViewWithTag("btnVerMas");
            if (botonExistente != null) gridPokemons.removeView(botonExistente);

            // Crea nuevo boton
            Button botonVerMas = new Button(this);
            botonVerMas.setTag("btnVerMas");
            botonVerMas.setText("Ver mas Pokemon");
            botonVerMas.setBackgroundResource(R.drawable.rounded_search_background_white);
            botonVerMas.setTextColor(Color.BLACK);
            botonVerMas.setPadding(20, 10, 20, 10);
            botonVerMas.setOnClickListener(v -> cargarMasPokemons());

            // Parametros para ocupar el ancho completo
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
