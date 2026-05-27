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
- [x] **CRUD completo de clientes** — Puede registrar, modificar y eliminar clientes. ✅ Implementado
- [x] **Gestionar visitas** — Ver todas las visitas asignadas a él, confirmar/realizar/cancelar. ✅ Implementado
- [x] **Registrar operaciones** — Crear operaciones de Venta/Arriendo y asociarlas a un cliente e inmueble. ✅ Implementado
- [x] **Ver reportes de rendimiento** — Accede a Reportes, pero solo ve los suyos propios (operaciones que él cerró, visitas que atendió). ✅ Implementado
- [x] **Dashboard personal** — Tarjeta con sus métricas: operaciones cerradas, visitas atendidas, calificación. ✅ Implementado

### 3. Admin (rol `admin`)
- [ ] **Acceso total a todo el sistema** — CRUD de inmuebles, clientes, operaciones, visitas, asesores.
- [ ] **Ver todos los reportes** — Sin filtro por asesor. Ve el negocio completo.
- [ ] **Gestión de asesores** — Alta/baja/modificación de asesores.
- [ ] **Gestión de usuarios** — Ver lista de usuarios registrados, cambiar roles.
- [ ] **Panel de anomalías** — Detección de visitas sin cierre, sobrecarga de asesores, cambios de precio frecuentes.

---

## Próximos pasos técnicos (backend)
- [ ] Agregar tabla `roles` con permisos específicos (ya existe en `UsuarioDAO`).
- [ ] Agregar middleware de autorización en las rutas de Javalin (verificar rol antes de cada endpoint).
- [ ] Crear endpoint `GET /api/usuario/rol` para que el front sepa qué mostrar.
- [ ] Mover la lógica de agendar visita a un endpoint con verificación de rol.

## Próximos pasos técnicos (frontend)
- [ ] Leer el rol del usuario desde `localStorage` al cargar la app.
- [ ] Ocultar/mostrar secciones del navbar según el rol.
- [ ] Mostrar botón "Agendar Visita" solo para clientes en el detalle del inmueble.
- [ ] Mostar "Registrar Inmueble/Cliente/Operación" solo para asesores y admins.

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
