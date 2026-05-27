package com.proptech;

import com.proptech.dao.ConexionDB;
import com.proptech.modelo.Inmueble;
import com.proptech.modelo.Cliente;
import com.proptech.modelo.Operacion;
import com.proptech.modelo.Alerta;
import com.proptech.modelo.Asesor;
import com.proptech.utilidades.estructuras.ListaEnlazada;

import com.proptech.servicio.InventarioInmueblesService;
import com.proptech.servicio.ClientesService;
import com.proptech.servicio.AlertasService;
import com.proptech.servicio.OperacionesService;
import com.proptech.servicio.RecomendacionService;
import com.proptech.servicio.DetectorAnomaliesService;
import com.proptech.servicio.ReporteService;
import com.proptech.servicio.AuthService;
import com.proptech.servicio.VisitaService;
import com.proptech.servicio.asesor.AiService;
import com.proptech.servicio.asesor.AsesorService;
import com.proptech.servicio.asesor.ChatRequest;
import com.proptech.servicio.asesor.ChatResponse;
import com.proptech.modelo.Usuario;
import com.proptech.modelo.Visita;
import com.proptech.dao.ClienteDAO;
import com.proptech.dao.InmuebleDAO;
import com.proptech.dao.AsesorDAO;
import com.proptech.dao.FavoritoDAO;
import com.proptech.dao.UsuarioDAO;
import com.proptech.modelo.Rol;

import io.javalin.Javalin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Iniciando Plataforma PropTech ===");
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("User home: " + System.getProperty("user.home"));
        String os = System.getProperty("os.name").toLowerCase();
        String separator = os.contains("win") ? "\\" : "/";
        System.out.println("DB path: " + System.getProperty("user.home") + separator + ".proptech" + separator + "inmobiliaria.db");
        
        // 1. Inicializamos la Base de Datos SQLite
        System.out.println("1. Inicializando base de datos...");
        ConexionDB.inicializarTablas();

        // 2. Cargamos nuestro cerebro (La capa de Servicio que usa las Tablas Hash y Árboles)
        System.out.println("2. Iniciando servicios...");
        InventarioInmueblesService inventarioService = new InventarioInmueblesService();

        // (Opcional) Si la base de datos está vacía, agregamos un inmueble de prueba
        if (inventarioService.buscarPorCodigo("WEB-001") == null) {
            inventarioService.registrarInmueble(new Inmueble("WEB-001", "Penthouse", "Zona Norte", 500.0, 200.0, 4, 3, true, "Hermoso penthouse en zona norte"));
            inventarioService.registrarInmueble(new Inmueble("WEB-002", "Casa", "Zona Sur", 150.0, 90.0, 3, 2, false, "Casa acogedora en zona sur"));
        }

        // 2.1 Iniciamos el servicio de alertas
        AlertasService alertasService = new AlertasService();
        alertasService.agregarAlerta(new Alerta("A-01", "Contrato a punto de vencer", 1, "2026-05-24"));
        alertasService.agregarAlerta(new Alerta("A-02", "Mantenimiento rutinario programado", 5, "2026-05-24"));
        alertasService.agregarAlerta(new Alerta("A-03", "Cliente VIP solicitó contacto", 2, "2026-05-24"));

        // 2.2 Iniciamos el servicio de clientes (TablaHash para búsqueda O(1))
        ClientesService clientesService = new ClientesService();

        // Si no hay clientes, sembramos datos de prueba
        if (clientesService.buscarPorIdentificacion("CC-1001") == null) {
            clientesService.registrarCliente(new Cliente("CC-1001", "María García", "310-555-0101", 300.0, "maria.garcia@email.com"));
            clientesService.registrarCliente(new Cliente("CC-1002", "Carlos Rodríguez", "320-555-0202", 450.0, "carlos.rod@email.com"));
            clientesService.registrarCliente(new Cliente("NIT-9001", "Inversiones ABC S.A.S.", "601-555-0303", 1200.0, "contacto@inversionesabc.com"));
        }

        // 2.3 Iniciamos el servicio de operaciones
        OperacionesService operacionesService = new OperacionesService();

        // 2.3.1 Sembrar asesores de prueba si no existen
        com.proptech.dao.AsesorDAO asesorDAO = new com.proptech.dao.AsesorDAO();
        if (asesorDAO.obtenerPorId("ASESOR-001") == null) {
            asesorDAO.guardar(new Asesor("ASESOR-001", "Carlos M\u00e9ndez", "Ventas Comerciales", "carlos@email.com", "300-555-0001"));
            asesorDAO.guardar(new Asesor("ASESOR-002", "Ana L\u00f3pez", "Arriendos Residenciales", "ana@email.com", "300-555-0002"));
            System.out.println("Asesores de prueba creados.");
        }

        // 2.3.2 Sembrar operaciones de prueba si no existen
        if (operacionesService.buscarPorId("OP-001") == null) {
            Inmueble inm = inventarioService.buscarPorCodigo("WEB-001");
            Cliente cli = clientesService.buscarPorIdentificacion("CC-1001");
            if (inm != null && cli != null) {
                operacionesService.registrarOperacion(new Operacion("OP-001", "Venta", inm, cli,
                    new Asesor("ASESOR-001", "Carlos M\u00e9ndez", "Ventas Comerciales", "carlos@email.com", "300-555-0001"),
                    500.0, "2026-05-20"));
                System.out.println("Operaci\u00f3n de prueba OP-001 creada.");
            }
        }

        // 2.4 Iniciamos el servicio de recomendación
        RecomendacionService recomendacionService = new RecomendacionService();

        // 2.5 Iniciamos el servicio de detección de anomalías
        DetectorAnomaliesService detectorAnomaliesService = new DetectorAnomaliesService();

        // 2.6 Iniciamos el servicio de reportes
        ReporteService reporteService = new ReporteService();

