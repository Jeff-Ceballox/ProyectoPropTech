package com.proptech.servicio;

import com.proptech.modelo.Inmueble;
import com.proptech.utilidades.estructuras.TablaHash;
import com.proptech.utilidades.estructuras.ArbolBinarioBusqueda;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import com.proptech.dao.InmuebleDAO;

/**
 * Servicio encargado de gestionar los inmuebles.
 * Ahora sincronizado con la base de datos SQL para persistencia permanente.
 */
public class InventarioInmueblesService {
    
    private TablaHash<String, Inmueble> mapaInmuebles;
    private ArbolBinarioBusqueda<Double, Inmueble> arbolPorPrecio;
    
    // Nuestro puente de conexión a la base de datos
    private InmuebleDAO inmuebleDAO; 

    public InventarioInmueblesService() {
        this.mapaInmuebles = new TablaHash<>(100);
        this.arbolPorPrecio = new ArbolBinarioBusqueda<>();
        this.inmuebleDAO = new InmuebleDAO();

        // Al iniciar el servicio, cargamos todo el disco duro a la memoria RAM
        cargarDatosDesdeSQL(); 
    }

    /**
     * Extrae los datos de SQLite y alimenta nuestras estructuras genéricas.
     */
    private void cargarDatosDesdeSQL() {
        ListaEnlazada<Inmueble> guardados = inmuebleDAO.obtenerTodos();
        
        for (int i = 0; i < guardados.getTamaño(); i++) {
            Inmueble obj = guardados.obtener(i);
            
            // Llenamos nuestras estructuras ultrarrápidas
            mapaInmuebles.insertar(obj.getCodigo(), obj);
            arbolPorPrecio.insertar(obj.getPrecio(), obj);
        }
        System.out.println("Sincronización completada: " + guardados.getTamaño() + " inmuebles cargados en memoria RAM.");
    }

    /**
     * Registra un nuevo inmueble tanto en la DB permanente como en la memoria caché (Estructuras).
     */
    public void registrarInmueble(Inmueble inmueble) {
        // 1. Lo guardamos permanentemente en el archivo SQLite
        inmuebleDAO.guardar(inmueble);
        
        // 2. Lo guardamos en las estructuras para no tener que consultar SQL cada vez que busquemos
        mapaInmuebles.insertar(inmueble.getCodigo(), inmueble);
        arbolPorPrecio.insertar(inmueble.getPrecio(), inmueble);
    }

    public Inmueble buscarPorCodigo(String codigo) {
        // La búsqueda sigue siendo instantánea porque lee de la RAM, no del disco duro
        return mapaInmuebles.obtener(codigo);
    }

    public void imprimirReportePorPrecio() {
        System.out.println("\n--- Catálogo de Inmuebles (Ordenado por Precio) ---");
        arbolPorPrecio.imprimirOrdenado();
    }

    /**
     * Obtiene todos los inmuebles del inventario.
     * @return Lista de todos los inmuebles
     */
    public ListaEnlazada<Inmueble> obtenerTodos() {
        return mapaInmuebles.valores();
    }
}