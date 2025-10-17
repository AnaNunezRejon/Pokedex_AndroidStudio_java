package com.example.intentopokedex3.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

//BUSCADOR
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;

public class ActivityDetalle extends AppCompatActivity {

    ImageButton btnVolverDetalle, logoInicioDetalle;
    ImageView imgPokemonDetalle;
    EditText inputBuscarDetalle;
    TextView txtNombrePokemon, txtNumeroPokemon, txtAltura, txtPeso, txtSexo, txtCategoria, txtHabilidad;
    LinearLayout contenedorTipos;
    FlexboxLayout contenedorDebilidades;

    String nombrePokemon;
    String numeroPokemon;
    String imagenUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        // 🔹 Referencias UI
        btnVolverDetalle = findViewById(R.id.btnVolverDetalle);
        logoInicioDetalle = findViewById(R.id.logoInicioDetalle);
        imgPokemonDetalle = findViewById(R.id.imgPokemonDetalle);
        inputBuscarDetalle = findViewById(R.id.inputBuscarDetalle);
        txtNombrePokemon = findViewById(R.id.txtNombrePokemon);
        txtNumeroPokemon = findViewById(R.id.txtNumeroPokemon);
        txtAltura = findViewById(R.id.txtAltura);
        txtPeso = findViewById(R.id.txtPeso);
        txtSexo = findViewById(R.id.txtSexo);
        txtCategoria = findViewById(R.id.txtCategoria);
        txtHabilidad = findViewById(R.id.txtHabilidad);
        contenedorTipos = findViewById(R.id.contenedorTipos);
        contenedorDebilidades = findViewById(R.id.contenedorDebilidades);

        // 🔙 Botón volver
        btnVolverDetalle.setOnClickListener(v -> finish());

        // 🏠 Logo inferior → volver al inicio
        logoInicioDetalle.setOnClickListener(v -> {
            Intent i = new Intent(ActivityDetalle.this, ActivityInicio.class);
            startActivity(i);
        });

        // 🔍 BUSCADOR
        ListView listaSugerencias = findViewById(R.id.listaSugerencias);
        ArrayList<String> nombresPokemon = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombresPokemon);
        listaSugerencias.setAdapter(adapter);

        // Escucha el texto del buscador
        inputBuscarDetalle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

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

            @Override public void afterTextChanged(Editable s) {}
        });

        // Cuando el usuario pulsa sobre un Pokémon de la lista
        listaSugerencias.setOnItemClickListener((parent, view, position, id) -> {
            String textoSeleccionado = nombresPokemon.get(position);
            // Ejemplo: "#1 BULBASAUR"
            String[] partes = textoSeleccionado.split(" ");
            String numero = partes[0].replace("#", "");
            String nombre = partes[1].toLowerCase();

            Intent i = new Intent(ActivityDetalle.this, ActivityDetalle.class);
            i.putExtra("nombrePokemon", nombre);
            i.putExtra("numeroPokemon", numero);
            i.putExtra("imagenUrl", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + numero + ".png");
            startActivity(i);

            listaSugerencias.setVisibility(View.GONE);
        });

        // 🔹 Recuperar datos del Intent
        Intent intent = getIntent();
        nombrePokemon = intent.getStringExtra("nombrePokemon");
        numeroPokemon = intent.getStringExtra("numeroPokemon");
        imagenUrl = intent.getStringExtra("imagenUrl");

        if (nombrePokemon != null) {
            txtNombrePokemon.setText(nombrePokemon.toUpperCase());
            txtNumeroPokemon.setText("#" + numeroPokemon);

            cargarDetallesPokemon(nombrePokemon.toLowerCase());

            // Descargar imagen
            PokedexApi.descargarImagen(imagenUrl, bitmap -> {
                if (bitmap != null) imgPokemonDetalle.setImageBitmap(bitmap);
            });
        } else {
            txtNombrePokemon.setText("Pokémon no encontrado");
        }
    }



    /**
     * 📦 Cargar todos los detalles del Pokémon
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
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
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

                // Tipos
                JSONArray tiposArray = json.getJSONArray("types");
                ArrayList<String> tipos = new ArrayList<>();
                for (int i = 0; i < tiposArray.length(); i++) {
                    tipos.add(tiposArray.getJSONObject(i)
                            .getJSONObject("type")
                            .getString("name"));
                }

                Pokemon p = new Pokemon();
                p.setNombre(nombrePokemon);
                p.setNumero(Integer.parseInt(numeroPokemon));
                p.setAltura(altura);
                p.setPeso(peso);
                p.setHabilidad(habilidad);
                p.setTipos(tipos);

                handler.post(() -> {
                    mostrarDetalles(p);
                    cargarDebilidades(p.getTipos());
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 🧾 Rellena la interfaz con los datos del Pokémon
     */
    private void mostrarDetalles(Pokemon p) {
        txtAltura.setText("Altura: " + p.getAltura() + " m");
        txtPeso.setText("Peso: " + p.getPeso() + " kg");
        txtSexo.setText("Sexo: ♂ ♀");
        txtCategoria.setText("Categoría: Semilla");
        txtHabilidad.setText("Habilidad: " + p.getHabilidad());
        mostrarTipos(p.getTipos());
    }

    /**
     * 🟩 Mostrar los tipos del Pokémon con color
     */
    private void mostrarTipos(ArrayList<String> tipos) {
        contenedorTipos.removeAllViews();
        for (String tipo : tipos) {
            TextView chip = crearChip(tipo);
            contenedorTipos.addView(chip);
        }
    }

    /**
     * ⚡ Cargar debilidades de los tipos
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
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonBuilder.append(line);
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

            handler.post(() -> {
                for (String d : debilidadesTotales) {
                    TextView chipDeb = crearChip(d);
                    contenedorDebilidades.addView(chipDeb);
                }
            });
        });
    }

    /**
     * 🎨 Crea un chip colorido según el tipo
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

    /**
     * 🎨 Devuelve color de fondo según tipo
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
