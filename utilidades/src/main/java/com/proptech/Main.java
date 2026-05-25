package com.proptech;

import com.proptech.dao.ConexionDB;
import com.proptech.modelo.Inmueble;
import com.proptech.modelo.Cliente;
import com.proptech.modelo.Operacion;
import com.proptech.modelo.Alerta;
import com.proptech.utilidades.estructuras.ListaEnlazada;

import com.proptech.servicio.InventarioInmueblesService;
import com.proptech.servicio.ClientesService;
import com.proptech.servicio.AlertasService;
import com.proptech.servicio.OperacionesService;
import com.proptech.servicio.RecomendacionService;
import com.proptech.servicio.DetectorAnomaliesService;
import com.proptech.servicio.ReporteService;
import com.proptech.servicio.AuthService;
import com.proptech.modelo.Usuario;

import io.javalin.Javalin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Iniciando Plataforma PropTech ===");
        
        // 1. Inicializamos la Base de Datos SQLite
        ConexionDB.inicializarTablas();

        // 2. Cargamos nuestro cerebro (La capa de Servicio que usa las Tablas Hash y Árboles)
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

        // 2.4 Iniciamos el servicio de recomendación
        RecomendacionService recomendacionService = new RecomendacionService();

        // 2.5 Iniciamos el servicio de detección de anomalías
        DetectorAnomaliesService detectorAnomaliesService = new DetectorAnomaliesService();

        // 2.6 Iniciamos el servicio de reportes
        ReporteService reporteService = new ReporteService();

        // 2.7 Iniciamos el servicio de autenticación
        AuthService authService = new AuthService();

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
            ListaEnlazada<Inmueble> guardados = new com.proptech.dao.InmuebleDAO().obtenerTodos();
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

        // --- RUTAS API DE AUTENTICACIÓN ---
        
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
                ctx.json(Map.of("mensaje", "Login exitoso", "rol", usuario.getRol().getNombre(), "nombre", usuario.getNombre()));
            } else {
                ctx.status(401).json(Map.of("error", "Credenciales inválidas"));
            }
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
        
        // Actualizar perfil
        app.put("/api/usuario/actualizar", ctx -> {
            Map<String, Object> datos = ctx.bodyAsClass(Map.class);
            String email = (String) datos.get("email");
            
            Usuario usuario = authService.obtenerPerfil(email);
            if (usuario == null) {
                ctx.status(404).json(Map.of("error", "Usuario no encontrado"));
                return;
            }
            
            if (datos.containsKey("nombre")) usuario.setNombre((String) datos.get("nombre"));
            if (datos.containsKey("telefono")) usuario.setTelefono((String) datos.get("telefono"));
            if (datos.containsKey("direccion")) usuario.setDireccion((String) datos.get("direccion"));
            if (datos.containsKey("intereses")) usuario.setIntereses((String) datos.get("intereses"));
            if (datos.containsKey("fotoPerfil")) usuario.setFotoPerfil((String) datos.get("fotoPerfil"));
            
            if (authService.actualizarPerfil(usuario)) {
                ctx.json(Map.of("mensaje", "Perfil actualizado"));
            } else {
                ctx.status(500).json(Map.of("error", "Error al actualizar"));
            }
        });

        System.out.println("Servidor corriendo en: http://localhost:7070");
    }
}