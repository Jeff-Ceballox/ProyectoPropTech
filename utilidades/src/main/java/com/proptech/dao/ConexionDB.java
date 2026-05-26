package com.proptech.dao;

import java.sql.*;

/**
 * Gestiona la conexión con la base de datos embebida SQLite.
 */
public class ConexionDB {
    
    // Ruta del archivo de base de datos. Se creará automáticamente en la raíz del proyecto.
    private static final String URL = "jdbc:sqlite:" + System.getProperty("user.home") + "/.proptech/inmobiliaria.db";

    /**
     * Establece la conexión con SQLite.
     * @return El objeto Connection de Java.
     */
    public static Connection conectar() {
        try {
            String dbPath = System.getProperty("user.home") + "/.proptech/inmobiliaria.db";
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(System.getProperty("user.home") + "/.proptech"));
            return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (SQLException | java.io.IOException e) {
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
                + "estado TEXT NOT NULL, "
                + "habitaciones INTEGER, "
                + "banos INTEGER, "
                + "tieneParqueadero BOOLEAN, "
                + "descripcion TEXT"
                + ");";
        String sqlClientes = "CREATE TABLE IF NOT EXISTS clientes ("
                + "identificacion TEXT PRIMARY KEY, "
                + "nombre TEXT NOT NULL, "
                + "telefono TEXT, "
                + "presupuestoMaximo REAL, "
                + "email TEXT"
                + ");";
        String sqlAsesores = "CREATE TABLE IF NOT EXISTS asesores ("
                + "idAsesor TEXT PRIMARY KEY, "
                + "nombre TEXT NOT NULL, "
                + "especialidad TEXT, "
                + "email TEXT, "
                + "telefono TEXT, "
                + "calificacion REAL DEFAULT 5.0, "
                + "negociosCerrados INTEGER DEFAULT 0"
                + ");";
        String sqlOperaciones = "CREATE TABLE IF NOT EXISTS operaciones ("
                + "idOperacion TEXT PRIMARY KEY, "
                + "tipo TEXT NOT NULL, "
                + "idInmueble TEXT, "
                + "idCliente TEXT, "
                + "idAsesor TEXT, "
                + "monto REAL NOT NULL, "
                + "fecha TEXT NOT NULL, "
                + "FOREIGN KEY(idInmueble) REFERENCES inmuebles(codigo), "
                + "FOREIGN KEY(idCliente) REFERENCES clientes(identificacion), "
                + "FOREIGN KEY(idAsesor) REFERENCES asesores(idAsesor)"
                + ");";
        String sqlVisitas = "CREATE TABLE IF NOT EXISTS visitas ("
                + "idVisita TEXT PRIMARY KEY, "
                + "identificacionCliente TEXT, "
                + "codigoInmueble TEXT, "
                + "idAsesor TEXT, "
                + "fechaHora TEXT NOT NULL, "
                + "estado TEXT NOT NULL, "
                + "FOREIGN KEY(identificacionCliente) REFERENCES clientes(identificacion), "
                + "FOREIGN KEY(codigoInmueble) REFERENCES inmuebles(codigo), "
                + "FOREIGN KEY(idAsesor) REFERENCES asesores(idAsesor)"
                + ");";

        try (Connection conn = conectar(); 
             Statement stmt = conn.createStatement()) {
             
            // Crear tablas
            stmt.execute(sqlInmuebles);
            stmt.execute(sqlClientes);
            stmt.execute(sqlAsesores);
            stmt.execute(sqlOperaciones);
             stmt.execute(sqlVisitas);
              System.out.println("Base de datos sincronizada: Tablas 'inmuebles', 'clientes', 'asesores', 'operaciones' y 'visitas' listas.");
               
          } catch (SQLException e) {
              System.out.println("Error creando las tablas: " + e.getMessage());
          }
      }

      /**
       * Main method to initialize the database.
       * @param args
       */
      public static void main(String[] args) {
          inicializarTablas();
      }
}