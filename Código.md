# ESTRUCTURA
```plsql
app/
 ├── manifests/
 │    └── AndroidManifest.xml
 │
 ├── java/
 │    └── com.example.pokedex/
 │         ├── controller/
 │         │     └── PokedexApi.java
 │         │
 │         ├── model/
 │         │     └── Pokemon.java
 │         │
 │         └── view/
 │               ├── ActividadDetalle.java
 │               ├── ActividadInicio.java
 │               ├── ActividadLista.java
 │               ├── ActividadTipos.java
 │
 ├── res/
 │    ├── drawable/
 │    │     ├── barra_inferior.png
 │    │     ├── borde_caja.xml
 │    │     ├── btn_acero.png
 │    │     ├── btn_agua.png
 │    │     ├── btn_bicho.png
 │    │     ├── btn_dragon.png
 │    │     ├── btn_electrico.png
 │    │     ├── btn_fantasma.png
 │    │     ├── btn_fuego.png
 │    │     ├── btn_hada.png
 │    │     ├── btn_hielo.png
 │    │     ├── btn_lucha.png
 │    │     ├── btn_normal.png
 │    │     ├── btn_planta.png
 │    │     ├── btn_psiquico.png
 │    │     ├── btn_roca.png
 │    │     ├── btn_siniestro.png
 │    │     ├── btn_tierra.png
 │    │     ├── btn_veneno.png
 │    │     ├── btn_volador.png
 │    │     ├── btn_volver.xml
 │    │     ├── chip_debilidad.xml
 │    │     ├── chip_tipo.xml
 │    │     ├── fondo_caja_detalle.xml
 │    │     ├── fondo_inicio.png
 │    │     ├── fondo_item_pokemon.xml
 │    │     ├── fondo_pokedex.png
 │    │     ├── fondo_pokedex_dos.png
 │    │     ├── fondo_pokedex_uno.png
 │    │     ├── fondo_pokemons.jpg
 │    │     ├── ic_arrow_back_black_24dp.xml
 │    │     ├── ic_launcher_background.xml
 │    │     ├── ic_launcher_foreground.xml
 │    │     ├── logo_pokedex.png
 │    │     ├── rounded_search_background.xml
 │    │     └── rounded_search_background_white.xml
 │    │
 │    └── layout/
 │          ├── activity_detalle.xml
 │          ├── activity_inicio.xml
 │          ├── activity_lista.xml
 │          ├── item_pokemon.xml
 │          └── activity_tipos.xml
 │
 └── com.example.pokedex (androidTest + test)

```

