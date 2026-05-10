package com.proptech.modelo;

/**
 * Representa a un agente inmobiliario que atiende clientes y cierra negocios.
 */
public class Asesor {
    private String idAsesor;
    private String nombre;
    private String especialidad; // Ej: "Ventas Comerciales", "Arriendos Residenciales"
    private int negociosCerrados; // Nos servirá para el sistema de Rankings

    public Asesor(String idAsesor, String nombre, String especialidad) {
        this.idAsesor = idAsesor;
        this.nombre = nombre;
        this.especialidad = especialidad;
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
    
    // Método especial para incrementar los negocios cuando se cierra un contrato
    public void registrarNegocioExitoso() {
        this.negociosCerrados++;
    }

    @Override
    public String toString() {
        return "Asesor: " + nombre + " | Especialidad: " + especialidad + " | Cierres: " + negociosCerrados;
    }
}