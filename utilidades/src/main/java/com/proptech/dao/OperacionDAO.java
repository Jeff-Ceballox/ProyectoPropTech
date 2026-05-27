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

public class OperacionDAO {

    private static final String COLUMNAS =
        "o.idOperacion AS o_idOperacion, o.tipo AS o_tipo, o.monto AS o_monto, o.fecha AS o_fecha, " +
        "i.codigo AS i_codigo, i.tipo AS i_tipo, i.direccion AS i_direccion, i.precio AS i_precio, " +
        "i.area AS i_area, i.estado AS i_estado, i.habitaciones AS i_habitaciones, i.banos AS i_banos, " +
        "i.tieneParqueadero AS i_tieneParqueadero, i.descripcion AS i_descripcion, " +
        "c.identificacion AS c_identificacion, c.nombre AS c_nombre, c.telefono AS c_telefono, " +
        "c.presupuestoMaximo AS c_presupuestoMaximo, c.email AS c_email, " +
        "a.idAsesor AS a_idAsesor, a.nombre AS a_nombre, a.especialidad AS a_especialidad, " +
        "a.email AS a_email, a.telefono AS a_telefono, a.calificacion AS a_calificacion, " +
        "a.negociosCerrados AS a_negociosCerrados";

    private static final String JOINS =
        " FROM operaciones o " +
        "LEFT JOIN inmuebles i ON o.idInmueble = i.codigo " +
        "LEFT JOIN clientes c ON o.idCliente = c.identificacion " +
        "LEFT JOIN asesores a ON o.idAsesor = a.idAsesor";

    private Operacion construirOperacion(ResultSet rs) throws SQLException {
        Inmueble inmueble = new Inmueble(
            rs.getString("i_codigo"),
            rs.getString("i_tipo"),
            rs.getString("i_direccion"),
            rs.getDouble("i_precio"),
            rs.getDouble("i_area"),
            rs.getInt("i_habitaciones"),
            rs.getInt("i_banos"),
            rs.getBoolean("i_tieneParqueadero"),
            rs.getString("i_descripcion")
        );
        inmueble.setEstado(rs.getString("i_estado"));

        Cliente cliente = new Cliente(
            rs.getString("c_identificacion"),
            rs.getString("c_nombre"),
            rs.getString("c_telefono"),
            rs.getDouble("c_presupuestoMaximo"),
            rs.getString("c_email")
        );

        Asesor asesor = new Asesor(
            rs.getString("a_idAsesor"),
            rs.getString("a_nombre"),
            rs.getString("a_especialidad"),
            rs.getString("a_email"),
            rs.getString("a_telefono")
        );
        asesor.setCalificacion(rs.getDouble("a_calificacion"));
        asesor.setNegociosCerrados(rs.getInt("a_negociosCerrados"));

        return new Operacion(
            rs.getString("o_idOperacion"),
            rs.getString("o_tipo"),
            inmueble,
            cliente,
            asesor,
            rs.getDouble("o_monto"),
            rs.getString("o_fecha")
        );
    }

    private String obtenerSqlCompleto() {
        return "SELECT " + COLUMNAS + JOINS;
    }

    public void guardar(Operacion operacion) {
        String sql = "INSERT INTO operaciones(idOperacion, tipo, idInmueble, idCliente, idAsesor, monto, fecha) VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, operacion.getIdOperacion());
            pstmt.setString(2, operacion.getTipo());
            pstmt.setString(3, operacion.getInmueble() != null ? operacion.getInmueble().getCodigo() : null);
            pstmt.setString(4, operacion.getCliente() != null ? operacion.getCliente().getIdentificacion() : null);
            pstmt.setString(5, operacion.getAsesor() != null ? operacion.getAsesor().getIdAsesor() : null);
            pstmt.setDouble(6, operacion.getMonto());
            pstmt.setString(7, operacion.getFecha());

            pstmt.executeUpdate();
            System.out.println("Operaci\u00f3n " + operacion.getIdOperacion() + " guardada permanentemente en DB.");

        } catch (SQLException e) {
            System.out.println("Error al guardar operaci\u00f3n: " + e.getMessage());
        }
    }

    public Operacion obtenerPorId(String idOperacion) {
        String sql = obtenerSqlCompleto() + " WHERE o.idOperacion = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idOperacion);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return construirOperacion(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener operaci\u00f3n: " + e.getMessage());
        }
        return null;
    }

    public ListaEnlazada<Operacion> obtenerTodas() {
        ListaEnlazada<Operacion> lista = new ListaEnlazada<>();
        String sql = obtenerSqlCompleto();

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                lista.agregar(construirOperacion(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al leer las operaciones: " + e.getMessage());
        }
        return lista;
    }

    public ListaEnlazada<Operacion> obtenerPorTipo(String tipo) {
        ListaEnlazada<Operacion> lista = new ListaEnlazada<>();
        String sql = obtenerSqlCompleto() + " WHERE o.tipo = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tipo);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lista.agregar(construirOperacion(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al leer operaciones por tipo: " + e.getMessage());
        }
        return lista;
    }

    public ListaEnlazada<Operacion> obtenerPorAsesor(String idAsesor) {
        ListaEnlazada<Operacion> lista = new ListaEnlazada<>();
        String sql = obtenerSqlCompleto() + " WHERE o.idAsesor = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idAsesor);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lista.agregar(construirOperacion(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al leer operaciones por asesor: " + e.getMessage());
        }
        return lista;
    }

    public ListaEnlazada<Operacion> obtenerOperacionesPorCliente(String identificacionCliente) {
        ListaEnlazada<Operacion> lista = new ListaEnlazada<>();
        String sql = obtenerSqlCompleto() + " WHERE o.idCliente = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, identificacionCliente);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lista.agregar(construirOperacion(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al leer operaciones por cliente: " + e.getMessage());
        }
        return lista;
    }

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
            System.out.println("Operaci\u00f3n " + operacion.getIdOperacion() + " actualizada.");

        } catch (SQLException e) {
            System.out.println("Error al actualizar operaci\u00f3n: " + e.getMessage());
        }
    }

    public void eliminar(String idOperacion) {
        String sql = "DELETE FROM operaciones WHERE idOperacion = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idOperacion);
            pstmt.executeUpdate();
            System.out.println("Operaci\u00f3n " + idOperacion + " eliminada.");

        } catch (SQLException e) {
            System.out.println("Error al eliminar operaci\u00f3n: " + e.getMessage());
        }
    }
}
