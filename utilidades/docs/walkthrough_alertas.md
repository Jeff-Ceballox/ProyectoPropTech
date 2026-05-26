# Resumen de Cambios: Sistema de Alertas

He implementado el Sistema de Alertas utilizando la estructura de datos obligatoria `ColaPrioridad`, cumpliendo así con el requerimiento 4.7 y 5.4 sugeridos por el análisis del proyecto.

## Modificaciones Realizadas

### 1. Modelo `Alerta`
- Se creó la clase `com.proptech.modelo.Alerta`.
- Atributos incluidos: `idAlerta`, `mensaje`, `prioridad`, `fechaCreacion`.
- Importancia de la Prioridad: Un valor de `1` representa una alerta crítica/urgente (ej. contratos a punto de vencer), mientras que valores mayores representan alertas regulares (ej. mantenimientos o notificaciones generales).

### 2. Servicio `AlertasService`
- Creado en `com.proptech.servicio.AlertasService`.
- **Estructura Interna:** Hace uso de `ColaPrioridad<Alerta>` para asegurar que el orden de extracción sea basado estrictamente en el valor de urgencia, ignorando el orden de llegada.
- **Funciones Principales:** `agregarAlerta()`, `atenderSiguienteAlerta()`, y `hayAlertasPendientes()`.

### 3. Integración en `Main.java`
- Se instanció el `AlertasService` dentro del hilo principal al arrancar el programa.
- **Simulación:** Se añadieron 3 alertas iniciales de prueba con distintas prioridades.
- **Endpoint de Prueba:** Se expuso la ruta `GET /api/alertas/siguiente`. Al visitarla, extrae la alerta más urgente de la cola de prioridad y la devuelve en formato JSON.

## ¿Cómo Probarlo?
Dado que el servidor ya fue reiniciado, puedes probar este nuevo sistema de alertas abriendo en tu navegador la siguiente dirección:

👉 `http://localhost:7070/api/alertas/siguiente`

Cada vez que recargues esta página, la cola prioritaria "desencolará" la siguiente alerta más importante. Primero verás la prioridad 1, luego la 2 y finalmente la 5, demostrando que la estructura funciona a la perfección.
