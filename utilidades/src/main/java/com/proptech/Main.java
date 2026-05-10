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

        // 3. Encendemos el Servidor Web Javalin
        Javalin app = Javalin.create(config -> {
            // Permitimos que cualquier página web se conecte a nuestra API (CORS)
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.anyHost());
            });
        }).start(7070);

        // --- DEFINICIÓN DE RUTAS (ENDPOINTS) ---

        // Ruta de prueba para saber si el servidor está vivo
        app.get("/", ctx -> ctx.result("¡Bienvenido a la API de PropTech!"));

        // Ruta para obtener todos los inmuebles
        app.get("/api/inmuebles", ctx -> {
            // Nota técnica: Convertimos nuestra ListaEnlazada a una List estándar de Java
            // SOLO para que el traductor JSON lo envíe correctamente al Frontend con formato de arreglo [...]
            ListaEnlazada<Inmueble> guardados = new com.proptech.dao.InmuebleDAO().obtenerTodos();
            List<Inmueble> listaParaWeb = new ArrayList<>();
            for (int i = 0; i < guardados.getTamaño(); i++) {
                listaParaWeb.add(guardados.obtener(i));
            }
            // Devolvemos los datos en formato JSON
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