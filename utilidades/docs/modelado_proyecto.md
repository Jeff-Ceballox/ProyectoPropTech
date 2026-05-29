# Modelado del Proyecto — **PropTech (Insignia Inmo)**

## Visión General

Plataforma **PropTech** para la gestión inmobiliaria que integra:

- **Estructuras de datos** clásicas implementadas desde cero (Lista Enlazada, Pila, Cola, Cola de Prioridad, Tabla Hash, Árbol Binario de Búsqueda, Grafo)
- **Persistencia** con SQLite embebido
- **API REST** con Javalin (Java)
- **Frontend** HTML/CSS/JS estático
- **Asesor IA** con OpenRouter

---

## 1. Arquitectura por Capas

```
┌─────────────────────────────────────────────┐
│               FRONTEND (Web)                 │
│   HTML / CSS / JS (Javalin Static Files)     │
├─────────────────────────────────────────────┤
│              API REST (Javalin)              │
│         Main.java (Rutas HTTP)              │
├─────────────────────────────────────────────┤
│             CAPA DE SERVICIOS               │
│   Lógica de negocio + estructuras en RAM    │
├─────────────────────────────────────────────┤
│         CAPA DE ACCESO A DATOS (DAO)        │
│           SQLite (Persistencia)             │
└─────────────────────────────────────────────┘
```

---

## 2. Estructura del Proyecto

```
utilidades/
├── src/
│   ├── main/
│   │   ├── java/com/proptech/
│   │   │   ├── Main.java                  ← Punto de entrada + API REST
│   │   │   ├── modelo/                    ← Clases del dominio
│   │   │   ├── dao/                       ← Acceso a base de datos
│   │   │   ├── servicio/                  ← Lógica de negocio
│   │   │   │   └── asesor/                ← Asesor IA
│   │   │   └── utilidades/
│   │   │       ├── HashUtils.java
│   │   │       └── estructuras/           ← ED implementadas desde cero
│   │   └── resources/public/              ← Frontend estático
│   └── test/java/com/proptech/            ← Tests unitarios
├── docs/                                  ← Documentación
└── target/                                ← Compilado
```

---

## 3. Modelo de Datos (Diagrama de Clases)

### 3.1 Clases del Dominio (`com.proptech.modelo`)

| Clase       | Atributos clave                                                              | Propósito                                   |
|-------------|------------------------------------------------------------------------------|---------------------------------------------|
| `Inmueble`  | codigo, tipo, direccion, precio, area, estado, habitaciones, banos, parqueadero, descripcion | Propiedad inmobiliaria                      |
| `Cliente`   | identificacion, nombre, telefono, presupuestoMaximo, email, historialConsultas, favoritos, tipoInmuebleDeseado, zonasInteres, cantMinHabitaciones | Comprador / arrendatario                    |
| `Asesor`    | idAsesor, nombre, especialidad, email, telefono, negociosCerrados, calificacion | Agente inmobiliario                         |
| `Usuario`   | idUsuario, email, passwordHash, rol, nombre, telefono, direccion, intereses, fotoPerfil, activo | Usuario del sistema (autenticación)         |
| `Rol`       | idRol, nombre (CLIENTE, VENDEDOR, ADMIN, GERENTE)                           | Roles de seguridad                          |
| `Operacion` | idOperacion, tipo (Venta/Arriendo), inmueble, cliente, asesor, monto, fecha | Transacción de negocio                      |
| `Visita`    | idVisita, cliente, inmueble, asesor, fechaHora, estado                      | Cita entre cliente-asesor-inmueble          |
| `Alerta`    | idAlerta, mensaje, prioridad, fechaCreacion                                 | Notificación con urgencia                   |

### 3.2 Relaciones entre Entidades

```
Cliente ──(1:N)──> Visita
Asesor  ──(1:N)──> Visita
Inmueble──(1:N)──> Visita

Cliente ──(1:N)──> Operacion
Asesor  ──(1:N)──> Operacion
Inmueble──(1:N)──> Operacion

Cliente ──(1:N)──> Favorito
Inmueble──(1:N)──> Favorito

Usuario ──(N:1)──> Rol
```

