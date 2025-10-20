
<img width="2180" height="1408" alt="mockup" src="https://github.com/user-attachments/assets/fb1bb715-259f-4650-8434-79891a28c94c" />
# Pokédex  

Aplicación Android basada en el patrón **Modelo - Vista - Controlador (MVC)**  

**Autora:** Ana Núñez  
**Curso:** Desarrollo de Aplicaciones Multiplataforma (DAM)  
**Fecha:** Octubre 2025  
**Proyecto:** Android Studio – Lenguaje Java  
**Repositorio:** [github.com/AnaNunezRejon/Pokedex](https://github.com/AnaNunezRejon/Pokedex)

---

## 1. Descripción de la aplicación

**Pokédex** es una app Android desarrollada en **Java** siguiendo el patrón **MVC**.  
Consume la API pública **[PokeAPI](https://pokeapi.co/)** para mostrar información de los Pokémon:  
**nombre, número, imagen, tipos, altura, peso, habilidad, categoría y descripción** (todo en **español**).  

La interfaz incluye:
- Buscador con sugerencias en tiempo real (nombre o número).
- Chips de tipos y debilidades.
- Carga por bloques con paginación.
- Traducción al español de habilidades, categorías y descripciones.

---

## 2. Objetivo general

Permitir **consultar** y **explorar** Pokémon como ejemplo práctico de:

- Uso del patrón **MVC** en Android.  
- **Consumo de APIs REST** (HTTP + JSON).  
- **Diseño de interfaces dinámicas** en XML.  
- **Buenas prácticas de UI y asincronía** (uso de hilos, `runOnUiThread`, `ExecutorService`).

---

## 3. Funcionamiento general

1. **Pantalla de inicio:** muestra el logo de Pokédex.  
2. **Pantalla de tipos:** botones de tipos (agua, fuego, planta, etc.) y buscador superior.  
3. **Pantalla de lista:** muestra los Pokémon del tipo elegido en un **GridLayout**.  
4. **Pantalla de detalle:** muestra la ficha completa del Pokémon: imagen, número, altura, peso, habilidad, categoría, descripción, tipos y debilidades.  

---

## 4. Arquitectura interna (MVC)

### Modelo (`com.example.intentopokedex3.model`)

- **`Pokemon.java`**  
  Clase que representa un Pokémon individual.  
  Contiene: `nombre`, `numero`, `imagenUrl`, `tipos`, `altura`, `peso`, `habilidad`.

- **`PokedexApi.java`**  
  Gestiona las peticiones HTTP a la API.  
  Métodos principales:
  - `obtenerPokemonPorTipo(String tipo)`  
  - `obtenerPokemonPorTipo(String tipo, int offset, int limite)`  
  - `buscarPorNombre(String texto)`  
  - `obtenerDetallePokemon(String urlDetalle)`  
  - `descargarImagen(String url)`  

Todos los métodos se ejecutan en **hilos** para no bloquear la interfaz.

---

### Controlador (`com.example.intentopokedex3.controller`)

- Controla la lógica entre las vistas y el modelo (`PokedexApi`).
- Gestiona hilos y comunicación entre UI y datos.
- En este proyecto, las Activities realizan funciones de controlador directo (por claridad académica).

---

### Vista (`com.example.intentopokedex3.view`)

| Archivo | Descripción |
|----------|--------------|
| **`ActivityInicio.java`** | Pantalla inicial con el logo. |
| **`ActivityTipos.java`** | Muestra botones de tipos y buscador. |
| **`ActivityLista.java`** | Lista de Pokémon del tipo seleccionado, con **paginación**. |
| **`ActivityDetalle.java`** | Ficha detallada del Pokémon. Carga información traducida al español. |

**Layouts XML:**  
Diseñados con colores personalizados, fondos temáticos (`fondo_pokedex`, `fondo_caja_detalle`), y contenedores dinámicos (`GridLayout`, `FlexboxLayout`).

---

## 5. Conexión con la API

**Base URL:** `https://pokeapi.co/api/v2/`

Endpoints utilizados:
- `type/{tipo}` → lista de Pokémon por tipo.  
- `pokemon/{nombre|id}` → datos principales.  
- `pokemon-species/{id}` → categoría y descripción.  
- `ability/{nombre}` → nombre de la habilidad en español.  

**Formato:** JSON  
**Conversión:** Manual mediante `JSONObject` y `JSONArray`.

Ejemplo de endpoint: https://pokeapi.co/api/v2/pokemon/pikachu

## 6. Flujo interno del programa

ActivityInicio
↓
ActivityTipos
↓ (envía tipo seleccionado)
ActivityLista
↓
PokedexApi
↓
ArrayList<Pokemon>
↓
ActivityDetalle


### 🔹 Pantalla de inicio
- Muestra el logo y redirige a **Tipos** mediante `Intent`.

### 🔹 Pantalla de tipos
- Botones con todos los tipos.
- Traduce tipo a inglés para API (`planta → grass`).
- Incluye buscador de Pokémon (nombre o número).

### 🔹 Pantalla de lista
- Carga Pokémon por tipo con **paginación (30 en 30)**.  
- Cada tarjeta muestra imagen + nombre.  
- Al pulsar, abre **ActivityDetalle**.  

### 🔹 Pantalla de detalle
- Carga información extendida:  
  - **Categoría y descripción** → `pokemon-species/{id}` (ES)  
  - **Habilidad** → `ability/{nombre}` (ES)  
- Chips de **tipos** y **debilidades** con colores.  
- **Buscador interno** para cambiar de Pokémon sin salir de la pantalla.

---

## 7. Internacionalización (Español)

| Elemento | Fuente | Traducción |
|-----------|--------|-------------|
| **Habilidad** | `/ability/{nombre}` | `names[language=es].name` |
| **Categoría** | `/pokemon-species/{id}` | `genera[language=es].genus` |
| **Descripción** | `/pokemon-species/{id}` | `flavor_text_entries[language=es].flavor_text` |
| **Tipos (UI)** | Mapeo interno (`fire→Fuego`, `grass→Planta`, etc.) |

---

## 🧠 8. Características técnicas

- **Lenguaje:** Java  
- **IDE:** Android Studio  
- **Arquitectura:** MVC  
- **API:** PokeAPI  
- **Diseño:** XML + Drawables personalizados  
- **Permisos:** `INTERNET`  
- **Librerías:** AndroidX, FlexboxLayout  

---

## 9. Funcionamiento del buscador

- Basado en `EditText` + `TextWatcher`.  
- Realiza llamadas a `PokedexApi.buscarPorNombre()` en un **Thread**.  
- Si el texto cambia durante la búsqueda → se **descarta el resultado anterior** (antirrebote).  
- Permite buscar por **nombre parcial o número**.  
- Muestra una lista flotante (`ListView`) con resultados.

---

## 10. Rendimiento y asincronía

- Uso de **ExecutorService** y **Handler** para evitar bloqueos en la UI.  
- **Carga por bloques (paginación)** para evitar sobrecarga.  
- **Descarga de imágenes** en hilos independientes.  
- **Sin `null`**: manejo seguro de valores predeterminados.  

---

## 11. Diseño visual

- **Temática Pokédex:** fondo morado degradado + logo inferior.  
- **Cajas de detalle:** `fondo_caja_detalle.xml` con esquinas redondeadas.  
- **Botones personalizados:** `btn_volver`, `rounded_search_background_white`.  
- **Chips de tipos y debilidades:** creados dinámicamente con colores por tipo.  

---

## 12. Estructura de carpetas

com.example.intentopokedex3  
├── model  
│ ├── Pokemon.java  
│  
├── controller  
│ ├── PokedexApi.java  
│  
├── view  
│ ├── ActivityInicio.java  
│ ├── ActivityTipos.java  
│ ├── ActivityLista.java  
│ ├── ActivityDetalle.java  
│  
└── res  
├── layout/  
├── drawable/  
└── values/  


---

## 13. Pruebas realizadas

✅ Búsqueda por nombre → *“char” → Charmander, Charizard…*  
✅ Búsqueda por número → *“25” → Pikachu*  
✅ Traducciones al español correctas (habilidad, descripción, categoría)  
✅ Paginación fluida (bloques de 30)  
✅ Antirrebote funcional  
✅ Carga de debilidades exacta según tipo  

---

## 14. Mejoras futuras

- Implementar **RecyclerView** en lugar de GridLayout.  
- Añadir **favoritos offline** (Room Database).  
- Cachear imágenes (Glide o LruCache).  
- Modo oscuro y soporte accesibilidad.  
- Pruebas unitarias de modelo y UI (Espresso).  

---

## 15. Mockups y referencias visuales

<img width="2180" height="1408" alt="mockup" src="https://github.com/user-attachments/assets/cfd40453-4493-4be5-98f1-f10eec42cc37" />
<img width="2180" height="1408" alt="ideas_1" src="https://github.com/user-attachments/assets/5abb4f70-2e38-495a-9325-81dd6303de1d" />
<img width="2180" height="1408" alt="ideas_2" src="https://github.com/user-attachments/assets/ad17c438-83d4-4af6-ad7c-2e567f05e0f2" />


| Pantalla | Descripción |
|-----------|-------------|
| **Inicio** | Logo Pokédex. |
| **Tipos** | Selección de tipo + buscador. |
| **Lista** | Cuadrícula de Pokémon del tipo elegido. |
| **Detalle** | Ficha con imagen, descripción y debilidades. |

---

## 16. Créditos

Proyecto académico desarrollado por **Ana Núñez**,  
como parte del módulo **Desarrollo de Aplicaciones Multiplataforma (DAM)**.  

Datos obtenidos de la API oficial [PokeAPI](https://pokeapi.co/).

---

*Pokédex Android – Octubre 2025*  
Desarrollado en **Java + Android Studio**  
Diseño y código original por **Ana Núñez**




