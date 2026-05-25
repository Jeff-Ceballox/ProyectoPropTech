package com.proptech.modelo;

public class Rol {
    public static final String CLIENTE = "CLIENTE";
    public static final String VENDEDOR = "VENDEDOR";
    public static final String ADMIN = "ADMIN";
    public static final String GERENTE = "GERENTE";
    
    private int idRol;
    private String nombre;
    
    public Rol() {}
    
    public Rol(int idRol, String nombre) {
        this.idRol = idRol;
        this.nombre = nombre;
    }
    
    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    @Override
    public String toString() {
        return nombre;
    }
}