package com.proptech;

import com.proptech.utilidades.estructuras.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Pruebas de Plataforma PropTech ===");
        
        // 1. Prueba de Lista (Lo que ya hicimos)
        ListaEnlazada<String> favoritos = new ListaEnlazada<>();
        favoritos.agregar("Apto Norte");
        System.out.println("Lista de Favoritos OK. Total: " + favoritos.getTamaño());

        // 2. Prueba de Pila (Historial para Deshacer)
        System.out.println("\n--- Historial de Edición de Precio (Pila) ---");
        Pila<String> historialCambios = new Pila<>();
        historialCambios.apilar("Precio inicial: $100M");
        historialCambios.apilar("Cambio a: $105M");
        historialCambios.apilar("Cambio a: $110M"); // Me equivoqué, quiero deshacer
        
        System.out.println("Deshaciendo última acción... Se eliminó: " + historialCambios.desapilar());
        System.out.println("Precio actual tras deshacer: " + historialCambios.desapilar());

        // 3. Prueba de Cola (Solicitudes de Visitas)
        System.out.println("\n--- Solicitudes de Visitas (Cola) ---");
        Cola<String> visitasPendientes = new Cola<>();
        visitasPendientes.encolar("Cliente Juan - Visita Apto Norte");
        visitasPendientes.encolar("Cliente Maria - Visita Casa Sur");
        
        System.out.println("Atendiendo solicitud: " + visitasPendientes.desencolar());
        System.out.println("Atendiendo solicitud: " + visitasPendientes.desencolar());
        System.out.println("¿Quedan visitas pendientes? " + (visitasPendientes.estaVacia() ? "No" : "Sí"));

        // 4. Prueba de Cola de Prioridad (Visitas Urgentes)
        System.out.println("\n--- Solicitudes de Visitas por Prioridad ---");
        ColaPrioridad<String> visitasInteligentes = new ColaPrioridad<>();
        
        // Encolamos con diferentes prioridades (1 es VIP/Urgente, 3 es Normal)
        visitasInteligentes.encolar("Cliente Normal 1 - Casa Sur", 3);
        visitasInteligentes.encolar("Cliente Normal 2 - Apto Este", 3);
        visitasInteligentes.encolar("Cliente VIP (Inversor) - Edificio Centro", 1); // ¡Llegó al último pero es VIP!
        visitasInteligentes.encolar("Cliente Urgente - Contrato por vencer", 2);

        System.out.println("Atendiendo 1ro: " + visitasInteligentes.desencolar());
        System.out.println("Atendiendo 2do: " + visitasInteligentes.desencolar());
        System.out.println("Atendiendo 3ro: " + visitasInteligentes.desencolar());
        System.out.println("Atendiendo 4to: " + visitasInteligentes.desencolar());

        // 5. Prueba de Tabla Hash (Búsqueda ultrarrápida)
        System.out.println("\n--- Base de Datos en Memoria (Tabla Hash) ---");
        
        // Creamos una tabla que recibe Strings como clave (Cédula) y Strings como valor (Datos)
        // Le damos una capacidad inicial de 10 "cajas"
        TablaHash<String, String> baseClientes = new TablaHash<>(10);
        
        // Insertamos clientes (Clave, Valor)
        baseClientes.insertar("CC-1001", "Juan Perez - Presupuesto: $150M");
        baseClientes.insertar("CC-1002", "Maria Gomez - Presupuesto: $200M");
        baseClientes.insertar("CE-9005", "Empresa XYZ - Presupuesto: $800M");

        System.out.println("Buscando CC-1002: " + baseClientes.obtener("CC-1002"));
        System.out.println("Buscando CE-9005: " + baseClientes.obtener("CE-9005"));
        System.out.println("Buscando ID falso: " + baseClientes.obtener("CC-0000")); // Debería dar null

        // 6. Prueba de Árbol (Ordenamiento automático)
        System.out.println("\n--- Ordenamiento de Inmuebles por Precio (Árbol BST) ---");
        // Clave: Double (Precio), Valor: String (Nombre)
        ArbolBinarioBusqueda<Double, String> arbolPrecios = new ArbolBinarioBusqueda<>();
        
        arbolPrecios.insertar(250.5, "Casa Sur (250.5M)");
        arbolPrecios.insertar(120.0, "Apto Centro (120.0M)");
        arbolPrecios.insertar(300.0, "Penthouse Norte (300.0M)");
        arbolPrecios.insertar(180.5, "Local Comercial (180.5M)");

        // Al imprimir, debería mostrarlos de menor a mayor precio automáticamente
        arbolPrecios.imprimirOrdenado();
           

        // 7. Prueba de Grafo (Relaciones complejas)
                System.out.println("\n--- Análisis de Relaciones Cliente-Inmueble (Grafo) ---");
                Grafo<String> redInmobiliaria = new Grafo<>();
                
                // 1. Agregamos los "Nodos" (Clientes e Inmuebles)
                redInmobiliaria.agregarVertice("Juan (Cliente)");
                redInmobiliaria.agregarVertice("Maria (Cliente)");
                redInmobiliaria.agregarVertice("Apto Norte (Inmueble)");
                redInmobiliaria.agregarVertice("Casa Centro (Inmueble)");
        
                // 2. Creamos las "Conexiones" (Quien visitó qué)
                // Juan visitó ambos
                redInmobiliaria.agregarArista("Juan (Cliente)", "Apto Norte (Inmueble)");
                redInmobiliaria.agregarArista("Juan (Cliente)", "Casa Centro (Inmueble)");
                
                // Maria solo visitó uno
                redInmobiliaria.agregarArista("Maria (Cliente)", "Casa Centro (Inmueble)");
        
                // 3. Imprimimos el mapa para análisis
                redInmobiliaria.imprimirRelaciones();
    }
}