package com.proptech.dao;

import java.sql.*;

/**
 * Gestiona la conexión con la base de datos embebida SQLite.
 */
public class ConexionDB {
    
    // Ruta del archivo de base de datos. Se creará automáticamente en la raíz del proyecto.
    private static final String URL = "jdbc:sqlite:inmobiliaria.db";

    /**
     * Establece la conexión con SQLite.
     * @return El objeto Connection de Java.
     */
    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Error fatal conectando a la base de datos: " + e.getMessage());
            return null;
        }
    }

    /**
     * Método que arranca la base de datos y crea las tablas si no existen.
     */
    public static void inicializarTablas() {
        String sqlInmuebles = "CREATE TABLE IF NOT EXISTS inmuebles ("
                + "codigo TEXT PRIMARY KEY, "
                + "tipo TEXT NOT NULL, "
                + "direccion TEXT NOT NULL, "
                + "precio REAL NOT NULL, "
                + "area REAL NOT NULL, "
                + "estado TEXT NOT NULL"
                + ");";

        try (Connection conn = conectar(); 
             Statement stmt = conn.createStatement()) {
            
            // Ejecutamos el comando SQL de creación
            stmt.execute(sqlInmuebles);
            System.out.println("Base de datos sincronizada: Tabla 'inmuebles' lista.");
            
        } catch (SQLException e) {
            System.out.println("Error creando las tablas: " + e.getMessage());
        }
    }
}