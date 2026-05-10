package com.proptech.servicio;

import com.proptech.modelo.*;
import com.proptech.utilidades.estructuras.ListaEnlazada;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la capa de servicios del negocio.
 */
public class ServiciosTest {

    @Test
    public void probarInventarioInmuebles() {
        InventarioInmueblesService inventario = new InventarioInmueblesService();
        
        // 1. Creamos inmuebles usando nuestros "Moldes"
        Inmueble i1 = new Inmueble("A-001", "Apartamento", "Norte", 250.0, 80.0);
        Inmueble i2 = new Inmueble("C-045", "Casa", "Centro", 120.0, 150.0);
        
        // 2. Los registramos en el servicio
        inventario.registrarInmueble(i1);
        inventario.registrarInmueble(i2);
        
        // 3. Verificamos que la Tabla Hash lo encuentre instantáneamente
        Inmueble encontrado = inventario.buscarPorCodigo("C-045");
        
        assertNotNull(encontrado, "Debería encontrar el inmueble C-045");
        assertEquals("Casa", encontrado.getTipo(), "El tipo debería ser 'Casa'");
        assertEquals(120.0, encontrado.getPrecio(), "El precio debería coincidir");
        
        // 4. Verificamos qué pasa si buscamos un código falso
        assertNull(inventario.buscarPorCodigo("X-999"), "Debería retornar null para IDs falsos");
    }

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

    @Test
    public void probarHistorialDeshacerCambios() {
        HistorialCambiosService historial = new HistorialCambiosService();
        Inmueble local = new Inmueble("L-500", "Local", "Sur", 300.0, 120.0);
        
        // Verificamos el estado inicial
        assertEquals(300.0, local.getPrecio(), "El precio inicial debe ser 300.0");
        assertEquals("Disponible", local.getEstado(), "El estado inicial debe ser Disponible");
        
        // Hacemos un cambio: Alguien se equivoca y le pone 50.0 de precio (¡muy barato!) y lo marca Vendido
        historial.actualizarInmueble(local, 50.0, "Vendido");
        
        // Verificamos que el cambio erróneo se aplicó
        assertEquals(50.0, local.getPrecio(), "El precio cambió erróneamente a 50.0");
        assertEquals("Vendido", local.getEstado(), "El estado cambió a Vendido");
        
        // ¡Oh no! Usamos nuestro botón de pánico (Deshacer)
        boolean exito = historial.deshacerUltimoCambio();
        
        // Verificamos que la acción fue exitosa y los datos volvieron a la normalidad
        assertTrue(exito, "Debería haber un cambio para deshacer");
        assertEquals(300.0, local.getPrecio(), "El precio debe haber regresado a 300.0 (LIFO)");
        assertEquals("Disponible", local.getEstado(), "El estado debe haber regresado a Disponible");
    }

 
    @Test
    public void probarMotorRecomendaciones() {
        AnalisisRelacionesService motor = new AnalisisRelacionesService();
        
        // 1. Creamos clientes e inmuebles simulados
        Cliente juan = new Cliente("C-JUAN", "Juan", "111", 0);
        Cliente maria = new Cliente("C-MARIA", "Maria", "222", 0);
        
        Inmueble aptoNorte = new Inmueble("A-NORTE", "Apto", "Norte", 0, 0);
        Inmueble casaSur = new Inmueble("C-SUR", "Casa", "Sur", 0, 0);
        Inmueble localCentro = new Inmueble("L-CENTRO", "Local", "Centro", 0, 0);

        // 2. Simulamos el historial de visitas
        // Juan visita el Apto Norte
        motor.registrarInteres(juan, aptoNorte);
        
        // Maria visita el Apto Norte (tienen un gusto en común)
        motor.registrarInteres(maria, aptoNorte);
        // Y Maria también visita la Casa Sur
        motor.registrarInteres(maria, casaSur);
        // Y el Local Centro
        motor.registrarInteres(maria, localCentro);

        // 3. Le pedimos al sistema que recomiende algo para Juan
        // Como a Juan le gustó el Apto Norte, y a Maria también... 
        // el sistema debería recomendarle a Juan lo OTRO que vio Maria (Casa Sur y Local Centro)
        ListaEnlazada<String> recomendacionesJuan = motor.obtenerRecomendaciones("C-JUAN");

        // 4. Verificaciones
        assertNotNull(recomendacionesJuan, "La lista de recomendaciones no debe ser nula");
        assertEquals(2, recomendacionesJuan.getTamaño(), "Debería haber exactamente 2 recomendaciones para Juan");
        
        // Verificamos que contenga la Casa Sur (obtenemos el primer o segundo elemento, el orden puede variar)
        boolean recomendadaCasaSur = recomendacionesJuan.obtener(0).equals("C-SUR") || recomendacionesJuan.obtener(1).equals("C-SUR");
        assertTrue(recomendadaCasaSur, "El sistema debió recomendar la Casa Sur");
    }

}