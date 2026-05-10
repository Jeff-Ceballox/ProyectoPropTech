package com.proptech.servicio;

import com.proptech.modelo.Visita;
import com.proptech.utilidades.estructuras.ColaPrioridad;

/**
 * Servicio encargado de gestionar la agenda y el flujo de atención de la inmobiliaria.
 * Utiliza una Cola de Prioridad para garantizar que las solicitudes urgentes se procesen primero.
 */
public class GestorVisitasService {
    
    // Nuestra estructura genérica ahora se especializa en guardar objetos Visita
    private ColaPrioridad<Visita> visitasPendientes;

    public GestorVisitasService() {
        this.visitasPendientes = new ColaPrioridad<>();
    }

    /**
     * Ingresa una nueva visita a la sala de espera (cola) según su urgencia.
     * @param visita El objeto con los datos completos de la cita.
     * @param prioridad 1 (VIP/Urgente), 2 (Alta), 3 (Normal).
     */
    public void programarVisita(Visita visita, int prioridad) {
        visitasPendientes.encolar(visita, prioridad);
        // Dejamos un registro visual en consola temporalmente para auditoría
        System.out.println("-> Visita [" + visita.getIdVisita() + "] programada con prioridad: " + prioridad);
    }

    /**
     * El sistema extrae la visita más importante para ser atendida por el asesor.
     * @return El objeto Visita listo para procesar, o null si no hay fila.
     */
    public Visita atenderProximaVisita() {
        if (visitasPendientes.estaVacia()) {
            return null;
        }
        
        // Desencolamos (sacamos de la fila) a la persona con mayor prioridad
        Visita proxima = visitasPendientes.desencolar();
        
        // Actualizamos su estado comercial
        proxima.setEstado("Realizada"); 
        
        return proxima;
    }

    // Consulta rápida para saber cuánta gente hay en espera
    public int obtenerTotalVisitasPendientes() {
        return visitasPendientes.getTamaño();
    }
}