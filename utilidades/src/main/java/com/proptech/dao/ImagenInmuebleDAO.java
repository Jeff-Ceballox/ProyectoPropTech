package com.proptech.dao;

import com.proptech.modelo.ImagenInmueble;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ImagenInmuebleDAO {

    public void guardar(ImagenInmueble img) {
        String sql = "INSERT INTO inmueble_imagenes(codigo_inmueble, imagen_base64, orden, fecha_subida) VALUES(?,?,?,?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, img.getCodigoInmueble());
            pstmt.setString(2, img.getImagenBase64());
            pstmt.setInt(3, img.getOrden());
            pstmt.setString(4, img.getFechaSubida());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al guardar imagen: " + e.getMessage());
        }
    }

    public ListaEnlazada<ImagenInmueble> obtenerPorInmueble(String codigoInmueble) {
        ListaEnlazada<ImagenInmueble> lista = new ListaEnlazada<>();
        String sql = "SELECT * FROM inmueble_imagenes WHERE codigo_inmueble = ? ORDER BY orden ASC, id ASC";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoInmueble);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ImagenInmueble img = new ImagenInmueble(
                    rs.getString("codigo_inmueble"),
                    rs.getString("imagen_base64"),
                    rs.getInt("orden"),
                    rs.getString("fecha_subida")
                );
                img.setId(rs.getInt("id"));
                lista.agregar(img);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener imagenes: " + e.getMessage());
        }
        return lista;
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM inmueble_imagenes WHERE id = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al eliminar imagen: " + e.getMessage());
        }
    }

    public void eliminarTodasPorInmueble(String codigoInmueble) {
        String sql = "DELETE FROM inmueble_imagenes WHERE codigo_inmueble = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoInmueble);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al eliminar imagenes del inmueble: " + e.getMessage());
        }
    }

    public int contarPorInmueble(String codigoInmueble) {
        String sql = "SELECT COUNT(*) as total FROM inmueble_imagenes WHERE codigo_inmueble = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoInmueble);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.out.println("Error al contar imagenes: " + e.getMessage());
        }
        return 0;
    }
}
