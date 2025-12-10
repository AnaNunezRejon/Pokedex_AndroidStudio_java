# ESTRUCTURA
```plsql
app/
 ├── manifests/
 │    └── AndroidManifest.xml
 │
 ├── java/
 │    └── com.example.pokedex/
 │         ├── controller/
 │         │     └── PokedexControlador.java
 │         │
 │         ├── model/
 │         │     ├── PokedexApi.java
 │         │     └── Pokemon.java
 │         │
 │         └── view/
 │               ├── ActividadDetalle.java
 │               ├── ActividadInicio.java
 │               ├── ActividadLista.java
 │               ├── ActividadTipos.java
 │               └── AdaptadorSimple.java
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
 │    │     ├── chip_base.xml
 │    │     ├── chip_debilidad.xml
 │    │     ├── chip_tipo.xml
 │    │     ├── fondo_detalle_degradado.xml
 │    │     ├── fondo_inicio.png
 │    │     ├── fondo_item_pokemon.xml
 │    │     ├── fondo_logo_redondeado.xml
 │    │     ├── fondo_pokedex.png
 │    │     ├── fondo_pokedex_dos.png
 │    │     ├── fondo_pokedex_uno.png
 │    │     ├── fondo_pokemons.jpg
 │    │     ├── ic_arrow_back_black_24dp.xml
 │    │     ├── ic_launcher_background.xml
 │    │     ├── ic_launcher_foreground.xml
 │    │     ├── logo_pokedex.png
 │    │     └── rounded_search_background.xml
 │    │
 │    └── layout/
 │          ├── activity_detalle.xml
 │          ├── activity_inicio.xml
 │          ├── activity_lista.xml
 │          └── activity_tipos.xml
 │
 └── com.example.pokedex (androidTest + test)

```

