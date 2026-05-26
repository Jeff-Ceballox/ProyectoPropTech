package com.proptech.dao;

import com.proptech.modelo.Inmueble;
import com.proptech.modelo.Usuario;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {

    @Test
    public void probarGuardadoYLecturaEnSQL() {
        ConexionDB.inicializarTablas();
        InmuebleDAO dao = new InmuebleDAO();
        String idPrueba = "TEST-" + System.currentTimeMillis();
        Inmueble i1 = new Inmueble(idPrueba, "Apartamento", "Prueba DB", 100.0, 50.0, 2, 1, false, "Desc");
        dao.guardar(i1);
        ListaEnlazada<Inmueble> guardados = dao.obtenerTodos();
        assertTrue(guardados.getTamaño() > 0, "La base de datos debe devolver al menos 1 inmueble");
        boolean encontrado = false;
        for (int i = 0; i < guardados.getTamaño(); i++) {
            if (guardados.obtener(i).getCodigo().equals(idPrueba)) {
                encontrado = true;
                break;
            }
        }
        assertTrue(encontrado, "El inmueble de prueba debe existir en la base de datos SQL");
    }
    
    @Test
    public void verificarUsuariosPersisten() {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        ListaEnlazada<Usuario> usuarios = usuarioDAO.obtenerTodos();
        System.out.println("Total usuarios en BD: " + usuarios.getTamaño());
        for (int i = 0; i < usuarios.getTamaño(); i++) {
            Usuario u = usuarios.obtener(i);
            System.out.println("  - " + u.getEmail() + " (" + u.getRol().getNombre() + ")");
        }
    }
}