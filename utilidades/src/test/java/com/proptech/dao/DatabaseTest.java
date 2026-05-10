package com.proptech.dao;

import com.proptech.modelo.Inmueble;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {

    @Test
    public void probarGuardadoYLecturaEnSQL() {
        // 1. Forzamos la creación del archivo y la tabla
        ConexionDB.inicializarTablas();
        
        InmuebleDAO dao = new InmuebleDAO();
        
        // 2. Creamos un inmueble de prueba (Usamos un ID aleatorio para que no choque si corremos el test varias veces)
        String idPrueba = "TEST-" + System.currentTimeMillis();
        Inmueble i1 = new Inmueble(idPrueba, "Apartamento", "Prueba DB", 100.0, 50.0);
        
        // 3. Lo guardamos en SQL
        dao.guardar(i1);
        
        // 4. Leemos toda la base de datos
        ListaEnlazada<Inmueble> guardados = dao.obtenerTodos();
        
        // 5. Verificamos que al menos haya 1 elemento guardado
        assertTrue(guardados.getTamaño() > 0, "La base de datos debe devolver al menos 1 inmueble");
        
        // Buscamos si nuestro inmueble específico se guardó
        boolean encontrado = false;
        for (int i = 0; i < guardados.getTamaño(); i++) {
            if (guardados.obtener(i).getCodigo().equals(idPrueba)) {
                encontrado = true;
                break;
            }
        }
        assertTrue(encontrado, "El inmueble de prueba debe existir en la base de datos SQL");
    }
}