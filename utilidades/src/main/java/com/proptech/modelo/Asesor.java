package com.proptech.modelo;

/**
 * Representa a un agente inmobiliario que atiende clientes y cierra negocios.
 */
public class Asesor {
    private String idAsesor;
    private String nombre;
    private String especialidad; // Ej: "Ventas Comerciales", "Arriendos Residenciales"
    private int negociosCerrados; // Nos servirá para el sistema de Rankings
    private String email;
    private String telefono;
    private double calificacion;

    public Asesor(String idAsesor, String nombre, String especialidad, String email, String telefono) {
        this.idAsesor = idAsesor;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.email = email;
        this.telefono = telefono;
        this.calificacion = 5.0; // Todo asesor empieza con calificación máxima
        this.negociosCerrados = 0; // Todo asesor empieza con 0 ventas/arriendos
    }

    // --- Getters y Setters ---
    public String getIdAsesor() { return idAsesor; }
    public void setIdAsesor(String idAsesor) { this.idAsesor = idAsesor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public int getNegociosCerrados() { return negociosCerrados; }
    public void setNegociosCerrados(int negociosCerrados) { this.negociosCerrados = negociosCerrados; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public double getCalificacion() { return calificacion; }
    public void setCalificacion(double calificacion) { this.calificacion = calificacion; }

    // Método especial para incrementar los negocios cuando se cierra un contrato
    public void registrarNegocioExitoso() {
        this.negociosCerrados++;
    }

    @Override
    public String toString() {
        return "Asesor: " + nombre + " | Especialidad: " + especialidad + " | Contacto: " + email + " | Cierres: " + negociosCerrados;
    }
}