Estructura del Proyecto — Pokedex (Formato .md para README)
# CONTROLLER
PokedexApi.java
```java
package com.example.intentopokedex3.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.intentopokedex3.model.Pokemon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PokedexApi {

    private static final String BASE_URL = "https://pokeapi.co/api/v2/";

    // -------------------------------------------------------------------------
    // 🔹 Obtener lista de Pokémon por tipo
    // -------------------------------------------------------------------------
    public static ArrayList<Pokemon> obtenerPokemonPorTipo(String tipo) {
        ArrayList<Pokemon> lista = new ArrayList<>();
        String urlString = BASE_URL + "type/" + tipo.toLowerCase();

        try {
            URL url = new URL(urlString);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");

            BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String linea;
            while ((linea = lector.readLine()) != null) {
                jsonBuilder.append(linea);
            }
            lector.close();
            conexion.disconnect();

            JSONObject json = new JSONObject(jsonBuilder.toString());
            JSONArray pokemons = json.getJSONArray("pokemon");

            int limite = Math.min(30, pokemons.length());
            for (int i = 0; i < limite; i++) {
                JSONObject pokeObj = pokemons.getJSONObject(i).getJSONObject("pokemon");
                String urlDetalle = pokeObj.getString("url");

                Pokemon p = obtenerDetallePokemon(urlDetalle);

                // Solo añadimos si tiene nombre válido
                if (!p.getNombre().isEmpty()) {
                    lista.add(p);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    // -------------------------------------------------------------------------
    // 🔹 Obtener detalles de un Pokémon concreto
    // -------------------------------------------------------------------------
    public static Pokemon obtenerDetallePokemon(String urlString) {
        Pokemon p = new Pokemon();

        try {
            URL url = new URL(urlString);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");

            BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String linea;
            while ((linea = lector.readLine()) != null) {
                jsonBuilder.append(linea);
            }

            lector.close();
            conexion.disconnect();

            JSONObject json = new JSONObject(jsonBuilder.toString());

            p.setNombre(json.optString("name", "Desconocido"));
            p.setNumero(json.optInt("id", 0));
            p.setImagenUrl(json.getJSONObject("sprites").optString("front_default", ""));

            // Tipos
            JSONArray tiposArray = json.optJSONArray("types");
            ArrayList<String> tipos = new ArrayList<>();
            if (tiposArray != null) {
                for (int i = 0; i < tiposArray.length(); i++) {
                    tipos.add(tiposArray.getJSONObject(i)
                            .getJSONObject("type")
                            .optString("name", "indefinido"));
                }
            }
            p.setTipos(tipos);

            // Altura y peso
            p.setAltura(json.optDouble("height", 0) / 10.0);
            p.setPeso(json.optDouble("weight", 0) / 10.0);

            // Habilidad
            JSONArray habilidades = json.optJSONArray("abilities");
            if (habilidades != null && habilidades.length() > 0) {
                String habilidad = habilidades.getJSONObject(0)
                        .getJSONObject("ability")
                        .optString("name", "sin habilidad");
                p.setHabilidad(habilidad);
            } else {
                p.setHabilidad("sin habilidad");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return p;
    }

    // -------------------------------------------------------------------------
    // 🔹 Descargar imagen desde URL
    // -------------------------------------------------------------------------
    public static Bitmap descargarImagen(String urlImagen) {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // bitmap vacío por defecto
        try {
            URL url = new URL(urlImagen);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.connect();
            InputStream input = conexion.getInputStream();
            Bitmap descargada = BitmapFactory.decodeStream(input);
            if (descargada != null) bitmap = descargada; // solo reemplaza si hay imagen válida
            input.close();
            conexion.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    // -------------------------------------------------------------------------
    // 🔹 Buscar Pokémon por nombre o número
    // -------------------------------------------------------------------------
    public static ArrayList<Pokemon> buscarPorNombre(String texto) {
        ArrayList<Pokemon> resultados = new ArrayList<>();

        try {
            if (texto.matches("\\d+")) {
                String urlDetalle = BASE_URL + "pokemon/" + texto;
                Pokemon p = obtenerDetallePokemon(urlDetalle);
                if (!p.getNombre().isEmpty()) resultados.add(p);

            } else {
                String urlString = BASE_URL + "pokemon?limit=1025";
                URL url = new URL(urlString);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("GET");

                BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                StringBuilder jsonBuilder = new StringBuilder();
                String linea;
                while ((linea = lector.readLine()) != null) {
                    jsonBuilder.append(linea);
                }

                lector.close();
                conexion.disconnect();

                JSONObject json = new JSONObject(jsonBuilder.toString());
                JSONArray pokemons = json.getJSONArray("results");

                for (int i = 0; i < pokemons.length(); i++) {
                    JSONObject pokeObj = pokemons.getJSONObject(i);
                    String nombre = pokeObj.getString("name");

                    if (nombre.startsWith(texto.toLowerCase())) {
                        String urlDetalle = pokeObj.getString("url");
                        Pokemon p = obtenerDetallePokemon(urlDetalle);
                        if (!p.getNombre().isEmpty()) resultados.add(p);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultados;
    }

    // -------------------------------------------------------------------------
    // 🔹 Obtener Pokémon por tipo (con paginación)
    // -------------------------------------------------------------------------
    public static ArrayList<Pokemon> obtenerPokemonPorTipo(String tipo, int offset, int limite) {
        ArrayList<Pokemon> lista = new ArrayList<>();
        String urlString = BASE_URL + "type/" + tipo.toLowerCase();

        try {
            URL url = new URL(urlString);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");

            BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String linea;
            while ((linea = lector.readLine()) != null) {
                jsonBuilder.append(linea);
            }

            lector.close();
            conexion.disconnect();

            JSONObject json = new JSONObject(jsonBuilder.toString());
            JSONArray pokemons = json.getJSONArray("pokemon");

            int inicio = offset;
            int fin = Math.min(offset + limite, pokemons.length());

            for (int i = inicio; i < fin; i++) {
                JSONObject pokeObj = pokemons.getJSONObject(i).getJSONObject("pokemon");
                String urlDetalle = pokeObj.getString("url");
                Pokemon p = obtenerDetallePokemon(urlDetalle);

                // Solo añadir si el nombre no está vacío
                if (!p.getNombre().isEmpty()) {
                    lista.add(p);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
}


```
# MODEL

