package com.proptech.dao;

import com.proptech.modelo.Visita;
import com.proptech.modelo.Cliente;
import com.proptech.modelo.Inmueble;
import com.proptech.modelo.Asesor;
import com.proptech.utilidades.estructuras.ListaEnlazada;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object para la entidad Visita.
 * Se encarga exclusivamente de las operaciones CRUD (Crear, Leer, Actualizar, Borrar) en SQL.
 */
public class VisitaDAO {

    /**
     * Guarda una nueva visita en la tabla SQL.
     */
    public void guardar(Visita visita) {
        String sql = "INSERT INTO visitas(idVisita, identificacionCliente, codigoInmueble, idAsesor, fechaHora, estado) VALUES(?,?,?,?,?,?)";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            // Reemplazamos los "?" del SQL por los datos reales del objeto
            pstmt.setString(1, visita.getIdVisita());
            pstmt.setString(2, visita.getCliente().getIdentificacion());
            pstmt.setString(3, visita.getInmueble().getCodigo());
            pstmt.setString(4, visita.getAsesor().getIdAsesor());
            pstmt.setString(5, visita.getFechaHora());
            pstmt.setString(6, visita.getEstado());
             
            pstmt.executeUpdate();
            System.out.println("Visita " + visita.getIdVisita() + " guardada permanentemente en DB.");
             
        } catch (SQLException e) {
            System.out.println("Error al guardar visita: " + e.getMessage());
        }
    }

    /**
     * Obtiene una visita por su ID.
     */
    public Visita obtenerPorId(String idVisita) {
        String sql = "SELECT v.*, c.*, i.*, a.* FROM visitas v " +
                     "LEFT JOIN clientes c ON v.identificacionCliente = c.identificacion " +
                     "LEFT JOIN inmuebles i ON v.codigoInmueble = i.codigo " +
                     "LEFT JOIN asesores a ON v.idAsesor = a.idAsesor " +
                     "WHERE v.idVisita = ?";
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, idVisita);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Construir los objetos relacionados desde el ResultSet
                Cliente cliente = new Cliente(
                    rs.getString("c.identificacion"),
                    rs.getString("c.nombre"),
                    rs.getString("c.telefono"),
                    rs.getDouble("c.presupuestoMaximo"),
                    rs.getString("c.email")
                );
                
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
                
                Asesor asesor = new Asesor(
                    rs.getString("a.idAsesor"),
                    rs.getString("a.nombre"),
                    rs.getString("a.especialidad"),
                    rs.getString("a.email"),
                    rs.getString("a.telefono")
                );
                asesor.setCalificacion(rs.getDouble("a.calificacion"));
                asesor.setNegociosCerrados(rs.getInt("a.negociosCerrados"));
                
                // Construir la visita
                Visita visita = new Visita(
                    rs.getString("v.idVisita"),
                    cliente,
                    inmueble,
                    asesor,
                    rs.getString("v.fechaHora")
                );
                visita.setEstado(rs.getString("v.estado"));
                return visita;
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener visita: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lee todas las visitas de la base de datos y las devuelve en nuestra propia estructura de datos.
     */
    public ListaEnlazada<Visita> obtenerTodas() {
        ListaEnlazada<Visita> lista = new ListaEnlazada<>();
        String sql = "SELECT v.*, c.*, i.*, a.* FROM visitas v " +
                     "LEFT JOIN clientes c ON v.identificacionCliente = c.identificacion " +
                     "LEFT JOIN inmuebles i ON v.codigoInmueble = i.codigo " +
                     "LEFT JOIN asesores a ON v.idAsesor = a.idAsesor";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
             
            // Recorremos fila por fila los resultados de la base de datos
            while (rs.next()) {
                // Construir los objetos relacionados desde el ResultSet
                Cliente cliente = new Cliente(
                    rs.getString("c.identificacion"),
                    rs.getString("c.nombre"),
                    rs.getString("c.telefono"),
                    rs.getDouble("c.presupuestoMaximo"),
                    rs.getString("c.email")
                );
                
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
                
                Asesor asesor = new Asesor(
                    rs.getString("a.idAsesor"),
                    rs.getString("a.nombre"),
                    rs.getString("a.especialidad"),
                    rs.getString("a.email"),
                    rs.getString("a.telefono")
                );
                asesor.setCalificacion(rs.getDouble("a.calificacion"));
                asesor.setNegociosCerrados(rs.getInt("a.negociosCerrados"));
                
                // Construir la visita
                Visita visita = new Visita(
                    rs.getString("v.idVisita"),
                    cliente,
                    inmueble,
                    asesor,
                    rs.getString("v.fechaHora")
                );
                visita.setEstado(rs.getString("v.estado"));
                
                // Lo agregamos a nuestra lista dinámica
                lista.agregar(visita);
            }
        } catch (SQLException e) {
            System.out.println("Error al leer las visitas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Actualiza una visita existente.
     */
    public void actualizar(Visita visita) {
        String sql = "UPDATE visitas SET identificacionCliente = ?, codigoInmueble = ?, idAsesor = ?, fechaHora = ?, estado = ? WHERE idVisita = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, visita.getCliente().getIdentificacion());
            pstmt.setString(2, visita.getInmueble().getCodigo());
            pstmt.setString(3, visita.getAsesor().getIdAsesor());
            pstmt.setString(4, visita.getFechaHora());
            pstmt.setString(5, visita.getEstado());
            pstmt.setString(6, visita.getIdVisita());
             
            pstmt.executeUpdate();
            System.out.println("Visita " + visita.getIdVisita() + " actualizada.");
             
        } catch (SQLException e) {
            System.out.println("Error al actualizar visita: " + e.getMessage());
        }
    }

/**
      * Elimina una visita por su ID.
      */
    public void eliminar(String idVisita) {
        String sql = "DELETE FROM visitas WHERE idVisita = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
              
            pstmt.setString(1, idVisita);
            pstmt.executeUpdate();
            System.out.println("Visita " + idVisita + " eliminada.");
              
        } catch (SQLException e) {
            System.out.println("Error al eliminar visita: " + e.getMessage());
        }
    }

    /**
      * Obtiene todas las visitas de un cliente específico.
      */
    public ListaEnlazada<Visita> obtenerVisitasPorCliente(String identificacionCliente) {
        ListaEnlazada<Visita> lista = new ListaEnlazada<>();
        String sql = "SELECT v.*, c.*, i.*, a.* FROM visitas v " +
                     "LEFT JOIN clientes c ON v.identificacionCliente = c.identificacion " +
                     "LEFT JOIN inmuebles i ON v.codigoInmueble = i.codigo " +
                     "LEFT JOIN asesores a ON v.idAsesor = a.idAsesor " +
                     "WHERE v.identificacionCliente = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
              
            pstmt.setString(1, identificacionCliente);
            ResultSet rs = pstmt.executeQuery();
              
            while (rs.next()) {
                Cliente cliente = new Cliente(
                    rs.getString("c.identificacion"),
                    rs.getString("c.nombre"),
                    rs.getString("c.telefono"),
                    rs.getDouble("c.presupuestoMaximo"),
                    rs.getString("c.email")
                );
                
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
                
                Asesor asesor = new Asesor(
                    rs.getString("a.idAsesor"),
                    rs.getString("a.nombre"),
                    rs.getString("a.especialidad"),
                    rs.getString("a.email"),
                    rs.getString("a.telefono")
                );
                asesor.setCalificacion(rs.getDouble("a.calificacion"));
                asesor.setNegociosCerrados(rs.getInt("a.negociosCerrados"));
                
                Visita visita = new Visita(
                    rs.getString("v.idVisita"),
                    cliente,
                    inmueble,
                    asesor,
                    rs.getString("v.fechaHora")
                );
                visita.setEstado(rs.getString("v.estado"));
                
                lista.agregar(visita);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener visitas por cliente: " + e.getMessage());
        }
        return lista;
    }

    /**
      * Obtiene todas las visitas de un asesor específico.
      */
    public ListaEnlazada<Visita> obtenerVisitasPorAsesor(String idAsesor) {
        ListaEnlazada<Visita> lista = new ListaEnlazada<>();
        String sql = "SELECT v.*, c.*, i.*, a.* FROM visitas v " +
                     "LEFT JOIN clientes c ON v.identificacionCliente = c.identificacion " +
                     "LEFT JOIN inmuebles i ON v.codigoInmueble = i.codigo " +
                     "LEFT JOIN asesores a ON v.idAsesor = a.idAsesor " +
                     "WHERE v.idAsesor = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
              
            pstmt.setString(1, idAsesor);
            ResultSet rs = pstmt.executeQuery();
              
            while (rs.next()) {
                Cliente cliente = new Cliente(
                    rs.getString("c.identificacion"),
                    rs.getString("c.nombre"),
                    rs.getString("c.telefono"),
                    rs.getDouble("c.presupuestoMaximo"),
                    rs.getString("c.email")
                );
                
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
                
                Asesor asesor = new Asesor(
                    rs.getString("a.idAsesor"),
                    rs.getString("a.nombre"),
                    rs.getString("a.especialidad"),
                    rs.getString("a.email"),
                    rs.getString("a.telefono")
                );
                asesor.setCalificacion(rs.getDouble("a.calificacion"));
                asesor.setNegociosCerrados(rs.getInt("a.negociosCerrados"));
                
                Visita visita = new Visita(
                    rs.getString("v.idVisita"),
                    cliente,
                    inmueble,
                    asesor,
                    rs.getString("v.fechaHora")
                );
                visita.setEstado(rs.getString("v.estado"));
                
                lista.agregar(visita);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener visitas por asesor: " + e.getMessage());
        }
        return lista;
    }
}