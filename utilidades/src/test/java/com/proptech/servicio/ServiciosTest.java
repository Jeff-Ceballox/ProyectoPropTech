package com.proptech.servicio;

import com.proptech.modelo.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la capa de servicios del negocio.
 */
public class ServiciosTest {

    @Test
    public void probarGestorVisitasPrioridad() {
        GestorVisitasService gestor = new GestorVisitasService();
        
        // 1. Preparamos los "actores" (Moldes) para la visita
        Cliente clienteNormal = new Cliente("CC-111", "Juan", "555-0000", 150.0);
        Cliente clienteVIP = new Cliente("NIT-999", "Empresa Inversora", "555-9999", 900.0);
        Inmueble apto = new Inmueble("A-123", "Apartamento", "Norte", 200.0, 70.0);
        Asesor asesor = new Asesor("ID-01", "Ana Asesora", "Ventas");
        
        // 2. Creamos las visitas
        Visita vNormal = new Visita("V-001", clienteNormal, apto, asesor, "Mañana a las 10am");
        Visita vUrgente = new Visita("V-002", clienteVIP, apto, asesor, "Hoy a las 4pm");
        
        // 3. ¡ATENCIÓN A ESTO! Ingresamos la visita normal PRIMERO.
        gestor.programarVisita(vNormal, 3); // Prioridad baja (3)
        // Ingresamos la visita VIP SEGUNDA.
        gestor.programarVisita(vUrgente, 1); // Prioridad alta (1)
        
        // 4. Verificamos que la Cola de Prioridad haga su magia
        Visita primeraAtendida = gestor.atenderProximaVisita();
        
        // La prueba exige que la primera en salir sea la V-002 (VIP), aunque llegó de última
        assertNotNull(primeraAtendida, "Debería retornar una visita");
        assertEquals("V-002", primeraAtendida.getIdVisita(), "La visita VIP debe saltarse la fila y salir primero");
        assertEquals("Realizada", primeraAtendida.getEstado(), "El estado de la visita debe cambiar a 'Realizada'");
        
        // Queda 1 visita pendiente en la fila
        assertEquals(1, gestor.obtenerTotalVisitasPendientes(), "Debería quedar 1 visita en espera");
    }
}