Pokemon.java
```java
package com.example.intentopokedex3.model;

import java.util.ArrayList;

/**
 * Clase modelo del objeto Pokemon
 *
 * Atributos:
 *    nombre       → nombre del pokemon
 *    numero       → numero de la pokedex
 *    urlImagen    → direccion del sprite
 *    tipos        → lista de tipos
 *    altura       → en metros
 *    peso         → en kg
 *    categoria    → especie o categoria (opcional)
 *    habilidad    → habilidad principal
 *
 * Metodos:
 *    • Getters y Setters → usados por PokedexApi y las Activities
 *    • Constructores vacio y rapido
 *
 * 🔹 Objetivo:
 *    Representar cada Pokemon como un objeto reutilizable
 *    que se pasa entre metodos y pantallas.
 */
public class Pokemon {

    private String nombre;
    private int numero;
    private String imagenUrl;
    private ArrayList<String> tipos;

    private double altura;
    private double peso;
    private String categoria;
    private String habilidad;

    public Pokemon() {}

    public Pokemon(String nombre, String url) {
        this.nombre = nombre;
        this.imagenUrl = url;
    }


    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public ArrayList<String> getTipos() {
        return tipos;
    }

    public void setTipos(ArrayList<String> tipos) {
        this.tipos = tipos;
    }

    public double getAltura() {
        return altura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getHabilidad() {
        return habilidad;
    }

    public void setHabilidad(String habilidad) {
        this.habilidad = habilidad;
    }
}
```
# VIEW
ActividadDetalle.java
```java
package com.example.intentopokedex3.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.intentopokedex3.R;
import com.example.intentopokedex3.controller.PokedexApi;
import com.example.intentopokedex3.model.Pokemon;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ActivityDetalle extends AppCompatActivity {

    // -------------------------------------------------------------------------
    // Variables de interfaz
    // -------------------------------------------------------------------------
    private ImageButton botonVolver, botonLogoInicio;
    private ImageView imagenPokemon;
    private EditText campoBuscar;
    private TextView textoNombre, textoNumero, textoAltura, textoPeso, textoSexo, textoCategoria, textoHabilidad;
    private LinearLayout contenedorTipos;
    private FlexboxLayout contenedorDebilidades;
    private ListView listaSugerencias;

    // -------------------------------------------------------------------------
    // Variables de datos
    // -------------------------------------------------------------------------
    private String nombrePokemon;
    private String numeroPokemon;
    private String urlImagen;

    // -------------------------------------------------------------------------
    // Metodo principal al crear la pantalla
    // -------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        // Referencias UI
        botonVolver = findViewById(R.id.btnVolverDetalle);
        botonLogoInicio = findViewById(R.id.logoInicioDetalle);
        imagenPokemon = findViewById(R.id.imgPokemonDetalle);
        campoBuscar = findViewById(R.id.inputBuscarDetalle);
        textoNombre = findViewById(R.id.txtNombrePokemon);
        textoNumero = findViewById(R.id.txtNumeroPokemon);
        textoAltura = findViewById(R.id.txtAltura);
        textoPeso = findViewById(R.id.txtPeso);
        textoCategoria = findViewById(R.id.txtCategoria);
        textoHabilidad = findViewById(R.id.txtHabilidad);
        contenedorTipos = findViewById(R.id.contenedorTipos);
        contenedorDebilidades = findViewById(R.id.contenedorDebilidades);
        listaSugerencias = findViewById(R.id.listaSugerencias);

        // Configurar botones y buscador
        configurarBotones();
        configurarBuscador();

        // Obtener datos recibidos desde ActivityLista o ActivityTipos
        recuperarDatosIntent();

        // Cargar los detalles del Pokemon
        if (nombrePokemon != null) {
            textoNombre.setText(nombrePokemon.toUpperCase());
            textoNumero.setText("#" + numeroPokemon);

            cargarDetallesPokemon(nombrePokemon.toLowerCase());

            // Descargar imagen con PokedexApi
            new Thread(() -> {
                Bitmap imagen = PokedexApi.descargarImagen(urlImagen);
                runOnUiThread(() -> {
                    if (imagen != null) imagenPokemon.setImageBitmap(imagen);
                });
            }).start();
        } else {
            textoNombre.setText("Pokemon no encontrado");
        }
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo para configurar botones principales
    // -------------------------------------------------------------------------
    private void configurarBotones() {
        // Boton para volver atras
        botonVolver.setOnClickListener(v -> finish());

        // Boton logo inferior -> vuelve al inicio
        botonLogoInicio.setOnClickListener(v -> {
            Intent i = new Intent(ActivityDetalle.this, ActivityInicio.class);
            startActivity(i);
        });
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo que configura el buscador flotante
    // -------------------------------------------------------------------------
    private void configurarBuscador() {
        ArrayList<String> nombresPokemon = new ArrayList<>();
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombresPokemon);
        listaSugerencias.setAdapter(adaptador);

        // Variable para guardar el texto más reciente
        final String[] ultimaBusqueda = {""};

        campoBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String texto = s.toString().trim().toLowerCase();
                ultimaBusqueda[0] = texto; // Guardamos lo último que escribió el usuario

                if (texto.length() >= 1) {
                    new Thread(() -> {
                        ArrayList<Pokemon> lista = PokedexApi.buscarPorNombre(texto);

                        runOnUiThread(() -> {
                            // ✅ Solo actualizamos si el texto no ha cambiado mientras la API respondía
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

            Intent i = new Intent(ActivityDetalle.this, ActivityDetalle.class);
            i.putExtra("nombrePokemon", nombre);
            i.putExtra("numeroPokemon", numero);
            i.putExtra("imagenUrl",
                    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + numero + ".png");
            startActivity(i);

            listaSugerencias.setVisibility(View.GONE);
        });
    }


    // -------------------------------------------------------------------------
    // 🔹 Metodo que obtiene los datos del Pokemon enviados desde otra pantalla
    // -------------------------------------------------------------------------
    private void recuperarDatosIntent() {
        Intent intent = getIntent();
        nombrePokemon = intent.getStringExtra("nombrePokemon");
        numeroPokemon = intent.getStringExtra("numeroPokemon");
        urlImagen = intent.getStringExtra("imagenUrl");
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo que carga los detalles del Pokemon desde la API
    // -------------------------------------------------------------------------
    /**
     * Este metodo realiza una conexion directa a la API:
     *  - Obtiene datos basicos (altura, peso, habilidad)
     *  - Obtiene tipos
     * Luego llama a:
     *  - {@link #mostrarDetalles(Pokemon)} para mostrar los datos
     *  - {@link #cargarDebilidades(ArrayList)} para mostrar las debilidades
     */
    private void cargarDetallesPokemon(String nombre) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // 1️⃣ Petición principal: datos básicos
                URL url = new URL("https://pokeapi.co/api/v2/pokemon/" + nombre);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder jsonBuilder = new StringBuilder();
                String linea;
                while ((linea = reader.readLine()) != null) jsonBuilder.append(linea);
                reader.close();
                conn.disconnect();

                JSONObject json = new JSONObject(jsonBuilder.toString());

                int id = json.getInt("id");
                double altura = json.getDouble("height") / 10.0;
                double peso = json.getDouble("weight") / 10.0;

                JSONArray habilidades = json.getJSONArray("abilities");
                String habilidad = habilidades.getJSONObject(0)
                        .getJSONObject("ability")
                        .getString("name");

                // Traducir habilidad al español desde endpoint /ability/
                String habilidadEsp = habilidad;
                try {
                    URL urlHab = new URL("https://pokeapi.co/api/v2/ability/" + habilidad);
                    HttpURLConnection connHab = (HttpURLConnection) urlHab.openConnection();
                    connHab.setRequestMethod("GET");

                    BufferedReader readerHab = new BufferedReader(new InputStreamReader(connHab.getInputStream()));
                    StringBuilder jsonHab = new StringBuilder();
                    String lineaHab;
                    while ((lineaHab = readerHab.readLine()) != null) jsonHab.append(lineaHab);
                    readerHab.close();
                    connHab.disconnect();

                    JSONObject jsonHabilidad = new JSONObject(jsonHab.toString());
                    JSONArray nombres = jsonHabilidad.getJSONArray("names");
                    for (int i = 0; i < nombres.length(); i++) {
                        JSONObject nameObj = nombres.getJSONObject(i);
                        if (nameObj.getJSONObject("language").getString("name").equals("es")) {
                            habilidadEsp = nameObj.getString("name");
                            break;
                        }
                    }

                } catch (Exception e) {
                    habilidadEsp = habilidad; // fallback
                }

                JSONArray tiposArray = json.getJSONArray("types");
                ArrayList<String> tipos = new ArrayList<>();
                for (int i = 0; i < tiposArray.length(); i++) {
                    tipos.add(tiposArray.getJSONObject(i)
                            .getJSONObject("type")
                            .getString("name"));
                }

                // 2️⃣ Petición secundaria: especie (categoría + descripción en español)
                String categoria = "";
                String descripcion = "";

                try {
                    URL urlEspecie = new URL("https://pokeapi.co/api/v2/pokemon-species/" + id);
                    HttpURLConnection connEsp = (HttpURLConnection) urlEspecie.openConnection();
                    connEsp.setRequestMethod("GET");

                    BufferedReader readerEsp = new BufferedReader(new InputStreamReader(connEsp.getInputStream()));
                    StringBuilder jsonEspBuilder = new StringBuilder();
                    String lineaEsp;
                    while ((lineaEsp = readerEsp.readLine()) != null) jsonEspBuilder.append(lineaEsp);
                    readerEsp.close();
                    connEsp.disconnect();

                    JSONObject jsonEsp = new JSONObject(jsonEspBuilder.toString());

                    // Categoría (genera)
                    JSONArray generaArray = jsonEsp.getJSONArray("genera");
                    for (int i = 0; i < generaArray.length(); i++) {
                        JSONObject genusObj = generaArray.getJSONObject(i);
                        if (genusObj.getJSONObject("language").getString("name").equals("es")) {
                            categoria = genusObj.getString("genus");
                            break;
                        }
                    }

                    // Descripción (flavor_text_entries)
                    JSONArray flavorArray = jsonEsp.getJSONArray("flavor_text_entries");
                    for (int i = 0; i < flavorArray.length(); i++) {
                        JSONObject flavor = flavorArray.getJSONObject(i);
                        if (flavor.getJSONObject("language").getString("name").equals("es")) {
                            descripcion = flavor.getString("flavor_text")
                                    .replace("\n", " ")
                                    .replace("\f", " ");
                            break;
                        }
                    }

                } catch (Exception e2) {
                    categoria = "Desconocida";
                    descripcion = "No hay descripción disponible.";
                }

                // 3️⃣ Crear objeto Pokemon con datos
                Pokemon p = new Pokemon();
                p.setNombre(nombrePokemon);
                p.setNumero(id);
                p.setAltura(altura);
                p.setPeso(peso);
                p.setHabilidad(habilidadEsp);
                p.setTipos(tipos);

                String finalCategoria = categoria;
                String finalDescripcion = descripcion;

                handler.post(() -> {
                    mostrarDetalles(p, finalCategoria, finalDescripcion);
                    cargarDebilidades(p.getTipos());
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }



    // -------------------------------------------------------------------------
    // 🔹 Metodo que muestra los detalles del Pokemon en pantalla
    // -------------------------------------------------------------------------
    /**
     * Rellena los textos con la informacion del objeto Pokemon.
     * Llama a {@link #mostrarTipos(ArrayList)} para mostrar los tipos con color.
     */
    private void mostrarDetalles(Pokemon p, String categoria, String descripcion) {
        textoAltura.setText("Altura: " + p.getAltura() + " m");
        textoPeso.setText("Peso: " + p.getPeso() + " kg");
        textoCategoria.setText("Categoría: " + categoria);
        textoHabilidad.setText("Habilidad: " + p.getHabilidad());
        mostrarTipos(p.getTipos());

        TextView textoDescripcion = findViewById(R.id.txtDescripcion);
        if (textoDescripcion != null) {
            textoDescripcion.setText(descripcion);
        }
    }



    // -------------------------------------------------------------------------
    // 🔹 Metodo que muestra los tipos del Pokemon con su color correspondiente
    // -------------------------------------------------------------------------
    private void mostrarTipos(ArrayList<String> tipos) {
        contenedorTipos.removeAllViews();
        for (String tipo : tipos) {
            TextView chip = crearChip(tipo);
            contenedorTipos.addView(chip);
        }
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo que carga las debilidades segun los tipos del Pokemon
    // -------------------------------------------------------------------------
    /**
     * Llama a la API de cada tipo para obtener su "double_damage_from"
     * (los tipos contra los que es debil) y los muestra con {@link #crearChip(String)}.
     */
    private void cargarDebilidades(ArrayList<String> tipos) {
        contenedorDebilidades.removeAllViews();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            ArrayList<String> debilidadesTotales = new ArrayList<>();
            for (String tipo : tipos) {
                try {
                    URL url = new URL("https://pokeapi.co/api/v2/type/" + tipo);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder jsonBuilder = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        jsonBuilder.append(linea);
                    }
                    reader.close();
                    conn.disconnect();

                    JSONObject json = new JSONObject(jsonBuilder.toString());
                    JSONArray damageArray = json.getJSONObject("damage_relations")
                            .getJSONArray("double_damage_from");

                    for (int i = 0; i < damageArray.length(); i++) {
                        String debilidad = damageArray.getJSONObject(i).getString("name");
                        if (!debilidadesTotales.contains(debilidad)) {
                            debilidadesTotales.add(debilidad);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Volvemos al hilo principal para mostrar chips de debilidades
            handler.post(() -> {
                for (String d : debilidadesTotales) {
                    TextView chipDeb = crearChip(d);
                    contenedorDebilidades.addView(chipDeb);
                }
            });
        });
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo que crea un chip de color segun el tipo o debilidad
    // -------------------------------------------------------------------------
    /**
     * Este metodo se usa tanto en {@link #mostrarTipos(ArrayList)} como
     * en {@link #cargarDebilidades(ArrayList)}.
     * Crea un TextView estilizado con color segun el tipo.
     */
    private TextView crearChip(String texto) {
        TextView chip = new TextView(this);
        chip.setText(texto.toUpperCase());
        chip.setTextColor(Color.WHITE);
        chip.setTextSize(14);
        chip.setPadding(24, 10, 24, 10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        chip.setLayoutParams(params);

        chip.setBackground(crearFondoTipo(texto));
        return chip;
    }

    // -------------------------------------------------------------------------
    // 🔹 Metodo que devuelve el color de fondo segun el tipo
    // -------------------------------------------------------------------------
    /**
     * Este metodo se llama desde {@link #crearChip(String)}.
     * Asigna un color especifico a cada tipo de Pokemon.
     */
    private GradientDrawable crearFondoTipo(String tipo) {
        GradientDrawable fondo = new GradientDrawable();
        fondo.setCornerRadius(50);

        Map<String, String> colores = new HashMap<>();
        colores.put("grass", "#78C850");
        colores.put("fire", "#F08030");
        colores.put("water", "#6890F0");
        colores.put("bug", "#A8B820");
        colores.put("normal", "#A8A878");
        colores.put("electric", "#F8D030");
        colores.put("ground", "#E0C068");
        colores.put("fairy", "#EE99AC");
        colores.put("fighting", "#C03028");
        colores.put("psychic", "#F85888");
        colores.put("rock", "#B8A038");
        colores.put("ghost", "#705898");
        colores.put("ice", "#98D8D8");
        colores.put("dragon", "#7038F8");
        colores.put("poison", "#A040A0");
        colores.put("steel", "#B8B8D0");
        colores.put("dark", "#705848");
        colores.put("flying", "#A890F0");

        String color = colores.get(tipo.toLowerCase());
        if (color == null) color = "#AAAAAA";
        fondo.setColor(Color.parseColor(color));

        return fondo;
    }
}
```
ActividadInicio.java
```java
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


```
ActividadLista.java
```java
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


```
ActividadTipos.java
```java
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

```