Estructura del Proyecto — Pokedex (Formato .md para README)
# CONTROLLER
PokedexControlador.java
```java
package com.example.pokedex.controller;

import com.example.pokedex.model.PokedexApi;
import com.example.pokedex.model.Pokemon;
import java.util.ArrayList;

/**
 * Clase PokedexControlador
 * Esta clase actúa como un "intermediario" entre las vistas (pantallas del usuario)
 * y el modelo (las clases que se encargan de obtener los datos desde la API).
 * Su función es recibir las peticiones de las Activities y devolver los resultados.
 * Así mantenemos el código ordenado y separado por responsabilidades (MVC).
 */
public class PokedexControlador {

    /**
     * Metodo para obtener una lista de Pokémon filtrados por tipo.
     * Llama al modelo (PokedexApi) para que se conecte a internet
     * y devuelva todos los Pokémon del tipo especificado (agua, fuego, planta, etc.)
     *
     * @param tipo El tipo de Pokémon (por ejemplo: "fire", "water", "grass"...)
     * @return Una lista con objetos Pokémon del tipo indicado.
     */
    public static ArrayList<Pokemon> obtenerPokemonsPorTipo(String tipo) {
        // Aquí simplemente llamamos al modelo para que haga el trabajo real
        return PokedexApi.obtenerPorTipo(tipo);
    }

    /**
     * Metodo para buscar un Pokémon por su nombre.
     * Este metodo pide al modelo (PokedexApi) que obtenga los datos
     * del Pokémon usando su nombre, como "pikachu" o "bulbasaur".
     * @param nombrePokemon El nombre del Pokémon a buscar.
     * @return Un objeto Pokémon con todos sus datos (altura, peso, tipos...).
     */
    public static Pokemon buscarPokemon(String nombrePokemon) {
        // El controlador simplemente reenvía la petición al modelo.
        return PokedexApi.obtenerDetallesPokemon(nombrePokemon);
    }
}

```
# MODEL
PokedexApi.java
```java
package com.example.pokedex.model;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Clase PokedexApi
 * Esta clase se encarga de conectarse a la API de Pokémon (https://pokeapi.co/)
 * y obtener los datos de los Pokémon.
 * Desde aquí se hacen las peticiones HTTP para descargar información de internet.
 * Es parte del "modelo" en la estructura MVC (Modelo - Vista - Controlador).
 */
public class PokedexApi {

    /**
     * Metodo que obtiene una lista de Pokémon según su tipo.
     * Por ejemplo: si el tipo es "fire", devuelve una lista de Pokémon de fuego.
     * @param tipo Tipo de Pokémon a buscar.
     * @return Una lista con los Pokémon de ese tipo.
     */
    public static ArrayList<Pokemon> obtenerPorTipo(String tipo) {

        // Lista donde guardaremos los Pokémon descargados
        ArrayList<Pokemon> lista = new ArrayList<>();

        try {
            // Creamos la URL de la API usando el tipo (por ejemplo: https://pokeapi.co/api/v2/type/fire)
            URL url = new URL("https://pokeapi.co/api/v2/type/" + tipo.toLowerCase());

            // Abrimos la conexión HTTP
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET"); // Metodo GET para leer datos

            // Leemos la respuesta de la API
            BufferedReader reader = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            StringBuilder resultado = new StringBuilder();
            String linea;

            // Vamos leyendo línea por línea
            while ((linea = reader.readLine()) != null) {
                resultado.append(linea);
            }
            reader.close(); // Cerramos la conexión

            // Convertimos el texto en un objeto JSON
            JSONObject json = new JSONObject(resultado.toString());
            JSONArray pokemons = json.getJSONArray("pokemon");

            // Limitamos a 30 Pokémon máximo para que no se sobrecargue la app
            int limite;
            if (pokemons.length() > 30) {
                limite = 30;
            } else {
                limite = pokemons.length();
            }

            // Recorremos cada Pokémon y lo guardamos en la lista
            for (int i = 0; i < limite; i++) {
                // Cada elemento tiene un objeto "pokemon" dentro
                JSONObject pokeObj = pokemons.getJSONObject(i).getJSONObject("pokemon");

                // Nombre del Pokémon
                String nombre = pokeObj.getString("name");

                // URL con más datos del Pokémon
                String urlPokemon = pokeObj.getString("url");

                // De la URL sacamos el ID (el número del final)
                String[] partes = urlPokemon.split("/");
                int id = Integer.parseInt(partes[partes.length - 1]);

                // Creamos el objeto Pokémon y le asignamos su tipo
                Pokemon p = new Pokemon(nombre, id);
                p.setTipo1(tipo);

                // Lo añadimos a la lista
                lista.add(p);
            }

        } catch (Exception e) {
            // Si ocurre algún error (por ejemplo, sin conexión), se muestra en consola
            e.printStackTrace();
        }

        // Devolvemos la lista final
        return lista;
    }

    /**
     * Metodo que obtiene los detalles de un Pokémon (altura, peso, tipos...).
     * @param nombrePokemon El nombre del Pokémon (por ejemplo, "pikachu").
     * @return Un objeto Pokémon con sus datos completos.
     */
    public static Pokemon obtenerDetallesPokemon(String nombrePokemon) {
        try {
            // Creamos la URL del Pokémon
            URL url = new URL("https://pokeapi.co/api/v2/pokemon/" + nombrePokemon.toLowerCase());

            // Abrimos conexión
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");

            // Leemos los datos
            BufferedReader reader = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            StringBuilder resultado = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                resultado.append(linea);
            }
            reader.close();

            // Convertimos el texto a JSON
            JSONObject json = new JSONObject(resultado.toString());

            // Obtenemos los datos básicos
            String nombre = json.getString("name");
            int id = json.getInt("id");

            // Altura y peso vienen en decímetros y hectogramos, los convertimos a metros y kg
            double altura = json.getDouble("height") / 10.0;
            double peso = json.getDouble("weight") / 10.0;

            // Creamos el objeto Pokémon
            Pokemon p = new Pokemon(nombre, id);
            p.setAltura(altura);
            p.setPeso(peso);

            // Tipos (puede tener 1 o 2)
            JSONArray tipos = json.getJSONArray("types");
            if (tipos.length() > 0) {
                String tipo1 = tipos.getJSONObject(0).getJSONObject("type").getString("name");
                p.setTipo1(tipo1);

                if (tipos.length() > 1) {
                    String tipo2 = tipos.getJSONObject(1).getJSONObject("type").getString("name");
                    p.setTipo2(tipo2);
                }
            }

            return p; // Devolvemos el Pokémon con todos sus datos

        } catch (Exception e) {
            e.printStackTrace(); // Mostramos error si falla algo
        }

        return null; // Si hubo un error, devolvemos null
    }

    /**
     * Metodo que obtiene los nombres de todos los Pokémon existentes.
     * Esto se usa, por ejemplo, para el buscador de la app.
     * @return Lista de nombres de Pokémon con la primera letra en mayúscula.
     */
    public static ArrayList<String> obtenerNombresPokemon() {
        ArrayList<String> nombres = new ArrayList<>();

        try {
            int offset = 0;
            int limit = 200;

            while (true) {
                URL url = new URL("https://pokeapi.co/api/v2/pokemon?limit=" + limit + "&offset=" + offset);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                StringBuilder resultado = new StringBuilder();
                String linea;
                while ((linea = reader.readLine()) != null) {
                    resultado.append(linea);
                }
                reader.close();

                JSONObject json = new JSONObject(resultado.toString());
                JSONArray resultados = json.getJSONArray("results");

                if (resultados.length() == 0) break; // No hay más páginas

                for (int i = 0; i < resultados.length(); i++) {
                    JSONObject obj = resultados.getJSONObject(i);
                    String nombre = obj.getString("name");
                    String nombreConMayuscula = nombre.substring(0, 1).toUpperCase() + nombre.substring(1);
                    nombres.add(nombreConMayuscula);
                }

                offset += limit; // Avanzamos a la siguiente página
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return nombres;
    }
}

```
Pokemon.java
```java
package com.example.pokedex.model;

/**
 * Clase Pokemon
 * Esta clase representa a un Pokémon con todos sus datos básicos.
 * Es una "plantilla" (clase modelo) que usamos para crear objetos Pokémon
 * en nuestro programa, con su nombre, tipo, id, peso, etc.
 */
public class Pokemon {

    // 🔹 Atributos (características del Pokémon)
    private String nombre;
    private String tipo1;
    private String tipo2;
    private String imagenUrl;
    private int id;
    private double altura;
    private double peso;

    /**
     * Constructor del Pokémon
     * Se ejecuta cuando creamos un nuevo Pokémon.
     * Por ejemplo: new Pokemon("pikachu", 25);
     * @param nombre Nombre del Pokémon.
     * @param id Número identificador (el de la Pokédex).
     */
    public Pokemon(String nombre, int id) {
        this.nombre = nombre;
        this.id = id;

        // Creamos automáticamente la URL de su imagen en base a su ID
        // (La PokéAPI tiene las imágenes organizadas por número)
        this.imagenUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png";
    }

    // -------------------- MÉTODOS GETTER --------------------
    // Los getters sirven para "obtener" el valor de una propiedad.

    public String getNombre() {
        return nombre;
    }

    public int getId() {
        return id;
    }

    public String getTipo1() {
        return tipo1;
    }

    public String getTipo2() {
        return tipo2;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public double getAltura() {
        return altura;
    }

    public double getPeso() {
        return peso;
    }

    // -------------------- MÉTODOS SETTER --------------------
    // Los setters sirven para "modificar" una propiedad después de crear el Pokémon.

    public void setTipo1(String tipo1) {
        this.tipo1 = tipo1;
    }

    public void setTipo2(String tipo2) {
        this.tipo2 = tipo2;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }
}

```
# VIEW
ActividadDetalle.java
```java
package com.example.pokedex.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.flexbox.FlexboxLayout;


import androidx.appcompat.app.AppCompatActivity;

import com.example.pokedex.R;
import com.example.pokedex.model.PokedexApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 📘 Clase ActividadDetalle
 *
 * Esta pantalla muestra la ficha completa de un Pokémon con:
 * - Imagen oficial
 * - Nombre e ID
 * - Altura, peso y habilidad principal
 * - Tipos (por ejemplo: Fuego, Volador)
 * - Debilidades (por ejemplo: Agua, Tierra)
 *
 * También tiene un buscador y botones para volver o ir al inicio.
 */
public class ActividadDetalle extends AppCompatActivity {

    // ---------------- ELEMENTOS DE LA INTERFAZ ----------------
    ImageView imgPokemon;          // Imagen del Pokémon
    TextView txtNombre;            // Nombre y número del Pokémon
    TextView txtDetalles;          // Altura, peso y habilidad
    LinearLayout layoutTipos;      // Donde se muestran los tipos (Fuego, Agua, etc.)
    FlexboxLayout layoutDebilidades;// Donde se muestran las debilidades
    AutoCompleteTextView buscador; // Buscador para buscar Pokémon

    // Hilos para tareas en segundo plano
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // -----------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        // ---------------- ENLACE DE VISTAS ----------------
        imgPokemon = findViewById(R.id.imgPokemon);
        txtNombre = findViewById(R.id.txtNombre);
        txtDetalles = findViewById(R.id.txtDetalles);
        layoutTipos = findViewById(R.id.layoutTipos);
        layoutDebilidades = findViewById(R.id.layoutDebilidades);
        buscador = findViewById(R.id.buscarLista);

        // ---------------- BOTONES DE NAVEGACIÓN ----------------
        // 🔙 Botón "Atrás"
        ImageButton btnAtrasDetalle = findViewById(R.id.btnAtrasDetalle);
        btnAtrasDetalle.setOnClickListener(v -> finish()); // Cierra esta pantalla

        // 🏠 Botón del logo para volver al inicio
        ImageButton btnLogoInicio = findViewById(R.id.btnLogoInicio);
        btnLogoInicio.setOnClickListener(v -> {
            Intent intent = new Intent(ActividadDetalle.this, ActividadInicio.class);
            // Evita abrir muchas pantallas iguales
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // ---------------- RECIBIR DATOS DEL INTENT ----------------
        // Al abrir esta pantalla, puede venir con:
        // - El ID del Pokémon (si viene de la lista)
        // - El nombre del Pokémon (si viene del buscador)
        int idPokemon = getIntent().getIntExtra("idPokemon", -1);
        String nombrePokemon = getIntent().getStringExtra("nombrePokemon");

        if (idPokemon != -1) {
            // Si tenemos el ID, cargamos sus detalles directamente
            new CargarPokemonDetalles().execute(idPokemon);
        } else if (nombrePokemon != null) {
            // Si solo tenemos el nombre, lo buscamos
            buscarIdYMostrar(nombrePokemon);
        } else {
            Toast.makeText(this, "Error: Pokémon no encontrado", Toast.LENGTH_SHORT).show();
            finish();
        }

        // ---------------- CONFIGURAR BUSCADOR ----------------
        cargarBuscador();
    }

    /**
     * 🔹 Carga todos los nombres de Pokémon para el buscador.
     * Muestra sugerencias mientras el usuario escribe.
     */
    private void cargarBuscador() {
        executor.execute(() -> {
            ArrayList<String> nombres = PokedexApi.obtenerNombresPokemon();
            handler.post(() -> {
                ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                        ActividadDetalle.this,
                        android.R.layout.simple_dropdown_item_1line,
                        nombres
                );
                buscador.setAdapter(adaptador);

                // Cuando el usuario selecciona un nombre del menú
                buscador.setOnItemClickListener((parent, view, position, id) -> {
                    String nombreSeleccionado = (String) parent.getItemAtPosition(position);
                    buscarIdYMostrar(nombreSeleccionado);
                });

                // Cuando el usuario escribe y pulsa "Enter"
                buscador.setOnEditorActionListener((v, actionId, event) -> {
                    String texto = buscador.getText().toString().trim();
                    if (!texto.isEmpty()) {
                        buscarIdYMostrar(texto);
                    }
                    return true;
                });
            });
        });
    }

    /**
     * 🔹 Busca un Pokémon por nombre en la API y obtiene su ID para mostrarlo.
     */
    private void buscarIdYMostrar(String nombrePokemon) {
        executor.execute(() -> {
            try {
                // Creamos la URL para consultar el Pokémon
                URL url = new URL("https://pokeapi.co/api/v2/pokemon/" + nombrePokemon.toLowerCase());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                // Leemos la respuesta
                InputStream input = con.getInputStream();
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = input.read()) != -1) {
                    sb.append((char) ch);
                }
                input.close();

                // Convertimos el texto a JSON y sacamos el ID
                JSONObject data = new JSONObject(sb.toString());
                int id = data.getInt("id");

                // Ejecutamos la carga de detalles en el hilo principal
                handler.post(() -> new CargarPokemonDetalles().execute(id));

            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() ->
                        Toast.makeText(ActividadDetalle.this, "Pokémon no encontrado", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // ------------------- CLASE INTERNA PARA CARGAR DETALLES -------------------
    /**
     * Esta clase descarga la información completa de un Pokémon
     * (nombre, imagen, altura, peso, tipos...).
     */
    private class CargarPokemonDetalles extends AsyncTask<Integer, Void, JSONObject> {
        Bitmap imagen; // Imagen del Pokémon

        @Override
        protected JSONObject doInBackground(Integer... params) {
            int id = params[0];
            try {
                URL url = new URL("https://pokeapi.co/api/v2/pokemon/" + id);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                // Leemos los datos
                InputStream input = con.getInputStream();
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = input.read()) != -1) {
                    sb.append((char) ch);
                }
                input.close();

                JSONObject data = new JSONObject(sb.toString());

                // Descargamos la imagen oficial del Pokémon
                String imgUrl = data.getJSONObject("sprites")
                        .getJSONObject("other")
                        .getJSONObject("official-artwork")
                        .getString("front_default");

                InputStream is = new URL(imgUrl).openStream();
                imagen = BitmapFactory.decodeStream(is);

                return data;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            if (data == null) {
                Toast.makeText(ActividadDetalle.this, "Error al cargar Pokémon", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Extraemos los datos básicos
                String nombre = data.getString("name");
                int id = data.getInt("id");
                double altura = data.getDouble("height") / 10.0; // Convertimos a metros
                double peso = data.getDouble("weight") / 10.0;   // Convertimos a kg

                // Mostramos nombre e ID formateados (por ejemplo: 004 CHARMANDER)
                txtNombre.setText(String.format("%03d %s", id, nombre.toUpperCase()));

                // Mostramos detalles (altura, peso, habilidad)
                String habilidad = data.getJSONArray("abilities")
                        .getJSONObject(0)
                        .getJSONObject("ability")
                        .getString("name");

                txtDetalles.setText("Altura: " + altura + " m\nPeso: " + peso + " kg\nHabilidad: " + habilidad);

                // Mostramos la imagen
                if (imagen != null) {
                    imgPokemon.setImageBitmap(imagen);
                }

                // ---------------- TIPOS ----------------
                layoutTipos.removeAllViews(); // Limpiamos por si había otro Pokémon antes
                JSONArray tipos = data.getJSONArray("types");

                for (int i = 0; i < tipos.length(); i++) {
                    String tipo = tipos.getJSONObject(i)
                            .getJSONObject("type")
                            .getString("name");

                    // Añadimos un “chip” visual con el tipo (por ejemplo: FUEGO)
                    agregarChip(layoutTipos, tipo, true);
                }

                // ---------------- DEBILIDADES ----------------
                // Obtenemos la URL del primer tipo para consultar sus debilidades
                String tipoUrl = tipos.getJSONObject(0)
                        .getJSONObject("type")
                        .getString("url");

                // Cargamos las debilidades del tipo principal
                new CargarDebilidades().execute(tipoUrl);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ------------------- CLASE INTERNA PARA DEBILIDADES -------------------
    private class CargarDebilidades extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                // Leemos la respuesta
                InputStream input = con.getInputStream();
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = input.read()) != -1) {
                    sb.append((char) ch);
                }
                input.close();

                JSONObject data = new JSONObject(sb.toString());

                // Las debilidades están dentro de "damage_relations"
                return data.getJSONObject("damage_relations").getJSONArray("double_damage_from");

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray debilidades) {
            layoutDebilidades.removeAllViews(); // Limpiamos antes de mostrar
            if (debilidades == null) return;

            try {
                for (int i = 0; i < debilidades.length(); i++) {
                    String tipo = debilidades.getJSONObject(i).getString("name");
                    agregarChip(layoutDebilidades, tipo, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ------------------- MÉTODO PARA CREAR UN CHIP DE TIPO -------------------
    private void agregarChip(ViewGroup layout, String tipo, boolean esTipo) {
        TextView chip = new TextView(this);
        chip.setText(tipo.toUpperCase());
        chip.setTextColor(getResources().getColor(android.R.color.white));
        chip.setPadding(28, 14, 28, 14);
        chip.setTextSize(14);
        chip.setAllCaps(true);
        chip.setTypeface(null, Typeface.BOLD);
        chip.setGravity(Gravity.CENTER);

        // Fondo redondeado con color según el tipo
        GradientDrawable fondo = new GradientDrawable();
        fondo.setCornerRadius(50f);
        fondo.setColor(getColorPorTipo(tipo));
        chip.setBackground(fondo);

        // Márgenes entre los chips
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(10, 10, 10, 10);
        chip.setLayoutParams(params);

        layout.addView(chip);
    }

    // ------------------- COLORES SEGÚN EL TIPO DE POKÉMON -------------------
    private int getColorPorTipo(String tipo) {
        switch (tipo.toLowerCase()) {
            case "grass": return 0xFF78C850;
            case "poison": return 0xFFA040A0;
            case "fire": return 0xFFF08030;
            case "water": return 0xFF6890F0;
            case "bug": return 0xFFA8B820;
            case "flying": return 0xFFA890F0;
            case "fighting": return 0xFFC03028;
            case "electric": return 0xFFF8D030;
            case "psychic": return 0xFFF85888;
            case "ice": return 0xFF98D8D8;
            case "ground": return 0xFFE0C068;
            case "rock": return 0xFFB8A038;
            case "ghost": return 0xFF705898;
            case "dragon": return 0xFF7038F8;
            case "dark": return 0xFF705848;
            case "steel": return 0xFFB8B8D0;
            case "fairy": return 0xFFEE99AC;
            case "normal": return 0xFFA8A878;
            default: return 0xFF888888; // Color gris por defecto
        }
    }
}

```
ActividadInicio.java
```java
package com.example.pokedex.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pokedex.R;

/**
 * Clase ActividadInicio
 * Esta es la primera pantalla que se muestra al abrir la app.
 * Contiene una imagen (como un logo o fondo) que, al tocarla,
 * lleva al usuario a la pantalla de los tipos de Pokémon.
 */
public class ActividadInicio extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si la barra de título está activa, la ocultamos para que la pantalla se vea limpia.
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Mostramos el diseño XML que pertenece a esta pantalla.
        setContentView(R.layout.activity_inicio);

        // Buscamos el elemento "ImageView" del XML usando su ID.
        // En este caso, es la imagen de fondo o logo de inicio.
        ImageView imgFondo = findViewById(R.id.imgFondoInicio);

        // Cuando el usuario toca la imagen, abrimos la pantalla de tipos de Pokémon.
        imgFondo.setOnClickListener(v -> {
            // Creamos un "Intent", que es como una orden para abrir otra pantalla.
            Intent intent = new Intent(ActividadInicio.this, ActividadTipos.class);

            // Iniciamos la nueva actividad (pantalla de tipos).
            startActivity(intent);
        });
    }
}

```
ActividadLista.java
```java
package com.example.pokedex.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pokedex.R;
import com.example.pokedex.controller.PokedexControlador;
import com.example.pokedex.model.PokedexApi;
import com.example.pokedex.model.Pokemon;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase ActividadLista
 * Muestra una lista (en cuadrícula) con todos los Pokémon de un tipo concreto.
 * Por ejemplo, si vienes desde "Tipo fuego", aquí verás todos los Pokémon de fuego.
 * También tiene un buscador en la parte superior para buscar cualquier Pokémon.
 */
public class ActividadLista extends AppCompatActivity {

    // ---------------- VARIABLES GLOBALES ----------------
    private GridView gridPokemons;                 // Cuadrícula donde se mostrarán los Pokémon
    private ArrayList<Pokemon> listaPokemons;      // Lista con los Pokémon que se van a mostrar
    private AdaptadorSimple adaptador;             // Adaptador para conectar los datos con la vista

    // Hilos para tareas de red (descargar datos sin bloquear la app)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // -----------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        // ---------------- ENLACE DE ELEMENTOS ----------------
        gridPokemons = findViewById(R.id.gridPokemons);
        listaPokemons = new ArrayList<>();

        // Botón "Atrás" — vuelve a la pantalla anterior
        ImageButton btnAtrasDetalle = findViewById(R.id.btnAtrasLista);
        btnAtrasDetalle.setOnClickListener(v -> finish());

        // Botón del logo — vuelve al inicio (pantalla principal)
        ImageButton btnLogoInicio = findViewById(R.id.btnLogoInicio);
        btnLogoInicio.setOnClickListener(v -> {
            Intent intent = new Intent(ActividadLista.this, ActividadInicio.class);
            // Evita abrir varias pantallas iguales al mismo tiempo
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // ---------------- CONFIGURAR BUSCADOR ----------------
        AutoCompleteTextView buscador = findViewById(R.id.buscarLista);

        // Cargamos los nombres de todos los Pokémon en un hilo aparte
        executor.execute(() -> {
            ArrayList<String> nombres = PokedexApi.obtenerNombresPokemon();

            // Volvemos al hilo principal para mostrar las sugerencias
            handler.post(() -> {
                ArrayAdapter<String> adaptadorBuscador = new ArrayAdapter<>(
                        ActividadLista.this,
                        android.R.layout.simple_dropdown_item_1line,
                        nombres
                );
                buscador.setAdapter(adaptadorBuscador);

                // Cuando el usuario elige un Pokémon de las sugerencias
                buscador.setOnItemClickListener((parent, view, position, id) -> {
                    String nombreSeleccionado = (String) parent.getItemAtPosition(position);
                    buscarIdYabrirDetalle(nombreSeleccionado);
                });

                // Cuando escribe un nombre y pulsa "Enter"
                buscador.setOnEditorActionListener((v, actionId, event) -> {
                    String texto = buscador.getText().toString().trim();
                    if (!texto.isEmpty()) {
                        buscarIdYabrirDetalle(texto);
                    }
                    return true;
                });
            });
        });

        // ---------------- RECIBIR EL TIPO DESDE LA OTRA PANTALLA ----------------
        String tipo = getIntent().getStringExtra("tipo");

        if (tipo != null) {
            // Si se recibió el tipo correctamente, cargamos los Pokémon
            cargarPokemonsPorTipo(tipo);
        } else {
            // Si no hay tipo, mostramos un aviso
            Toast.makeText(this, "No se recibió tipo de Pokémon", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------- METODO PARA CARGAR POKÉMON POR TIPO ----------------
    /**
     * Este metodo obtiene los Pokémon de un tipo específico usando el controlador.
     */
    private void cargarPokemonsPorTipo(String tipo) {
        executor.execute(() -> {
            // Llamamos al controlador para obtener la lista desde la API
            ArrayList<Pokemon> lista = PokedexControlador.obtenerPokemonsPorTipo(tipo);

            handler.post(() -> {
                if (lista.isEmpty()) {
                    Toast.makeText(ActividadLista.this, "No se encontraron Pokémon", Toast.LENGTH_SHORT).show();
                } else {
                    listaPokemons = lista;

                    // Creamos el adaptador para mostrar los Pokémon en la cuadrícula
                    adaptador = new AdaptadorSimple(ActividadLista.this, listaPokemons);
                    gridPokemons.setAdapter(adaptador);

                    // Cuando se hace clic en un Pokémon, abrimos su detalle
                    gridPokemons.setOnItemClickListener((parent, view, position, id) -> {
                        Pokemon seleccionado = listaPokemons.get(position);

                        // Creamos un Intent para ir a la pantalla de detalles
                        Intent intent = new Intent(ActividadLista.this, ActividadDetalle.class);
                        intent.putExtra("idPokemon", seleccionado.getId());
                        intent.putExtra("nombrePokemon", seleccionado.getNombre());
                        startActivity(intent);
                    });
                }
            });
        });
    }

    // ---------------- METODO PARA BUSCAR UN POKÉMON POR NOMBRE ----------------
    /**
     * Busca un Pokémon en la PokéAPI por su nombre y abre su pantalla de detalles.
     */
    private void buscarIdYabrirDetalle(String nombrePokemon) {
        executor.execute(() -> {
            try {
                // Creamos la URL del Pokémon
                URL url = new URL("https://pokeapi.co/api/v2/pokemon/" + nombrePokemon.toLowerCase());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                // Leemos la respuesta de la API
                InputStream input = con.getInputStream();
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = input.read()) != -1) {
                    sb.append((char) ch);
                }
                input.close();

                // Convertimos el texto a JSON para leer el ID del Pokémon
                JSONObject data = new JSONObject(sb.toString());
                int id = data.getInt("id");

                // Volvemos al hilo principal y abrimos la pantalla de detalles
                handler.post(() -> {
                    Intent intent = new Intent(ActividadLista.this, ActividadDetalle.class);
                    intent.putExtra("idPokemon", id);
                    intent.putExtra("nombrePokemon", nombrePokemon);
                    startActivity(intent);
                });

            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() ->
                        Toast.makeText(ActividadLista.this, "Pokémon no encontrado", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}

```
ActividadTipos.java
```java
package com.example.pokedex.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pokedex.R;
import com.example.pokedex.model.PokedexApi;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


 /**
 * Clase ActividadTipos
 * Esta pantalla muestra todos los tipos de Pokémon (fuego, agua, planta, etc.)
 * Cada tipo es un botón que, al tocarlo, abre la lista de Pokémon de ese tipo.
 * También incluye un buscador global para encontrar cualquier Pokémon por nombre.
 */

public class ActividadTipos extends AppCompatActivity {

    // ---------------- VARIABLES GLOBALES ----------------
    // Aquí declaramos los botones de cada tipo de Pokémon.
    Button btnPlanta, btnFuego, btnAgua, btnElectrico, btnHielo, btnRoca,
            btnTierra, btnLucha, btnBicho, btnFantasma, btnAcero, btnPsiquico,
            btnVolador, btnVeneno, btnSiniestro, btnHada, btnDragon, btnNormal;

    // Estos objetos permiten ejecutar tareas en segundo plano (para no bloquear la app)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // -----------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipos); // Cargamos el diseño XML

        // ---------------- ENLACE DE ELEMENTOS ----------------
        // Aquí buscamos los botones del XML por su ID.
        TextView btnPlanta = findViewById(R.id.btnPlanta);
        TextView btnFuego = findViewById(R.id.btnFuego);
        TextView btnAgua = findViewById(R.id.btnAgua);
        TextView btnElectrico = findViewById(R.id.btnElectrico);
        TextView btnHielo = findViewById(R.id.btnHielo);
        TextView btnRoca = findViewById(R.id.btnRoca);
        TextView btnTierra = findViewById(R.id.btnTierra);
        TextView btnLucha = findViewById(R.id.btnLucha);
        TextView btnBicho = findViewById(R.id.btnBicho);
        TextView btnFantasma = findViewById(R.id.btnFantasma);
        TextView btnAcero = findViewById(R.id.btnAcero);
        TextView btnPsiquico = findViewById(R.id.btnPsiquico);
        TextView btnVolador = findViewById(R.id.btnVolador);
        TextView btnVeneno = findViewById(R.id.btnVeneno);
        TextView btnSiniestro = findViewById(R.id.btnSiniestro);
        TextView btnHada = findViewById(R.id.btnHada);
        TextView btnDragon = findViewById(R.id.btnDragon);
        TextView btnNormal = findViewById(R.id.btnNormal);

        // ---------------- BOTONES DE NAVEGACIÓN ----------------

        // Botón "Atrás"
        ImageButton btnAtras = findViewById(R.id.btnAtrasTipos);
        btnAtras.setOnClickListener(v -> finish()); // Cierra esta pantalla y vuelve atrás

        // Botón del logo para volver al inicio
        ImageButton btnLogoInicio = findViewById(R.id.btnLogoInicio);
        btnLogoInicio.setOnClickListener(v -> {
            Intent intent = new Intent(ActividadTipos.this, ActividadInicio.class);
            // Estas banderas evitan abrir muchas pantallas iguales
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // ---------------- BUSCADOR GLOBAL --------------------------------------------------------------------------------
        AutoCompleteTextView buscador = findViewById(R.id.buscarLista);

        // Ejecutamos la tarea en un hilo secundario (para no bloquear la app)
        executor.execute(() -> {
            ArrayList<String> nombres = PokedexApi.obtenerNombresPokemon();

            // Volvemos al hilo principal para actualizar la interfaz
            handler.post(() -> {
                // Adaptador que muestra los nombres en las sugerencias
                ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                        ActividadTipos.this,
                        android.R.layout.simple_dropdown_item_1line,
                        nombres
                );
                buscador.setAdapter(adaptador);

                // Cuando el usuario selecciona un Pokémon de la lista
                buscador.setOnItemClickListener((parent, view, position, id) -> {
                    String nombreSeleccionado = (String) parent.getItemAtPosition(position);
                    buscarIdYabrirDetalle(nombreSeleccionado);
                });

                // Cuando el usuario escribe y pulsa "Enter"
                buscador.setOnEditorActionListener((v, actionId, event) -> {
                    String texto = buscador.getText().toString().trim();
                    if (!texto.isEmpty()) {
                        buscarIdYabrirDetalle(texto);
                    }
                    return true;
                });
            });
        });

        // ---------------- LISTENER PARA LOS BOTONES DE TIPO ----------------
        // Este objeto detecta qué botón se ha tocado y abre la lista correspondiente.
        View.OnClickListener listenerTipos = v -> {
            String tipoSeleccionado = "";

            // Comprobamos qué botón se ha pulsado y asignamos el tipo
            int id = v.getId();
            if (id == R.id.btnPlanta) tipoSeleccionado = "grass";
            else if (id == R.id.btnFuego) tipoSeleccionado = "fire";
            else if (id == R.id.btnAgua) tipoSeleccionado = "water";
            else if (id == R.id.btnElectrico) tipoSeleccionado = "electric";
            else if (id == R.id.btnHielo) tipoSeleccionado = "ice";
            else if (id == R.id.btnRoca) tipoSeleccionado = "rock";
            else if (id == R.id.btnTierra) tipoSeleccionado = "ground";
            else if (id == R.id.btnLucha) tipoSeleccionado = "fighting";
            else if (id == R.id.btnBicho) tipoSeleccionado = "bug";
            else if (id == R.id.btnFantasma) tipoSeleccionado = "ghost";
            else if (id == R.id.btnAcero) tipoSeleccionado = "steel";
            else if (id == R.id.btnPsiquico) tipoSeleccionado = "psychic";
            else if (id == R.id.btnVolador) tipoSeleccionado = "flying";
            else if (id == R.id.btnVeneno) tipoSeleccionado = "poison";
            else if (id == R.id.btnSiniestro) tipoSeleccionado = "dark";
            else if (id == R.id.btnHada) tipoSeleccionado = "fairy";
            else if (id == R.id.btnDragon) tipoSeleccionado = "dragon";
            else if (id == R.id.btnNormal) tipoSeleccionado = "normal";

            // Si se ha pulsado un tipo válido, abrimos la lista de Pokémon
            if (!tipoSeleccionado.isEmpty()) {
                Intent intent = new Intent(ActividadTipos.this, ActividadLista.class);
                intent.putExtra("tipo", tipoSeleccionado);
                startActivity(intent);
            }
        };

        // Asignamos el mismo listener a todos los botones
        btnPlanta.setOnClickListener(listenerTipos);
        btnFuego.setOnClickListener(listenerTipos);
        btnAgua.setOnClickListener(listenerTipos);
        btnElectrico.setOnClickListener(listenerTipos);
        btnHielo.setOnClickListener(listenerTipos);
        btnRoca.setOnClickListener(listenerTipos);
        btnTierra.setOnClickListener(listenerTipos);
        btnLucha.setOnClickListener(listenerTipos);
        btnBicho.setOnClickListener(listenerTipos);
        btnFantasma.setOnClickListener(listenerTipos);
        btnAcero.setOnClickListener(listenerTipos);
        btnPsiquico.setOnClickListener(listenerTipos);
        btnVolador.setOnClickListener(listenerTipos);
        btnVeneno.setOnClickListener(listenerTipos);
        btnSiniestro.setOnClickListener(listenerTipos);
        btnHada.setOnClickListener(listenerTipos);
        btnDragon.setOnClickListener(listenerTipos);
        btnNormal.setOnClickListener(listenerTipos);
    }

    // ---------------- METODO PARA BUSCAR POR NOMBRE ----------------

    //Busca un Pokémon por su nombre en la API y abre su pantalla de detalle.

    private void buscarIdYabrirDetalle(String nombrePokemon) {
        executor.execute(() -> {
            try {
                // Creamos la URL de la API (ej: https://pokeapi.co/api/v2/pokemon/pikachu)
                URL url = new URL("https://pokeapi.co/api/v2/pokemon/" + nombrePokemon.toLowerCase());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                // Leemos la respuesta
                InputStream input = con.getInputStream();
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = input.read()) != -1) {
                    sb.append((char) ch);
                }
                input.close();

                // Convertimos el texto a JSON
                JSONObject data = new JSONObject(sb.toString());

                // Obtenemos el ID del Pokémon
                int id = data.getInt("id");

                // Volvemos al hilo principal para abrir la nueva pantalla
                handler.post(() -> {
                    Intent intent = new Intent(ActividadTipos.this, ActividadDetalle.class);
                    intent.putExtra("idPokemon", id);
                    intent.putExtra("nombrePokemon", nombrePokemon);
                    startActivity(intent);
                });

            } catch (Exception e) {
                // Si ocurre un error (nombre mal escrito o sin internet)
                e.printStackTrace();
                handler.post(() -> Toast.makeText(
                        ActividadTipos.this,
                        "Pokémon no encontrado",
                        Toast.LENGTH_SHORT
                ).show());
            }
        });
    }
}
```
AdaptadorSimple.java
```java
package com.example.pokedex.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pokedex.R;
import com.example.pokedex.model.Pokemon;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase AdaptadorSimple
 * Esta clase conecta la lista de Pokémon con el GridView.
 * Cada Pokémon se muestra en una "tarjeta" (item_pokemon.xml)
 * que tiene una imagen, su nombre y su número de Pokédex.
 */
public class AdaptadorSimple extends BaseAdapter {

    private final Context contexto;                  // Contexto de la aplicación (la Activity)
    private final ArrayList<Pokemon> listaPokemons;  // Lista de Pokémon a mostrar

    // Ejecutores para cargar imágenes sin bloquear la app
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Constructor del adaptador
     * @param contexto El Activity donde se muestra el GridView.
     * @param listaPokemons Lista de objetos Pokémon que queremos mostrar.
     */
    public AdaptadorSimple(Context contexto, ArrayList<Pokemon> listaPokemons) {
        this.contexto = contexto;
        this.listaPokemons = listaPokemons;
    }

    // ---------------- MÉTODOS OBLIGATORIOS DE BaseAdapter ----------------

    /**
     * Devuelve cuántos elementos hay en la lista.
     */
    @Override
    public int getCount() {
        return listaPokemons.size();
    }

    /**
     * Devuelve el Pokémon en la posición indicada.
     */
    @Override
    public Object getItem(int position) {
        return listaPokemons.get(position);
    }

    /**
     * Devuelve el ID del Pokémon en la posición indicada.
     */
    @Override
    public long getItemId(int position) {
        return listaPokemons.get(position).getId();
    }

    /**
     * Metodo principal del adaptador.
     * Este metodo crea (o reutiliza) la vista de cada Pokémon en el GridView.
     * Aquí se colocan el nombre, el número y la imagen de cada Pokémon.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Si la vista no existe todavía, la "inflamos" desde el XML item_pokemon
        if (convertView == null) {
            convertView = LayoutInflater.from(contexto).inflate(R.layout.item_pokemon, parent, false);
        }

        // Enlazamos los elementos visuales del layout
        ImageView imagenPokemon = convertView.findViewById(R.id.imagenPokemon);
        TextView nombrePokemon = convertView.findViewById(R.id.nombrePokemon);
        TextView numeroPokemon = convertView.findViewById(R.id.numeroPokemon);

        // Obtenemos el Pokémon actual según la posición
        Pokemon p = listaPokemons.get(position);

        // Mostramos el nombre (primera letra en mayúscula)
        nombrePokemon.setText(capitalize(p.getNombre()));

        // Mostramos su número con formato de 3 cifras (#001, #025...)
        numeroPokemon.setText(String.format("#%03d", p.getId()));

        // ---------------- CARGAR IMAGEN ----------------
        // Usamos un hilo para cargar la imagen desde internet sin bloquear la interfaz
        executor.execute(() -> {
            try {
                // Descargamos la imagen desde la URL del Pokémon
                InputStream input = new URL(p.getImagenUrl()).openStream();
                final Bitmap bmp = BitmapFactory.decodeStream(input);

                // Volvemos al hilo principal para poner la imagen en el ImageView
                handler.post(() -> imagenPokemon.setImageBitmap(bmp));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Devolvemos la vista terminada (la tarjeta del Pokémon)
        return convertView;
    }

    /**
     * Metodo auxiliar que pone la primera letra en mayúscula.
     * Ejemplo: "pikachu" → "Pikachu"
     */
    private String capitalize(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        } else {
            String primeraLetra = texto.substring(0, 1).toUpperCase();
            String resto = texto.substring(1);
            return primeraLetra + resto;
        }
    }
}

```
# DRAWABLE (solo nombres de recursos)
barra_inferior.png
borde_caja.xml
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <solid android:color="#FFFFFF"/> <!-- Fondo blanco del campo -->
    <stroke android:width="2dp" android:color="#FFD6E7"/> <!-- Borde rosa -->
    <corners android:radius="24dp"/> <!-- Bordes redondeados -->