---

## 4. Base de Datos (SQLite)

Archivo: `~/.proptech/inmobiliaria.db`

### 4.1 Esquema de Tablas

```sql
-- Inmuebles
CREATE TABLE inmuebles (
    codigo TEXT PRIMARY KEY, tipo TEXT NOT NULL, direccion TEXT NOT NULL,
    precio REAL NOT NULL, area REAL NOT NULL, estado TEXT NOT NULL,
    habitaciones INTEGER, banos INTEGER, tieneParqueadero BOOLEAN,
    descripcion TEXT
);

-- Clientes
CREATE TABLE clientes (
    identificacion TEXT PRIMARY KEY, nombre TEXT NOT NULL, telefono TEXT,
    presupuestoMaximo REAL, email TEXT
);

-- Asesores
CREATE TABLE asesores (
    idAsesor TEXT PRIMARY KEY, nombre TEXT NOT NULL, especialidad TEXT,
    email TEXT, telefono TEXT, calificacion REAL DEFAULT 5.0,
    negociosCerrados INTEGER DEFAULT 0
);

-- Operaciones (con claves foráneas)
CREATE TABLE operaciones (
    idOperacion TEXT PRIMARY KEY, tipo TEXT NOT NULL,
    idInmueble TEXT, idCliente TEXT, idAsesor TEXT,
    monto REAL NOT NULL, fecha TEXT NOT NULL,
    FOREIGN KEY(idInmueble) REFERENCES inmuebles(codigo),
    FOREIGN KEY(idCliente) REFERENCES clientes(identificacion),
    FOREIGN KEY(idAsesor) REFERENCES asesores(idAsesor)
);

-- Visitas
CREATE TABLE visitas (
    idVisita TEXT PRIMARY KEY, identificacionCliente TEXT,
    codigoInmueble TEXT, idAsesor TEXT, fechaHora TEXT NOT NULL,
    estado TEXT NOT NULL,
    FOREIGN KEY(identificacionCliente) REFERENCES clientes(identificacion),
    FOREIGN KEY(codigoInmueble) REFERENCES inmuebles(codigo),
    FOREIGN KEY(idAsesor) REFERENCES asesores(idAsesor)
);

-- Favoritos
CREATE TABLE favoritos (
    identificacionCliente TEXT NOT NULL, codigoInmueble TEXT NOT NULL,
    PRIMARY KEY(identificacionCliente, codigoInmueble),
    FOREIGN KEY(identificacionCliente) REFERENCES clientes(identificacion),
    FOREIGN KEY(codigoInmueble) REFERENCES inmuebles(codigo)
);

-- Cambios pendientes (workflow de aprobación)
CREATE TABLE cambios_pendientes (
    id_cambio INTEGER PRIMARY KEY AUTOINCREMENT,
    email_usuario TEXT NOT NULL, campo TEXT NOT NULL,
    valor_anterior TEXT, valor_nuevo TEXT NOT NULL,
    estado TEXT NOT NULL DEFAULT 'pendiente', fecha_solicitud TEXT NOT NULL,
    fecha_revision TEXT, revisado_por TEXT
);
```

### 4.2 Gestión de Conexión (`ConexionDB.java`)

- Driver: `jdbc:sqlite`
- `inicializarTablas()` crea todas las tablas si no existen
- `conectar()` devuelve una conexión SQLite
- Ruta: `{user.home}/.proptech/inmobiliaria.db`

---

## 5. Capa de Acceso a Datos (DAO)

