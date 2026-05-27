package com.proptech.dao;

import com.proptech.modelo.Cliente;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import java.sql.*;

/**
 * Data Access Object for Cliente entity.
 * Provides CRUD operations against the SQLite 'clientes' table.
 */
public class ClienteDAO {

    /**
     * Guarda un nuevo cliente en la base de datos.
     */
    public void guardar(Cliente cliente) {
        String sql = "INSERT INTO clientes(identificacion, nombre, telefono, presupuestoMaximo, email) VALUES(?,?,?,?,?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cliente.getIdentificacion());
            pstmt.setString(2, cliente.getNombre());
            pstmt.setString(3, cliente.getTelefono());
            pstmt.setDouble(4, cliente.getPresupuestoMaximo());
            pstmt.setString(5, cliente.getEmail());
            pstmt.executeUpdate();
            System.out.println("Cliente " + cliente.getIdentificacion() + " guardado en DB.");
        } catch (SQLException e) {
            System.out.println("Error al guardar cliente: " + e.getMessage());
        }
    }

    /**
     * Obtiene un cliente por su identificación.
     */
    public Cliente obtenerPorId(String identificacion) {
        String sql = "SELECT * FROM clientes WHERE identificacion = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identificacion);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Cliente c = new Cliente(
                    rs.getString("identificacion"),
                    rs.getString("nombre"),
                    rs.getString("telefono"),
                    rs.getDouble("presupuestoMaximo"),
                    rs.getString("email")
                );
                return c;
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener cliente: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtiene un cliente por su email.
     */
    public Cliente obtenerPorEmail(String email) {
        String sql = "SELECT * FROM clientes WHERE email = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Cliente(
                    rs.getString("identificacion"),
                    rs.getString("nombre"),
                    rs.getString("telefono"),
                    rs.getDouble("presupuestoMaximo"),
                    rs.getString("email")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener cliente por email: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtiene todos los clientes registrados.
     */
    public ListaEnlazada<Cliente> obtenerTodos() {
        ListaEnlazada<Cliente> lista = new ListaEnlazada<>();
        String sql = "SELECT * FROM clientes";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Cliente c = new Cliente(
                    rs.getString("identificacion"),
                    rs.getString("nombre"),
                    rs.getString("telefono"),
                    rs.getDouble("presupuestoMaximo"),
                    rs.getString("email")
                );
                lista.agregar(c);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener clientes: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Actualiza los datos de un cliente existente.
     */
    public void actualizar(Cliente cliente) {
        String sql = "UPDATE clientes SET nombre = ?, telefono = ?, presupuestoMaximo = ?, email = ? WHERE identificacion = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getTelefono());
            pstmt.setDouble(3, cliente.getPresupuestoMaximo());
            pstmt.setString(4, cliente.getEmail());
            pstmt.setString(5, cliente.getIdentificacion());
            pstmt.executeUpdate();
            System.out.println("Cliente " + cliente.getIdentificacion() + " actualizado.");
        } catch (SQLException e) {
            System.out.println("Error al actualizar cliente: " + e.getMessage());
        }
    }

    /**
     * Elimina un cliente por identificación.
     */
    public void eliminar(String identificacion) {
        String sql = "DELETE FROM clientes WHERE identificacion = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identificacion);
            pstmt.executeUpdate();
            System.out.println("Cliente " + identificacion + " eliminado.");
        } catch (SQLException e) {
            System.out.println("Error al eliminar cliente: " + e.getMessage());
        }
    }
}
