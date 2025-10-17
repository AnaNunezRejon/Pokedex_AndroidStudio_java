package com.example.intentopokedex3.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.example.intentopokedex3.model.Pokemon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controlador PokeAPI sin librerías externas.
 * Usa ExecutorService + Handler (sustituto moderno de AsyncTask).
 */
public class PokedexApi {

    private static final String BASE_URL = "https://pokeapi.co/api/v2/";

    // Interfaz para callback cuando termina una lista de Pokémon
    public interface OnPokemonListReady {
        void onResult(ArrayList<Pokemon> lista);
    }

    // Interfaz para callback cuando termina de descargar una imagen
    public interface OnImagenDescargada {
        void onDescargada(Bitmap imagen);
    }

    // -------------------------------------------------------------------------
    // 🔹 Obtener lista de Pokémon por tipo (asíncrono, sin bloquear UI)
    // -------------------------------------------------------------------------
    public static void obtenerPokemonPorTipo(String tipo, OnPokemonListReady listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            ArrayList<Pokemon> lista = obtenerPokemonPorTipoSync(tipo);
            handler.post(() -> {
                if (listener != null) listener.onResult(lista);
            });
        });
    }

    // -------------------------------------------------------------------------
    // 🔹 Versión sincrónica (para usar dentro del executor)
    // -------------------------------------------------------------------------
    public static ArrayList<Pokemon> obtenerPokemonPorTipoSync(String tipo) {
        ArrayList<Pokemon> lista = new ArrayList<>();
        String urlString = BASE_URL + "type/" + tipo.toLowerCase();

        try {
            // 1️⃣ Conectamos con la API
            URL url = new URL(urlString);
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

            // 2️⃣ Parseamos el JSON
            JSONObject json = new JSONObject(jsonBuilder.toString());
            JSONArray pokemons = json.getJSONArray("pokemon");

            int limite = Math.min(30, pokemons.length());
            for (int i = 0; i < limite; i++) {
                JSONObject pokeObj = pokemons.getJSONObject(i).getJSONObject("pokemon");
                String nombre = pokeObj.getString("name");
                String urlDetalle = pokeObj.getString("url");

                Pokemon p = obtenerDetallePokemon(urlDetalle);
                if (p != null) lista.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    // -------------------------------------------------------------------------
    // 🔹 Obtener detalles de un Pokémon
    // -------------------------------------------------------------------------
    public static Pokemon obtenerDetallePokemon(String urlString) {
        try {
            URL url = new URL(urlString);
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
            String nombre = json.getString("name");
            int numero = json.getInt("id");
            String imagen = json.getJSONObject("sprites").getString("front_default");

            // Tipos
            JSONArray tiposArray = json.getJSONArray("types");
            ArrayList<String> tipos = new ArrayList<>();
            for (int i = 0; i < tiposArray.length(); i++) {
                tipos.add(tiposArray.getJSONObject(i)
                        .getJSONObject("type")
                        .getString("name"));
            }

            // Creamos el objeto Pokémon
            Pokemon p = new Pokemon();
            p.setNombre(nombre);
            p.setNumero(numero);
            p.setImagenUrl(imagen);
            p.setTipos(tipos);

            // Detalles adicionales
            p.setAltura(json.getDouble("height") / 10.0);
            p.setPeso(json.getDouble("weight") / 10.0);

            JSONArray habilidades = json.getJSONArray("abilities");
            if (habilidades.length() > 0) {
                String habilidad = habilidades.getJSONObject(0)
                        .getJSONObject("ability")
                        .getString("name");
                p.setHabilidad(habilidad);
            }

            return p;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // 🔹 Descargar imagen (sin bloqueo, con callback)
    // -------------------------------------------------------------------------
    public static void descargarImagen(String urlImagen, OnImagenDescargada listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Bitmap bitmap = null;
            try {
                URL url = new URL(urlImagen);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                InputStream input = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                input.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Bitmap finalBitmap = bitmap;
            handler.post(() -> {
                if (listener != null) listener.onDescargada(finalBitmap);
            });
        });
    }


    // -------------------------------------------------------------------------
    // 🔹 Buscador
    // -------------------------------------------------------------------------
    public static void buscarPorNombre(String texto, OnPokemonListReady listener) {
        new Thread(() -> {
            ArrayList<Pokemon> resultados = new ArrayList<>();
            String urlString = BASE_URL + "pokemon?limit=1025";

            try {
                URL url = new URL(urlString);
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
                JSONArray pokemons = json.getJSONArray("results");

                for (int i = 0; i < pokemons.length(); i++) {
                    JSONObject pokeObj = pokemons.getJSONObject(i);
                    String nombre = pokeObj.getString("name");

                    if (nombre.startsWith(texto.toLowerCase())) {
                        String urlDetalle = pokeObj.getString("url");
                        Pokemon p = obtenerDetallePokemon(urlDetalle);
                        if (p != null) resultados.add(p);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (listener != null) listener.onResult(resultados);
        }).start();
    }

    public static ArrayList<Pokemon> obtenerPokemonPorTipoSync(String tipo, int offset, int limite) {
        ArrayList<Pokemon> lista = new ArrayList<>();
        String urlString = BASE_URL + "type/" + tipo.toLowerCase();

        try {
            URL url = new URL(urlString);
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
            JSONArray pokemons = json.getJSONArray("pokemon");

            // Cargar en bloques (por ejemplo 30)
            int inicio = offset;
            int fin = Math.min(offset + limite, pokemons.length());

            for (int i = inicio; i < fin; i++) {
                JSONObject pokeObj = pokemons.getJSONObject(i).getJSONObject("pokemon");
                String nombre = pokeObj.getString("name");
                String urlDetalle = pokeObj.getString("url");

                Pokemon p = obtenerDetallePokemon(urlDetalle);
                if (p != null) lista.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }


}
