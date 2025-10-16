package com.example.intentopokedex3.view;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.intentopokedex3.R;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ActivityLista extends AppCompatActivity {

    GridLayout gridPokemons;
    ImageButton btnVolverLista, btnLogoInicioLista;
    EditText buscarPokemon;
    String tipoSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        gridPokemons = findViewById(R.id.gridPokemons);
        btnVolverLista = findViewById(R.id.btnVolverLista);
        btnLogoInicioLista = findViewById(R.id.btnLogoInicioLista);
        buscarPokemon = findViewById(R.id.buscarPokemon);

        // Recibimos el tipo desde la activity anterior
        tipoSeleccionado = getIntent().getStringExtra("tipo");

        // 🔙 Botón volver
        btnVolverLista.setOnClickListener(v -> finish());

        // 🏠 Logo inferior
        btnLogoInicioLista.setOnClickListener(v -> {
            Intent i = new Intent(ActivityLista.this, ActivityInicio.class);
            startActivity(i);
        });

        // Llamada a la API
        new ObtenerPokemonsPorTipo().execute("https://pokeapi.co/api/v2/type/" + tipoSeleccionado);
    }

    /**
     * 🔹 AsyncTask que obtiene los Pokémon del tipo seleccionado
     */
    private class ObtenerPokemonsPorTipo extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... urls) {
            StringBuilder resultado = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String linea;
                while ((linea = reader.readLine()) != null) {
                    resultado.append(linea);
                }
                reader.close();

                JSONObject json = new JSONObject(resultado.toString());
                return json.getJSONArray("pokemon"); // lista de pokémon

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray listaPokemons) {
            if (listaPokemons == null) return;

            try {
                // Limitar para que no sature el diseño (máx 30)
                int cantidad = Math.min(listaPokemons.length(), 30);

                for (int i = 0; i < cantidad; i++) {
                    JSONObject pokeObj = listaPokemons.getJSONObject(i).getJSONObject("pokemon");
                    String nombre = pokeObj.getString("name");
                    String url = pokeObj.getString("url");

                    // obtener número (viene al final de la URL)
                    String[] partes = url.split("/");
                    String numero = partes[partes.length - 1];

                    // construir imagen oficial
                    String imagenUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + numero + ".png";

                    // Añadir a la cuadrícula
                    addPokemon("#" + numero, capitalize(nombre), imagenUrl);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 🔹 Crea una tarjeta visual para cada Pokémon
     */
    private void addPokemon(String numero, String nombre, String imagenUrl) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(8, 8, 8, 8);
        layout.setBackgroundResource(R.drawable.fondo_caja_detalle);
        layout.setClickable(true);
        layout.setFocusable(true);

        // 🔹 Imagen
        ImageView img = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(220, 220);
        img.setLayoutParams(imgParams);
        img.setAdjustViewBounds(true);
        Glide.with(this).load(imagenUrl).into(img);

        // 🔹 Texto
        TextView txt = new TextView(this);
        txt.setText(numero + " " + nombre);
        txt.setTextColor(Color.WHITE);
        txt.setGravity(Gravity.CENTER);
        txt.setTextSize(13);
        txt.setPadding(0, 10, 0, 10);

        // 🔹 Añadir vista al layout
        layout.addView(img);
        layout.addView(txt);

        // 🔹 Al tocar el Pokémon → abrir ActivityDetalle
        layout.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityLista.this, ActivityDetalle.class);
            intent.putExtra("nombrePokemon", nombre);
            intent.putExtra("numeroPokemon", numero.replace("#", ""));
            intent.putExtra("imagenUrl", imagenUrl);
            startActivity(intent);
        });

        // 🔹 Ajustes del GridLayout
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(16, 16, 16, 16);
        layout.setLayoutParams(params);

        gridPokemons.addView(layout);
    }

    /**
     * 🔹 Convierte la primera letra a mayúscula
     */
    private String capitalize(String texto) {
        if (texto == null || texto.length() == 0) return "";
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
}
