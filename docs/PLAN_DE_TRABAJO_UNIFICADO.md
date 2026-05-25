# Plan de Trabajo Unificado - Proyecto PropTech

## Fase 1: Evaluación y Preparación (Semana 1)

### 1.1 Auditoría de Estado Actual
- [x] Revisar código existente contra requisitos documentados
- [x] Verificar funcionamiento de:
  - Gestión de inmuebles (InmuebleDAO, Inmueble)
  - Gestión de clientes (ClienteDAO, Cliente, ClientesService)
  - Gestión de asesores (Asesor, verificar si tiene servicio completo)
  - Estructuras de datos personalizadas (listas, pilas, colas, árboles, grafos, hash)
- [x] Ejecutar pruebas existentes: `mvn test` en utilidades/
- [x] Documentar brechas entre requisitos y estado actual

### 1.2 Análisis de Requisitos Pendientes
Basado en el documento de requisitos proporcionado, identificar módulos faltantes o incompletos:
- [x] Programación de visitas completas (VisitaService, endpoints)
- [x] Historial de interés y favoritos (relaciones cliente-inmueble) - **IMPLEMENTADO**
- [x] Operaciones de negocio (OperacionService para arriendo/venta) - **IMPLEMENTADO**
- [x] Alertas automáticas (AlertasService mejorado) - **IMPLEMENTADO**
- [x] Sistema de recomendación - **IMPLEMENTADO**
- [x] Detección de comportamientos inusuales - **IMPLEMENTADO**
- [x] Reportes y análisis avanzados - **IMPLEMENTADO**

### 1.3 Establecimiento de Baseline
- [x] Crear backup del estado actual: `git tag fase1-baseline`
- [x] Verificar que la aplicación compile y arranque: `mvn spring-boot:run` (o equivalente Javalin)
- [x] Probar endpoints básicos existentes (clientes desde plan de búsqueda)

## Fase 2: Implementación Incremental (Semanas 2-4)

### 2.1 Capa de Servicios y Lógica de Negocio
#### 2.1.1 Gestión de Visitas
- [x] Crear VisitaService.java siguiendo patrón de AlertasService/OperacionesService
- [x] Implementar CRUD completo para visitas
- [x] Añadir validaciones de disponibilidad (inmueble y asesor)
- [x] Implementar estados: pendiente, confirmada, realizada, cancelada, reprogramada
- [x] Añadir métodos para reprogramar y cancelar visitas

#### 2.1.2 Operaciones de Negocio
- [x] Crear OperacionService.java (extendiendo lo existente)
- [x] Implementar registro de arriendo, venta, renovación, cancelación
- [x] Añadir lógica de comisión y valor acordado
- [x] Implementar consultas por tipo de operación, fecha, inmueble/cliente

#### 2.1.3 Historial y Favoritos
- [x] Extender Cliente para incluir listas de favoritos e historial
- [x] Crear servicio de gestión de historial (visitas, consultas, favoritos)
- [x] Implementar métodos para marcar/desmarcar favoritos
- [x] Añadir tracking de inmuebles consultados

#### 2.1.4 Sistema de Alertas
- [x] Mejorar AlertasService existente con:
  - Alertas de contratos próximos a vencer
  - Inmuebles sin visitas en mucho tiempo
  - Propiedades con alta demanda
  - Visitas pendientes por confirmar
- [x] Implementar scheduler para generación periódica de alertas
- [x] Añadir niveles de prioridad (baja, media, alta)

#### 2.1.5 Recomendación de Inmuebles
- [x] Crear RecomendacionService.java
- [x] Implementar algoritmo basado en:
  - Presupuesto, zona, tipo, habitaciones
  - Historial de consultas y visitas
  - Propiedades similares visitadas
- [x] Añadir endpoint `/api/recomendaciones/{clienteId}`

#### 2.1.6 Detección de Comportamientos Inusuales
- [x] Crear DetectorAnomaliesService.java
- [x] Implementar detección de:
  - Visitas altas sin cierre
  - Múltiples visitas cortas sin continuidad
  - Sobrecarga de asesores
  - Cambios frecuentes de precio
  - Concentración de interés geográfica
- [x] Integrar con sistema de alertas

