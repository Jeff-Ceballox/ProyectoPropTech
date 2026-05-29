# Plan de Cambio: Roles y Permisos por Perfil

## Objetivo
Asignar funciones específicas a cada rol (Cliente, Asesor, Admin) y reflejarlas en el frontend según el usuario autenticado.

---

## Lista de Tareas (pendientes de aprobación)

### 1. Cliente (rol `cliente`)
- [x] **Agendar cita para ver un inmueble** — Desde la tarjeta de detalle del inmueble, el cliente puede seleccionar fecha/hora y solicitar una visita. Queda registrada en `visitas` con estado `Pendiente`. Si el cliente no existe en la tabla `clientes`, se crea automáticamente al agendar. ✅ Implementado
- [x] **Ver historial de sus visitas** — Sección "Mis Visitas" donde el cliente ve solo sus propias citas (pendientes/realizadas/canceladas). ✅ Implementado
- [x] **Cancelar una cita** — El cliente puede cancelar una visita pendiente desde "Mis Visitas". Al cancelar, el horario queda disponible nuevamente para agendar. ✅ Implementado
- [x] **Agregar/quitar inmuebles de favoritos** — Botón ❤️ en cada tarjeta de inmueble. Persistir en la tabla `favoritos`. ✅ Implementado
- [x] **Ver solo su perfil** — El cliente puede editar su nombre, teléfono, email, presupuesto. No ve datos de otros clientes. ✅ Implementado
- [x] **No puede ver operaciones ni reportes** — Las secciones "Operaciones" y "Reportes" se ocultan del navbar. ✅ Implementado
- [x] **Subir foto de perfil** — En "Mi Perfil", el cliente puede subir una imagen que se guarda como base64 en la BD y se muestra como avatar. ✅ Implementado
- [x] **Edición de datos con aprobación de admin** — Todos los datos del cliente (nombre, teléfono, email, dirección) son editables, pero los cambios quedan pendientes de aprobación por un administrador. ✅ Implementado
- [x] **Intereses sin aprobación** — El campo "intereses" se actualiza al instante sin necesidad de aprobación de administrador. ✅ Implementado
- [x] **Botón "Guardar cambios" → "Volver"** — Al guardar cambios exitosamente, el botón cambia a "Volver a inmuebles" (o "Volver a página principal"). Al salir y volver a entrar a "Mi Perfil", el botón vuelve a mostrar "Guardar cambios". ✅ Implementado

### 2. Asesor (rol `asesor`)
- [x] **CRUD completo de inmuebles** — Puede crear, editar y eliminar propiedades. ✅ Implementado
- [x] **Cargar imágenes a un inmueble** — Al crear o editar un inmueble, el asesor puede agregar una o varias imágenes. Se almacenan como base64 y se muestran en la galería del detalle del inmueble. ✅ Implementado
- [x] **CRUD completo de clientes** — Puede registrar, modificar y eliminar clientes. ✅ Implementado
- [x] **Gestionar visitas** — Ver todas las visitas asignadas a él, confirmar/realizar/cancelar. ✅ Implementado
- [x] **Registrar operaciones** — Crear operaciones de Venta/Arriendo y asociarlas a un cliente e inmueble. ✅ Implementado
- [x] **Ver reportes de rendimiento** — Accede a Reportes, pero solo ve los suyos propios (operaciones que él cerró, visitas que atendió). ✅ Implementado (Dashboard personal vía `/api/asesor/dashboard`)
- [x] **Dashboard personal** — Tarjeta con sus métricas: operaciones cerradas, visitas atendidas, calificación. ✅ Implementado

### 3. Admin (rol `admin`)
- [x] **Acceso total a todo el sistema** — CRUD de inmuebles, clientes, operaciones, visitas, asesores. ✅ Implementado
- [x] **Cargar imágenes a un inmueble** — Ídem al asesor: puede agregar n imágenes a cualquier inmueble. ✅ Implementado
- [x] **Ver todos los reportes** — Sin filtro por asesor. Ve el negocio completo. ✅ Implementado
- [x] **Gestión de asesores** — Alta/baja/modificación de asesores. ✅ Implementado
- [x] **Gestión de usuarios** — Ver lista de usuarios registrados, cambiar roles. ✅ Implementado
- [x] **Panel de anomalías** — Detección de visitas sin cierre, sobrecarga de asesores, cambios de precio frecuentes. ✅ Implementado (endpoints listos, falta tarjeta en frontend admin)

