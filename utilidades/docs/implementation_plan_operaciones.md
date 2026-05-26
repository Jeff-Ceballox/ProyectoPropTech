# Plan de Implementación: Gestión de Operaciones y Frontend (Punto 4.2 / 4.6)

De acuerdo con las sugerencias para avanzar en el desarrollo (Gestión de Operaciones) y tu solicitud de actualizar el frontend para ir visualizando los cambios en el localhost, propongo el siguiente plan de acción:

## 1. Actualización del Frontend (Localhost)
Para aprovechar los nuevos atributos modernos (habitaciones, baños, parqueadero, descripción) que agregamos al `Inmueble`, vamos a modificar la interfaz gráfica:
- **`app.js`**: Actualizaremos la función `cargarInmuebles()` para que inyecte los nuevos datos en las tarjetas HTML (mostrando iconos para camas, baños, y parqueadero).
- **`index.html`**: Agregaremos enlaces en el menú superior o ajustaremos la maqueta si es necesario para dar paso a las Operaciones.

## 2. Modelo `Operacion` (`com.proptech.modelo.Operacion`)
Crearemos una nueva clase que representará un contrato de venta o arriendo.
- **Atributos**:
  - `String idOperacion`
  - `String tipo` (Ej: "Venta", "Arriendo")
  - `Inmueble inmueble`
  - `Cliente cliente`
  - `Asesor asesor`
  - `double monto`
  - `String fecha`
- **Funcionalidad**: Getters, Setters y constructor.

## 3. Servicio `OperacionesService` (`com.proptech.servicio.OperacionesService`)
Crearemos el servicio que gestionará estos contratos utilizando la estructura de datos obligatoria.
- **Estructura**: Utilizará una `ListaEnlazada<Operacion>` (cumpliendo con el requerimiento de uso de Listas para historiales de contratos).
- **Métodos**:
  - `registrarOperacion(Operacion operacion)`: Guarda la operación, cambia el estado del Inmueble a "Vendido" o "Arrendado", e incrementa el contador de ventas del Asesor.
  - `obtenerTodas()`: Retorna la lista de operaciones.

## User Review Required

> [!IMPORTANT]
> - ¿Consideras que la clase `Operacion` debería tener algún atributo adicional, como "Comisión del Asesor" o "Estado del Contrato"?
> - He asumido que el "paso 4.2" se refiere a la **Gestión de Operaciones** (que en algunas tablas figuraba como 4.6 pero era la segunda acción sugerida). Si te referías a otro requerimiento exacto, por favor confírmamelo.
> - El backend ya está corriendo en tu localhost (puerto 7070). Una vez aprobemos el plan, haré los cambios en el Frontend para que recargues la página y veas la nueva interfaz.

## Verification Plan
1. Se crearán los archivos `Operacion.java` y `OperacionesService.java`.
2. Se modificará `app.js` para renderizar los nuevos campos de Inmueble.
3. El proyecto se compilará y el backend en ejecución servirá automáticamente el HTML y JS actualizados, permitiéndote verlo en `http://localhost:7070/`.
