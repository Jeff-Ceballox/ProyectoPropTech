package com.proptech.dao;

import com.proptech.modelo.Asesor;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object para la entidad Asesor.
 */
public class AsesorDAO {

    public void guardar(Asesor asesor) {
        String sql = "INSERT INTO asesores(idAsesor, nombre, especialidad, email, telefono, calificacion, negociosCerrados) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, asesor.getIdAsesor());
            pstmt.setString(2, asesor.getNombre());
            pstmt.setString(3, asesor.getEspecialidad());
            pstmt.setString(4, asesor.getEmail());
            pstmt.setString(5, asesor.getTelefono());
            pstmt.setDouble(6, asesor.getCalificacion());
            pstmt.setInt(7, asesor.getNegociosCerrados());
            pstmt.executeUpdate();
            System.out.println("Asesor " + asesor.getIdAsesor() + " guardado en DB.");
        } catch (SQLException e) {
            System.out.println("Error al guardar asesor: " + e.getMessage());
        }
    }

    public Asesor obtenerPorId(String idAsesor) {
        String sql = "SELECT * FROM asesores WHERE idAsesor = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idAsesor);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Asesor a = new Asesor(
                    rs.getString("idAsesor"),
                    rs.getString("nombre"),
                    rs.getString("especialidad"),
                    rs.getString("email"),
                    rs.getString("telefono")
                );
                a.setCalificacion(rs.getDouble("calificacion"));
                a.setNegociosCerrados(rs.getInt("negociosCerrados"));
                return a;
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener asesor: " + e.getMessage());
        }
        return null;
    }

    public ListaEnlazada<Asesor> obtenerTodos() {
        ListaEnlazada<Asesor> lista = new ListaEnlazada<>();
        String sql = "SELECT * FROM asesores";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Asesor a = new Asesor(
                    rs.getString("idAsesor"),
                    rs.getString("nombre"),
                    rs.getString("especialidad"),
                    rs.getString("email"),
                    rs.getString("telefono")
                );
                a.setCalificacion(rs.getDouble("calificacion"));
                a.setNegociosCerrados(rs.getInt("negociosCerrados"));
                lista.agregar(a);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener asesores: " + e.getMessage());
        }
        return lista;
    }

    public void actualizar(Asesor asesor) {
        String sql = "UPDATE asesores SET nombre = ?, especialidad = ?, email = ?, telefono = ?, calificacion = ?, negociosCerrados = ? WHERE idAsesor = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, asesor.getNombre());
            pstmt.setString(2, asesor.getEspecialidad());
            pstmt.setString(3, asesor.getEmail());
            pstmt.setString(4, asesor.getTelefono());
            pstmt.setDouble(5, asesor.getCalificacion());
            pstmt.setInt(6, asesor.getNegociosCerrados());
            pstmt.setString(7, asesor.getIdAsesor());
            pstmt.executeUpdate();
            System.out.println("Asesor " + asesor.getIdAsesor() + " actualizado.");
        } catch (SQLException e) {
            System.out.println("Error al actualizar asesor: " + e.getMessage());
        }
    }

    public void eliminar(String idAsesor) {
        String sql = "DELETE FROM asesores WHERE idAsesor = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idAsesor);
            pstmt.executeUpdate();
            System.out.println("Asesor " + idAsesor + " eliminado.");
        } catch (SQLException e) {
            System.out.println("Error al eliminar asesor: " + e.getMessage());
        }
    }
}