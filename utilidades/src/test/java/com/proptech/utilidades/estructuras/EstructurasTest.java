package com.proptech.utilidades.estructuras;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase dedicada exclusivamente a probar nuestras estructuras de datos.
 */
public class EstructurasTest {

    @Test
    public void probarListaEnlazada() {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        lista.agregar("Apto 1");
        lista.agregar("Apto 2");
        
        // Verificamos que el tamaño sea exactamente 2
        assertEquals(2, lista.getTamaño(), "El tamaño de la lista debería ser 2");
        // Verificamos que el primer elemento sea 'Apto 1'
        assertEquals("Apto 1", lista.obtener(0), "El índice 0 debería ser Apto 1");
    }

    @Test
    public void probarPilaDeshacer() {
        Pila<String> historial = new Pila<>();
        historial.apilar("Acción 1");
        historial.apilar("Acción 2"); // Esta fue la última en entrar
        
        // Verificamos que la primera en salir sea la Acción 2 (LIFO)
        assertEquals("Acción 2", historial.desapilar(), "La cima debería ser Acción 2");
        assertEquals(1, historial.getTamaño(), "El tamaño debería reducirse a 1");
    }

    @Test
    public void probarTablaHash() {
        TablaHash<String, String> base = new TablaHash<>(5);
        base.insertar("CC-123", "Juan");
        
        assertEquals("Juan", base.obtener("CC-123"), "Debería encontrar a Juan al instante");
        assertNull(base.obtener("XX-999"), "Debería retornar null si la clave no existe");
    }
}