| DAO             | Tabla           | CRUD completo | Métodos adicionales                      |
|-----------------|-----------------|---------------|------------------------------------------|
| `InmuebleDAO`   | inmuebles       | ✓             | -                                        |
| `ClienteDAO`    | clientes        | ✓             | obtenerPorEmail()                        |
| `AsesorDAO`     | asesores        | ✓             | -                                        |
| `VisitaDAO`     | visitas         | ✓             | obtenerVisitasPorCliente(), porAsesor(), porInmueble() |
| `OperacionDAO`  | operaciones     | ✓             | obtenerPorTipo(), porAsesor(), porCliente() |
| `FavoritoDAO`   | favoritos       | parcial       | esFavorito(), agregarFavorito(), quitarFavorito() |
| `UsuarioDAO`    | usuarios        | ✓             | buscarPorEmail(), crear()                |

---

## 6. Estructuras de Datos Implementadas desde Cero

Todas las estructuras son **genéricas** y utilizan **nodos propios** (no `java.util.*`).

### 6.1 Lista Enlazada Simple (`ListaEnlazada<T>`)

- **Nodo base**: `Nodo<T>` (dato + puntero al siguiente)
- Operaciones: `agregar(T)`, `obtener(int)`, `remover(int)`, `getTamaño()`, `estaVacia()`
- Iteración O(n), inserción al final O(n)
- *Uso*: Historial de operaciones, historial de consultas, favoritos

### 6.2 Pila — LIFO (`Pila<T>`)

- **Nodo base**: `Nodo<T>`
- Operaciones: `apilar(T)`, `desapilar()`, `getTamaño()`, `estaVacia()`
- *Uso*: HistorialCambiosService (Ctrl+Z de modificaciones a inmuebles)

### 6.3 Cola — FIFO (`Cola<T>`)

- **Nodo base**: `Nodo<T>`
- Punteros a `frente` y `finalCola`
- Operaciones: `encolar(T)`, `desencolar()`, `getTamaño()`, `estaVacia()`
- *Uso*: Estructura base no utilizada directamente en servicios actuales

### 6.4 Cola de Prioridad (`ColaPrioridad<T>`)

- **Nodo base**: `NodoPrioridad<T>` (dato + prioridad int + puntero)
- Se inserta ordenadamente por prioridad (menor número = mayor urgencia)
- Operaciones: `encolar(T, prioridad)`, `desencolar()`, `getTamaño()`, `estaVacia()`
- *Uso*: AlertasService (alertas urgentes primero), GestorVisitasService

### 6.5 Tabla Hash (`TablaHash<K, V>`)

- **Nodo base**: `EntradaHash<K, V>` (clave + valor + puntero para colisiones)
- Resolución de colisiones: **encadenamiento separado** ( lista enlazada por bucket)
- Función hash: `Math.abs(clave.hashCode()) % capacidad`
- Capacidad inicial configurable (100, 200, etc.)
- Operaciones: `insertar(K, V)`, `obtener(K)`, `eliminar(K)`, `valores()`, `getTamaño()`
- Búsqueda **O(1)** promedio
- *Uso*:
  - `InventarioInmueblesService`: `mapaInmuebles` (código → Inmueble)
  - `ClientesService`: `mapaClientes` (identificación → Cliente)
  - `OperacionesService`: `mapaOperaciones` (id → Operacion)
  - `VisitaService`: `mapaVisitas` (id → Visita)

### 6.6 Árbol Binario de Búsqueda — BST (`ArbolBinarioBusqueda<K, V>`)

- **Nodo base**: `NodoArbol<K, V>` (clave comparable + valor + hijo izquierdo + hijo derecho)
- Inserción recursiva manteniendo la propiedad BST
- Recorrido **In-Order** para imprimir ordenado
- Búsqueda **O(log n)** promedio
- *Uso*: `InventarioInmueblesService`: `arbolPorPrecio` (precio → Inmueble) para ordenar catálogo por precio

### 6.7 Grafo (`Grafo<T>`)

- **Nodo base**: `Vertice<T>` (dato + `ListaEnlazada<T>` de adyacentes)
- Lista de adyacencia
- Grafo **no dirigido**
- Operaciones: `agregarVertice(T)`, `agregarArista(T, T)`, `imprimirRelaciones()`
- *Uso*: `AnalisisRelacionesService` — red de intereses cliente-inmueble para recomendaciones colaborativas (filtrado colaborativo: "clientes que vieron esto también vieron...")

