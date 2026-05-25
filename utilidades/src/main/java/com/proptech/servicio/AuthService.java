package com.proptech.servicio;

import com.proptech.modelo.Usuario;
import com.proptech.modelo.Rol;
import com.proptech.dao.UsuarioDAO;
import com.proptech.utilidades.HashUtils;

public class AuthService {
    private UsuarioDAO usuarioDAO;
    
    public AuthService() {
        this.usuarioDAO = new UsuarioDAO();
    }
    
    public Usuario registrar(String email, String password, String nombre) {
        if (usuarioDAO.buscarPorEmail(email) != null) {
            return null;
        }
        String passwordHash = HashUtils.sha256(password);
        return usuarioDAO.crear(email, passwordHash, nombre, Rol.CLIENTE);
    }
    
    public Usuario login(String email, String password) {
        Usuario usuario = usuarioDAO.buscarPorEmail(email);
        if (usuario == null || !usuario.isActivo()) {
            return null;
        }
        String passwordHash = HashUtils.sha256(password);
        if (passwordHash.equals(usuario.getPasswordHash())) {
            return usuario;
        }
        return null;
    }
    
    public Usuario crearAdmin(String email, String password, String nombre) {
        if (usuarioDAO.buscarPorEmail(email) != null) {
            return null;
        }
        String passwordHash = HashUtils.sha256(password);
        return usuarioDAO.crear(email, passwordHash, nombre, Rol.ADMIN);
    }
}