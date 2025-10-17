package com.example.intentopokedex3.model;

import java.util.ArrayList;

/**
 * Clase modelo del objeto Pokemon
 *
 * Atributos:
 *    nombre       → nombre del pokemon
 *    numero       → numero de la pokedex
 *    urlImagen    → direccion del sprite
 *    tipos        → lista de tipos
 *    altura       → en metros
 *    peso         → en kg
 *    categoria    → especie o categoria (opcional)
 *    habilidad    → habilidad principal
 *
 * Metodos:
 *    • Getters y Setters → usados por PokedexApi y las Activities
 *    • Constructores vacio y rapido
 *
 * 🔹 Objetivo:
 *    Representar cada Pokemon como un objeto reutilizable
 *    que se pasa entre metodos y pantallas.
 */
public class Pokemon {

    private String nombre;
    private int numero;
    private String imagenUrl;
    private ArrayList<String> tipos;

    private double altura;
    private double peso;
    private String categoria;
    private String habilidad;

    public Pokemon() {}

    public Pokemon(String nombre, String url) {
        this.nombre = nombre;
        this.imagenUrl = url;
    }


    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public ArrayList<String> getTipos() {
        return tipos;
    }

    public void setTipos(ArrayList<String> tipos) {
        this.tipos = tipos;
    }

    public double getAltura() {
        return altura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getHabilidad() {
        return habilidad;
    }

    public void setHabilidad(String habilidad) {
        this.habilidad = habilidad;
    }
}
