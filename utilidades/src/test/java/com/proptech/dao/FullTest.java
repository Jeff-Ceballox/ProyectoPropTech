package com.proptech.dao;

import com.proptech.modelo.Inmueble;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FullTest {

    @Test
    public void testFullConexionYDatos() {
        System.out.println("=== TEST COMPLETO DE CONEXIÓN ===");
        
        // 1. Inicializar BD
        System.out.println("1. Inicializando BD...");
        ConexionDB.inicializarTablas();
        
        // 2. Verificar conexión
        System.out.println("2. Verificando conexión...");
        var conn = ConexionDB.conectar();
        assertNotNull(conn, "La conexión no debe ser null");
        
        // 3. Cargar inmuebles
        System.out.println("3. Cargando inmuebles...");
        InmuebleDAO inmuebleDAO = new InmuebleDAO();
        ListaEnlazada<Inmueble> inmuebles = inmuebleDAO.obtenerTodos();
        System.out.println("   Total inmuebles: " + inmuebles.getTamaño());
        for (int i = 0; i < inmuebles.getTamaño(); i++) {
            System.out.println("   - " + inmuebles.obtener(i).getCodigo());
        }
        
        // 4. Verificar usuarios
        System.out.println("4. Verificando usuarios...");
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        System.out.println("   Usuarios cargados en cache: " + usuarioDAO.obtenerTodos().getTamaño());
        
        System.out.println("=== TEST COMPLETADO ===");
    }
}