// 2.7 Iniciamos el servicio de autenticación
        AuthService authService = new AuthService();
        // Crear usuario admin por defecto si no existe
        if (authService.login("admin@proptech.com", "admin123") == null) {
            authService.crearAdmin("admin@proptech.com", "admin123", "Administrador");
        }

        // 2.8 Iniciamos el servicio de visitas
        VisitaService visitaService = new VisitaService();

        // 2.9 Iniciamos el Asesor IA
        AiService aiService = new AiService();
        AsesorService asesorService = new AsesorService(aiService, inventarioService, clientesService, operacionesService);
        if (aiService.isConfigurado()) {
            System.out.println("✅ Asesor IA listo en /api/chat");
        } else {
            System.out.println("ℹ️  Asesor IA no configurado. Define OPENROUTER_API_KEY para activarlo.");
        }

        // --- DEFINICIÓN DE RUTAS (ENDPOINTS) ---
        // 3. Encendemos el Servidor Web Javalin
        Javalin app = Javalin.create(config -> {
            
            // Registramos la carpeta de archivos estáticos (HTML, JS, CSS)
            // IMPORTANTE: Javalin buscará dentro de src/main/resources/public
            config.staticFiles.add("/public"); 
            
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.anyHost());
            });
        }).start(7070);

        // --- DEFINICIÓN DE RUTAS API ---
        // Ruta raíz redirige a landing page
        app.get("/", ctx -> {
            ctx.redirect("/landing.html");
        });

        app.get("/api/inmuebles", ctx -> {
            ListaEnlazada<Inmueble> guardados = inventarioService.obtenerTodos();
            List<Inmueble> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < guardados.getTamaño(); i++) {
                listaParaWeb.add(guardados.obtener(i));
            }
            ctx.json(listaParaWeb);
        });

        // Ruta para buscar un inmueble ultra rápido usando nuestra Tabla Hash
        app.get("/api/inmuebles/{codigo}", ctx -> {
            String codigoBuscado = ctx.pathParam("codigo");
            Inmueble encontrado = inventarioService.buscarPorCodigo(codigoBuscado);
            
            if (encontrado != null) {
                ctx.json(encontrado);
            } else {
                ctx.status(404).result("Inmueble no encontrado");
            }
        });

        // Registrar un nuevo inmueble (persiste en SQLite + indexa en estructuras)
        app.post("/api/inmuebles", ctx -> {
            Inmueble nuevo = ctx.bodyAsClass(Inmueble.class);
            if (nuevo.getEstado() == null || nuevo.getEstado().isEmpty()) {
                nuevo.setEstado("Disponible");
            }
            inventarioService.registrarInmueble(nuevo);
            ctx.status(201).json(nuevo);
        });

        // Actualizar un inmueble existente
        app.put("/api/inmuebles/{codigo}", ctx -> {
            String codigo = ctx.pathParam("codigo");
            String email = ctx.queryParam("email");
            Inmueble existente = inventarioService.buscarPorCodigo(codigo);
            if (existente == null) {
                ctx.status(404).json(Map.of("error", "Inmueble no encontrado: " + codigo));
                return;
            }

            // Verificar si el usuario es admin
            boolean esAdmin = false;
            if (email != null) {
                UsuarioDAO userDao = new UsuarioDAO();
                Usuario user = userDao.buscarPorEmail(email);
                if (user != null && user.getRol() != null && Rol.ADMIN.equals(user.getRol().getNombre())) {
                    esAdmin = true;
                }
            }

            // Bloquear edición si el inmueble está Vendido y el usuario no es admin
            if ("Vendido".equals(existente.getEstado()) && !esAdmin) {
                ctx.status(403).json(Map.of("error", "No puedes modificar un inmueble vendido. Solo el administrador puede hacerlo."));
                return;
            }

            Inmueble datos = ctx.bodyAsClass(Inmueble.class);
            datos.setCodigo(codigo);
            String estadoAnterior = existente.getEstado();
            String estadoNuevo = datos.getEstado();
            if (estadoNuevo == null || estadoNuevo.isEmpty()) {
                datos.setEstado(estadoAnterior);
            }

            inventarioService.actualizarInmueble(datos);

            // Auto-crear operación si cambia a Vendido o Arrendado
            if (estadoNuevo != null && !estadoNuevo.equals(estadoAnterior)) {
                Asesor asesor = null;
                if (email != null) {
                    AsesorDAO asesorDao = new AsesorDAO();
                    ListaEnlazada<Asesor> todos = asesorDao.obtenerTodos();
                    for (int i = 0; i < todos.getTamaño(); i++) {
                        Asesor a = todos.obtener(i);
                        if (email.equals(a.getEmail())) {
                            asesor = a;
                            break;
                        }
                    }
                }

                String opId = "OP-AUTO-" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String fecha = java.time.LocalDate.now().toString();

                if ("Vendido".equals(estadoNuevo)) {
                    Operacion op = new Operacion(opId, "Venta", datos, null, asesor, datos.getPrecio(), fecha);
                    operacionesService.registrarOperacion(op);
                    System.out.println("Operación de venta auto-creada: " + opId);
                } else if ("Arrendado".equals(estadoNuevo)) {
                    Operacion op = new Operacion(opId, "Arriendo", datos, null, asesor, datos.getPrecio(), fecha);
                    operacionesService.registrarOperacion(op);
                    System.out.println("Operación de arriendo auto-creada: " + opId);
                }
            }

            ctx.json(datos);
        });

        // Eliminar un inmueble
        app.delete("/api/inmuebles/{codigo}", ctx -> {
            String codigo = ctx.pathParam("codigo");
            Inmueble existente = inventarioService.buscarPorCodigo(codigo);
            if (existente == null) {
                ctx.status(404).json(Map.of("error", "Inmueble no encontrado: " + codigo));
                return;
            }
            inventarioService.eliminarInmueble(codigo);
            ctx.json(Map.of("mensaje", "Inmueble eliminado", "codigo", codigo));
        });

        // Ruta de prueba para Alertas
        app.get("/api/alertas/siguiente", ctx -> {
            if (alertasService.hayAlertasPendientes()) {
                Alerta siguiente = alertasService.atenderSiguienteAlerta();
                ctx.json(siguiente);
            } else {
                ctx.status(200).result("No hay alertas pendientes.");
            }
        });

        // --- RUTAS API DE CLIENTES ---

        // Obtener todos los clientes (desde TablaHash en RAM)
        app.get("/api/clientes", ctx -> {
            ListaEnlazada<Cliente> clientes = clientesService.obtenerTodos();
            List<Cliente> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < clientes.getTamaño(); i++) {
                listaParaWeb.add(clientes.obtener(i));
            }
            ctx.json(listaParaWeb);
        });

        // Buscar cliente por identificación — O(1) vía TablaHash
        app.get("/api/clientes/{id}", ctx -> {
            String idBuscado = ctx.pathParam("id");
            Cliente encontrado = clientesService.buscarPorIdentificacion(idBuscado);

            if (encontrado != null) {
                ctx.json(encontrado);
            } else {
                ctx.status(404).result("Cliente no encontrado con ID: " + idBuscado);
            }
        });

        // Registrar un nuevo cliente (persiste en SQLite + indexa en TablaHash)
        app.post("/api/clientes", ctx -> {
            Cliente nuevo = ctx.bodyAsClass(Cliente.class);
            clientesService.registrarCliente(nuevo);
            ctx.status(201).json(nuevo);
        });

        // Re-sembrar datos de clientes de prueba (útil si la BD se reinició)
        app.post("/api/clientes/seed", ctx -> {
            clientesService.registrarCliente(new Cliente("CC-1001", "María García", "310-555-0101", 300.0, "maria.garcia@email.com"));
            clientesService.registrarCliente(new Cliente("CC-1002", "Carlos Rodríguez", "320-555-0202", 450.0, "carlos.rod@email.com"));
            clientesService.registrarCliente(new Cliente("NIT-9001", "Inversiones ABC S.A.S.", "601-555-0303", 1200.0, "contacto@inversionesabc.com"));
            ctx.json(Map.of("mensaje", "Datos de prueba re-sembrados", "total", 3));
        });

        // Actualizar un cliente existente (para asesor/admin)
        app.put("/api/clientes/{id}", ctx -> {
            String id = ctx.pathParam("id");
            Cliente existente = clientesService.buscarPorIdentificacion(id);
            if (existente == null) {
                ctx.status(404).json(Map.of("error", "Cliente no encontrado: " + id));
                return;
            }
            Cliente datos = ctx.bodyAsClass(Cliente.class);
            datos.setIdentificacion(id);
            clientesService.actualizarCliente(datos);
            ctx.json(datos);
        });

        // Eliminar un cliente (para asesor/admin)
        app.delete("/api/clientes/{id}", ctx -> {
            String id = ctx.pathParam("id");
            Cliente existente = clientesService.buscarPorIdentificacion(id);
            if (existente == null) {
                ctx.status(404).json(Map.of("error", "Cliente no encontrado: " + id));
                return;
            }
            ClienteDAO cd = new ClienteDAO();
            cd.eliminar(id);
            ctx.json(Map.of("mensaje", "Cliente eliminado", "id", id));
        });

        // --- RUTAS API DE OPERACIONES ---

        // Obtener todas las operaciones (desde TablaHash en RAM)
        app.get("/api/operaciones", ctx -> {
            ListaEnlazada<Operacion> operaciones = operacionesService.obtenerTodas();
            List<Operacion> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < operaciones.getTamaño(); i++) {
                listaParaWeb.add(operaciones.obtener(i));
            }
            ctx.json(listaParaWeb);
        });

        // Obtener operación por ID — O(1) vía TablaHash
        app.get("/api/operaciones/{id}", ctx -> {
            String idBuscado = ctx.pathParam("id");
            Operacion encontrada = operacionesService.buscarPorId(idBuscado);

            if (encontrada != null) {
                ctx.json(encontrada);
            } else {
                ctx.status(404).result("Operación no encontrada con ID: " + idBuscado);
            }
        });

        // Registrar una nueva operación (arriendo/venta)
        app.post("/api/operaciones", ctx -> {
            Operacion nueva = ctx.bodyAsClass(Operacion.class);
            operacionesService.registrarOperacion(nueva);
            ctx.status(201).json(nueva);
        });

        // Registrar una renovación de contrato
        app.post("/api/operaciones/renovacion", ctx -> {
            Operacion renovacion = ctx.bodyAsClass(Operacion.class);
            operacionesService.registrarRenovacion(renovacion);
            ctx.status(201).json(renovacion);
        });

        // Registrar una cancelación de negocio
        app.post("/api/operaciones/cancelacion", ctx -> {
            Operacion cancelacion = ctx.bodyAsClass(Operacion.class);
            operacionesService.registrarCancelacion(cancelacion);
            ctx.status(201).json(cancelacion);
        });

        // Obtener operaciones por tipo (Venta, Arriendo, etc.)
        app.get("/api/operaciones/tipo/{tipo}", ctx -> {
            String tipoBuscado = ctx.pathParam("tipo");
            ListaEnlazada<Operacion> operaciones = operacionesService.obtenerPorTipo(tipoBuscado);
            List<Operacion> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < operaciones.getTamaño(); i++) {
                listaParaWeb.add(operaciones.obtener(i));
            }
            ctx.json(listaParaWeb);
        });

        // Obtener operaciones por asesor
        app.get("/api/operaciones/asesor/{idAsesor}", ctx -> {
            String idAsesorBuscado = ctx.pathParam("idAsesor");
            ListaEnlazada<Operacion> operaciones = operacionesService.obtenerPorAsesor(idAsesorBuscado);
            List<Operacion> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < operaciones.getTamaño(); i++) {
                listaParaWeb.add(operaciones.obtener(i));
            }
            ctx.json(listaParaWeb);
        });

        // --- RUTAS API DE RECOMENDACIONES ---

        // Obtener recomendaciones de inmuebles para un cliente
        app.get("/api/recomendaciones/{idCliente}", ctx -> {
            String idClienteBuscado = ctx.pathParam("idCliente");
            ListaEnlazada<Inmueble> recomendaciones = recomendacionService.generarRecomendaciones(idClienteBuscado);
            List<Inmueble> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < recomendaciones.getTamaño(); i++) {
                listaParaWeb.add(recomendaciones.obtener(i));
            }
            ctx.json(listaParaWeb);
        });

        // --- RUTAS API DE ANOMALÍAS ---

        // Detectar visitas sin cierre
        app.get("/api/anomalias/visitas-sin-cierre", ctx -> {
            ListaEnlazada<Map<String, Object>> anomalias = detectorAnomaliesService.detectarVisitasSinCierre();
            List<Map<String, Object>> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < anomalias.getTamaño(); i++) {
                listaParaWeb.add(anomalias.obtener(i));
            }
            ctx.json(listaParaWeb);
        });

        // Detectar sobrecarga de asesores
        app.get("/api/anomalias/sobrecarga-asesores", ctx -> {
            ListaEnlazada<Map<String, Object>> anomalias = detectorAnomaliesService.detectarSobrecargaAsesores();
            List<Map<String, Object>> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < anomalias.getTamaño(); i++) {
                listaParaWeb.add(anomalias.obtener(i));
            }
            ctx.json(listaParaWeb);
        });

        // Detectar cambios de precio frecuentes
        app.get("/api/anomalias/cambios-precio", ctx -> {
            ListaEnlazada<Map<String, Object>> anomalias = detectorAnomaliesService.detectarCambiosPrecioFrecuentes();
            List<Map<String, Object>> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < anomalias.getTamaño(); i++) {
                listaParaWeb.add(anomalias.obtener(i));
            }
            ctx.json(listaParaWeb);
        });

        // Detectar concentración geográfica
        app.get("/api/anomalias/concentracion-geografica", ctx -> {
            ListaEnlazada<Map<String, Object>> anomalias = detectorAnomaliesService.detectarConcentracionGeografica();
            List<Map<String, Object>> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < anomalias.getTamaño(); i++) {
                listaParaWeb.add(anomalias.obtener(i));
            }
            ctx.json(listaParaWeb);
        });

        // Obtener total de anomalías
        app.get("/api/anomalias/total", ctx -> {
            int total = detectorAnomaliesService.obtenerTotalAnomalias();
            ctx.json(Map.of("totalAnomalias", total));
        });

        // --- RUTAS API DE REPORTES ---

        // Reporte de rendimiento de inmuebles
        app.get("/api/reportes/rendimiento-inmuebles", ctx -> {
            Map<String, Object> reporte = reporteService.generarReporteRendimientoInmuebles();
            ctx.json(reporte);
        });

        // Reporte de desempeño de asesores
        app.get("/api/reportes/asesores", ctx -> {
            ListaEnlazada<Map<String, Object>> reporte = reporteService.generarReporteAsesores();
            List<Map<String, Object>> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < reporte.getTamaño(); i++) {
                listaParaWeb.add(reporte.obtener(i));
            }
            ctx.json(listaParaWeb);
        });

        // Reporte de precios por zona
        app.get("/api/reportes/precios-zona", ctx -> {
            Map<String, Double> reporte = reporteService.generarReportePreciosPorZona();
            ctx.json(reporte);
        });

        // Reporte de clientes activos
        app.get("/api/reportes/clientes-activos", ctx -> {
            Map<String, Object> reporte = reporteService.generarReporteClientesActivos();
            ctx.json(reporte);
        });

        // Reporte de tipos de operación
        app.get("/api/reportes/tipos-operacion", ctx -> {
            Map<String, Integer> reporte = reporteService.generarReporteTiposOperacion();
            ctx.json(reporte);
        });

        // Reporte con filtros
        app.get("/api/reportes/rendimiento-filtrado", ctx -> {
            String tipo = ctx.queryParam("tipo");
            String zona = ctx.queryParam("zona");
            String precioMinStr = ctx.queryParam("precioMin");
            
            Double precioMin = null;
            if (precioMinStr != null) {
                try {
                    precioMin = Double.parseDouble(precioMinStr);
                } catch (NumberFormatException ignored) {}
            }
            
            Map<String, Object> reporte = reporteService.generarReporteRendimientoFiltrado(tipo, zona, precioMin);
            ctx.json(reporte);
        });

        // --- RUTAS API DE VISITAS ---

        // Programar una nueva visita (cliente agenda cita para ver un inmueble)
        app.post("/api/visitas", ctx -> {
            Map<String, String> datos = ctx.bodyAsClass(Map.class);
            String codigoInmueble = datos.get("codigoInmueble");
            String fechaHora = datos.get("fechaHora");
            String emailCliente = datos.get("emailCliente");

            if (codigoInmueble == null || fechaHora == null || emailCliente == null) {
                ctx.status(400).json(Map.of("error", "Faltan campos requeridos: codigoInmueble, fechaHora, emailCliente"));
                return;
            }

            // Buscar cliente por email, si no existe lo crea automáticamente
            ClienteDAO cliDAO = new ClienteDAO();
            Cliente cliente = cliDAO.obtenerPorEmail(emailCliente);
            if (cliente == null) {
                String idCliente = "CLI-" + System.currentTimeMillis();
                cliente = new Cliente(idCliente, emailCliente.split("@")[0], "", 0, emailCliente);
                cliDAO.guardar(cliente);
                clientesService.registrarCliente(cliente);
                System.out.println("Cliente creado automáticamente y cacheado para email: " + emailCliente);
            }

            // Buscar inmueble por código
            Inmueble inmueble = inventarioService.buscarPorCodigo(codigoInmueble);
            if (inmueble == null) {
                ctx.status(404).json(Map.of("error", "Inmueble no encontrado: " + codigoInmueble));
                return;
            }

            // No permitir visitas en inmuebles vendidos o arrendados
            if ("Vendido".equals(inmueble.getEstado()) || "Arrendado".equals(inmueble.getEstado())) {
                ctx.status(400).json(Map.of("error", "No puedes agendar una visita en un inmueble que ya está " + inmueble.getEstado().toLowerCase() + "."));
                return;
            }

            // Validar que no haya otra visita para el mismo inmueble con diferencia menor a 2 horas
            ListaEnlazada<Visita> visitasExistentes = visitaService.obtenerVisitasPorInmueble(codigoInmueble);
            for (int i = 0; i < visitasExistentes.getTamaño(); i++) {
                Visita v = visitasExistentes.obtener(i);
                if ("Cancelada".equals(v.getEstado())) continue;
                String fechaExistente = v.getFechaHora();
                if (fechasConConflicto(fechaHora, fechaExistente)) {
                    ctx.status(409).json(Map.of("error", "Ya existe una visita programada para este inmueble con diferencia menor a 2 horas: " + fechaExistente));
                    return;
                }
            }

            // Asignar el asesor seleccionado por el cliente (o el primero disponible)
            AsesorDAO asesorDao = new AsesorDAO();
            String idAsesorSeleccionado = datos.get("idAsesor");
            Asesor asesor = null;
            if (idAsesorSeleccionado != null && !idAsesorSeleccionado.isEmpty()) {
                asesor = asesorDao.obtenerPorId(idAsesorSeleccionado);
            }
            if (asesor == null) {
                ListaEnlazada<Asesor> asesores = asesorDao.obtenerTodos();
                if (asesores.getTamaño() > 0) {
                    asesor = asesores.obtener(0);
                }
            }

            // Generar ID único para la visita
            String idVisita = "VIS-" + System.currentTimeMillis();

            Visita visita = new Visita(idVisita, cliente, inmueble, asesor, fechaHora);
            visitaService.programarVisita(visita);

            ctx.status(201).json(Map.of(
                "idVisita", idVisita,
                "mensaje", "Visita programada exitosamente",
                "fechaHora", fechaHora,
                "estado", "Pendiente"
            ));
        });

        // Obtener visitas de un cliente por email
        app.get("/api/visitas/cliente/{email}", ctx -> {
            String email = ctx.pathParam("email");
            ClienteDAO cliDAO = new ClienteDAO();
            Cliente cliente = cliDAO.obtenerPorEmail(email);
            if (cliente == null) {
                ctx.json(new ArrayList<>());
                return;
            }
            ListaEnlazada<Visita> visitas = visitaService.obtenerVisitasPorCliente(cliente.getIdentificacion());
            List<Visita> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < visitas.getTamaño(); i++) {
                listaParaWeb.add(visitas.obtener(i));
            }
            ctx.json(listaParaWeb);
        });

        // Cancelar una visita (cliente cancela su cita)
        app.put("/api/visitas/{idVisita}/cancelar", ctx -> {
            String idVisita = ctx.pathParam("idVisita");
            Visita v = visitaService.buscarPorId(idVisita);
            if (v == null) {
                ctx.status(404).json(Map.of("error", "Visita no encontrada: " + idVisita));
                return;
            }
            visitaService.cancelarVisita(idVisita);
            ctx.json(Map.of("mensaje", "Visita cancelada exitosamente", "idVisita", idVisita));
        });

        // Obtener visitas asignadas a un asesor
        app.get("/api/visitas/asesor/{idAsesor}", ctx -> {
            String idAsesor = ctx.pathParam("idAsesor");
            ListaEnlazada<Visita> todas = visitaService.obtenerTodas();
            List<Visita> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < todas.getTamaño(); i++) {
                Visita v = todas.obtener(i);
                if (v.getAsesor() != null && v.getAsesor().getIdAsesor() != null &&
                    idAsesor.equals(v.getAsesor().getIdAsesor())) {
                    listaParaWeb.add(v);
                }
            }
            ctx.json(listaParaWeb);
        });

        // Listar todos los asesores disponibles
        app.get("/api/asesores", ctx -> {
            AsesorDAO asesorDao = new AsesorDAO();
            ListaEnlazada<Asesor> asesores = asesorDao.obtenerTodos();
            List<Map<String, Object>> lista = new ArrayList<>();
            for (int i = 0; i < asesores.getTamaño(); i++) {
                Asesor a = asesores.obtener(i);
                Map<String, Object> m = new java.util.HashMap<>();
                m.put("idAsesor", a.getIdAsesor());
                m.put("nombre", a.getNombre());
                m.put("especialidad", a.getEspecialidad());
                m.put("email", a.getEmail());
                lista.add(m);
            }
            ctx.json(lista);
        });

        // Confirmar una visita (asesor confirma la cita)
        app.put("/api/visitas/{idVisita}/confirmar", ctx -> {
            String idVisita = ctx.pathParam("idVisita");
            Visita v = visitaService.buscarPorId(idVisita);
            if (v == null) {
                ctx.status(404).json(Map.of("error", "Visita no encontrada: " + idVisita));
                return;
            }
            visitaService.actualizarEstadoVisita(idVisita, "Confirmada");
            ctx.json(Map.of("mensaje", "Visita confirmada", "idVisita", idVisita));
        });

        // Marcar visita como realizada (asesor completó la cita)
        app.put("/api/visitas/{idVisita}/realizar", ctx -> {
            String idVisita = ctx.pathParam("idVisita");
            Visita v = visitaService.buscarPorId(idVisita);
            if (v == null) {
                ctx.status(404).json(Map.of("error", "Visita no encontrada: " + idVisita));
                return;
            }
            visitaService.actualizarEstadoVisita(idVisita, "Realizada");
            ctx.json(Map.of("mensaje", "Visita marcada como realizada", "idVisita", idVisita));
        });

        // --- DASHBOARD DEL ASESOR ---
        app.get("/api/asesor/dashboard", ctx -> {
            String email = ctx.queryParam("email");
            if (email == null) {
                ctx.status(400).json(Map.of("error", "Email requerido"));
                return;
            }
            AsesorDAO asesorDao = new AsesorDAO();
            // Buscar asesor por email
            ListaEnlazada<Asesor> todosAsesores = asesorDao.obtenerTodos();
            Asesor asesor = null;
            for (int i = 0; i < todosAsesores.getTamaño(); i++) {
                Asesor a = todosAsesores.obtener(i);
                if (email.equals(a.getEmail())) {
                    asesor = a;
                    break;
                }
            }
            if (asesor == null) {
                // Auto-crear asesor para usuarios con rol VENDEDOR
                UsuarioDAO usuarioDao = new UsuarioDAO();
                Usuario u = usuarioDao.buscarPorEmail(email);
                if (u != null && u.getRol() != null && (Rol.VENDEDOR.equals(u.getRol().getNombre()) || Rol.ADMIN.equals(u.getRol().getNombre()))) {
                    String nuevoId = "ASESOR-AUTO-" + email.hashCode();
                    asesor = new Asesor(nuevoId, u.getNombre(), "General", email, u.getTelefono());
                    asesorDao.guardar(asesor);
                    System.out.println("Asesor auto-creado para VENDEDOR: " + email);
                } else {
                    ctx.status(404).json(Map.of("error", "Asesor no encontrado con email: " + email));
                    return;
                }
            }

            // Calcular métricas desde las visitas
            int visitasAtendidas = 0;
            int visitasPendientes = 0;
            ListaEnlazada<Visita> todasVisitas = visitaService.obtenerTodas();
            for (int i = 0; i < todasVisitas.getTamaño(); i++) {
                Visita v = todasVisitas.obtener(i);
                if (v.getAsesor() != null && asesor.getIdAsesor().equals(v.getAsesor().getIdAsesor())) {
                    if ("Realizada".equals(v.getEstado())) visitasAtendidas++;
                    if ("Pendiente".equals(v.getEstado()) || "Confirmada".equals(v.getEstado())) visitasPendientes++;
                }
            }

            // Operaciones cerradas desde el servicio
            ListaEnlazada<Operacion> todasOps = operacionesService.obtenerTodas();
            int operacionesCerradas = 0;
            double totalVentas = 0;
            for (int i = 0; i < todasOps.getTamaño(); i++) {
                Operacion op = todasOps.obtener(i);
                if (op.getAsesor() != null && asesor.getIdAsesor().equals(op.getAsesor().getIdAsesor())) {
                    operacionesCerradas++;
                    totalVentas += op.getMonto();
                }
            }

            Map<String, Object> dashboard = new java.util.HashMap<>();
            dashboard.put("nombre", asesor.getNombre());
            dashboard.put("idAsesor", asesor.getIdAsesor());
            dashboard.put("especialidad", asesor.getEspecialidad());
            dashboard.put("email", asesor.getEmail());
            dashboard.put("telefono", asesor.getTelefono());
            dashboard.put("calificacion", asesor.getCalificacion());
            dashboard.put("operacionesCerradas", operacionesCerradas);
            dashboard.put("totalVentas", totalVentas);
            dashboard.put("visitasAtendidas", visitasAtendidas);
            dashboard.put("visitasPendientes", visitasPendientes);
            ctx.json(dashboard);
        });

        // --- RUTAS API DE FAVORITOS ---

        FavoritoDAO favoritoDAO = new FavoritoDAO();

        // Agregar o quitar favorito (toggle)
        app.post("/api/favoritos/{codigoInmueble}", ctx -> {
            String codigoInmueble = ctx.pathParam("codigoInmueble");
            String email = ctx.queryParam("email");
            if (email == null) {
                ctx.status(400).json(Map.of("error", "Email requerido"));
                return;
            }
            Cliente c = new ClienteDAO().obtenerPorEmail(email);
            if (c == null) {
                ctx.status(404).json(Map.of("error", "Cliente no encontrado"));
                return;
            }
            boolean esFav = favoritoDAO.esFavorito(c.getIdentificacion(), codigoInmueble);
            if (esFav) {
                favoritoDAO.quitarFavorito(c.getIdentificacion(), codigoInmueble);
                ctx.json(Map.of("favorito", false, "mensaje", "Favorito quitado"));
            } else {
                favoritoDAO.agregarFavorito(c.getIdentificacion(), codigoInmueble);
                ctx.json(Map.of("favorito", true, "mensaje", "Favorito agregado"));
            }
        });

        // Obtener favoritos del cliente
        app.get("/api/favoritos", ctx -> {
            String email = ctx.queryParam("email");
            if (email == null) {
                ctx.status(400).json(Map.of("error", "Email requerido"));
                return;
            }
            Cliente c = new ClienteDAO().obtenerPorEmail(email);
            if (c == null) {
                ctx.json(new ArrayList<>());
                return;
            }
            ListaEnlazada<String> codigos = favoritoDAO.obtenerFavoritos(c.getIdentificacion());
            List<String> lista = new ArrayList<>();
            for (int i = 0; i < codigos.getTamaño(); i++) {
                lista.add(codigos.obtener(i));
            }
            ctx.json(lista);
        });

        // --- RUTAS API DE PERFIL DEL CLIENTE ---

        app.put("/api/clientes/perfil", ctx -> {
            Map<String, Object> datos = ctx.bodyAsClass(Map.class);
            String email = (String) datos.get("email");
            ClienteDAO cd = new ClienteDAO();
            Cliente c = cd.obtenerPorEmail(email);
            if (c == null) {
                ctx.status(404).json(Map.of("error", "Cliente no encontrado"));
                return;
            }
            if (datos.containsKey("nombre")) c.setNombre((String) datos.get("nombre"));
            if (datos.containsKey("telefono")) c.setTelefono((String) datos.get("telefono"));
            if (datos.containsKey("email") && !email.equals(datos.get("email"))) {
                c.setEmail((String) datos.get("email"));
            }
            if (datos.containsKey("presupuestoMaximo")) {
                c.setPresupuestoMaximo(((Number) datos.get("presupuestoMaximo")).doubleValue());
            }
            clientesService.actualizarCliente(c);
            ctx.json(Map.of("mensaje", "Perfil actualizado"));
        });

        app.get("/api/clientes/perfil", ctx -> {
            String email = ctx.queryParam("email");
            if (email == null) {
                ctx.status(400).json(Map.of("error", "Email requerido"));
                return;
            }
            Cliente c = new ClienteDAO().obtenerPorEmail(email);
            if (c == null) {
                ctx.status(404).json(Map.of("error", "Cliente no encontrado"));
                return;
            }
            ctx.json(c);
        });

        // --- RUTAS API DE DIAGNÓSTICO ---
        app.get("/api/diagnostico/usuarios", ctx -> {
            try (Connection conn = ConexionDB.conectar()) {
                if (conn == null) {
                    ctx.json(Map.of("error", "No se pudo conectar a la base de datos"));
                    return;
                }
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as total FROM usuarios");
                ResultSet rs = stmt.executeQuery();
                rs.next();
                int total = rs.getInt("total");
                ctx.json(Map.of("usuariosEnBD", total, "bdPath", System.getProperty("user.home") + "/.proptech/inmobiliaria.db"));
            } catch (SQLException e) {
                ctx.json(Map.of("error", e.getMessage()));
            }
        });
        
        // --- RUTAS API DE AUTENTICACIÓN ---
        // Auth service ya fue instanciado antes
        
        // Registro de usuario
        app.post("/api/auth/registro", ctx -> {
            Map<String, String> datos = ctx.bodyAsClass(Map.class);
            String email = datos.get("email");
            String password = datos.get("password");
            String nombre = datos.get("nombre");
            
            if (email == null || password == null || nombre == null) {
                ctx.status(400).json(Map.of("error", "Faltan campos requeridos"));
                return;
            }
            
            Map<String, Object> resultado = authService.registrarConRecomendaciones(email, password, nombre);
            if ((Boolean)resultado.get("exito")) {
                ctx.json(resultado);
            } else {
                ctx.status(400).json(Map.of("error", resultado.get("error")));
            }
        });
        
        // Login
        app.post("/api/auth/login", ctx -> {
            Map<String, String> datos = ctx.bodyAsClass(Map.class);
            String email = datos.get("email");
            String password = datos.get("password");
            
            Usuario usuario = authService.login(email, password);
            if (usuario != null) {
                Map<String, Object> resp = new java.util.HashMap<>();
                resp.put("mensaje", "Login exitoso");
                resp.put("rol", usuario.getRol().getNombre());
                resp.put("nombre", usuario.getNombre());
                resp.put("telefono", usuario.getTelefono());
                resp.put("direccion", usuario.getDireccion());
                resp.put("intereses", usuario.getIntereses());
                resp.put("fotoPerfil", usuario.getFotoPerfil());
                resp.put("email", usuario.getEmail());
                ctx.json(resp);
            } else {
                ctx.status(401).json(Map.of("error", "Credenciales inválidas"));
            }
        });
        
        // Obtener todos los usuarios (directo de BD)
        app.get("/api/usuarios", ctx -> {
            com.proptech.dao.UsuarioDAO usuarioDAO = new com.proptech.dao.UsuarioDAO();
            ListaEnlazada<Usuario> usuarios = usuarioDAO.obtenerTodos();
            List<Usuario> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < usuarios.getTamaño(); i++) {
                listaParaWeb.add(usuarios.obtener(i));
            }
            ctx.json(listaParaWeb);
        });
        
        // Obtener perfil
        app.get("/api/usuario/perfil", ctx -> {
            String email = ctx.queryParam("email");
            if (email == null) {
                ctx.status(400).json(Map.of("error", "Email requerido"));
                return;
            }
            Usuario usuario = authService.obtenerPerfil(email);
            if (usuario != null) {
                ctx.json(usuario);
            } else {
                ctx.status(404).json(Map.of("error", "Usuario no encontrado"));
            }
        });
        
        // Actualizar perfil de usuario
        // - intereses, fotoPerfil: se actualizan al instante
        // - nombre, telefono, direccion, email: crean cambio_pendiente (aprobación admin requerida)
        // - si el usuario es ADMIN, todos los campos se aplican directamente
        app.put("/api/usuario/actualizar", ctx -> {
            Map<String, Object> datos = ctx.bodyAsClass(Map.class);
            String email = (String) datos.get("email");
            
            Usuario usuario = authService.obtenerPerfil(email);
            if (usuario == null) {
                ctx.status(404).json(Map.of("error", "Usuario no encontrado"));
                return;
            }
            
            String rol = usuario.getRol().getNombre().toLowerCase();
            boolean esAdminOAsesor = "admin".equals(rol) || "vendedor".equals(rol);
            List<String> cambiosPendientes = new ArrayList<>();
            boolean hayCambioDirecto = false;
            
            String intereses = (String) datos.get("intereses");
            String fotoPerfil = (String) datos.get("fotoPerfil");
            String nombre = (String) datos.get("nombre");
            String telefono = (String) datos.get("telefono");
            String direccion = (String) datos.get("direccion");
            String nuevoEmail = (String) datos.get("nuevoEmail");
            
            // Actualizar intereses directamente
            if (intereses != null) {
                usuario.setIntereses(intereses);
                hayCambioDirecto = true;
            }
            
            // Actualizar foto directamente
            if (fotoPerfil != null) {
                usuario.setFotoPerfil(fotoPerfil);
                hayCambioDirecto = true;
            }
            
            if (esAdminOAsesor) {
                // Admin/Asesor: todos los campos se aplican directamente
                if (nombre != null) usuario.setNombre(nombre);
                if (telefono != null) usuario.setTelefono(telefono);
                if (direccion != null) usuario.setDireccion(direccion);
                if (nuevoEmail != null && !nuevoEmail.equals(email)) usuario.setEmail(nuevoEmail);
                hayCambioDirecto = true;
            } else {
                // Cliente: nombre, telefono, direccion, email van a pendientes
                String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
                if (nombre != null && !nombre.equals(usuario.getNombre())) {
                    try (Connection conn = ConexionDB.conectar();
                         PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO cambios_pendientes (email_usuario, campo, valor_anterior, valor_nuevo, estado, fecha_solicitud) VALUES (?, ?, ?, ?, 'pendiente', ?)")) {
                        pstmt.setString(1, email);
                        pstmt.setString(2, "nombre");
                        pstmt.setString(3, usuario.getNombre());
                        pstmt.setString(4, nombre);
                        pstmt.setString(5, fecha);
                        pstmt.executeUpdate();
                        cambiosPendientes.add("nombre");
                    } catch (SQLException e) {
                        System.err.println("Error creando cambio pendiente: " + e.getMessage());
                    }
                }
                
                if (telefono != null && !telefono.equals(usuario.getTelefono())) {
                    try (Connection conn = ConexionDB.conectar();
                         PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO cambios_pendientes (email_usuario, campo, valor_anterior, valor_nuevo, estado, fecha_solicitud) VALUES (?, ?, ?, ?, 'pendiente', ?)")) {
                        pstmt.setString(1, email);
                        pstmt.setString(2, "telefono");
                        pstmt.setString(3, usuario.getTelefono());
                        pstmt.setString(4, telefono);
                        pstmt.setString(5, fecha);
                        pstmt.executeUpdate();
                        cambiosPendientes.add("telefono");
                    } catch (SQLException e) {
                        System.err.println("Error creando cambio pendiente: " + e.getMessage());
                    }
                }
                
                if (direccion != null && !direccion.equals(usuario.getDireccion())) {
                    try (Connection conn = ConexionDB.conectar();
                         PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO cambios_pendientes (email_usuario, campo, valor_anterior, valor_nuevo, estado, fecha_solicitud) VALUES (?, ?, ?, ?, 'pendiente', ?)")) {
                        pstmt.setString(1, email);
                        pstmt.setString(2, "direccion");
                        pstmt.setString(3, usuario.getDireccion());
                        pstmt.setString(4, direccion);
                        pstmt.setString(5, fecha);
                        pstmt.executeUpdate();
                        cambiosPendientes.add("direccion");
                    } catch (SQLException e) {
                        System.err.println("Error creando cambio pendiente: " + e.getMessage());
                    }
                }
                
                if (nuevoEmail != null && !nuevoEmail.equals(email)) {
                    try (Connection conn = ConexionDB.conectar();
                         PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO cambios_pendientes (email_usuario, campo, valor_anterior, valor_nuevo, estado, fecha_solicitud) VALUES (?, ?, ?, ?, 'pendiente', ?)")) {
                        pstmt.setString(1, email);
                        pstmt.setString(2, "email");
                        pstmt.setString(3, email);
                        pstmt.setString(4, nuevoEmail);
                        pstmt.setString(5, fecha);
                        pstmt.executeUpdate();
                        cambiosPendientes.add("email");
                    } catch (SQLException e) {
                        System.err.println("Error creando cambio pendiente: " + e.getMessage());
                    }
                }
            }
            
            // Guardar cambios directos (intereses, foto, o campos admin)
            if (hayCambioDirecto) {
                authService.actualizarPerfil(usuario);
            }
            
            Map<String, Object> respuesta = new java.util.HashMap<>();
            respuesta.put("mensaje", "Perfil actualizado");
            if (!cambiosPendientes.isEmpty()) {
                respuesta.put("pendientes", cambiosPendientes);
                respuesta.put("mensaje", "Intereses y foto actualizados. Los demás cambios quedaron pendientes de aprobación del administrador.");
            }
            ctx.json(respuesta);
        });
        
        // --- RUTAS API DE ADMIN: CAMBIOS PENDIENTES ---
        
        // Obtener todos los cambios pendientes
        app.get("/api/admin/cambios-pendientes", ctx -> {
            List<Map<String, Object>> lista = new ArrayList<>();
            try (Connection conn = ConexionDB.conectar();
                 PreparedStatement stmt = conn.prepareStatement(
                    "SELECT cp.*, u.nombre as nombre_usuario FROM cambios_pendientes cp " +
                    "JOIN usuarios u ON cp.email_usuario = u.email " +
                    "WHERE cp.estado = 'pendiente' ORDER BY cp.fecha_solicitud DESC")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("id", rs.getInt("id_cambio"));
                    item.put("email", rs.getString("email_usuario"));
                    item.put("nombreUsuario", rs.getString("nombre_usuario"));
                    item.put("campo", rs.getString("campo"));
                    item.put("valorAnterior", rs.getString("valor_anterior"));
                    item.put("valorNuevo", rs.getString("valor_nuevo"));
                    item.put("fechaSolicitud", rs.getString("fecha_solicitud"));
                    lista.add(item);
                }
            } catch (SQLException e) {
                ctx.status(500).json(Map.of("error", e.getMessage()));
                return;
            }
            ctx.json(lista);
        });
        
        // Aprobar un cambio pendiente
        app.put("/api/admin/cambios-pendientes/{id}/aprobar", ctx -> {
            int idCambio = Integer.parseInt(ctx.pathParam("id"));
            String emailAdmin = ctx.queryParam("adminEmail");
            
            try (Connection conn = ConexionDB.conectar()) {
                // Obtener el cambio pendiente
                PreparedStatement getStmt = conn.prepareStatement(
                    "SELECT * FROM cambios_pendientes WHERE id_cambio = ? AND estado = 'pendiente'");
                getStmt.setInt(1, idCambio);
                ResultSet rs = getStmt.executeQuery();
                if (!rs.next()) {
                    ctx.status(404).json(Map.of("error", "Cambio pendiente no encontrado"));
                    return;
                }
                
                String emailUsuario = rs.getString("email_usuario");
                String campo = rs.getString("campo");
                String valorNuevo = rs.getString("valor_nuevo");
                
                // Aplicar el cambio al usuario
                Usuario usuario = authService.obtenerPerfil(emailUsuario);
                if (usuario == null) {
                    ctx.status(404).json(Map.of("error", "Usuario no encontrado"));
                    return;
                }
                
                switch (campo) {
                    case "nombre": usuario.setNombre(valorNuevo); break;
                    case "telefono": usuario.setTelefono(valorNuevo); break;
                    case "direccion": usuario.setDireccion(valorNuevo); break;
                    case "email": usuario.setEmail(valorNuevo); break;
                }
                authService.actualizarPerfil(usuario);
                
                // Marcar como aprobado
                String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE cambios_pendientes SET estado = 'aprobado', fecha_revision = ?, revisado_por = ? WHERE id_cambio = ?");
                updateStmt.setString(1, fecha);
                updateStmt.setString(2, emailAdmin);
                updateStmt.setInt(3, idCambio);
                updateStmt.executeUpdate();
                
                ctx.json(Map.of("mensaje", "Cambio aprobado y aplicado", "campo", campo, "email", emailUsuario));
            } catch (SQLException e) {
                ctx.status(500).json(Map.of("error", e.getMessage()));
            }
        });
        
        // Rechazar un cambio pendiente
        app.put("/api/admin/cambios-pendientes/{id}/rechazar", ctx -> {
            int idCambio = Integer.parseInt(ctx.pathParam("id"));
            String emailAdmin = ctx.queryParam("adminEmail");
            
            try (Connection conn = ConexionDB.conectar()) {
                String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE cambios_pendientes SET estado = 'rechazado', fecha_revision = ?, revisado_por = ? WHERE id_cambio = ? AND estado = 'pendiente'");
                stmt.setString(1, fecha);
                stmt.setString(2, emailAdmin);
                stmt.setInt(3, idCambio);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    ctx.json(Map.of("mensaje", "Cambio rechazado"));
                } else {
                    ctx.status(404).json(Map.of("error", "Cambio pendiente no encontrado"));
                }
            } catch (SQLException e) {
                ctx.status(500).json(Map.of("error", e.getMessage()));
            }
        });

        // --- ENDPOINT: CHAT ASESOR IA ---
        app.post("/api/chat", ctx -> {
            ChatRequest request = ctx.bodyAsClass(ChatRequest.class);
            if (request.getPregunta() == null || request.getPregunta().trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "La pregunta no puede estar vacía", "exito", false));
                return;
            }
            ChatResponse response = asesorService.procesarPregunta(request);
            ctx.json(response);
        });

        // Health check del asesor IA
        app.get("/api/chat/health", ctx -> {
            ctx.json(Map.of(
                "status", asesorService.getEstado(),
                "modelo", asesorService.getModelo(),
                "apiKeyConfigurada", aiService.isConfigurado()
            ));
        });

        System.out.println("Servidor corriendo en: http://localhost:7070");
    }

    private static boolean fechasConConflicto(String fecha1, String fecha2) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dt1 = LocalDateTime.parse(fecha1, fmt);
            LocalDateTime dt2 = LocalDateTime.parse(fecha2, fmt);
            long diffHoras = Math.abs(Duration.between(dt1, dt2).toHours());
            return diffHoras < 2;
        } catch (Exception e) {
            return false;
        }
    }
}