# DRAWABLE (solo nombres de recursos)

btn_volver.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Fondo circular rosa oscuro -->
    <item>
        <shape android:shape="oval">
            <solid android:color="#FF96CE" /> <!-- Rosa más oscuro -->
            <size android:width="35dp" android:height="35dp" />
        </shape>
    </item>

    <!-- Flecha morada -->
    <item>
        <vector
            android:width="12dp"
            android:height="12dp"
            android:viewportWidth="24"
            android:viewportHeight="24"
            android:gravity="center"
            android:tint="#8D74D9"> <!-- Morado (violeta brillante) -->
            <path
                android:fillColor="#8A2BE2"
                android:pathData="M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z" />
        </vector>
    </item>

</layer-list>

```
chip_base.xml
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="?attr/colorControlHighlight"/> <!-- se sobreescribe desde Java -->
    <corners android:radius="50dp"/>
</shape>

```
fondo_caja_detalle.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <gradient
        android:startColor="#B3F9C7D4"
    android:endColor="#B35B68E0"
    android:angle="270" />

    <corners android:radius="20dp"/>

    <padding android:bottom="4dp"/>
</shape>

```
fondo_inicio.png
fondo_item_pokemon.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#803E1F47" />
    <corners android:radius="20dp" />
</shape>

```
fondo_logo_redondeado.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Sombra morada muy clarita -->
    <item>
        <shape android:shape="rectangle">
            <solid android:color="#E8D7FF" />
            <corners android:topLeftRadius="60dp" android:topRightRadius="60dp" />
        </shape>
    </item>

    <!-- Franja principal rosita -->
    <item android:top="4dp">
        <shape android:shape="rectangle">
            <solid android:color="#FFD6E7" />
            <corners android:topLeftRadius="55dp" android:topRightRadius="55dp" />
        </shape>
    </item>
