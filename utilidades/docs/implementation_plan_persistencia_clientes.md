# Plan de Implementación: Persistencia de Clientes (Paso 4)

De acuerdo con el plan trazado en la terminal, el siguiente paso es asegurar que la información de los Clientes no se pierda al reiniciar el servidor. Actualmente, los inmuebles persisten en SQLite (`InmuebleDAO`), pero los clientes no.

## 1. Actualización de Base de Datos (`com.proptech.dao.ConexionDB`)
Modificaremos la función `inicializarTablas()` para que también genere la tabla `clientes` si esta no existe.
- **Columnas**:
  - `identificacion` (TEXT PRIMARY KEY)
  - `nombre` (TEXT NOT NULL)
  - `telefono` (TEXT)
  - `presupuestoMaximo` (REAL)
  - `email` (TEXT)

## 2. Creación de `ClienteDAO` (`com.proptech.dao.ClienteDAO`)
Crearemos una nueva clase que actúe como puente entre el modelo `Cliente` y la base de datos SQLite.
- **Método `guardar(Cliente cliente)`**: Insertará (o reemplazará) un cliente en la base de datos.
- **Método `obtenerTodos()`**: Ejecutará un `SELECT * FROM clientes`, convirtiendo cada registro en un objeto `Cliente` y retornando una `ListaEnlazada<Cliente>`.

## 3. Integración Básica (`Main.java`)
Para probar el sistema, inicializaremos el proceso en `Main.java`:
- Al arrancar, leeremos los clientes de la base de datos.
- Agregaremos un cliente de prueba si la base de datos de clientes está vacía, demostrando su correcto guardado permanente.

## User Review Required

> [!IMPORTANT]
> - Por el momento, este paso **sólo persiste los datos básicos** del cliente. El historial de consultas y sus inmuebles favoritos (que son listas) no se persistirán de inmediato en bases de datos relacionales, ya que primero queremos asegurar la estructura principal (Paso 4) y luego refactorizar búsquedas (Paso 5). ¿Estás de acuerdo con avanzar con esta persistencia básica?
> - Al terminar esto, tu sistema tendrá su base de datos 100% robusta tanto para Inmuebles como para Clientes.

## Verification Plan
1. Se actualizará `ConexionDB.java`.
2. Se creará `ClienteDAO.java`.
3. Se agregará código en `Main.java` para crear un par de clientes al vuelo y guardarlos.
4. Se reiniciará el servidor y se comprobará la consola para ver los mensajes de "Cliente guardado permanentemente en DB" y "X clientes cargados en memoria".
