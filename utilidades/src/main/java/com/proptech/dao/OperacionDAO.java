package com.proptech.dao;

import com.proptech.modelo.Operacion;
import com.proptech.modelo.Asesor;
import com.proptech.modelo.Cliente;
import com.proptech.modelo.Inmueble;
import com.proptech.utilidades.estructuras.ListaEnlazada;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object para la entidad Operacion.
 * Se encarga exclusivamente de las operaciones CRUD (Crear, Leer, Actualizar, Borrar) en SQL.
 */
public class OperacionDAO {

    /**
     * Guarda una nueva operación en la tabla SQL.
     */
    public void guardar(Operacion operacion) {
        String sql = "INSERT INTO operaciones(idOperacion, tipo, idInmueble, idCliente, idAsesor, monto, fecha) VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            // Reemplazamos los "?" del SQL por los datos reales del objeto
            pstmt.setString(1, operacion.getIdOperacion());
            pstmt.setString(2, operacion.getTipo());
            pstmt.setString(3, operacion.getInmueble().getCodigo());
            pstmt.setString(4, operacion.getCliente().getIdentificacion());
            pstmt.setString(5, operacion.getAsesor().getIdAsesor());
            pstmt.setDouble(6, operacion.getMonto());
            pstmt.setString(7, operacion.getFecha());
             
            pstmt.executeUpdate();
            System.out.println("Operación " + operacion.getIdOperacion() + " guardada permanentemente en DB.");
             
        } catch (SQLException e) {
            System.out.println("Error al guardar operación: " + e.getMessage());
        }
    }

    /**
     * Obtiene una operación por su ID.
     */
    public Operacion obtenerPorId(String idOperacion) {
        String sql = "SELECT o.*, i.*, c.*, a.* FROM operaciones o " +
                     "LEFT JOIN inmuebles i ON o.idInmueble = i.codigo " +
                     "LEFT JOIN clientes c ON o.idCliente = c.identificacion " +
                     "LEFT JOIN asesores a ON o.idAsesor = a.idAsesor " +
                     "WHERE o.idOperacion = ?";
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, idOperacion);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Construir los objetos relacionados desde el ResultSet
                Inmueble inmueble = new Inmueble(
                    rs.getString("i.codigo"),
                    rs.getString("i.tipo"),
                    rs.getString("i.direccion"),
                    rs.getDouble("i.precio"),
                    rs.getDouble("i.area"),
                    rs.getInt("i.habitaciones"),
                    rs.getInt("i.banos"),
                    rs.getBoolean("i.tieneParqueadero"),
                    rs.getString("i.descripcion")
                );
                inmueble.setEstado(rs.getString("i.estado"));
                
                Cliente cliente = new Cliente(
                    rs.getString("c.identificacion"),
                    rs.getString("c.nombre"),
                    rs.getString("c.telefono"),
                    rs.getDouble("c.presupuestoMaximo"),
                    rs.getString("c.email")
                );
                
                Asesor asesor = new Asesor(
                    rs.getString("a.idAsesor"),
                    rs.getString("a.nombre"),
                    rs.getString("a.especialidad"),
                    rs.getString("a.email"),
                    rs.getString("a.telefono")
                );
                asesor.setCalificacion(rs.getDouble("a.calificacion"));
                asesor.setNegociosCerrados(rs.getInt("a.negociosCerrados"));
                
                // Construir la operación
                Operacion operacion = new Operacion(
                    rs.getString("o.idOperacion"),
                    rs.getString("o.tipo"),
                    inmueble,
                    cliente,
                    asesor,
                    rs.getDouble("o.monto"),
                    rs.getString("o.fecha")
                );
                return operacion;
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener operación: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lee todas las operaciones de la base de datos y las devuelve en nuestra propia estructura de datos.
     */
    public ListaEnlazada<Operacion> obtenerTodas() {
        ListaEnlazada<Operacion> lista = new ListaEnlazada<>();
        String sql = "SELECT o.*, i.*, c.*, a.* FROM operaciones o " +
                     "LEFT JOIN inmuebles i ON o.idInmueble = i.codigo " +
                     "LEFT JOIN clientes c ON o.idCliente = c.identificacion " +
                     "LEFT JOIN asesores a ON o.idAsesor = a.idAsesor";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
             
            // Recorremos fila por fila los resultados de la base de datos
            while (rs.next()) {
                // Construir los objetos relacionados desde el ResultSet
                Inmueble inmueble = new Inmueble(
                    rs.getString("i.codigo"),
                    rs.getString("i.tipo"),
                    rs.getString("i.direccion"),
                    rs.getDouble("i.precio"),
                    rs.getDouble("i.area"),
                    rs.getInt("i.habitaciones"),
                    rs.getInt("i.banos"),
                    rs.getBoolean("i.tieneParqueadero"),
                    rs.getString("i.descripcion")
                );
                inmueble.setEstado(rs.getString("i.estado"));
                
                Cliente cliente = new Cliente(
                    rs.getString("c.identificacion"),
                    rs.getString("c.nombre"),
                    rs.getString("c.telefono"),
                    rs.getDouble("c.presupuestoMaximo"),
                    rs.getString("c.email")
                );
                
                Asesor asesor = new Asesor(
                    rs.getString("a.idAsesor"),
                    rs.getString("a.nombre"),
                    rs.getString("a.especialidad"),
                    rs.getString("a.email"),
                    rs.getString("a.telefono")
                );
                asesor.setCalificacion(rs.getDouble("a.calificacion"));
                asesor.setNegociosCerrados(rs.getInt("a.negociosCerrados"));
                
                // Construir la operación
                Operacion operacion = new Operacion(
                    rs.getString("o.idOperacion"),
                    rs.getString("o.tipo"),
                    inmueble,
                    cliente,
                    asesor,
                    rs.getDouble("o.monto"),
                    rs.getString("o.fecha")
                );
                
                // Lo agregamos a nuestra lista dinámica
                lista.agregar(operacion);
            }
        } catch (SQLException e) {
            System.out.println("Error al leer las operaciones: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtiene operaciones por tipo (Venta o Arriendo).
     */
    public ListaEnlazada<Operacion> obtenerPorTipo(String tipo) {
        ListaEnlazada<Operacion> lista = new ListaEnlazada<>();
        String sql = "SELECT o.*, i.*, c.*, a.* FROM operaciones o " +
                     "LEFT JOIN inmuebles i ON o.idInmueble = i.codigo " +
                     "LEFT JOIN clientes c ON o.idCliente = c.identificacion " +
                     "LEFT JOIN asesores a ON o.idAsesor = a.idAsesor " +
                     "WHERE o.tipo = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, tipo);
            ResultSet rs = pstmt.executeQuery();
            
            // Recorremos fila por fila los resultados de la base de datos
            while (rs.next()) {
                // Construir los objetos relacionados desde el ResultSet
                Inmueble inmueble = new Inmueble(
                    rs.getString("i.codigo"),
                    rs.getString("i.tipo"),
                    rs.getString("i.direccion"),
                    rs.getDouble("i.precio"),
                    rs.getDouble("i.area"),
                    rs.getInt("i.habitaciones"),
                    rs.getInt("i.banos"),
                    rs.getBoolean("i.tieneParqueadero"),
                    rs.getString("i.descripcion")
                );
                inmueble.setEstado(rs.getString("i.estado"));
                
                Cliente cliente = new Cliente(
                    rs.getString("c.identificacion"),
                    rs.getString("c.nombre"),
                    rs.getString("c.telefono"),
                    rs.getDouble("c.presupuestoMaximo"),
                    rs.getString("c.email")
                );
                
                Asesor asesor = new Asesor(
                    rs.getString("a.idAsesor"),
                    rs.getString("a.nombre"),
                    rs.getString("a.especialidad"),
                    rs.getString("a.email"),
                    rs.getString("a.telefono")
                );
                asesor.setCalificacion(rs.getDouble("a.calificacion"));
                asesor.setNegociosCerrados(rs.getInt("a.negociosCerrados"));
                
                // Construir la operación
                Operacion operacion = new Operacion(
                    rs.getString("o.idOperacion"),
                    rs.getString("o.tipo"),
                    inmueble,
                    cliente,
                    asesor,
                    rs.getDouble("o.monto"),
                    rs.getString("o.fecha")
                );
                
                // Lo agregamos a nuestra lista dinámica
                lista.agregar(operacion);
            }
        } catch (SQLException e) {
            System.out.println("Error al leer operaciones por tipo: " + e.getMessage());
        }
        return lista;
    }

/**
      * Obtiene operaciones por ID de asesor.
      */
    public ListaEnlazada<Operacion> obtenerPorAsesor(String idAsesor) {
        ListaEnlazada<Operacion> lista = new ListaEnlazada<>();
        String sql = "SELECT o.*, i.*, c.*, a.* FROM operaciones o " +
                     "LEFT JOIN inmuebles i ON o.idInmueble = i.codigo " +
                     "LEFT JOIN clientes c ON o.idCliente = c.identificacion " +
                     "LEFT JOIN asesores a ON o.idAsesor = a.idAsesor " +
                     "WHERE o.idAsesor = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
              
            pstmt.setString(1, idAsesor);
            ResultSet rs = pstmt.executeQuery();
              
            // Recorremos fila por fila los resultados de la base de datos
            while (rs.next()) {
                // Construir los objetos relacionados desde el ResultSet
                Inmueble inmueble = new Inmueble(
                    rs.getString("i.codigo"),
                    rs.getString("i.tipo"),
                    rs.getString("i.direccion"),
                    rs.getDouble("i.precio"),
                    rs.getDouble("i.area"),
                    rs.getInt("i.habitaciones"),
                    rs.getInt("i.banos"),
                    rs.getBoolean("i.tieneParqueadero"),
                    rs.getString("i.descripcion")
                );
                inmueble.setEstado(rs.getString("i.estado"));
                
                Cliente cliente = new Cliente(
                    rs.getString("c.identificacion"),
                    rs.getString("c.nombre"),
                    rs.getString("c.telefono"),
                    rs.getDouble("c.presupuestoMaximo"),
                    rs.getString("c.email")
                );
                
                Asesor asesor = new Asesor(
                    rs.getString("a.idAsesor"),
                    rs.getString("a.nombre"),
                    rs.getString("a.especialidad"),
                    rs.getString("a.email"),
                    rs.getString("a.telefono")
                );
                asesor.setCalificacion(rs.getDouble("a.calificacion"));
                asesor.setNegociosCerrados(rs.getInt("a.negociosCerrados"));
                
                // Construir la operación
                Operacion operacion = new Operacion(
                    rs.getString("o.idOperacion"),
                    rs.getString("o.tipo"),
                    inmueble,
                    cliente,
                    asesor,
                    rs.getDouble("o.monto"),
                    rs.getString("o.fecha")
                );
                
                // Lo agregamos a nuestra lista dinámica
                lista.agregar(operacion);
            }
        } catch (SQLException e) {
            System.out.println("Error al leer operaciones por asesor: " + e.getMessage());
        }
        return lista;
    }

    /**
      * Obtiene operaciones por ID de cliente.
      */
    public ListaEnlazada<Operacion> obtenerOperacionesPorCliente(String identificacionCliente) {
        ListaEnlazada<Operacion> lista = new ListaEnlazada<>();
        String sql = "SELECT o.*, i.*, c.*, a.* FROM operaciones o " +
                     "LEFT JOIN inmuebles i ON o.idInmueble = i.codigo " +
                     "LEFT JOIN clientes c ON o.idCliente = c.identificacion " +
                     "LEFT JOIN asesores a ON o.idAsesor = a.idAsesor " +
                     "WHERE o.idCliente = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
              
            pstmt.setString(1, identificacionCliente);
            ResultSet rs = pstmt.executeQuery();
              
            // Recorremos fila por fila los resultados de la base de datos
            while (rs.next()) {
                // Construir los objetos relacionados desde el ResultSet
                Inmueble inmueble = new Inmueble(
                    rs.getString("i.codigo"),
                    rs.getString("i.tipo"),
                    rs.getString("i.direccion"),
                    rs.getDouble("i.precio"),
                    rs.getDouble("i.area"),
                    rs.getInt("i.habitaciones"),
                    rs.getInt("i.banos"),
                    rs.getBoolean("i.tieneParqueadero"),
                    rs.getString("i.descripcion")
                );
                inmueble.setEstado(rs.getString("i.estado"));
                
                Cliente cliente = new Cliente(
                    rs.getString("c.identificacion"),
                    rs.getString("c.nombre"),
                    rs.getString("c.telefono"),
                    rs.getDouble("c.presupuestoMaximo"),
                    rs.getString("c.email")
                );
                
                Asesor asesor = new Asesor(
                    rs.getString("a.idAsesor"),
                    rs.getString("a.nombre"),
                    rs.getString("a.especialidad"),
                    rs.getString("a.email"),
                    rs.getString("a.telefono")
                );
                asesor.setCalificacion(rs.getDouble("a.calificacion"));
                asesor.setNegociosCerrados(rs.getInt("a.negociosCerrados"));
                
                // Construir la operación
                Operacion operacion = new Operacion(
                    rs.getString("o.idOperacion"),
                    rs.getString("o.tipo"),
                    inmueble,
                    cliente,
                    asesor,
                    rs.getDouble("o.monto"),
                    rs.getString("o.fecha")
                );
                
                // Lo agregamos a nuestra lista dinámica
                lista.agregar(operacion);
            }
        } catch (SQLException e) {
            System.out.println("Error al leer operaciones por cliente: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Actualiza una operación existente.
     */
    public void actualizar(Operacion operacion) {
        String sql = "UPDATE operaciones SET tipo = ?, idInmueble = ?, idCliente = ?, idAsesor = ?, monto = ?, fecha = ? WHERE idOperacion = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, operacion.getTipo());
            pstmt.setString(2, operacion.getInmueble().getCodigo());
            pstmt.setString(3, operacion.getCliente().getIdentificacion());
            pstmt.setString(4, operacion.getAsesor().getIdAsesor());
            pstmt.setDouble(5, operacion.getMonto());
            pstmt.setString(6, operacion.getFecha());
            pstmt.setString(7, operacion.getIdOperacion());
             
            pstmt.executeUpdate();
            System.out.println("Operación " + operacion.getIdOperacion() + " actualizada.");
             
        } catch (SQLException e) {
            System.out.println("Error al actualizar operación: " + e.getMessage());
        }
    }

    /**
     * Elimina una operación por su ID.
     */
    public void eliminar(String idOperacion) {
        String sql = "DELETE FROM operaciones WHERE idOperacion = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, idOperacion);
            pstmt.executeUpdate();
            System.out.println("Operación " + idOperacion + " eliminada.");
             
        } catch (SQLException e) {
            System.out.println("Error al eliminar operación: " + e.getMessage());
        }
    }
}