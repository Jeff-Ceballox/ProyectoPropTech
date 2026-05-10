package com.proptech.servicio;

import com.proptech.modelo.Inmueble;
import com.proptech.utilidades.estructuras.TablaHash;
import com.proptech.utilidades.estructuras.ArbolBinarioBusqueda;

/**
 * Servicio encargado de gestionar toda la lógica relacionada con los inmuebles.
 * Utiliza estructuras de datos avanzadas para garantizar alta eficiencia.
 */
public class InventarioInmueblesService {
    
    // 1. Tabla Hash para búsquedas instantáneas por código (Ej: "A-001")
    private TablaHash<String, Inmueble> mapaInmuebles;
    
    // 2. Árbol BST para mantener los inmuebles siempre ordenados por precio
    private ArbolBinarioBusqueda<Double, Inmueble> arbolPorPrecio;

    public InventarioInmueblesService() {
        // Inicializamos la tabla hash con una capacidad base de 100 "cajas"
        this.mapaInmuebles = new TablaHash<>(100);
        this.arbolPorPrecio = new ArbolBinarioBusqueda<>();
    }

    /**
     * Registra un nuevo inmueble en el sistema en todas las estructuras necesarias.
     */
    public void registrarInmueble(Inmueble inmueble) {
        // 1. Lo guardamos en la Tabla Hash para encontrarlo rápido luego
        mapaInmuebles.insertar(inmueble.getCodigo(), inmueble);
        
        // 2. Lo guardamos en el Árbol para los reportes financieros
        arbolPorPrecio.insertar(inmueble.getPrecio(), inmueble);
    }

    /**
     * Busca un inmueble en tiempo récord (O(1)) usando su código.
     */
    public Inmueble buscarPorCodigo(String codigo) {
        return mapaInmuebles.obtener(codigo);
    }

    /**
     * Imprime el catálogo de inmuebles desde el más barato al más costoso.
     */
    public void imprimirReportePorPrecio() {
        System.out.println("\n--- Catálogo de Inmuebles (Ordenado por Precio) ---");
        arbolPorPrecio.imprimirOrdenado();
    }
}