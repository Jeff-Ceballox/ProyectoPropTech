package com.proptech.modelo;

/**
 * Representa a un usuario que busca comprar o arrendar en la plataforma.
 */
public class Cliente {
    private String identificacion; // Cédula o NIT (Será la clave en nuestra Tabla Hash)
    private String nombre;
    private String telefono;
    private double presupuestoMaximo;

    public Cliente(String identificacion, String nombre, String telefono, double presupuestoMaximo) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.telefono = telefono;
        this.presupuestoMaximo = presupuestoMaximo;
    }

    // --- Getters y Setters ---
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public double getPresupuestoMaximo() { return presupuestoMaximo; }
    public void setPresupuestoMaximo(double presupuestoMaximo) { this.presupuestoMaximo = presupuestoMaximo; }

    @Override
    public String toString() {
        return nombre + " (ID: " + identificacion + ") | Presupuesto: $" + presupuestoMaximo + "M";
    }
}