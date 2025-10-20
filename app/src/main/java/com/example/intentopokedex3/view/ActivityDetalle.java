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
