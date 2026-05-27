package com.proptech.dao;

import com.proptech.utilidades.estructuras.ListaEnlazada;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FavoritoDAO {

    public boolean agregarFavorito(String identificacionCliente, String codigoInmueble) {
        String sql = "INSERT OR IGNORE INTO favoritos(identificacionCliente, codigoInmueble) VALUES(?,?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identificacionCliente);
            pstmt.setString(2, codigoInmueble);
            int filas = pstmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.out.println("Error al agregar favorito: " + e.getMessage());
            return false;
        }
    }

    public boolean quitarFavorito(String identificacionCliente, String codigoInmueble) {
        String sql = "DELETE FROM favoritos WHERE identificacionCliente = ? AND codigoInmueble = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identificacionCliente);
            pstmt.setString(2, codigoInmueble);
            int filas = pstmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.out.println("Error al quitar favorito: " + e.getMessage());
            return false;
        }
    }

    public ListaEnlazada<String> obtenerFavoritos(String identificacionCliente) {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        String sql = "SELECT codigoInmueble FROM favoritos WHERE identificacionCliente = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identificacionCliente);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lista.agregar(rs.getString("codigoInmueble"));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener favoritos: " + e.getMessage());
        }
        return lista;
    }

    public boolean esFavorito(String identificacionCliente, String codigoInmueble) {
        String sql = "SELECT COUNT(*) FROM favoritos WHERE identificacionCliente = ? AND codigoInmueble = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identificacionCliente);
            pstmt.setString(2, codigoInmueble);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Error al verificar favorito: " + e.getMessage());
            return false;
        }
    }
}
