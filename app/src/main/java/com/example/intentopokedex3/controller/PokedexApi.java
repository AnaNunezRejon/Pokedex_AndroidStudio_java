package com.example.intentopokedex3.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.intentopokedex3.model.Pokemon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PokedexApi {

    private static final String BASE_URL = "https://pokeapi.co/api/v2/";

    /**
     * Método que devuelve una lista de Pokémon de un tipo concreto.
     */
    public static void getPokemonsPorTipo(Context context, String tipo, Response.Listener<ArrayList<Pokemon>> successListener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "type/" + tipo.toLowerCase();

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    ArrayList<Pokemon> listaPokemons = new ArrayList<>();
                    try {
                        JSONObject json = new JSONObject(response);
                        JSONArray pokemonsArray = json.getJSONArray("pokemon");

                        // Limitamos a 30 para evitar sobrecargar
                        int limite = Math.min(30, pokemonsArray.length());
                        for (int i = 0; i < limite; i++) {
                            JSONObject pokeObj = pokemonsArray.getJSONObject(i).getJSONObject("pokemon");
                            String nombre = pokeObj.getString("name");
                            String urlPokemon = pokeObj.getString("url");
                            listaPokemons.add(new Pokemon(nombre, urlPokemon));
                        }

                        successListener.onResponse(listaPokemons);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                errorListener);

        queue.add(request);
    }

    /**
     * Método que obtiene los detalles de un Pokémon concreto.
     */
    public static void getDetallesPokemon(Context context, String nombrePokemon, Response.Listener<Pokemon> successListener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "pokemon/" + nombrePokemon.toLowerCase();

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Pokemon p = new Pokemon();
                        p.setNombre(response.getString("name"));
                        p.setNumero(response.getInt("id"));
                        p.setImagenUrl(response.getJSONObject("sprites").getString("front_default"));
                        p.setAltura(response.getDouble("height") / 10.0); // decímetros -> metros
                        p.setPeso(response.getDouble("weight") / 10.0);   // hectogramos -> kg

                        // Habilidad principal
                        JSONArray habilidades = response.getJSONArray("abilities");
                        if (habilidades.length() > 0) {
                            p.setHabilidad(habilidades.getJSONObject(0).getJSONObject("ability").getString("name"));
                        }

                        // Categoría (en la API no viene directa, puedes dejarla vacía o calcularla después)
                        p.setCategoria("Semilla"); // temporal

                        // Tipos
                        JSONArray tiposArray = response.getJSONArray("types");
                        ArrayList<String> tipos = new ArrayList<>();
                        for (int i = 0; i < tiposArray.length(); i++) {
                            tipos.add(tiposArray.getJSONObject(i).getJSONObject("type").getString("name"));
                        }
                        p.setTipos(tipos);

                        successListener.onResponse(p);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                errorListener);

        queue.add(request);
    }

    /**
     * Método auxiliar: carga una imagen desde una URL (sin librerías externas)
     */
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.e("PokedexApi", "Error al cargar imagen: " + e.getMessage());
            return null;
        }
    }
}