</layer-list>

```
ic_arrow_back_black_24dp.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z" />
</vector>

```
rounded_search_background_white.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="@android:color/white" />
    <corners android:radius="30dp" />
    <stroke android:width="1dp" android:color="#DDDDDD" />
</shape>


```
# LAYOUT
activity_detalle.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".view.ActivityDetalle">

    <!-- 🔹 CONTENIDO PRINCIPAL -->
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/fondo_pokedex"
        android:paddingTop="20dp">

        <!-- 🔹 Barra superior -->
        <LinearLayout
            android:id="@+id/barraSuperior"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:paddingHorizontal="12dp">

            <ImageButton
                android:id="@+id/btnVolverDetalle"
                android:layout_width="45dp"
                android:layout_height="45dp"
                android:background="@drawable/btn_volver"
                android:contentDescription="Volver"
                android:padding="8dp"
                android:scaleType="centerInside" />

            <EditText
                android:id="@+id/inputBuscarDetalle"
                android:layout_width="0dp"
                android:layout_height="40dp"
                android:layout_weight="1"
                android:hint="Buscar Pokémon..."
                android:background="@drawable/rounded_search_background_white"
                android:textColorHint="#999"
                android:textColor="#000"
                android:paddingHorizontal="15dp"
                android:layout_marginLeft="8dp" />
        </LinearLayout>

        <!-- 🟣 Caja morada -->
        <LinearLayout
            android:id="@+id/cajaMorada"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_below="@id/barraSuperior"
            android:layout_above="@id/logoInicioDetalle"
            android:layout_marginHorizontal="18dp"
            android:layout_marginTop="16dp"
            android:layout_marginBottom="17dp"
            android:background="@drawable/fondo_caja_detalle"
            android:clipToOutline="true"
            android:elevation="8dp"
            android:gravity="center_horizontal"
            android:orientation="vertical"
            android:padding="20dp">

            <!-- Imagen Pokémon -->
            <ImageView
                android:id="@+id/imgPokemonDetalle"
                android:layout_width="250dp"
                android:layout_height="227dp"
                android:layout_marginBottom="10dp"
                android:scaleType="fitCenter" />

            <!-- Número -->
            <TextView
                android:id="@+id/txtNumeroPokemon"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:text="#003"
                android:textColor="#FFFFFF"
                android:textSize="16sp" />

            <!-- Nombre -->
            <TextView
                android:id="@+id/txtNombrePokemon"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginBottom="14dp"
                android:text="VENUSAUR"
                android:textColor="#FFFFFF"
                android:textSize="28sp"
                android:textStyle="bold" />

            <!-- Datos -->
            <LinearLayout
                android:id="@+id/cajaDatos"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="6dp"
                android:layout_marginBottom="12dp"
                android:gravity="center_horizontal"
                android:orientation="vertical"
                android:paddingHorizontal="16dp">

                <TextView
                    android:id="@+id/txtAltura"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Altura: 2.0 m"
                    android:textColor="#FFFFFF"
                    android:textSize="16sp" />

                <TextView
                    android:id="@+id/txtPeso"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Peso: 100.0 kg"
                    android:textColor="#FFFFFF"
                    android:textSize="16sp" />


                <TextView
                    android:id="@+id/txtCategoria"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Categoría: Semilla"
                    android:textColor="#FFFFFF"
                    android:textSize="16sp" />

                <TextView
                    android:id="@+id/txtHabilidad"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Habilidad: Espesura"
                    android:textColor="#FFFFFF"
                    android:textSize="16sp" />
                <TextView
                    android:id="@+id/txtDescripcion"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="10dp"
                    android:text="Descripción del Pokémon..."
                    android:textColor="#EDEDED"
                    android:textSize="15sp"
                    android:gravity="center"
                    android:lineSpacingExtra="4dp"
                    android:paddingHorizontal="10dp"
                    android:textAlignment="center" />
            </LinearLayout>

            <!-- Tipo -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="10dp"
                android:gravity="center"
                android:text="Tipo"
                android:textColor="#FFFFFF"
                android:textSize="17sp"
                android:textStyle="bold" />

            <LinearLayout
                android:id="@+id/contenedorTipos"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="6dp"
                android:layout_marginBottom="8dp"
                android:gravity="center"
                android:orientation="horizontal" />

            <!-- Debilidad -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="10dp"
                android:gravity="center"
                android:text="Debilidad"
                android:textColor="#FFFFFF"
                android:textSize="17sp"
                android:textStyle="bold" />

            <com.google.android.flexbox.FlexboxLayout
                android:id="@+id/contenedorDebilidades"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="6dp"
                android:layout_marginBottom="8dp"
                app:flexWrap="wrap"
                app:justifyContent="center" />
        </LinearLayout>

        <!-- Logo Pokédex -->
        <ImageButton
            android:id="@+id/logoInicioDetalle"
            android:layout_width="180dp"
            android:layout_height="70dp"
            android:layout_alignParentBottom="true"
            android:layout_centerHorizontal="true"
            android:src="@drawable/logo_pokedex"
            android:background="@android:color/transparent"
            android:scaleType="fitCenter"
            android:layout_marginBottom="4dp" />

    </RelativeLayout>

    <!-- 🌟 LISTA DE SUGERENCIAS FLOTANTE -->
    <ListView
        android:id="@+id/listaSugerencias"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="16dp"
        android:layout_marginTop="75dp"
        android:background="#E6FFFFFF"
        android:divider="@android:color/darker_gray"
        android:dividerHeight="1dp"
        android:elevation="16dp"
        android:visibility="gone"
        android:padding="10dp"
        android:clipToOutline="true"
        android:outlineProvider="background" />

</FrameLayout>

```
activity_inicio.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/fondo_pokedex_dos"
    tools:context=".view.ActivityInicio">

    <!-- Imagen de fondo completa y clicable -->
    <ImageButton
        android:id="@+id/btnIrATipos"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/fondo_pokedex_dos"
        android:background="@null"
        android:scaleType="fitXY"
        android:contentDescription="Ir a selección de tipos" />