### 6.8 Jerarquía de Nodos

```
Nodo<T>                           ← Base (para Lista, Pila, Cola)
├── EntradaHash<K,V>             ← Para TablaHash (agrega clave)
├── NodoPrioridad<T>             ← Para ColaPrioridad (agrega prioridad)
└── (base para Vertice en Grafo)

NodoArbol<K,V>                   ← Para BST (agrega hijos izq/der)
```

---

## 7. Capa de Servicios — Lógica de Negocio

### 7.1 InventarioInmueblesService
- **Estructuras**: `TablaHash<String, Inmueble>` + `ArbolBinarioBusqueda<Double, Inmueble>`
- Arranca cargando todos los inmuebles de SQLite a RAM
- `registrarInmueble()`: guarda en DB + indexa en ambas estructuras
- `buscarPorCodigo()`: O(1) por Tabla Hash
- `obtenerTodos()`: desde Tabla Hash

### 7.2 ClientesService
- **Estructura**: `TablaHash<String, Cliente>`
- Carga desde SQLite al iniciar
- `buscarPorIdentificacion()`: O(1)

### 7.3 OperacionesService
- **Estructuras**: `TablaHash<String, Operacion>` + `ListaEnlazada<Operacion>` (historial)
- Al registrar: persiste en DB, actualiza estado del inmueble (Vendido/Arrendado), incrementa negocios del asesor
- `registrarRenovacion()`, `registrarCancelacion()`

### 7.4 VisitaService
- **Estructura**: `TablaHash<String, Visita>`
- `programarVisita()`, `actualizarEstadoVisita()`, `cancelarVisita()`, `reprogramarVisita()`
- Filtros por cliente, inmueble

### 7.5 AlertasService
- **Estructura**: `ColaPrioridad<Alerta>`
- Alertas manuales y automáticas:
  - Contratos por vencer (7 días)
  - Propiedades sin visitas (30+ días)
  - Alta demanda (5+ visitas/mes)
  - Visitas pendientes de confirmación (24h+)
  - Propiedades reservadas sin cierre (14+ días)
  - Clientes sin seguimiento (15+ días)

### 7.6 RecomendacionService
- Sistema de puntuación multicriterio (100 pts máx):
  1. Presupuesto (30 pts)
  2. Tipo de inmueble deseado (25 pts)
  3. Zona de interés (20 pts)
  4. Habitaciones mínimas (15 pts)
  5. Historial de consultas (10 pts)
  6. Favoritos (5 pts)
- Retorna top 10 recomendaciones

### 7.7 DetectorAnomaliesService
- Umbares configurables
- Detecta: visitas sin cierre, sobrecarga de asesores, cambios de precio frecuentes, concentración geográfica

### 7.8 ReporteService
- Reportes: rendimiento inmuebles, desempeño asesores, precios por zona, clientes activos, tipos de operación, filtros combinados

### 7.9 AuthService
- Registro con hash SHA-256
- Login, creación de admin
- Workflow de cambios pendientes (clientes modifican datos sensibles → admin aprueba)

### 7.10 HistorialCambiosService
- **Estructura**: `Pila<RegistroCambio>`
- Deshacer cambios de precio/estado en inmuebles (Ctrl+Z)

### 7.11 AnalisisRelacionesService
- **Estructura**: `Grafo<String>`
- Filtrado colaborativo: recomienda inmuebles basado en el comportamiento de clientes similares

### 7.12 GestorVisitasService
- **Estructura**: `ColaPrioridad<Visita>`
- Atiende visitas por orden de prioridad

### 7.13 HistorialYFavoritosService
- Gestiona consultas y favoritos de clientes

