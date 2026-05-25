package com.proptech.modelo;

public class Usuario {
    private int idUsuario;
    private String email;
    private String passwordHash;
    private Rol rol;
    private String nombre;
    private String telefono;
    private String direccion;
    private String intereses;
    private String fotoPerfil;
    private boolean activo;
    
    public Usuario() {
        this.activo = true;
    }
    
    public Usuario(int idUsuario, String email, String passwordHash, Rol rol, String nombre) {
        this.idUsuario = idUsuario;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.nombre = nombre;
        this.activo = true;
    }
    
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public String getIntereses() { return intereses; }
    public void setIntereses(String intereses) { this.intereses = intereses; }
    
    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}