package com.proptech.servicio;

import com.proptech.modelo.Inmueble;
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
}