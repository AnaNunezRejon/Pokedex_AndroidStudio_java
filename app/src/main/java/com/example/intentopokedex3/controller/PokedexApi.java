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