</shape>

```
btn_acero.png
btn_agua.png
btn_bicho.png
btn_dragon.png
btn_electrico.png
btn_fantasma.png
btn_fuego.png
btn_hada.png
btn_hielo.png
btn_lucha.png
btn_normal.png
btn_planta.png
btn_psiquico.png
btn_roca.png
btn_siniestro.png
btn_tierra.png
btn_veneno.png
btn_volador.png
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
chip_debilidad.xml
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#F08030"/> <!-- naranja tipo fuego -->
    <corners android:radius="50dp"/>
</shape>
```
chip_tipo.xml
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#78C850"/> <!-- verde tipo planta -->
    <corners android:radius="50dp"/>
</shape>
```
fondo_detalle_degradado.xml
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <gradient
        android:startColor="#F5B5CD"
        android:centerColor="#C7A2F0"
        android:endColor="#909EF2"
        android:angle="270" />
    <corners android:radius="24dp" />
</shape>
```
fondo_inicio.png
fondo_item_pokemon.xml
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <!-- Color de fondo -->
    <solid android:color="#80909EF2" />

    <!-- Bordes redondeados -->
    <corners android:radius="40dp" />

    <!-- Opcional: sombra ligera -->
    <padding
        android:left="4dp"
        android:top="4dp"
        android:right="4dp"
        android:bottom="4dp" />
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
fondo_pokedex.png
fondo_pokedex_dos.png
fondo_pokedex_uno.png
fondo_pokemons.jpg
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
ic_launcher_background.xml
ic_launcher_foreground.xml
logo_pokedex.png
rounded_search_background.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#FFFFFFFF" />
    <corners android:radius="50dp" />
    <padding
        android:left="10dp"
        android:top="5dp"
        android:right="10dp"
        android:bottom="5dp" />
