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
 * Controlador de conexion con la PokeAPI.
 * Se encarga de descargar datos e imagenes de los Pokemon.
 *
 * Usa hilos (ExecutorService) y Handler para no bloquear la interfaz.
 *
 * FLUJO CONTROLLER:
 * obtenerPokemonPorTipo(tipo, listener)
 *    → asincrono: lanza hilo, llama a obtenerPokemonPorTipoSync(tipo)
 *    ↳ devuelve lista al listener (callback)
 *
 * obtenerPokemonPorTipoSync(tipo)
 *    → peticion a "https://pokeapi.co/api/v2/type/{tipo}"
 *    ↳ obtiene lista de URLs de pokemons de ese tipo
 *    ↳ por cada uno llama a obtenerDetallePokemon(url)
 *    ↳ devuelve hasta 30 pokemons con datos basicos
 *
 * obtenerPokemonPorTipoSync(tipo, offset, limite)
 *    → version con paginacion manual
 *    ↳ usa offset y limite para cargar bloques de pokemons
 *    ↳ tambien llama a obtenerDetallePokemon()
 *
 * obtenerDetallePokemon(url)
 *    → peticion directa a la URL del pokemon
 *    ↳ obtiene nombre, id, sprite, tipos, altura, peso, habilidad
 *    ↳ devuelve un objeto Pokemon completo
 *
 * buscarPorNombre(texto, listener)
 *    → busca coincidencias en "https://pokeapi.co/api/v2/pokemon?limit=1025"
 *    ↳ filtra nombres que empiecen por el texto
 *    ↳ por cada uno llama a obtenerDetallePokemon(url)
 *    ↳ devuelve lista al listener
 *
 * descargarImagen(url, listener)
 *    → descarga un sprite desde su URL
 *    ↳ decodifica a Bitmap
 *    ↳ devuelve la imagen en el hilo principal
 *
 * 🔹 Objetivo:
 *    Controla toda la comunicacion con la PokeAPI:
 *    • Listas por tipo
 *    • Busqueda por nombre
 *    • Detalles individuales
 *    • Descarga de imagenes
 *
 */
public class PokedexApi {

    // -------------------------------------------------------------------------
    // URL base de la API
    // -------------------------------------------------------------------------
    private static final String BASE_URL = "https://pokeapi.co/api/v2/";

    // -------------------------------------------------------------------------
    // Interfaces de callback
    // -------------------------------------------------------------------------
    // Estas interfaces permiten devolver los datos de forma asincrona (sin bloquear)
    public interface AlListoPokemon {
        void alObtener(ArrayList<Pokemon> lista);
    }

    public interface AlDescargarImagen {
        void alDescargar(Bitmap imagen);
    }

    // -------------------------------------------------------------------------
    // 🔹 METODO: obtenerPokemonPorTipo
    // -------------------------------------------------------------------------
    /**
     * Metodo asincrono que obtiene una lista de Pokemon de un tipo concreto.
     *
     * Llama internamente a {@link #obtenerPokemonPorTipoSync(String)}.
     * Se usa en ActivityLista para cargar los Pokemon del tipo seleccionado.
     */
    public static void obtenerPokemonPorTipo(String tipo, AlListoPokemon listener) {
        ExecutorService ejecutor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        ejecutor.execute(() -> {
            ArrayList<Pokemon> lista = obtenerPokemonPorTipoSync(tipo);
            handler.post(() -> {
                if (listener != null) listener.alObtener(lista);
            });
        });
    }