</RelativeLayout>


```
activity_lista.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".view.ActivityLista">

    <!-- 🔹 Contenido principal -->
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/fondo_pokedex"
        android:paddingTop="20dp">

        <!-- 🔹 Barra superior -->
        <LinearLayout
            android:id="@+id/barraSuperior"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:paddingHorizontal="12dp">

            <ImageButton
                android:id="@+id/btnVolverLista"
                android:layout_width="45dp"
                android:layout_height="45dp"
                android:background="@drawable/btn_volver"
                android:contentDescription="Volver"
                android:padding="8dp"
                android:scaleType="centerInside" />

            <EditText
                android:id="@+id/buscarPokemon"
                android:layout_width="0dp"
                android:layout_height="40dp"
                android:layout_weight="1"
                android:hint="Buscar Pokémon..."
                android:background="@drawable/rounded_search_background_white"
                android:textColorHint="#888"
                android:textColor="#000"
                android:paddingHorizontal="15dp"
                android:layout_marginLeft="8dp" />
        </LinearLayout>

        <!-- 🔹 Scroll con el grid -->
        <ScrollView
            android:id="@+id/scrollPokemons"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_below="@id/barraSuperior"
            android:layout_above="@id/btnLogoInicioLista"
            android:fillViewport="true">

            <!-- 🔸 Grid con 2 columnas -->
            <GridLayout
                android:id="@+id/gridPokemons"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:columnCount="2"
                android:alignmentMode="alignMargins"
                android:useDefaultMargins="true"
                android:padding="12dp"
                android:rowOrderPreserved="false"
                android:gravity="center" />
        </ScrollView>

        <!-- 🔹 Logo inferior -->
        <ImageButton
            android:id="@+id/btnLogoInicioLista"
            android:layout_width="match_parent"
            android:layout_height="60dp"
            android:layout_alignParentBottom="true"
            android:layout_marginBottom="6dp"
            android:src="@drawable/logo_pokedex"
            android:scaleType="fitCenter"
            android:adjustViewBounds="true"
            android:background="@android:color/transparent"
            android:contentDescription="Volver al inicio"/>
    </RelativeLayout>

    <!-- 🌟 LISTVIEW FLOTANTE ENCIMA -->
    <ListView
        android:id="@+id/listaSugerencias"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="16dp"
        android:layout_marginTop="80dp"
        android:background="#F8F8F8"
        android:divider="@android:color/darker_gray"
        android:dividerHeight="1dp"
        android:elevation="16dp"
        android:visibility="gone"
        android:padding="8dp"
        android:clipToOutline="true"
        android:outlineProvider="background" />

</FrameLayout>


```
activity_tipos.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".view.ActivityTipos">

    <!-- 🌸 CONTENIDO PRINCIPAL -->
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/fondo_pokedex"
        android:paddingTop="16dp"
        android:paddingBottom="8dp">

        <!-- 🔹 Barra superior -->
        <LinearLayout
            android:id="@+id/barraSuperior"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginHorizontal="12dp"
            android:layout_marginBottom="12dp">


            <ImageButton
                android:id="@+id/btnVolverTipos"
                android:layout_width="45dp"
                android:layout_height="45dp"
                android:background="@drawable/btn_volver"
                android:contentDescription="Volver"
                android:padding="8dp"
                android:scaleType="centerInside" />

            <EditText
                android:id="@+id/inputBuscarTipos"
                android:layout_width="0dp"
                android:layout_height="40dp"
                android:layout_weight="1"
                android:hint="Buscar Pokémon..."
                android:background="@drawable/rounded_search_background_white"
                android:textColorHint="#888"
                android:textColor="#000"
                android:paddingHorizontal="15dp"
                android:layout_marginLeft="8dp" />
        </LinearLayout>

    <!-- 🟣 Contenedor de tipos -->
    <GridLayout
        android:id="@+id/gridTipos"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/barraSuperior"
        android:columnCount="2"
        android:alignmentMode="alignMargins"
        android:useDefaultMargins="true"
        android:layout_marginHorizontal="16dp"
        android:layout_marginTop="20dp"
        android:gravity="center">

        <!-- Fila 1 -->
        <TextView
            android:id="@+id/btnPlanta"
            android:text="PLANTA"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_planta"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <TextView
            android:id="@+id/btnBicho"
            android:text="BICHO"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_bicho"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <!-- Fila 2 -->
        <TextView
            android:id="@+id/btnAgua"
            android:text="AGUA"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_agua"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <TextView
            android:id="@+id/btnHielo"
            android:text="HIELO"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_hielo"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <!-- Fila 3 -->
        <TextView
            android:id="@+id/btnFuego"
            android:text="FUEGO"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_fuego"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <TextView
            android:id="@+id/btnRoca"
            android:text="ROCA"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_roca"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <!-- Fila 4 -->
        <TextView
            android:id="@+id/btnTierra"
            android:text="TIERRA"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_tierra"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <TextView
            android:id="@+id/btnLucha"
            android:text="LUCHA"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_lucha"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <!-- Fila 5 -->
        <TextView
            android:id="@+id/btnElectrico"
            android:text="ELÉCTRICO"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_electrico"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <TextView
            android:id="@+id/btnSiniestro"
            android:text="SINIESTRO"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_siniestro"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <!-- Fila 6 -->
        <TextView
            android:id="@+id/btnNormal"
            android:text="NORMAL"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_normal"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <TextView
            android:id="@+id/btnFantasma"
            android:text="FANTASMA"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_fantasma"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <!-- Fila 7 -->
        <TextView
            android:id="@+id/btnVeneno"
            android:text="VENENO"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_veneno"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <TextView
            android:id="@+id/btnPsiquico"
            android:text="PSÍQUICO"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_psiquico"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <!-- Fila 8 -->
        <TextView
            android:id="@+id/btnHada"
            android:text="HADA"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_hada"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <TextView
            android:id="@+id/btnDragon"
            android:text="DRAGÓN"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_dragon"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <!-- Fila 9 -->
        <TextView
            android:id="@+id/btnAcero"
            android:text="ACERO"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_acero"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />

        <TextView
            android:id="@+id/btnVolador"
            android:text="VOLADOR"
            android:layout_width="0dp"
            android:layout_height="60dp"
            android:layout_columnWeight="1"
            android:layout_margin="6dp"
            android:textColor="@android:color/black"
            android:textStyle="bold"
            android:gravity="center"
            android:background="@drawable/btn_volador"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?attr/selectableItemBackground" />
    </GridLayout>


    <!-- 🔘 Logo inferior -->
    <ImageButton
        android:id="@+id/btnLogoInicio"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:layout_alignParentBottom="true"
        android:layout_marginBottom="6dp"
        android:src="@drawable/logo_pokedex"
        android:scaleType="fitCenter"
        android:adjustViewBounds="true"
        android:background="@android:color/transparent"
        android:contentDescription="Volver al inicio"/>