### 4. Inmuebles — Gestión de Imágenes
- [x] **Agregar tabla `inmueble_imagenes` en SQLite** — Columnas: `id INTEGER PRIMARY KEY AUTOINCREMENT`, `codigo_inmueble TEXT NOT NULL`, `imagen_base64 TEXT NOT NULL`, `orden INTEGER DEFAULT 0`, `fecha_subida TEXT`, con FOREIGN KEY a `inmuebles(codigo)`. ✅ Implementado
- [x] **Modelo `ImagenInmueble.java`** — Clase simple con id, codigoInmueble, imagenBase64, orden, fechaSubida. ✅ Implementado
- [x] **DAO `ImagenInmuebleDAO.java`** — `guardar()`, `obtenerPorInmueble(codigo)`, `eliminar(id)`, `eliminarTodasPorInmueble(codigo)`. ✅ Implementado
- [x] **Endpoint `POST /api/inmuebles/{codigo}/imagenes`** — Recibe un JSON con un arreglo de imágenes (base64) y las persiste en la BD. Solo accesible para asesor/admin. ✅ Implementado
- [x] **Endpoint `GET /api/inmuebles/{codigo}/imagenes`** — Retorna todas las imágenes de un inmueble como arreglo de base64 (o URLs). Público (cualquier rol puede verlas). ✅ Implementado
- [x] **Endpoint `DELETE /api/inmuebles/{codigo}/imagenes/{idImagen}`** — Elimina una imagen específica. Solo asesor/admin. ✅ Implementado
- [x] **Carga en lote** — Al crear un inmueble vía `POST /api/inmuebles`, aceptar un campo opcional `imagenes: string[]` (base64) y persistirlas automáticamente en la tabla de imágenes. ✅ Implementado
- [x] **Galería en frontend** — En el detalle del inmueble (modal/tarjeta), mostrar un carrusel o galería con las imágenes disponibles. Si no hay imágenes, mostrar un placeholder. ✅ Implementado
- [x] **Subida múltiple en frontend** — En el formulario de crear/editar inmueble, agregar un selector de archivos múltiple (`<input type="file" multiple accept="image/*">`), convertir a base64 en el cliente y enviarlas junto con los datos del inmueble. ✅ Implementado
- [x] **Previsualización** — Al seleccionar imágenes en el formulario, mostrar miniaturas de previsualización antes de enviar el formulario. ✅ Implementado
- [x] **Límite de imágenes** — Definir un máximo configurable (ej: 10 imágenes por inmueble) y validarlo tanto en frontend como en backend. ✅ Implementado

---

## Próximos pasos técnicos (backend)
- [ ] Agregar tabla `roles` con permisos específicos (ya existe en `UsuarioDAO`).
- [ ] Agregar middleware de autorización en las rutas de Javalin (verificar rol antes de cada endpoint).
- [ ] Crear endpoint `GET /api/usuario/rol` para que el front sepa qué mostrar.
- [ ] Mover la lógica de agendar visita a un endpoint con verificación de rol.
- [ ] **Nuevo endpoint `POST /api/inmuebles/{codigo}/imagenes`** — Subir una o varias imágenes en base64 (body: `{ "imagenes": ["data:image/png;base64,...", ...] }`). Solo permitido para roles `admin` y `vendedor`. Validar máximo 10 imágenes y que el inmueble exista.
- [ ] **Nuevo endpoint `GET /api/inmuebles/{codigo}/imagenes`** — Retorna arreglo de objetos `{ id, imagenBase64, orden }`. Accesible por cualquier rol autenticado.
- [ ] **Nuevo endpoint `DELETE /api/inmuebles/{codigo}/imagenes/{idImagen}`** — Eliminar una imagen por ID. Solo `admin` y `vendedor`.
- [ ] **Nuevo DAO `ImagenInmuebleDAO`** — CRUD contra tabla `inmueble_imagenes`.
- [ ] **Validar tamaño de imagen** — Rechazar imágenes mayores a 5 MB antes de convertir a base64 y persistir.

## Próximos pasos técnicos (frontend)
- [ ] Leer el rol del usuario desde `localStorage` al cargar la app.
- [ ] Ocultar/mostrar secciones del navbar según el rol.
- [ ] Mostrar botón "Agendar Visita" solo para clientes en el detalle del inmueble.
- [ ] Mostar "Registrar Inmueble/Cliente/Operación" solo para asesores y admins.
- [ ] **Subida múltiple de imágenes en formulario de inmueble** — En el formulario de crear/editar inmueble, agregar `<input type="file" multiple accept="image/*">` que convierta las fotos a base64 en el cliente y las envíe junto con el POST/PUT. Solo visible si el rol es `admin` o `vendedor`.
- [ ] **Previsualización de imágenes** — Antes de enviar, mostrar miniaturas de las imágenes seleccionadas con botón "X" para quitar cada una.
- [ ] **Galería de imágenes en detalle del inmueble** — En el modal de detalle, si el inmueble tiene imágenes, mostrar un carrusel simple con flechas anterior/siguiente o miniaturas clickeables. Si no tiene imágenes, mostrar placeholder genérico.
- [ ] **Eliminar imágenes desde frontend** — Botón "Eliminar" en cada imagen de la galería, visible solo para admin/asesor, que llame al endpoint DELETE correspondiente.
- [ ] **Límite visual de subida** — Mostrar contador "3/10 imágenes" en el formulario y deshabilitar el input al alcanzar el máximo.

---

## Notas
- El seed data ya crea 2 asesores (`ASESOR-001`, `ASESOR-002`).
- El registro de usuarios (`/api/auth/registro`) ya asigna rol `cliente` por defecto.
- En `UsuarioDAO` ya existe el soporte para tabla `roles` y asignación de rol al usuario.

---

## Changelog

### 26/05/2026 — Implementación inicial de visitas (tarea 1)
- **POST /api/visitas** — Crea una visita. Si el email del cliente no existe en `clientes`, se crea automáticamente con ID `CLI-{timestamp}`.
- **Validación de 2 horas**: Antes de crear una visita, se verifica que no exista otra visita activa (no cancelada) para el mismo inmueble con diferencia menor a 2 horas. Si existe, responde `409 Conflict`.
- **PUT /api/visitas/{id}/cancelar** — Cambia el estado de la visita a `"Cancelada"`. Al cancelar, el horario queda disponible para nuevas agendas (la validación ignora visitas canceladas).
- **GET /api/visitas/cliente/{email}** — Devuelve todas las visitas de un cliente, incluyendo datos del inmueble y asesor asignado.
- **Frontend**: Botón "Agendar Visita" en el modal de detalle del inmueble (solo visible si hay sesión iniciada). Modal con selector `datetime-local`. Sección "Mis Visitas" en el menú de perfil con lista de visitas y botón "Cancelar Visita" para las pendientes.
