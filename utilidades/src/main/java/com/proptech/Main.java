package com.proptech;

import com.proptech.utilidades.estructuras.ListaEnlazada;
import com.proptech.utilidades.estructuras.Pila;
import com.proptech.utilidades.estructuras.Cola;
import com.proptech.utilidades.estructuras.ColaPrioridad;

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
        
    }
}