</shape>

```
# LAYOUT
activity_detalle.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F5B5CD"
    android:fitsSystemWindows="true">

    <!-- 🔹 Barra superior igual que en lista -->
    <LinearLayout
        android:id="@+id/barraSuperior"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:padding="8dp"
        android:layout_marginTop="15dp">

        <ImageButton
            android:id="@+id/btnAtrasDetalle"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:background="@drawable/btn_volver"
            android:contentDescription="Volver"
            android:layout_margin="8dp"
            android:scaleType="centerInside"
            android:padding="8dp" />

        <AutoCompleteTextView
            android:id="@+id/buscarLista"
            android:layout_width="0dp"
            android:layout_height="40dp"
            android:layout_weight="1"
            android:hint="Buscar Pokémon..."
            android:background="@drawable/borde_caja"
            android:padding="10dp"
            android:layout_marginLeft="8dp"
            android:textColorHint="#999"
            android:completionThreshold="1" />
    </LinearLayout>

    <!-- 🔹 Cuadro degradado principal -->
    <LinearLayout
        android:id="@+id/contenedorDetalles"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:gravity="center_horizontal"
        android:background="@drawable/fondo_detalle_degradado"
        android:layout_below="@id/barraSuperior"
        android:layout_above="@id/logoFondo"
        android:layout_marginStart="20dp"
        android:layout_marginEnd="20dp"
        android:clipToOutline="true"
        android:padding="20dp">

        <ImageView
            android:id="@+id/imgPokemon"
            android:layout_width="250dp"
            android:layout_height="250dp"
            android:layout_marginTop="30dp"
            android:contentDescription="Imagen Pokémon"
            android:scaleType="fitCenter" />

        <TextView
            android:id="@+id/txtNombre"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:gravity="center"
            android:text="001 BULBASAUR"
            android:textColor="#FFFFFF"
            android:textSize="22sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/txtDetalles"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="10dp"
            android:gravity="center"
            android:text="Altura: 0.7 m\nPeso: 6.9 kg\nHabilidad: overgrow"
            android:textColor="#FFFFFF"
            android:textSize="16sp" />

        <TextView
            android:id="@+id/txtTipoTitulo"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="Tipo"
            android:textColor="#FFFFFF"
            android:textStyle="bold" />

        <LinearLayout
            android:id="@+id/layoutTipos"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:orientation="horizontal" />

        <TextView
            android:id="@+id/txtDebilidadTitulo"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="Debilidad"
            android:textColor="#FFFFFF"
            android:textStyle="bold" />

        <!-- 🔹 AQUÍ el FlexboxLayout, ya sin xmlns:app dentro -->
        <com.google.android.flexbox.FlexboxLayout
            android:id="@+id/layoutDebilidades"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:layout_marginBottom="40dp"
            android:padding="8dp"
            app:flexWrap="wrap"
            app:justifyContent="center"
            app:alignItems="center" />
    </LinearLayout>

    <LinearLayout
        android:id="@+id/logoFondo"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:layout_alignParentBottom="true"
        android:gravity="center"
        android:clickable="true"
        android:focusable="true">

        <ImageButton
            android:id="@+id/btnLogoInicio"
            android:layout_width="120dp"
            android:layout_height="55dp"
            android:src="@drawable/logo_pokedex"
            android:scaleType="fitCenter"
            android:adjustViewBounds="true"
            android:background="@android:color/transparent"
            android:contentDescription="Volver al inicio" />
    </LinearLayout>
</RelativeLayout>

```
activity_inicio.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/fondo_pokedex_dos"
    android:gravity="center">

    <!-- Imagen de fondo interactiva -->
    <ImageView
        android:id="@+id/imgFondoInicio"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop"
        android:alpha="0.9"
        android:contentDescription="Pantalla de inicio" />