</RelativeLayout>

    <!-- 🌟 LISTVIEW FLOTANTE (encima del contenido) -->
    <ListView
        android:id="@+id/listaSugerencias"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="16dp"
        android:layout_marginTop="75dp"
        android:background="#E6FFFFFF"
        android:divider="@android:color/transparent"
        android:dividerHeight="8dp"
        android:elevation="16dp"
        android:visibility="gone"
        android:padding="10dp"
        android:clipToOutline="true"
        android:outlineProvider="background" />
</FrameLayout>

```

# item_pokemon.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@drawable/fondo_item_pokemon"
    android:padding="8dp"
    android:layout_margin="6dp"
    android:gravity="center"
    android:elevation="3dp">

    <ImageView
        android:id="@+id/imgPokemon"
        android:layout_width="90dp"
        android:layout_height="90dp"
        android:layout_gravity="center"
        android:adjustViewBounds="true"
        android:scaleType="fitCenter" />

    <TextView
        android:id="@+id/txtNombrePokemon"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="#045 Girafflower"
        android:textColor="@android:color/white"
        android:textStyle="bold"
        android:gravity="center"
        android:layout_marginTop="6dp" />

</LinearLayout>


```
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.intentopokedex3">

    <!-- 🔐 Permiso para acceder a Internet (necesario para Volley y la API) -->
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.IntentoPokedex3">

        <!-- 🏠 Pantalla principal: ActivityInicio -->
        <activity
            android:name=".view.ActivityInicio"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- 🔹 Resto de actividades -->
        <activity android:name=".view.ActivityTipos" android:exported="false" />
        <activity android:name=".view.ActivityLista" android:exported="false" />
        <activity android:name=".view.ActivityDetalle" android:exported="false" />

    </application>

</manifest>


```
