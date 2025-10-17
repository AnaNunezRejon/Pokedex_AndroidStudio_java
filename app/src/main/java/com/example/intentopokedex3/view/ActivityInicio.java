package com.example.intentopokedex3.view;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.content.Intent;

import com.example.intentopokedex3.R;

/**
 * Pantalla de inicio de la aplicacion Pokedex.
 * Muestra una imagen de fondo que al pulsarla lleva al menu de tipos.
 *
 * -------------------------------------------------------------------------------------------------------------
 *
 * FUNCIONAMIENTO DE LA APLICACION:
 ActivityInicio
 ↳ abre ActivityTipos

 ActivityTipos
 ↳ muestra botones de tipo
 ↳ llama a ActivityLista pasando el tipo
 ↳ usa buscarPorNombre()

 ActivityLista
 ↳ recibe tipo
 ↳ usa obtenerPokemonPorTipoSync() y descargarImagen()
 ↳ usa buscarPorNombre()
 ↳ abre ActivityDetalle

 ActivityDetalle
 ↳ recibe datos del Pokemon
 ↳ usa descargarImagen()
 ↳ carga detalles directos desde la API
 ↳ muestra tipos y debilidades

 PokedexApi
 ↳ centraliza toda la comunicacion con la API
 ↳ es llamada por las tres Activities

 Pokemon
 ↳ clase modelo compartida por todas las anteriores
 *
 * -------------------------------------------------------------------------
 *
 * FLUJO INICIO:
 * onCreate() → se ejecuta al abrir la app
 *    ↳ carga el layout activity_inicio.xml
 *    ↳ obtiene referencia del boton btnIrATipos
 *    ↳ setOnClickListener() → al pulsar abre ActivityTipos
 *
 * 🔹 Objetivo:
 *    Muestra la pantalla inicial con el fondo de la Pokédex.
 *    El fondo completo actua como un boton que lleva a la seleccion de tipos.
 *
 *
 */
public class ActivityInicio extends AppCompatActivity {

    // -------------------------------------------------------------------------
    // Variables de interfaz
    // -------------------------------------------------------------------------
    private ImageButton botonIrATipos;

    // -------------------------------------------------------------------------
    // 🔹 Metodo principal al crear la actividad
    // -------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        // Referencia al boton (fondo clicable)
        botonIrATipos = findViewById(R.id.btnIrATipos);

        // Al pulsar el fondo se abre la pantalla de tipos
        botonIrATipos.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityInicio.this, ActivityTipos.class);
            startActivity(intent);
        });
    }
}