### 7.14 Asesor IA (AsesorService + AiService)
- Integración con OpenRouter API
- Prompt contextual con datos del sistema (inmuebles, clientes, operaciones)
- Chattime widget en frontend

---

## 8. API REST (Javalin — Puerto 7070)

### 8.1 Inmuebles
| Método | Ruta                        | Propósito                  |
|--------|-----------------------------|----------------------------|
| GET    | `/api/inmuebles`            | Listar todos               |
| GET    | `/api/inmuebles/{codigo}`   | Buscar por código (O(1))   |
| POST   | `/api/inmuebles`            | Registrar                  |
| PUT    | `/api/inmuebles/{codigo}`   | Actualizar (+ auto-crear operación si cambia estado) |
| DELETE | `/api/inmuebles/{codigo}`   | Eliminar                   |

### 8.2 Clientes
| Método | Ruta                        | Propósito                  |
|--------|-----------------------------|----------------------------|
| GET    | `/api/clientes`             | Listar todos               |
| GET    | `/api/clientes/{id}`        | Buscar por ID (O(1))       |
| POST   | `/api/clientes`             | Registrar                  |
| PUT    | `/api/clientes/{id}`        | Actualizar                 |
| DELETE | `/api/clientes/{id}`        | Eliminar                   |

### 8.3 Operaciones
| Método | Ruta                                | Propósito                  |
|--------|-------------------------------------|----------------------------|
| GET    | `/api/operaciones`                  | Listar todas               |
| GET    | `/api/operaciones/{id}`             | Buscar por ID              |
| POST   | `/api/operaciones`                  | Registrar                  |
| POST   | `/api/operaciones/renovacion`       | Renovar contrato           |
| POST   | `/api/operaciones/cancelacion`      | Cancelar operación         |
| GET    | `/api/operaciones/tipo/{tipo}`      | Filtrar por tipo           |
| GET    | `/api/operaciones/asesor/{id}`      | Filtrar por asesor         |

### 8.4 Alertas
| Método | Ruta                        | Propósito                  |
|--------|-----------------------------|----------------------------|
| GET    | `/api/alertas/siguiente`    | Atender siguiente alerta   |

### 8.5 Recomendaciones
| Método | Ruta                                   | Propósito                  |
|--------|----------------------------------------|----------------------------|
| GET    | `/api/recomendaciones/{idCliente}`     | Recomendaciones para cliente |

### 8.6 Anomalías
| Método | Ruta                                       | Propósito                  |
|--------|--------------------------------------------|----------------------------|
| GET    | `/api/anomalias/visitas-sin-cierre`        | Visitas sin cierre         |
| GET    | `/api/anomalias/sobrecarga-asesores`       | Sobrecarga de asesores     |
| GET    | `/api/anomalias/cambios-precio`            | Cambios de precio          |
| GET    | `/api/anomalias/concentracion-geografica`  | Concentración geográfica   |
| GET    | `/api/anomalias/total`                     | Total de anomalías         |

### 8.7 Reportes
| Método | Ruta                                        | Propósito                  |
|--------|---------------------------------------------|----------------------------|
| GET    | `/api/reportes/rendimiento-inmuebles`       | Rendimiento general        |
| GET    | `/api/reportes/asesores`                    | Desempeño de asesores      |
| GET    | `/api/reportes/precios-zona`                | Precios por zona           |
| GET    | `/api/reportes/clientes-activos`            | Clientes activos           |
| GET    | `/api/reportes/tipos-operacion`             | Tipos de operación         |
| GET    | `/api/reportes/rendimiento-filtrado`        | Reporte con filtros        |

### 8.8 Visitas
| Método | Ruta                                    | Propósito                  |
|--------|-----------------------------------------|----------------------------|
| POST   | `/api/visitas`                          | Programar visita           |
| GET    | `/api/visitas/cliente/{email}`          | Visitas de un cliente      |
| PUT    | `/api/visitas/{id}/cancelar`            | Cancelar visita            |
| PUT    | `/api/visitas/{id}/confirmar`           | Confirmar visita           |
| PUT    | `/api/visitas/{id}/realizar`            | Marcar como realizada      |
| GET    | `/api/visitas/asesor/{id}`              | Visitas de un asesor       |

