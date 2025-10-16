package com.example.intentopokedex3.view;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.example.intentopokedex3.R;
import com.example.intentopokedex3.controller.PokedexApi;
import com.example.intentopokedex3.model.Pokemon;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

        // 🔹 Recuperar datos del intent
        Intent intent = getIntent();
        nombrePokemon = intent.getStringExtra("nombrePokemon");
        numeroPokemon = intent.getStringExtra("numeroPokemon");
        imagenUrl = intent.getStringExtra("imagenUrl");

        if (nombrePokemon != null) {
            txtNombrePokemon.setText("#" + numeroPokemon + " " + nombrePokemon.toUpperCase());
            Glide.with(this).load(imagenUrl).into(imgPokemonDetalle);
            cargarDetallesPokemon(nombrePokemon.toLowerCase());
        } else {
            txtNombrePokemon.setText("Error: Pokémon no encontrado");
        }

        // 🔙 Botón volver
        btnVolverDetalle.setOnClickListener(v -> finish());

        // 🏠 Logo → volver al inicio
        logoInicioDetalle.setOnClickListener(v -> {
            Intent i = new Intent(ActivityDetalle.this, ActivityInicio.class);
            startActivity(i);
        });
    }

    /**
     * Llama a la API para obtener los detalles del Pokémon
     */
    private void cargarDetallesPokemon(String nombre) {
        PokedexApi.getDetallesPokemon(this, nombre,
                new Response.Listener<Pokemon>() {
                    @Override
                    public void onResponse(Pokemon pokemon) {
                        mostrarDetalles(pokemon);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        txtNombrePokemon.setText("Error al cargar datos");
                    }
                });
    }

    /**
     * Rellena la interfaz con los datos del Pokémon
     */
    private void mostrarDetalles(Pokemon p) {
        txtNumeroPokemon.setText("#" + p.getNumero());
        txtAltura.setText("Altura: " + p.getAltura() + " m");
        txtPeso.setText("Peso: " + p.getPeso() + " kg");
        txtSexo.setText("Sexo: ♂ ♀");
        txtCategoria.setText("Categoría: " + (p.getCategoria() != null ? p.getCategoria() : "-"));
        txtHabilidad.setText("Habilidad: " + (p.getHabilidad() != null ? p.getHabilidad() : "-"));

        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... urls) {
                return PokedexApi.getBitmapFromURL(urls[0]);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) imgPokemonDetalle.setImageBitmap(bitmap);
            }
        }.execute(p.getImagenUrl());

        mostrarTipos(p.getTipos());
        cargarDebilidades(p.getTipos());
    }

    private void mostrarTipos(ArrayList<String> tipos) {
        contenedorTipos.removeAllViews();
        for (String tipo : tipos) {
            TextView chip = crearChip(tipo);
            contenedorTipos.addView(chip);
        }
    }

    private void cargarDebilidades(ArrayList<String> tipos) {
        contenedorDebilidades.removeAllViews();
        ArrayList<String> debilidadesTotales = new ArrayList<>();

        for (String tipo : tipos) {
            String url = "https://pokeapi.co/api/v2/type/" + tipo;
            com.android.volley.RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(this);

            com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                    com.android.volley.Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray damageArray = response.getJSONObject("damage_relations")
                                    .getJSONArray("double_damage_from");
                            for (int i = 0; i < damageArray.length(); i++) {
                                String debilidad = damageArray.getJSONObject(i).getString("name");
                                if (!debilidadesTotales.contains(debilidad)) {
                                    debilidadesTotales.add(debilidad);
                                }
                            }

                            contenedorDebilidades.removeAllViews();
                            for (String d : debilidadesTotales) {
                                TextView chipDeb = crearChip(d);
                                contenedorDebilidades.addView(chipDeb);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {}
            );
            queue.add(request);
        }
    }

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

    private GradientDrawable crearFondoTipo(String tipo) {
        GradientDrawable fondo = new GradientDrawable();
        fondo.setCornerRadius(50);

        Map<String, String> colores = new HashMap<>();
        colores.put("planta", "#78C850");
        colores.put("fuego", "#F08030");
        colores.put("agua", "#6890F0");
        colores.put("bicho", "#A8B820");
        colores.put("normal", "#A8A878");
        colores.put("electrico", "#F8D030");
        colores.put("tierra", "#E0C068");
        colores.put("hada", "#EE99AC");
        colores.put("lucha", "#C03028");
        colores.put("psiquico", "#F85888");
        colores.put("roca", "#B8A038");
        colores.put("fantasma", "#705898");
        colores.put("hielo", "#98D8D8");
        colores.put("dragon", "#7038F8");
        colores.put("veneno", "#A040A0");
        colores.put("acero", "#B8B8D0");
        colores.put("siniestro", "#705848");
        colores.put("volador", "#A890F0");

        String color = colores.get(tipo.toLowerCase());
        if (color == null) color = "#AAAAAA";
        fondo.setColor(Color.parseColor(color));

        return fondo;
    }
}