#### 2.1.7 Reportes y Análisis Avanzados
- [x] Crear ReporteService.java
- [x] Implementar reportes de:
  - Rendimiento de inmuebles
  - Desempeño de asesores
  - Tendencias de precios por zona
  - Clientes activos
  - Tipos de operación

### 2.2 Capa de Acceso a Datos
- [x] Extender DAOs según necesiten nuevos campos o relaciones
- [x] Añadir índices en SQLite para consultas frecuentes (ej: zonas, precios)
- [x] Implementar consultas complejas para reportes y análisis
- [x] Verificar transaccionalidad donde sea necesario (operaciones + actualizaciones)

### 2.3 Capa de Presentación (Frontend)
- [x] Extender app.js con funciones para nuevos módulos
- [x] Actualizar index.html con secciones para:
  - Gestión de visitas
  - Registro de operaciones
  - Visualización de alertas
  - Sistema de recomendaciones
  - Reportes de anomalías
- [x] Mejorar estilos.css para nuevos componentes
- [x] Implementar validaciones frontend básicas

## Fase 3: Integración, Validación y Documentación (Semana 5)

### 3.1 Pruebas de Integración
- [ ] Crear pruebas de flujo completo para casos de uso clave:
  - Cliente busca inmueble → agenda visita → realiza operación
  - Sistema genera alerta basada en comportamiento
  - Recomendación basada en historial
- [x] Probar APIs REST con curl o Postman
- [ ] Verificar rendimiento de búsquedas críticas (<100ms)

### 3.2 Garantía de Calidad
- [ ] Escribir pruebas unitarias para nuevos servicios (80% cobertura objetivo)
- [ ] Ejecutar linter y formateador de código
- [x] Revisar que no se rompiera funcionalidad existente
- [x] Validar uso adecuado de estructuras de datos:
  - Listas para historiales
  - Pilas para deshacer operaciones
  - Colas para visitas pendientes
  - Colas de prioridad para alertas urgentes
  - Hash para búsquedas O(1) por ID
  - Árboles para rangos de precio/área
  - Grafos para análisis de relaciones

### 3.3 Actualización de Documentación
- [ ] Actualizar README.md con instrucciones de uso
- [ ] Documentar nuevos endpoints API en docs/
- [ ] Justificar técnica de cada estructura de datos añadida
- [ ] Incluir diagramas de secuencia para flujos complejos
- [ ] Actualizar Proyecto_Final_estructura.pdf si corresponde

### 3.4 Entrega Final
- [ ] Crear release tag: `git tag release-v1.0-completo`
- [x] Generar archivo JAR ejecutable: `mvn package`
- [ ] Preparar informe final con:
  - Problema resuelto
  - Solución implementada
  - Justificación de estructuras de datos usadas
  - Resultados de pruebas
  - Lecciones aprendidas

## Principios de Conservación
- [x] No modificar lógica probada sin necesidad
- [x] Extender en lugar de reemplazar cuando sea posible
- [x] Mantener compatibilidad hacia atrás en APIs
- [x] Preservar estilos y patrones de código existentes
- [x] Reutilizar componentes frontend ya desarrollados
- [x] Aprovechar estructuras de datos personalizadas ya implementadas

---
*Plan generado para integrar con requisitos existentes mientras se agregan funcionalidades faltantes del proyecto PropTech*

## 📋 Modelado de tareas pendientes.

### Frontend - Mejoras pendientes
1.1 Refactorizar sección de Reportes:
- [x] Eliminar buscador inteligente de la sección de reportes
- [x] Implementar búsqueda de reportes por filtros (fecha, nombre, tipo)
- [x] Mejorar visualización de datos en reportes
- [x] Añadir exportación de reportes (PDF/Excel)

1.2 Otras mejoras frontend:
- [x] Validaciones adicionales en formularios
- [x] Mejoras de UX en modales (toast notifications)
- [x] Mensajes de error más descriptivos

### Backend - Mejoras pendientes
2.1 Reportes y APIs:
- [x] Añadir más endpoints para reportes personalizados (rendimiento filtrado)
- [x] Implementar filtros avanzados en APIs
- [x] Mejorar manejo de errores y excepciones

