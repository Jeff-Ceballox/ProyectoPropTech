package com.proptech;

import com.proptech.dao.ConexionDB;
import com.proptech.modelo.Inmueble;
import com.proptech.servicio.InventarioInmueblesService;
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
            inventarioService.registrarInmueble(new Inmueble("WEB-001", "Penthouse", "Zona Norte", 500.0, 200.0));
            inventarioService.registrarInmueble(new Inmueble("WEB-002", "Casa", "Zona Sur", 150.0, 90.0));
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

        System.out.println("Servidor corriendo en: http://localhost:7070");
    }
}