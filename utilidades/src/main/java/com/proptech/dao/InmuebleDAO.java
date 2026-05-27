package com.proptech.dao;

import com.proptech.modelo.Inmueble;
import com.proptech.utilidades.estructuras.ListaEnlazada;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InmuebleDAO {

    public void guardar(Inmueble inmueble) {
        String sql = "INSERT INTO inmuebles(codigo, tipo, direccion, precio, area, estado, habitaciones, banos, tieneParqueadero, descripcion) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, inmueble.getCodigo());
            pstmt.setString(2, inmueble.getTipo());
            pstmt.setString(3, inmueble.getDireccion());
            pstmt.setDouble(4, inmueble.getPrecio());
            pstmt.setDouble(5, inmueble.getArea());
            pstmt.setString(6, inmueble.getEstado());
            pstmt.setInt(7, inmueble.getHabitaciones());
            pstmt.setInt(8, inmueble.getBanos());
            pstmt.setBoolean(9, inmueble.isTieneParqueadero());
            pstmt.setString(10, inmueble.getDescripcion());
            pstmt.executeUpdate();
            System.out.println("Inmueble " + inmueble.getCodigo() + " guardado permanentemente en DB.");
        } catch (SQLException e) {
            System.out.println("Error al guardar inmueble: " + e.getMessage());
        }
    }

    public ListaEnlazada<Inmueble> obtenerTodos() {
        ListaEnlazada<Inmueble> lista = new ListaEnlazada<>();
        String sql = "SELECT * FROM inmuebles";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Inmueble obj = new Inmueble(
                        rs.getString("codigo"),
                        rs.getString("tipo"),
                        rs.getString("direccion"),
                        rs.getDouble("precio"),
                        rs.getDouble("area"),
                        rs.getInt("habitaciones"),
                        rs.getInt("banos"),
                        rs.getBoolean("tieneParqueadero"),
                        rs.getString("descripcion")
                );
                obj.setEstado(rs.getString("estado"));
                lista.agregar(obj);
            }
        } catch (SQLException e) {
            System.out.println("Error al leer los inmuebles: " + e.getMessage());
        }
        return lista;
    }

    public void actualizar(Inmueble inmueble) {
        String sql = "UPDATE inmuebles SET tipo = ?, direccion = ?, precio = ?, area = ?, estado = ?, habitaciones = ?, banos = ?, tieneParqueadero = ?, descripcion = ? WHERE codigo = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, inmueble.getTipo());
            pstmt.setString(2, inmueble.getDireccion());
            pstmt.setDouble(3, inmueble.getPrecio());
            pstmt.setDouble(4, inmueble.getArea());
            pstmt.setString(5, inmueble.getEstado());
            pstmt.setInt(6, inmueble.getHabitaciones());
            pstmt.setInt(7, inmueble.getBanos());
            pstmt.setBoolean(8, inmueble.isTieneParqueadero());
            pstmt.setString(9, inmueble.getDescripcion());
            pstmt.setString(10, inmueble.getCodigo());
            pstmt.executeUpdate();
            System.out.println("Inmueble " + inmueble.getCodigo() + " actualizado en DB.");
        } catch (SQLException e) {
            System.out.println("Error al actualizar inmueble: " + e.getMessage());
        }
    }

    public void eliminar(String codigo) {
        String sql = "DELETE FROM inmuebles WHERE codigo = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigo);
            pstmt.executeUpdate();
            System.out.println("Inmueble " + codigo + " eliminado de DB.");
        } catch (SQLException e) {
            System.out.println("Error al eliminar inmueble: " + e.getMessage());
        }
    }
}