### Sistema de Autenticación y Roles
2.1.5 Implementar autenticación y roles:
- [x] Crear Rol.java (roles: CLIENTE, VENDEDOR, ADMIN, GERENTE)
- [x] Crear Usuario.java con password_hash y rol_id
- [x] Crear UsuarioDAO.java con tablas roles/usuarios en SQLite
- [x] Crear AuthService.java con registro/login
- [x] Crear HashUtils.java para SHA-256 de contraseñas
- [x] Crear login.html con tema PropTech
- [x] Crear registro.html con tema PropTech
- [x] Registro público: siempre asignar rol_id = CLIENTE por defecto
- [x] Implementar endpoints: POST /api/auth/registro, POST /api/auth/login


#### Landing Page
**Objetivo:** Captar leads y generar registros

**Estructura propuesta:**
- **Título:** "Encuentra tu propiedad ideal con PropTech"
- **Subtítulo:** "La plataforma inteligente que conecta compradores y vendedores con recomendaciones personalizadas"
- **Beneficios:**
  1. Recomendaciones inteligentes basadas en tus preferencias
  2. Historial y favoritos para comparar propiedades
  3. Análisis de mercado en tiempo real por zona
- **Prueba social:** "Más de 500 usuarios confían en PropTech"
- **CTA:** "Regístrate gratis y recibe tus primeras 3 recomendaciones"

**Tareas:**
- [x] Crear landing.html con tema PropTech
- [x] Hero section con gradientes PropTech y título persuasivo
- [x] Sección de características del sistema
- [x] Testimonios y prueba social
- [x] Formulario de captura de leads
- [x] Diseño responsive
- [x] Variantes A/B de título para pruebas:
  1. "Tu hogar ideal te está esperando - Descubre propiedades inteligentes"
  2. "PropTech: La forma más inteligente de encontrar propiedades"
  3. "Conecta con tu próximo hogar mediante tecnología predictiva"
- [x] Testimonio simulado: "PropTech me encontró una casa en mi zona favorita justo dentro de mi presupuesto"
- [x] Envío de 3 recomendaciones al registrarse
- [x] Simulación de correo con recomendaciones
- [x] Redirección de raíz "/" a landing.html en Main.java

### Perfil de Usuario
**Objetivo:** Gestión completa del perfil de usuario con foto y cierre de sesión

**Tareas:**
- [x] Ampliar Usuario.java con campos: telefono, direccion, intereses, fotoPerfil
- [x] Actualizar UsuarioDAO.java con nuevos campos y método actualizar()
- [x] Actualizar tabla usuarios en SQLite con nuevas columnas
- [x] Agregar métodos en AuthService: obtenerPerfil(), actualizarPerfil()
- [x] Crear perfil.html con tema PropTech
- [x] Menú desplegable en header (arriba a la derecha)
- [x] Endpoint GET /api/usuario/perfil
- [x] Endpoint PUT /api/usuario/actualizar
- [x] Subir foto de perfil (almacenamiento local)
- [x] Editar nombre, teléfono, correo, dirección, intereses
- [x] No permitir cambio de ID
- [x] Opción "Cerrar sesión" que redirige a landing.html
- [x] Persistir sesión en localStorage

### Corrección de bugs y unificación de estilos
**Objetivo:** Corregir errores y unificar la identidad visual

**Tareas:**
- [x] Corregir error al acceder a "Mi Perfil" desde menú (cierra sesión inesperadamente) - Se cambió a redirección directa a landing.html con localStorage.removeItem en onclick
- [x] Unificar estilos landing.html con index.html (gradiente #1a1a2e → #16213e → #0f3460)
- [x] Unificar estilos login.html con index.html
- [x] Unificar estilos registro.html con index.html
- [x] Agregar Sticky Header en landing.html con botones login/registro
- [x] Agregar Ticker/Marquee con CSS Animations debajo del header con mensaje "Descubre el mejor inmueble para ti"
- [x] Cambiar nombre frontend de "PropTech" a "Insignia Inmo" (ver docs para nombre del proyecto)

## Nota sobre el nombre del proyecto
**Insignia Inmo** es el nombre comercial del proyecto. El backend mantiene referencias a "PropTech" para compatibilidad técnica, pero la interfaz de usuario muestra "Insignia Inmo".
*(Esta sección será actualizada conforme se identifiquen nuevas tareas)*