</RelativeLayout>

```
activity_lista.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/fondo_pokedex"
    android:fitsSystemWindows="true">

    <!-- Barra superior -->
    <LinearLayout
        android:id="@+id/barraSuperior"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:padding="8dp"
        android:layout_marginTop="15dp">

        <ImageButton
            android:id="@+id/btnAtrasLista"
            android:layout_width="50dp"
            android:layout_height="50dp"
            android:background="@drawable/btn_volver"
            android:contentDescription="Volver"
            android:layout_margin="8dp"
            android:scaleType="centerInside"
            android:padding="10dp" />

        <AutoCompleteTextView
            android:id="@+id/buscarLista"
            android:layout_width="0dp"
            android:layout_height="40dp"
            android:layout_weight="1"
            android:hint="Buscar Pokémon..."
            android:background="@drawable/borde_caja"
            android:padding="10dp"
            android:layout_marginLeft="8dp"
            android:textColorHint="#999"
            android:completionThreshold="1" />
    </LinearLayout>

    <!-- Grid -->
    <GridView
        android:id="@+id/gridPokemons"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:numColumns="2"
        android:verticalSpacing="12dp"
        android:horizontalSpacing="12dp"
        android:padding="16dp"
        android:layout_below="@id/barraSuperior"
        android:layout_above="@id/logoFondo"
        android:clipToPadding="false"
        android:stretchMode="columnWidth"
        android:gravity="center"/>

    <!-- Fondo recto del logo (estrecho) -->
    <LinearLayout
        android:id="@+id/logoFondo"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:layout_alignParentBottom="true"
        android:elevation="4dp"
        android:gravity="center">

        <ImageButton
            android:id="@+id/btnLogoInicio"
            android:layout_width="120dp"
            android:layout_height="55dp"
            android:src="@drawable/logo_pokedex"
            android:scaleType="fitCenter"
            android:adjustViewBounds="true"
            android:background="@android:color/transparent"
            android:contentDescription="Volver al inicio"/>
    </LinearLayout>
</RelativeLayout>


```
activity_tipos.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/fondo_pokedex"
    android:fitsSystemWindows="true"
    android:paddingTop="10dp">

        <!-- 🔹 Barra superior con botón atrás y buscador -->
        <LinearLayout
            android:id="@+id/barraSuperior"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:padding="6dp"
            android:layout_marginTop="10dp">

                <ImageButton
                    android:id="@+id/btnAtrasTipos"
                    android:layout_width="50dp"
                    android:layout_height="50dp"
                    android:background="@drawable/btn_volver"
                    android:contentDescription="Volver"
                    android:layout_margin="8dp"
                    android:scaleType="centerInside"
                    android:padding="10dp" />

                <AutoCompleteTextView
                    android:id="@+id/buscarLista"
                    android:layout_width="0dp"
                    android:layout_height="40dp"
                    android:layout_weight="1"
                    android:hint="Buscar Pokémon..."
                    android:background="@drawable/borde_caja"
                    android:padding="10dp"
                    android:layout_marginLeft="8dp"
                    android:textColorHint="#999"
                    android:completionThreshold="1" />
        </LinearLayout>

        <!-- 🔹 Contenedor de los botones (sin scroll) -->
        <GridLayout
            android:id="@+id/gridTipos"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_below="@id/barraSuperior"
            android:layout_above="@id/btnLogoInicio"
            android:alignmentMode="alignMargins"
            android:columnCount="2"
            android:rowCount="9"
            android:useDefaultMargins="true"
            android:layout_marginTop="6dp"
            android:paddingHorizontal="12dp"
            android:gravity="center">
                <TextView
                    android:id="@+id/btnPlanta"
                    android:text="PLANTA"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_planta"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnFuego"
                    android:text="FUEGO"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="#000000"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_fuego"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnAgua"
                    android:text="AGUA"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_agua"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnElectrico"
                    android:text="ELÉCTRICO"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_electrico"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnHielo"
                    android:text="HIELO"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_hielo"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnRoca"
                    android:text="ROCA"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_roca"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnTierra"
                    android:text="TIERRA"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_tierra"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnLucha"
                    android:text="LUCHA"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_lucha"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnBicho"
                    android:text="BICHO"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_bicho"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnFantasma"
                    android:text="FANTASMA"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_fantasma"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnAcero"
                    android:text="ACERO"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_acero"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnPsiquico"
                    android:text="PSÍQUICO"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_psiquico"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnVolador"
                    android:text="VOLADOR"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_volador"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnVeneno"
                    android:text="VENENO"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_veneno"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnSiniestro"
                    android:text="SINIESTRO"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_siniestro"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnHada"
                    android:text="HADA"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_hada"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnDragon"
                    android:text="DRAGÓN"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_dragon"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />

                <TextView
                    android:id="@+id/btnNormal"
                    android:text="NORMAL"
                    android:layout_width="0dp"
                    android:layout_height="60dp"
                    android:layout_columnWeight="1"
                    android:layout_margin="6dp"
                    android:textColor="@color/black"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:background="@drawable/btn_normal"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?attr/selectableItemBackground"
                    />
        </GridLayout>

        <!-- 🔹 Logotipo Pokédex (más pequeño, para liberar espacio) -->
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
```

# item_pokemon.xml

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="8dp"
    android:layout_margin="6dp"
    android:elevation="4dp"
    android:clipToOutline="true"
    android:background="@drawable/fondo_item_pokemon"
    android:foreground="?android:attr/selectableItemBackground">

    <ImageView
        android:id="@+id/imagenPokemon"
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:scaleType="fitCenter"
        android:adjustViewBounds="true" />

    <TextView
        android:id="@+id/numeroPokemon"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="#001"
        android:textColor="#E6E6FA"
        android:textSize="16sp"
        android:layout_marginTop="2dp"
        android:gravity="center"/>

    <TextView
        android:id="@+id/nombrePokemon"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Bulbasaur"
        android:textColor="#FFFFFF"
        android:textStyle="bold"
        android:textSize="18sp"
        android:gravity="center"
        android:layout_marginTop="4dp" />
</LinearLayout>


```
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Permiso para usar Internet -->
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Pokedex"
        tools:targetApi="31">

        <!-- 🟢 Pantalla principal (solo una con MAIN + LAUNCHER) -->
        <activity
            android:name=".view.ActividadInicio"
            android:exported="true"
            android:theme="@style/Theme.Pokedex.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Otras pantallas de la app -->
        <activity android:name=".view.ActividadTipos" android:exported="true" />
        <activity android:name=".view.ActividadLista" android:exported="true" />
        <activity android:name=".view.ActividadDetalle" android:exported="true" />

    </application>
</manifest>

```
