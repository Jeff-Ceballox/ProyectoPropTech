package com.proptech.servicio;

import com.proptech.modelo.Cliente;
import com.proptech.utilidades.estructuras.TablaHash;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import com.proptech.dao.ClienteDAO;

/**
 * Servicio encargado de gestionar clientes.
 * Indexa los clientes en una TablaHash<String, Cliente> para búsquedas O(1) por identificación.
 * Sincronizado con SQLite para persistencia permanente.
 */
public class ClientesService {

    // Tabla Hash: clave = identificación (cédula/NIT), valor = objeto Cliente
    private TablaHash<String, Cliente> mapaClientes;

    // Puente de conexión a la base de datos
    private ClienteDAO clienteDAO;

    public ClientesService() {
        this.mapaClientes = new TablaHash<>(200);
        this.clienteDAO = new ClienteDAO();

        // Al iniciar, cargamos todos los clientes del disco duro a la memoria RAM
        cargarDatosDesdeSQL();
    }

    /**
     * Extrae los clientes de SQLite y alimenta nuestra TablaHash genérica.
     */
    private void cargarDatosDesdeSQL() {
        ListaEnlazada<Cliente> guardados = clienteDAO.obtenerTodos();

        for (int i = 0; i < guardados.getTamaño(); i++) {
            Cliente c = guardados.obtener(i);
            // Indexamos por identificación para búsqueda instantánea
            mapaClientes.insertar(c.getIdentificacion(), c);
        }
        System.out.println("Sincronización completada: " + guardados.getTamaño() + " clientes cargados en memoria RAM.");
    }

    /**
     * Registra un nuevo cliente tanto en DB permanente como en caché de memoria (TablaHash).
     */
    public void registrarCliente(Cliente cliente) {
        // 1. Guardamos permanentemente en SQLite
        clienteDAO.guardar(cliente);

        // 2. Indexamos en la TablaHash para no tener que consultar SQL cada vez
        mapaClientes.insertar(cliente.getIdentificacion(), cliente);
        System.out.println("Cliente " + cliente.getNombre() + " indexado en TablaHash.");
    }

    /**
     * Busca un cliente por identificación en O(1) — sin tocar el disco.
     */
    public Cliente buscarPorIdentificacion(String identificacion) {
        return mapaClientes.obtener(identificacion);
    }

    /**
     * Devuelve todos los clientes indexados en memoria.
     */
    public ListaEnlazada<Cliente> obtenerTodos() {
        return mapaClientes.valores();
    }

    /**
     * Actualiza un cliente en la DB y en la TablaHash.
     */
    public void actualizarCliente(Cliente cliente) {
        clienteDAO.actualizar(cliente);
        mapaClientes.insertar(cliente.getIdentificacion(), cliente);
    }
}