### 8.9 Asesores
| Método | Ruta                     | Propósito                  |
|--------|--------------------------|----------------------------|
| GET    | `/api/asesores`          | Listar asesores            |
| GET    | `/api/asesor/dashboard`  | Dashboard del asesor       |

### 8.10 Favoritos
| Método | Ruta                               | Propósito                  |
|--------|------------------------------------|----------------------------|
| POST   | `/api/favoritos/{codigoInmueble}`  | Toggle favorito            |
| GET    | `/api/favoritos`                   | Listar favoritos           |

### 8.11 Autenticación
| Método | Ruta                       | Propósito                  |
|--------|----------------------------|----------------------------|
| POST   | `/api/auth/registro`       | Registro + recomendaciones |
| POST   | `/api/auth/login`          | Login                      |
| GET    | `/api/usuario/perfil`      | Obtener perfil             |
| PUT    | `/api/usuario/actualizar`  | Actualizar perfil          |

### 8.12 Administración
| Método | Ruta                                          | Propósito                  |
|--------|-----------------------------------------------|----------------------------|
| GET    | `/api/admin/cambios-pendientes`               | Ver cambios pendientes     |
| PUT    | `/api/admin/cambios-pendientes/{id}/aprobar`  | Aprobar cambio             |
| PUT    | `/api/admin/cambios-pendientes/{id}/rechazar` | Rechazar cambio            |

### 8.13 Asesor IA
| Método | Ruta                  | Propósito                  |
|--------|-----------------------|----------------------------|
| POST   | `/api/chat`           | Enviar pregunta al asesor  |
| GET    | `/api/chat/health`    | Health check               |

---

## 9. Frontend

Archivos estáticos en `src/main/resources/public/`:

| Archivo              | Propósito                          |
|----------------------|------------------------------------|
| `landing.html`       | Página de inicio                   |
| `index.html`         | Dashboard principal                |
| `login.html`         | Inicio de sesión                   |
| `registro.html`      | Registro de usuario                |
| `perfil.html`        | Perfil de usuario                  |
| `app.js`             | Lógica frontend (fetch a la API)   |
| `styles.css`         | Estilos visuales                   |
| `chat-widget.html`   | Widget del asesor IA               |
| `chat-widget.js`     | Lógica del chat                    |
| `chat-widget.css`    | Estilos del chat                   |

---

## 10. Seguridad

- **Hash de contraseñas**: SHA-256 (`HashUtils.java`)
- **Roles del sistema**: CLIENTE, VENDEDOR, ADMIN, GERENTE
- **Workflow de aprobación**: Cambios de datos sensibles (nombre, teléfono, dirección, email) de clientes normales requieren aprobación del administrador
- **Protección de inmuebles vendidos**: Solo ADMIN puede modificar inmuebles con estado "Vendido"

---

## 11. Flujo de Inicio de la Aplicación (Main.java)

```
1. ConexionDB.inicializarTablas()    → Crea tablas SQLite si no existen
2. InventarioInmueblesService()      → Carga inmuebles de DB a RAM (Hash + BST)
3. AlertasService()                  → ColaPrioridad con datos semilla
4. ClientesService()                 → Carga clientes de DB a RAM (Hash)
5. OperacionesService()              → Carga operaciones de DB a RAM (Hash + Lista)
6. RecomendacionService()            → Listo para generar recomendaciones
7. DetectorAnomaliesService()        → Listo para detectar anomalías
8. ReporteService()                  → Listo para generar reportes
9. AuthService()                     → Crea admin por defecto si no existe
10. VisitaService()                  → Carga visitas de DB a RAM
11. Asesor IA (AiService)            → Listo si OPENROUTER_API_KEY está configurada
12. Javalin.start(7070)              → Servidor web escuchando
```

