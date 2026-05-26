package com.proptech.servicio;

import com.proptech.modelo.Usuario;
import com.proptech.modelo.Rol;
import com.proptech.dao.UsuarioDAO;
import com.proptech.utilidades.HashUtils;
import com.proptech.modelo.Inmueble;
import com.proptech.utilidades.estructuras.ListaEnlazada;

import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private UsuarioDAO usuarioDAO;
    private RecomendacionService recomendacionService;
    
    public AuthService() {
        this.usuarioDAO = new UsuarioDAO();
        this.recomendacionService = new RecomendacionService();
    }
    
    public Usuario registrar(String email, String password, String nombre) {
        if (usuarioDAO.buscarPorEmail(email) != null) {
            return null;
        }
        String passwordHash = HashUtils.sha256(password);
        return usuarioDAO.crear(email, passwordHash, nombre, Rol.CLIENTE);
    }
    
    public Usuario login(String email, String password) {
        // Primero sincronizamos con BD por si se reinició la app
        Usuario usuario = usuarioDAO.buscarPorEmail(email);
        if (usuario == null || !usuario.isActivo()) {
            System.out.println("Login fallido para: " + email + " - Usuario no encontrado o inactivo");
            return null;
        }
        String passwordHash = HashUtils.sha256(password);
        if (passwordHash.equals(usuario.getPasswordHash())) {
            System.out.println("Login exitoso para: " + email);
            return usuario;
        }
        System.out.println("Login fallido - Contraseña incorrecta para: " + email);
        return null;
    }
    
    public Usuario crearAdmin(String email, String password, String nombre) {
        if (usuarioDAO.buscarPorEmail(email) != null) {
            return null;
        }
        String passwordHash = HashUtils.sha256(password);
        return usuarioDAO.crear(email, passwordHash, nombre, Rol.ADMIN);
    }
    
    public Map<String, Object> registrarConRecomendaciones(String email, String password, String nombre) {
        Usuario usuario = registrar(email, password, nombre);
        Map<String, Object> resultado = new HashMap<>();
        
        if (usuario == null) {
            resultado.put("exito", false);
            resultado.put("error", "El email ya está registrado");
            return resultado;
        }
        
        ListaEnlazada<Inmueble> recomendaciones = recomendacionService.generarRecomendaciones(usuario.getEmail());
        resultado.put("exito", true);
        resultado.put("mensaje", "¡Cuenta creada! Recibe tus 3 recomendaciones personalizadas");
        resultado.put("recomendaciones", recomendaciones);
        resultado.put("email", usuario.getEmail());
        
        System.out.println("=== EMAIL SIMULADO ===");
        System.out.println("Para: " + email);
        System.out.println("Asunto: Tus 3 recomendaciones de PropTech");
        System.out.println("Contenido: Hola " + nombre + ", aquí tienes 3 propiedades recomendadas...");
        for (int i = 0; i < Math.min(3, recomendaciones.getTamaño()); i++) {
            System.out.println("  - " + recomendaciones.obtener(i).getTipo() + " en " + recomendaciones.obtener(i).getDireccion());
        }
        System.out.println("======================");
        
        return resultado;
    }
    
    public Usuario obtenerPerfil(String email) {
        return usuarioDAO.buscarPorEmail(email);
    }
    
    public boolean actualizarPerfil(Usuario usuario) {
        return usuarioDAO.actualizar(usuario);
    }
}