package com.proptech.servicio;

import com.proptech.modelo.Asesor;
import com.proptech.modelo.Cliente;
import com.proptech.modelo.Inmueble;
import com.proptech.modelo.Operacion;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import com.proptech.utilidades.estructuras.TablaHash;
import com.proptech.dao.OperacionDAO;
import com.proptech.dao.InmuebleDAO;
import com.proptech.dao.ClienteDAO;

/**
 * Servicio encargado de gestionar las operaciones de negocio (arriendo, venta, renovación, cancelación).
 * Indexa las operaciones en una TablaHash<String, Operacion> para búsquedas O(1) por ID.
 * Sincronizado con SQLite para persistencia permanente.
 * Utiliza ListaEnlazada para mantener el historial como se requiere en los requisitos.
 */
public class OperacionesService {

    // Tabla Hash: clave = idOperacion, valor = objeto Operacion
    private TablaHash<String, Operacion> mapaOperaciones;

    // Lista Enlazada para historial de operaciones (requisito específico)
    private ListaEnlazada<Operacion> historialOperaciones;

    // Puentes de conexión a las bases de datos
    private OperacionDAO operacionDAO;
    private InmuebleDAO inmuebleDAO;
    private ClienteDAO clienteDAO;

    public OperacionesService() {
        this.mapaOperaciones = new TablaHash<>(200);
        this.historialOperaciones = new ListaEnlazada<>();
        this.operacionDAO = new OperacionDAO();
        this.inmuebleDAO = new InmuebleDAO();
        this.clienteDAO = new ClienteDAO();

        // Al iniciar, cargamos todas las operaciones del disco duro a la memoria RAM
        cargarDatosDesdeSQL();
    }

    /**
     * Extrae las operaciones de SQLite y alimenta nuestras estructuras de datos.
     */
    private void cargarDatosDesdeSQL() {
        ListaEnlazada<Operacion> guardados = operacionDAO.obtenerTodas();

        for (int i = 0; i < guardados.getTamaño(); i++) {
            Operacion op = guardados.obtener(i);
            // Indexamos por idOperacion para búsqueda instantánea
            mapaOperaciones.insertar(op.getIdOperacion(), op);
            // También lo agregamos al historial
            historialOperaciones.agregar(op);
        }
        System.out.println("Sincronización completada: " + guardados.getTamaño() + " operaciones cargadas en memoria RAM.");
    }

    /**
     * Registra una nueva operación tanto en DB permanente como en caché de memoria.
     * Actualiza estados relacionados (inmueble, asesor) según corresponda.
     */
    public void registrarOperacion(Operacion operacion) {
        // 1. Guardamos permanentemente en SQLite
        operacionDAO.guardar(operacion);

        // 2. Indexamos en la TablaHash para búsquedas O(1)
        mapaOperaciones.insertar(operacion.getIdOperacion(), operacion);
        
        // 3. Agregamos al historial de operaciones (ListaEnlazada como se requirió)
        historialOperaciones.agregar(operacion);

        // 4. Actualizamos estado del inmueble
        Inmueble inmueble = operacion.getInmueble();
        if (inmueble != null) {
            if ("Venta".equalsIgnoreCase(operacion.getTipo())) {
                inmueble.setEstado("Vendido");
                // Actualizar el inmueble en BD
                inmuebleDAO.actualizar(inmueble);
            } else if ("Arriendo".equalsIgnoreCase(operacion.getTipo())) {
                inmueble.setEstado("Arrendado");
                // Actualizar el inmueble en BD
                inmuebleDAO.actualizar(inmueble);
            } else if ("Renovación".equalsIgnoreCase(operacion.getTipo())) {
                // Para renovación, el inmueble sigue disponible o arrendado según corresponda
                // Pero actualizamos la fecha en la operación
            }
        }
        
        // 5. Incrementamos métricas del asesor
        Asesor asesor = operacion.getAsesor();
        if (asesor != null) {
            asesor.registrarNegocioExitoso();
            // Nota: En una implementación completa, también actualizaríamos el asesor en BD
        }
        
        System.out.println("Operación registrada exitosamente: " + operacion.toString());
    }

    /**
     * Registra una renovación de contrato.
     */
    public void registrarRenovacion(Operacion operacion) {
        // Establecemos el tipo como Renovación
        operacion.setTipo("Renovación");
        registrarOperacion(operacion);
    }

    /**
     * Registra una cancelación de negocio.
     */
    public void registrarCancelacion(Operacion operacion) {
        // Establecemos el tipo como Cancelación
        operacion.setTipo("Cancelación");
        // En caso de cancelación, podríamos cambiar el estado del inmueble de nuevo a Disponible
        Inmueble inmueble = operacion.getInmueble();
        if (inmueble != null) {
            // Solo cambiamos a disponible si estaba arrendado (no si era vendido)
            // En un sistema más complejo, tendríamos estados más detallados
        }
        registrarOperacion(operacion);
    }

    /**
     * Busca una operación por su ID en O(1) — sin tocar el disco.
     */
    public Operacion buscarPorId(String idOperacion) {
        return mapaOperaciones.obtener(idOperacion);
    }

    /**
     * Devuelve todas las operaciones indexadas en memoria.
     */
    public ListaEnlazada<Operacion> obtenerTodas() {
        return mapaOperaciones.valores();
    }

    /**
     * Obtiene el historial completo de operaciones (ListaEnlazada como se requirió).
     */
    public ListaEnlazada<Operacion> obtenerHistorialCompleto() {
        return historialOperaciones;
    }

    /**
     * Obtiene operaciones por tipo (Venta, Arriendo, Renovación, Cancelación).
     */
    public ListaEnlazada<Operacion> obtenerPorTipo(String tipo) {
        return operacionDAO.obtenerPorTipo(tipo);
    }

    /**
     * Obtiene operaciones por ID de asesor.
     */
    public ListaEnlazada<Operacion> obtenerPorAsesor(String idAsesor) {
        return operacionDAO.obtenerPorAsesor(idAsesor);
    }

    /**
     * Obtiene operaciones por ID de cliente.
     */
    public ListaEnlazada<Operacion> obtenerPorCliente(String idCliente) {
        // Similar a obtenerPorAsesor pero por cliente
        ListaEnlazada<Operacion> lista = new ListaEnlazada<>();
        ListaEnlazada<Operacion> todas = obtenerTodas();
        
        for (int i = 0; i < todas.getTamaño(); i++) {
            Operacion op = todas.obtener(i);
            if (op.getCliente() != null && op.getCliente().getIdentificacion().equals(idCliente)) {
                lista.agregar(op);
            }
        }
        return lista;
    }

    /**
     * Actualiza una operación existente.
     */
    public void actualizarOperacion(Operacion operacion) {
        operacionDAO.actualizar(operacion);
        mapaOperaciones.insertar(operacion.getIdOperacion(), operacion);
        // También actualizar en historial si es necesario
        System.out.println("Operación " + operacion.getIdOperacion() + " actualizada.");
    }
}
