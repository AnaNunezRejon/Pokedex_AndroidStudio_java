package com.example.intentopokedex3.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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

/**
 * Pantalla de detalle de un Pokemon concreto.
 * Muestra su imagen, datos basicos, tipos y debilidades.
 *
 * FLUJO DETALLE:
 * onCreate()
 *    ↳ carga el layout activity_detalle.xml
 *    ↳ obtiene referencias (imagen, textos, botones, buscador, contenedores)
 *    ↳ configura navegacion:
 *         • btnVolverDetalle → finish()
 *         • logoInicioDetalle → abre ActivityInicio
 *    ↳ configura el buscador (igual que en Tipos y Lista)
 *         • PokedexApi.buscarPorNombre()
 *         • muestra resultados y abre ActivityDetalle al pulsar
 *    ↳ recupera datos del Intent:
 *         nombrePokemon, numeroPokemon, imagenUrl
 *    ↳ muestra el nombre y numero
 *    ↳ PokedexApi.descargarImagen() → muestra sprite
 *    ↳ cargaDetallesPokemon(nombre)
 *
 * cargarDetallesPokemon(nombre)
 *    ↳ crea hilo con ExecutorService
 *    ↳ hace peticion a URL "https://pokeapi.co/api/v2/pokemon/{nombre}"
 *    ↳ obtiene JSON con:
 *         • altura
 *         • peso
 *         • habilidad
 *         • tipos
 *    ↳ crea objeto Pokemon con esos datos
 *    ↳ en el hilo principal:
 *         mostrarDetalles(p)
 *         cargarDebilidades(p.getTipos())
 *
 * mostrarDetalles(p)
 *    ↳ muestra altura, peso, habilidad, etc. en TextViews
 *    ↳ llama a mostrarTipos(p.getTipos())
 *
 * mostrarTipos(tipos)
 *    ↳ limpia el contenedor de tipos
 *    ↳ por cada tipo crea un chip de color con crearChip()
 *
 * cargarDebilidades(tipos)
 *    ↳ recorre cada tipo
 *    ↳ consulta la API: "https://pokeapi.co/api/v2/type/{tipo}"
 *    ↳ obtiene el JSON con "double_damage_from"
 *    ↳ añade cada tipo de debilidad a una lista
 *    ↳ en el hilo principal:
 *         crea chips con crearChip() y los agrega al contenedor
 *
 * crearChip(texto)
 *    ↳ crea un TextView con texto y color de fondo
 *    ↳ llama a crearFondoTipo(tipo) para obtener el color
 *
 * crearFondoTipo(tipo)
 *    ↳ crea un GradientDrawable con color segun el tipo (mapa de colores)
 *    ↳ devuelve el fondo para el chip
 *
 * 🔹 Objetivo:
 *    Muestra toda la informacion detallada del Pokemon:
 *    imagen, altura, peso, tipos, habilidades y debilidades.
 */
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
        textoSexo = findViewById(R.id.txtSexo);
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
            PokedexApi.descargarImagen(urlImagen, bitmap -> {
                if (bitmap != null) imagenPokemon.setImageBitmap(bitmap);
            });
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

        // Detecta texto en el buscador
        campoBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String texto = s.toString().trim().toLowerCase();

                if (texto.length() >= 1) {
                    // Llamada a la API para buscar por nombre
                    PokedexApi.buscarPorNombre(texto, lista -> {
                        runOnUiThread(() -> {
                            nombresPokemon.clear();
                            for (Pokemon p : lista) {
                                nombresPokemon.add("#" + p.getNumero() + " " + p.getNombre().toUpperCase());
                            }
                            adaptador.notifyDataSetChanged();
                            listaSugerencias.setVisibility(android.view.View.VISIBLE);
                        });
                    });
                } else {
                    listaSugerencias.setVisibility(android.view.View.GONE);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        // Al pulsar una sugerencia se abre el detalle del Pokemon
        listaSugerencias.setOnItemClickListener((parent, view, position, id) -> {
            String textoSeleccionado = nombresPokemon.get(position);
            String[] partes = textoSeleccionado.split(" ");
            String numero = partes[0].replace("#", "");
            String nombre = partes[1].toLowerCase();

            Intent i = new Intent(ActivityDetalle.this, ActivityDetalle.class);
            i.putExtra("nombrePokemon", nombre);
            i.putExtra("numeroPokemon", numero);
            i.putExtra("imagenUrl", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + numero + ".png");
            startActivity(i);

            listaSugerencias.setVisibility(android.view.View.GONE);
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
                URL url = new URL("https://pokeapi.co/api/v2/pokemon/" + nombre);
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

                double altura = json.getDouble("height") / 10.0;
                double peso = json.getDouble("weight") / 10.0;

                JSONArray habilidades = json.getJSONArray("abilities");
                String habilidad = habilidades.getJSONObject(0)
                        .getJSONObject("ability")
                        .getString("name");

                // Tipos del Pokemon
                JSONArray tiposArray = json.getJSONArray("types");
                ArrayList<String> tipos = new ArrayList<>();
                for (int i = 0; i < tiposArray.length(); i++) {
                    tipos.add(tiposArray.getJSONObject(i)
                            .getJSONObject("type")
                            .getString("name"));
                }

                // Crear objeto Pokemon con los datos
                Pokemon p = new Pokemon();
                p.setNombre(nombrePokemon);
                p.setNumero(Integer.parseInt(numeroPokemon));
                p.setAltura(altura);
                p.setPeso(peso);
                p.setHabilidad(habilidad);
                p.setTipos(tipos);

                // Volver al hilo principal para actualizar la UI
                handler.post(() -> {
                    mostrarDetalles(p);
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
    private void mostrarDetalles(Pokemon p) {
        textoAltura.setText("Altura: " + p.getAltura() + " m");
        textoPeso.setText("Peso: " + p.getPeso() + " kg");
        textoSexo.setText("Sexo: ♂ ♀");
        textoCategoria.setText("Categoria: Semilla");
        textoHabilidad.setText("Habilidad: " + p.getHabilidad());
        mostrarTipos(p.getTipos());
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
