package com.proptech.dao;

import java.sql.*;

public class CheckUsuarios {
    public static void main(String[] args) {
        try (Connection conn = ConexionDB.conectar()) {
            if (conn == null) {
                System.out.println("Error: No se pudo conectar a la base de datos");
                return;
            }
            
            // Verificar tabla roles
            System.out.println("=== ROLES ===");
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM roles")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.println("Rol: id=" + rs.getInt("id_rol") + ", nombre=" + rs.getString("nombre"));
                }
            }
            
            // Verificar tabla usuarios
            System.out.println("\n=== USUARIOS ===");
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT u.id_usuario, u.email, u.nombre, u.telefono, u.activo, r.nombre as rol " +
                "FROM usuarios u JOIN roles r ON u.rol_id = r.id_rol")) {
                ResultSet rs = stmt.executeQuery();
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("Usuario: id=" + rs.getInt("id_usuario") + 
                        ", email=" + rs.getString("email") + 
                        ", nombre=" + rs.getString("nombre") +
                        ", rol=" + rs.getString("rol") +
                        ", activo=" + rs.getInt("activo"));
                }
                System.out.println("Total usuarios: " + count);
                
                if (count == 0) {
                    System.out.println("\nNo hay usuarios guardados. Necesitas registrar uno primero.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}