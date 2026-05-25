package com.proptech.dao;

import com.proptech.modelo.Usuario;
import com.proptech.modelo.Rol;
import com.proptech.utilidades.estructuras.ListaEnlazada;

import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;

public class UsuarioDAO {
    private ConcurrentHashMap<String, Usuario> cacheUsuarios;
    private ConcurrentHashMap<String, Rol> cacheRoles;
    
    public UsuarioDAO() {
        this.cacheUsuarios = new ConcurrentHashMap<>();
        this.cacheRoles = new ConcurrentHashMap<>();
        inicializarRoles();
    }
    
    private void inicializarRoles() {
        try (Connection conn = ConexionDB.conectar()) {
            Statement stmt = conn.createStatement();
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS roles (
                    id_rol INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT UNIQUE NOT NULL
                )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                    email TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    rol_id INTEGER NOT NULL,
                    nombre TEXT NOT NULL,
                    telefono TEXT,
                    direccion TEXT,
                    intereses TEXT,
                    foto_perfil TEXT,
                    activo INTEGER DEFAULT 1,
                    FOREIGN KEY (rol_id) REFERENCES roles(id_rol)
                )
                """);
            
            try {
                stmt.execute("ALTER TABLE usuarios ADD COLUMN telefono TEXT");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE usuarios ADD COLUMN direccion TEXT");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE usuarios ADD COLUMN intereses TEXT");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE usuarios ADD COLUMN foto_perfil TEXT");
            } catch (SQLException ignored) {}
            
            PreparedStatement checkRoles = conn.prepareStatement("SELECT COUNT(*) FROM roles");
            ResultSet rs = checkRoles.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                PreparedStatement insertRol = conn.prepareStatement("INSERT INTO roles (nombre) VALUES (?)");
                insertRol.setString(1, Rol.CLIENTE);
                insertRol.executeUpdate();
                insertRol.setString(1, Rol.VENDEDOR);
                insertRol.executeUpdate();
                insertRol.setString(1, Rol.ADMIN);
                insertRol.executeUpdate();
                insertRol.setString(1, Rol.GERENTE);
                insertRol.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error inicializando roles: " + e.getMessage());
        }
    }
    
    public Usuario crear(String email, String passwordHash, String nombre, String nombreRol) {
        try (Connection conn = ConexionDB.conectar()) {
            Rol rol = obtenerRolPorNombre(nombreRol);
            if (rol == null) return null;
            
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO usuarios (email, password_hash, rol_id, nombre) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            stmt.setString(1, email);
            stmt.setString(2, passwordHash);
            stmt.setInt(3, rol.getIdRol());
            stmt.setString(4, nombre);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                Usuario usuario = new Usuario(rs.getInt(1), email, passwordHash, rol, nombre);
                cacheUsuarios.put(email, usuario);
                return usuario;
            }
        } catch (SQLException e) {
            System.err.println("Error creando usuario: " + e.getMessage());
        }
        return null;
    }
    
    public boolean actualizar(Usuario usuario) {
        try (Connection conn = ConexionDB.conectar()) {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE usuarios SET nombre = ?, telefono = ?, direccion = ?, intereses = ?, foto_perfil = ? WHERE id_usuario = ?"
            );
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getTelefono());
            stmt.setString(3, usuario.getDireccion());
            stmt.setString(4, usuario.getIntereses());
            stmt.setString(5, usuario.getFotoPerfil());
            stmt.setInt(6, usuario.getIdUsuario());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                cacheUsuarios.put(usuario.getEmail(), usuario);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error actualizando usuario: " + e.getMessage());
        }
        return false;
    }
    
    public Usuario buscarPorEmail(String email) {
        if (cacheUsuarios.containsKey(email)) {
            return cacheUsuarios.get(email);
        }
        try (Connection conn = ConexionDB.conectar()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT u.id_usuario, u.email, u.password_hash, u.nombre, u.telefono, u.direccion, u.intereses, u.foto_perfil, u.activo, r.id_rol, r.nombre as rol_nombre " +
                "FROM usuarios u JOIN roles r ON u.rol_id = r.id_rol WHERE u.email = ?"
            );
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Rol rol = new Rol(rs.getInt("id_rol"), rs.getString("rol_nombre"));
                Usuario usuario = new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rol,
                    rs.getString("nombre")
                );
                usuario.setTelefono(rs.getString("telefono"));
                usuario.setDireccion(rs.getString("direccion"));
                usuario.setIntereses(rs.getString("intereses"));
                usuario.setFotoPerfil(rs.getString("foto_perfil"));
                usuario.setActivo(rs.getInt("activo") == 1);
                cacheUsuarios.put(email, usuario);
                return usuario;
            }
        } catch (SQLException e) {
            System.err.println("Error buscando usuario: " + e.getMessage());
        }
        return null;
    }
    
    public ListaEnlazada<Usuario> obtenerTodos() {
        ListaEnlazada<Usuario> usuarios = new ListaEnlazada<>();
        try (Connection conn = ConexionDB.conectar()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT u.id_usuario, u.email, u.password_hash, u.nombre, u.telefono, u.direccion, u.intereses, u.foto_perfil, u.activo, r.id_rol, r.nombre as rol_nombre " +
                "FROM usuarios u JOIN roles r ON u.rol_id = r.id_rol"
            );
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Rol rol = new Rol(rs.getInt("id_rol"), rs.getString("rol_nombre"));
                Usuario usuario = new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rol,
                    rs.getString("nombre")
                );
                usuario.setTelefono(rs.getString("telefono"));
                usuario.setDireccion(rs.getString("direccion"));
                usuario.setIntereses(rs.getString("intereses"));
                usuario.setFotoPerfil(rs.getString("foto_perfil"));
                usuario.setActivo(rs.getInt("activo") == 1);
                usuarios.agregar(usuario);
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo usuarios: " + e.getMessage());
        }
        return usuarios;
    }
    
    public Rol obtenerRolPorNombre(String nombre) {
        if (cacheRoles.containsKey(nombre)) {
            return cacheRoles.get(nombre);
        }
        try (Connection conn = ConexionDB.conectar()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT id_rol, nombre FROM roles WHERE nombre = ?");
            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Rol rol = new Rol(rs.getInt("id_rol"), rs.getString("nombre"));
                cacheRoles.put(nombre, rol);
                return rol;
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo rol: " + e.getMessage());
        }
        return null;
    }
}