    // -------------------------------------------------------------------------
    // 🔹 METODO: obtenerPokemonPorTipoSync
    // -------------------------------------------------------------------------
    /**
     * Metodo sincrono (bloqueante) que obtiene los Pokemon de un tipo.
     *
     * Llamado desde {@link #obtenerPokemonPorTipo(String, AlListoPokemon)}.
     *
     * 1️⃣ Hace la peticion a la API (https://pokeapi.co/api/v2/type/{tipo})
     * 2️⃣ Crea objetos Pokemon basicos
     * 3️⃣ Devuelve una lista con los primeros 30 resultados
     */
    public static ArrayList<Pokemon> obtenerPokemonPorTipoSync(String tipo) {
        ArrayList<Pokemon> lista = new ArrayList<>();
        String urlString = BASE_URL + "type/" + tipo.toLowerCase();

        try {
            URL url = new URL(urlString);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");

            BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String linea;
            while ((linea = lector.readLine()) != null) jsonBuilder.append(linea);
            lector.close();
            conexion.disconnect();

            JSONObject json = new JSONObject(jsonBuilder.toString());
            JSONArray pokemons = json.getJSONArray("pokemon");

            int limite = Math.min(30, pokemons.length());
            for (int i = 0; i < limite; i++) {
                JSONObject pokeObj = pokemons.getJSONObject(i).getJSONObject("pokemon");
                String nombre = pokeObj.getString("name");
                String urlDetalle = pokeObj.getString("url");

                Pokemon p = obtenerDetallePokemon(urlDetalle); // 🔸 Llama al metodo de detalles
                if (p != null) lista.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    // -------------------------------------------------------------------------
    // 🔹 METODO: obtenerDetallePokemon
    // -------------------------------------------------------------------------
    /**
     * Descarga los detalles de un Pokemon (nombre, numero, imagen, tipos, peso, altura, habilidad)
     *
     * Este metodo es usado por:
     * - {@link #obtenerPokemonPorTipoSync(String)}
     * - {@link #buscarPorNombre(String, AlListoPokemon)}
     */
    public static Pokemon obtenerDetallePokemon(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");

            BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String linea;
            while ((linea = lector.readLine()) != null) jsonBuilder.append(linea);
            lector.close();
            conexion.disconnect();

            JSONObject json = new JSONObject(jsonBuilder.toString());
            String nombre = json.getString("name");
            int numero = json.getInt("id");
            String imagen = json.getJSONObject("sprites").getString("front_default");

            // Obtener tipos
            JSONArray tiposArray = json.getJSONArray("types");
            ArrayList<String> tipos = new ArrayList<>();
            for (int i = 0; i < tiposArray.length(); i++) {
                tipos.add(tiposArray.getJSONObject(i)
                        .getJSONObject("type")
                        .getString("name"));
            }

            // Crear el objeto Pokemon con los datos obtenidos
            Pokemon p = new Pokemon();
            p.setNombre(nombre);
            p.setNumero(numero);
            p.setImagenUrl(imagen);
            p.setTipos(tipos);
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
    // 🔹 METODO: descargarImagen
    // -------------------------------------------------------------------------
    /**
     * Descarga una imagen desde una URL (sprite del Pokemon).
     *
     * Llamado desde ActivityDetalle para mostrar la imagen del Pokemon.
     */
    public static void descargarImagen(String urlImagen, AlDescargarImagen listener) {
        ExecutorService ejecutor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        ejecutor.execute(() -> {
            Bitmap bitmap = null;
            try {
                URL url = new URL(urlImagen);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.connect();
                InputStream input = conexion.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                input.close();
                conexion.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Bitmap imagenFinal = bitmap;
            handler.post(() -> {
                if (listener != null) listener.alDescargar(imagenFinal);
            });
        });
    }

    // -------------------------------------------------------------------------
    // 🔹 METODO: buscarPorNombre
    // -------------------------------------------------------------------------
    /**
     * Busca Pokemon que empiecen por un texto.
     *
     * Se usa en los buscadores de:
     * - ActivityLista
     * - ActivityTipos
     * - ActivityDetalle
     *
     * Llama a {@link #obtenerDetallePokemon(String)} para obtener los datos de cada resultado.
     */
    public static void buscarPorNombre(String texto, AlListoPokemon listener) {
        new Thread(() -> {
            ArrayList<Pokemon> resultados = new ArrayList<>();
            String urlString = BASE_URL + "pokemon?limit=1025"; // Se obtienen todos los nombres

            try {
                URL url = new URL(urlString);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("GET");

                BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                StringBuilder jsonBuilder = new StringBuilder();
                String linea;
                while ((linea = lector.readLine()) != null) jsonBuilder.append(linea);
                lector.close();
                conexion.disconnect();

                JSONObject json = new JSONObject(jsonBuilder.toString());
                JSONArray pokemons = json.getJSONArray("results");

                for (int i = 0; i < pokemons.length(); i++) {
                    JSONObject pokeObj = pokemons.getJSONObject(i);
                    String nombre = pokeObj.getString("name");

                    // Filtra los nombres que empiecen por el texto introducido
                    if (nombre.startsWith(texto.toLowerCase())) {
                        String urlDetalle = pokeObj.getString("url");
                        Pokemon p = obtenerDetallePokemon(urlDetalle); // 🔸 Llama al metodo de detalles
                        if (p != null) resultados.add(p);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (listener != null) listener.alObtener(resultados);
        }).start();
    }

    // -------------------------------------------------------------------------
    // 🔹 METODO: obtenerPokemonPorTipoSync con paginacion (offset, limite)
    // -------------------------------------------------------------------------
    /**
     * Version mejorada del metodo de tipo, con paginacion.
     *
     * Se usa en ActivityLista para el boton "Ver mas Pokemon".
     * Carga los siguientes 30 resultados a partir del offset.
     */
    public static ArrayList<Pokemon> obtenerPokemonPorTipoSync(String tipo, int offset, int limite) {
        ArrayList<Pokemon> lista = new ArrayList<>();
        String urlString = BASE_URL + "type/" + tipo.toLowerCase();

        try {
            URL url = new URL(urlString);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");

            BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String linea;
            while ((linea = lector.readLine()) != null) jsonBuilder.append(linea);
            lector.close();
            conexion.disconnect();

            JSONObject json = new JSONObject(jsonBuilder.toString());
            JSONArray pokemons = json.getJSONArray("pokemon");

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
