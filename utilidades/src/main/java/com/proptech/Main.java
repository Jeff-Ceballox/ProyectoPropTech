package com.proptech;

import com.proptech.dao.ConexionDB;
import com.proptech.modelo.Inmueble;
import com.proptech.servicio.InventarioInmueblesService;
import com.proptech.servicio.AlertasService;
import com.proptech.modelo.Alerta;
import com.proptech.utilidades.estructuras.ListaEnlazada;

import io.javalin.Javalin;
import java.util.ArrayList;
import java.util.List;

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
        // Borramos la ruta app.get("/", ...) que causaba el conflicto

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

        System.out.println("Servidor corriendo en: http://localhost:7070");
    }
}