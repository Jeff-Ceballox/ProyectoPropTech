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

public class VisitaDAO {

    private static final String COLUMNAS =
        "v.idVisita AS v_idVisita, v.fechaHora AS v_fechaHora, v.estado AS v_estado, " +
        "c.identificacion AS c_identificacion, c.nombre AS c_nombre, c.telefono AS c_telefono, " +
        "c.presupuestoMaximo AS c_presupuestoMaximo, c.email AS c_email, " +
        "i.codigo AS i_codigo, i.tipo AS i_tipo, i.direccion AS i_direccion, i.precio AS i_precio, " +
        "i.area AS i_area, i.estado AS i_estado, i.habitaciones AS i_habitaciones, i.banos AS i_banos, " +
        "i.tieneParqueadero AS i_tieneParqueadero, i.descripcion AS i_descripcion, " +
        "a.idAsesor AS a_idAsesor, a.nombre AS a_nombre, a.especialidad AS a_especialidad, " +
        "a.email AS a_email, a.telefono AS a_telefono, a.calificacion AS a_calificacion, " +
        "a.negociosCerrados AS a_negociosCerrados";

    private static final String JOINS =
        " FROM visitas v " +
        "LEFT JOIN clientes c ON v.identificacionCliente = c.identificacion " +
        "LEFT JOIN inmuebles i ON v.codigoInmueble = i.codigo " +
        "LEFT JOIN asesores a ON v.idAsesor = a.idAsesor";

    private Visita construirVisita(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(
            rs.getString("c_identificacion"),
            rs.getString("c_nombre"),
            rs.getString("c_telefono"),
            rs.getDouble("c_presupuestoMaximo"),
            rs.getString("c_email")
        );

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

        Asesor asesor = new Asesor(
            rs.getString("a_idAsesor"),
            rs.getString("a_nombre"),
            rs.getString("a_especialidad"),
            rs.getString("a_email"),
            rs.getString("a_telefono")
        );
        asesor.setCalificacion(rs.getDouble("a_calificacion"));
        asesor.setNegociosCerrados(rs.getInt("a_negociosCerrados"));

        Visita visita = new Visita(
            rs.getString("v_idVisita"),
            cliente,
            inmueble,
            asesor,
            rs.getString("v_fechaHora")
        );
        visita.setEstado(rs.getString("v_estado"));
        return visita;
    }

    private String obtenerSqlCompleto() {
        return "SELECT " + COLUMNAS + JOINS;
    }

    public void guardar(Visita visita) {
        String sql = "INSERT INTO visitas(idVisita, identificacionCliente, codigoInmueble, idAsesor, fechaHora, estado) VALUES(?,?,?,?,?,?)";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    public Visita obtenerPorId(String idVisita) {
        String sql = obtenerSqlCompleto() + " WHERE v.idVisita = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idVisita);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return construirVisita(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener visita: " + e.getMessage());
        }
        return null;
    }

    public ListaEnlazada<Visita> obtenerTodas() {
        ListaEnlazada<Visita> lista = new ListaEnlazada<>();
        String sql = obtenerSqlCompleto();

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                lista.agregar(construirVisita(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al leer las visitas: " + e.getMessage());
        }
        return lista;
    }

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

    public ListaEnlazada<Visita> obtenerVisitasPorCliente(String identificacionCliente) {
        ListaEnlazada<Visita> lista = new ListaEnlazada<>();
        String sql = obtenerSqlCompleto() + " WHERE v.identificacionCliente = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, identificacionCliente);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lista.agregar(construirVisita(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener visitas por cliente: " + e.getMessage());
        }
        return lista;
    }

    public ListaEnlazada<Visita> obtenerVisitasPorAsesor(String idAsesor) {
        ListaEnlazada<Visita> lista = new ListaEnlazada<>();
        String sql = obtenerSqlCompleto() + " WHERE v.idAsesor = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idAsesor);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lista.agregar(construirVisita(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener visitas por asesor: " + e.getMessage());
        }
        return lista;
    }
}