---

## 12. Mapeo Estructura de Datos ↔ Servicio

| Estructura              | Servicio que la usa                     | Clave              | Valor            |
|-------------------------|-----------------------------------------|--------------------|------------------|
| `TablaHash<String, Inmueble>`    | InventarioInmueblesService   | código inmueble    | Inmueble         |
| `ArbolBinarioBusqueda<Double, Inmueble>` | InventarioInmueblesService | precio     | Inmueble         |
| `TablaHash<String, Cliente>`     | ClientesService              | identificación     | Cliente          |
| `TablaHash<String, Operacion>`   | OperacionesService           | idOperacion        | Operacion        |
| `ListaEnlazada<Operacion>`       | OperacionesService           | — (historial)      | Operacion        |
| `TablaHash<String, Visita>`      | VisitaService                | idVisita           | Visita           |
| `ColaPrioridad<Alerta>`          | AlertasService               | prioridad          | Alerta           |
| `ColaPrioridad<Visita>`          | GestorVisitasService         | prioridad          | Visita           |
| `Pila<RegistroCambio>`           | HistorialCambiosService      | — (stack)          | RegistroCambio   |
| `Grafo<String>`                  | AnalisisRelacionesService    | — (vértices)       | idCliente/idInmueble |

---

## 13. Pruebas

Los tests unitarios cubren tres niveles:

- `EstructurasTest` — Pruebas de todas las estructuras de datos
- `ServiciosTest` — Pruebas de servicios de negocio
- `DatabaseTest` / `FullTest` — Pruebas de persistencia y flujo completo

---

## 14. Cómo se construiría desde 0 (Plan de Implementación)

### Fase 1: Estructuras de Datos
1. `Nodo<T>` — Clase base genérica
2. `ListaEnlazada<T>` — Insertar, obtener, remover
3. `Pila<T>` — Apilar, desapilar
4. `Cola<T>` — Encolar, desencolar
5. `ColaPrioridad<T>` + `NodoPrioridad<T>`
6. `EntradaHash<K,V>` + `TablaHash<K,V>` con encadenamiento
7. `NodoArbol<K,V>` + `ArbolBinarioBusqueda<K,V>`
8. `Vertice<T>` + `Grafo<T>` con listas de adyacencia

### Fase 2: Modelo de Dominio
9. Clases `Inmueble`, `Cliente`, `Asesor`, `Operacion`, `Visita`, `Alerta`, `Usuario`, `Rol`

### Fase 3: Persistencia
10. `HashUtils.java` — SHA-256
11. `ConexionDB.java` — SQLite embebido
12. `InmuebleDAO`, `ClienteDAO`, `AsesorDAO`, `OperacionDAO`, `VisitaDAO`, `FavoritoDAO`, `UsuarioDAO`

### Fase 4: Servicios
13. `InventarioInmueblesService` — CRUD + caché en RAM
14. `ClientesService` — CRUD + TablaHash
15. `OperacionesService` — CRUD + actualización de estados
16. `AlertasService` — ColaPrioridad + generación automática
17. `VisitaService` — Programación y gestión de visitas
18. `RecomendacionService` — Sistema de puntuación multicriterio
19. `DetectorAnomaliesService` — 4 tipos de anomalías
20. `ReporteService` — 6 tipos de reportes
21. `AuthService` — Registro, login, cambios pendientes
22. `HistorialCambiosService` — Pila para deshacer
23. `AnalisisRelacionesService` — Grafo + filtrado colaborativo
24. `GestorVisitasService` — ColaPrioridad de visitas
25. `HistorialYFavoritosService` — Consultas y favoritos
26. `AsesorService` + `AiService` — Chat con OpenRouter

### Fase 5: API REST
27. `Main.java` — Servidor Javalin con todos los endpoints

### Fase 6: Frontend
28. HTML, CSS, JS estáticos + widget de chat

### Fase 7: Pruebas
29. Tests unitarios para estructuras, servicios y DAOs
