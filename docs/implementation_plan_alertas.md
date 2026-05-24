# Plan de Implementación: Sistema de Alertas (Paso 3)

Según los requisitos de la terminal y las acciones sugeridas, vamos a implementar un Sistema de Alertas utilizando nuestra estructura de datos `ColaPrioridad`. Este sistema permitirá notificar eventos críticos (como contratos próximos a vencer, visitas VIP, o revisiones urgentes) dando prioridad de atención según la urgencia.

## 1. Modelo `Alerta` (`com.proptech.modelo.Alerta`)
Crearemos una clase para representar las notificaciones dentro del sistema.
- **Atributos:**
  - `String idAlerta`
  - `String mensaje` (Ej: "Contrato de arriendo del Inmueble WEB-001 vence en 3 días")
  - `int prioridad` (1 = Urgente, 2 = Alta, 3 = Media, etc.)
  - `String fechaCreacion`
- **Funcionalidad:** Getters, Setters y constructor.

## 2. Servicio `AlertasService` (`com.proptech.servicio.AlertasService`)
Desarrollaremos el servicio que gestionará las alertas, cumpliendo con el uso obligatorio de `ColaPrioridad`.
- **Estructura:** `private ColaPrioridad<Alerta> colaAlertas;`
- **Métodos:**
  - `agregarAlerta(Alerta alerta)`: Encola la alerta utilizando el valor de `prioridad` extraído del objeto.
  - `atenderSiguienteAlerta()`: Extrae (`desencolar`) y devuelve la alerta más urgente.
  - `hayAlertasPendientes()`: Verifica si la cola está vacía.

## User Review Required

> [!IMPORTANT]
> - Para este sistema, el número **1** representará la prioridad más alta (urgente), mientras que números mayores (como el 5) serán de prioridad baja. ¿Estás de acuerdo con este enfoque?
> - Las alertas en un futuro se podrán integrar con el Frontend (un icono de "campanita" 🔔). Por ahora, implementaremos la lógica del negocio en el Backend para poder encolar y desencolar. ¿Te parece bien?

## Verification Plan
1. Se crearán los archivos `Alerta.java` y `AlertasService.java`.
2. Se actualizará `Main.java` (opcionalmente) o se creará un endpoint de prueba `/api/alertas` que permita inyectar y ver alertas.
3. Se verificará que el código compile y el servidor de Javalin reinicie correctamente sin errores.
