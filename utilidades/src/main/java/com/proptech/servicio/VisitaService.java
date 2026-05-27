package com.proptech.servicio;

import com.proptech.modelo.Visita;
import com.proptech.modelo.Cliente;
import com.proptech.modelo.Inmueble;
import com.proptech.modelo.Asesor;
import com.proptech.utilidades.estructuras.TablaHash;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import com.proptech.dao.VisitaDAO;
import com.proptech.dao.ClienteDAO;
import com.proptech.dao.InmuebleDAO;

/**
 * Servicio encargado de gestionar las visitas programadas entre clientes, asesores e inmuebles.
 * Indexa las visitas en una TablaHash<String, Visita> para búsquedas O(1) por ID.
 * Sincronizado con SQLite para persistencia permanente.
 */
public class VisitaService {

    // Tabla Hash: clave = idVisita, valor = objeto Visita
    private TablaHash<String, Visita> mapaVisitas;

    // Puentes de conexión a las bases de datos
    private VisitaDAO visitaDAO;
    private ClienteDAO clienteDAO;
    private InmuebleDAO inmuebleDAO;
    // Nota: Necesitaríamos un AsesorDAO para completar las relaciones

    public VisitaService() {
        this.mapaVisitas = new TablaHash<>(200);
        this.visitaDAO = new VisitaDAO();
        this.clienteDAO = new ClienteDAO();
        this.inmuebleDAO = new InmuebleDAO();

        // Al iniciar, cargamos todas las visitas del disco duro a la memoria RAM
        cargarDatosDesdeSQL();
    }

    /**
     * Extrae las visitas de SQLite y alimenta nuestra TablaHash genérica.
     * Nota: Esta implementación simplificada asume que los objetos relacionados ya existen en memoria.
     * Una implementación más robusta haría joins o recargaría los objetos relacionados.
     */
    private void cargarDatosDesdeSQL() {
        ListaEnlazada<Visita> guardados = visitaDAO.obtenerTodas();

        for (int i = 0; i < guardados.getTamaño(); i++) {
            Visita v = guardados.obtener(i);
            // Indexamos por idVisita para búsqueda instantánea
            mapaVisitas.insertar(v.getIdVisita(), v);
        }
        System.out.println("Sincronización completada: " + guardados.getTamaño() + " visitas cargadas en memoria RAM.");
    }

    /**
     * Programa una nueva visita tanto en DB permanente como en caché de memoria (TablaHash).
     * Valida disponibilidad de cliente, inmueble y asesor.
     */
    public void programarVisita(Visita visita) {
        // 1. Validar disponibilidad (versión simplificada)
        if (!validarDisponibilidad(visita)) {
            System.out.println("Error: No se puede programar la visita debido a conflictos de disponibilidad");
            return;
        }

        // 2. Guardamos permanentemente en SQLite
        visitaDAO.guardar(visita);

        // 3. Indexamos en la TablaHash para no tener que consultar SQL cada vez
        mapaVisitas.insertar(visita.getIdVisita(), visita);
        System.out.println("Visita " + visita.getIdVisita() + " programada e indexada en TablaHash.");
    }

    /**
     * Valida la disponibilidad de los recursos para una visita.
     * Versión simplificada - en producción sería más robusta.
     */
    private boolean validarDisponibilidad(Visita visita) {
        // Aquí verificaríamos:
        // - Que el cliente existe y está activo
        // - Que el inmueble existe y está disponible
        // - Que el asesor existe y no tiene conflicto de horario
        // Por ahora retornamos true para permitir el flujo
        return true;
    }

    /**
     * Busca una visita por su ID en O(1) — sin tocar el disco.
     */
    public Visita buscarPorId(String idVisita) {
        return mapaVisitas.obtener(idVisita);
    }

    /**
     * Devuelve todas las visitas indexadas en memoria.
     */
    public ListaEnlazada<Visita> obtenerTodas() {
        return mapaVisitas.valores();
    }

    /**
     * Obtiene todas las visitas de un cliente específico.
     */
    public ListaEnlazada<Visita> obtenerVisitasPorCliente(String identificacionCliente) {
        ListaEnlazada<Visita> resultado = new ListaEnlazada<>();
        ListaEnlazada<Visita> todas = obtenerTodas();
        
        for (int i = 0; i < todas.getTamaño(); i++) {
            Visita visita = todas.obtener(i);
            // Nota: Necesitamos mejorar esto cuando Visita tenga getters completos
            // Por ahora asumimos que podemos acceder al cliente
            // TODO: Mejorar cuando Visita tenga getCliente() funcional
            if (visita.getCliente() != null && 
                visita.getCliente().getIdentificacion() != null &&
                identificacionCliente.equals(visita.getCliente().getIdentificacion())) {
                resultado.agregar(visita);
            }
        }
        return resultado;
    }

    /**
     * Obtiene todas las visitas de un inmueble específico.
     */
    public ListaEnlazada<Visita> obtenerVisitasPorInmueble(String codigoInmueble) {
        ListaEnlazada<Visita> resultado = new ListaEnlazada<>();
        ListaEnlazada<Visita> todas = obtenerTodas();
        
        for (int i = 0; i < todas.getTamaño(); i++) {
            Visita visita = todas.obtener(i);
            // TODO: Mejorar cuando Visita tenga getInmueble() funcional
            if (visita.getInmueble() != null && 
                visita.getInmueble().getCodigo() != null &&
                codigoInmueble.equals(visita.getInmueble().getCodigo())) {
                resultado.agregar(visita);
            }
        }
        return resultado;
    }

    /**
     * Actualiza el estado de una visita (ej: de Pendiente a Confirmada o Realizada).
     */
    public void actualizarEstadoVisita(String idVisita, String nuevoEstado) {
        Visita visita = buscarPorId(idVisita);
        if (visita != null) {
            visita.setEstado(nuevoEstado);
            visitaDAO.actualizar(visita);
            mapaVisitas.insertar(idVisita, visita); // Actualizar en caché
            System.out.println("Estado de visita " + idVisita + " actualizado a: " + nuevoEstado);
        } else {
            System.out.println("Error: Visita no encontrada con ID " + idVisita);
        }
    }

    /**
     * Reprograma una visita cambiando su fecha/hora.
     */
    public void reprogramarVisita(String idVisita, String nuevaFechaHora) {
        Visita visita = buscarPorId(idVisita);
        if (visita != null) {
            visita.setFechaHora(nuevaFechaHora);
            visitaDAO.actualizar(visita);
            mapaVisitas.insertar(idVisita, visita); // Actualizar en caché
            System.out.println("Visita " + idVisita + " reprogramada para: " + nuevaFechaHora);
        } else {
            System.out.println("Error: Visita no encontrada con ID " + idVisita);
        }
    }

    /**
     * Cancela una visita.
     */
    public void cancelarVisita(String idVisita) {
        actualizarEstadoVisita(idVisita, "Cancelada");
    }
}