# Plan de Implementación: Refactorización de Búsqueda (Punto 5)

## Contexto
- `InventarioInmueblesService` indexaba inmuebles en TablaHash por código — ya existía.
- `ClienteDAO` persistía clientes en SQLite — ya existía.
- **Faltaba**: Un servicio que indexe clientes en RAM para búsquedas O(1).

## Cambios Realizados

### 1. TablaHash — Nuevo método `valores()`
- Recorre todos los buckets y cadenas de colisión.
- Devuelve `ListaEnlazada<V>` con todos los valores almacenados.

### 2. ClientesService.java (NUEVO)
- `TablaHash<String, Cliente>` indexada por identificación.
- `cargarDatosDesdeSQL()` al arrancar.
- `registrarCliente()` — dual-write (SQLite + RAM).
- `buscarPorIdentificacion()` — O(1) sin tocar disco.
- `obtenerTodos()` — usa el nuevo `valores()`.

### 3. Main.java — Endpoints REST
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/clientes` | Lista todos los clientes |
| GET | `/api/clientes/{id}` | Busca por identificación O(1) |
| POST | `/api/clientes` | Registra nuevo cliente |

### 4. Frontend
- `index.html` — Secciones Inmuebles/Clientes con tabs, buscadores, modales.
- `styles.css` — Design system con gradientes, animaciones, tarjetas.
- `app.js` — Funciones de búsqueda, listado y registro de clientes.

### 5. Cliente.java — Constructor vacío
- Necesario para que Jackson deserialice el JSON del POST.
