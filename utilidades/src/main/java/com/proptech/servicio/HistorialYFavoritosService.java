package com.proptech.servicio;

import com.proptech.modelo.Cliente;
import com.proptech.modelo.Inmueble;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import com.proptech.dao.ClienteDAO;

/**
 * Servicio encargado de gestionar el historial de consultas y favoritos de los clientes.
 * Mantiene el historial de inmuebles consultados y marcados como favoritos por cada cliente.
 */
public class HistorialYFavoritosService {

    private ClienteDAO clienteDAO;

    public HistorialYFavoritosService() {
        this.clienteDAO = new ClienteDAO();
    }

    /**
     * Registra que un cliente ha consultado un inmueble.
     * @param identificacionCliente Identificación del cliente
     * @param inmueble Inmueble consultado
     */
    public void registrarConsulta(String identificacionCliente, Inmueble inmueble) {
        Cliente cliente = clienteDAO.obtenerPorId(identificacionCliente);
        if (cliente != null) {
            cliente.agregarConsulta(inmueble);
            clienteDAO.actualizar(cliente); // Persistir cambios
            System.out.println("Consulta registrada: Cliente " + identificacionCliente + " consultó inmueble " + inmueble.getCodigo());
        } else {
            System.out.println("Error: Cliente no encontrado con identificación " + identificacionCliente);
        }
    }

    /**
     * Registra que un cliente ha marcado un inmueble como favorito.
     * @param identificacionCliente Identificación del cliente
     * @param inmueble Inmueble marcado como favorito
     */
    public void registrarFavorito(String identificacionCliente, Inmueble inmueble) {
        Cliente cliente = clienteDAO.obtenerPorId(identificacionCliente);
        if (cliente != null) {
            cliente.agregarFavorito(inmueble);
            clienteDAO.actualizar(cliente); // Persistir cambios
            System.out.println("Favorito registrado: Cliente " + identificacionCliente + " marcó como favorito el inmueble " + inmueble.getCodigo());
        } else {
            System.out.println("Error: Cliente no encontrado con identificación " + identificacionCliente);
        }
    }

    /**
     * Elimina un inmueble de los favoritos de un cliente.
     * @param identificacionCliente Identificación del cliente
     * @param codigoInmueble Código del inmueble a remover de favoritos
     */
    public void removerFavorito(String identificacionCliente, String codigoInmueble) {
        Cliente cliente = clienteDAO.obtenerPorId(identificacionCliente);
        if (cliente != null) {
            ListaEnlazada<Inmueble> favoritos = cliente.getFavoritos();
            // Buscar y remover el inmueble por código
            for (int i = 0; i < favoritos.getTamaño(); i++) {
                Inmueble inmueble = favoritos.obtener(i);
                if (inmueble.getCodigo().equals(codigoInmueble)) {
                    favoritos.remover(i);
                    clienteDAO.actualizar(cliente); // Persistir cambios
                    System.out.println("Favorito removido: Cliente " + identificacionCliente + " quitó de favoritos el inmueble " + codigoInmueble);
                    return;
                }
            }
            System.out.println("Inmueble " + codigoInmueble + " no encontrado en favoritos del cliente " + identificacionCliente);
        } else {
            System.out.println("Error: Cliente no encontrado con identificación " + identificacionCliente);
        }
    }

    /**
     * Obtiene el historial de consultas de un cliente.
     * @param identificacionCliente Identificación del cliente
     * @return Lista de inmuebles consultados por el cliente
     */
    public ListaEnlazada<Inmueble> obtenerHistorialConsultas(String identificacionCliente) {
        Cliente cliente = clienteDAO.obtenerPorId(identificacionCliente);
        if (cliente != null) {
            return cliente.getHistorialConsultas();
        }
        return new ListaEnlazada<>();
    }

    /**
     * Obtiene la lista de favoritos de un cliente.
     * @param identificacionCliente Identificación del cliente
     * @return Lista de inmuebles favoritos del cliente
     */
    public ListaEnlazada<Inmueble> obtenerFavoritos(String identificacionCliente) {
        Cliente cliente = clienteDAO.obtenerPorId(identificacionCliente);
        if (cliente != null) {
            return cliente.getFavoritos();
        }
        return new ListaEnlazada<>();
    }

    /**
     * Obtiene estadísticas de interés para un inmueble específico.
     * @param codigoInmueble Código del inmueble
     * @return Número de veces que el inmueble ha sido consultado
     */
    public int obtenerVecesConsultado(String codigoInmueble) {
        // Esta sería una implementación más compleja que requeriría
        // recorrer todos los clientes y contar consultas
        // Por simplicidad, retornamos 0 y se puede mejorar después
        System.out.println("Funcionalidad de conteo de consultas por inmueble pendiente de implementar completamente");
        return 0;
    }
}