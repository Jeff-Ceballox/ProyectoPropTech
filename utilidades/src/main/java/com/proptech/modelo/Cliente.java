package com.proptech.modelo;

import com.proptech.utilidades.estructuras.ListaEnlazada;

/**
 * Representa a un usuario que busca comprar o arrendar en la plataforma.
 */
public class Cliente {
    private String identificacion; // Cédula o NIT (Será la clave en nuestra Tabla Hash)
    private String nombre;
    private String telefono;
    private double presupuestoMaximo;
    private String email;
    private ListaEnlazada<Inmueble> historialConsultas;
    private ListaEnlazada<Inmueble> favoritos;
    
    // Nuevos campos para el sistema de recomendación
    private String tipoInmuebleDeseado; // Ej: "Apartamento", "Casa"
    private String zonasInteres; // Ej: "Norte, Sur, Centro" o "Zona Norte, Zona Sur"
    private int cantMinHabitaciones; // Cantidad mínima de habitaciones requerida

    /**
     * Constructor vacío requerido por Jackson (deserialización JSON).
     */
    public Cliente() {
        this.historialConsultas = new ListaEnlazada<>();
        this.favoritos = new ListaEnlazada<>();
        this.tipoInmuebleDeseado = "";
        this.zonasInteres = "";
        this.cantMinHabitaciones = 1;
    }

    public Cliente(String identificacion, String nombre, String telefono, double presupuestoMaximo, String email) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.telefono = telefono;
        this.presupuestoMaximo = presupuestoMaximo;
        this.email = email;
        this.historialConsultas = new ListaEnlazada<>();
        this.favoritos = new ListaEnlazada<>();
        this.tipoInmuebleDeseado = "";
        this.zonasInteres = "";
        this.cantMinHabitaciones = 1;
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

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public ListaEnlazada<Inmueble> getHistorialConsultas() { return historialConsultas; }
    
    public void agregarConsulta(Inmueble inmueble) {
        this.historialConsultas.agregar(inmueble);
    }

    public ListaEnlazada<Inmueble> getFavoritos() { return favoritos; }
    
    public void agregarFavorito(Inmueble inmueble) {
        this.favoritos.agregar(inmueble);
    }
    
    // Nuevos getters y setters para el sistema de recomendación
    public String getTipoInmuebleDeseado() { return tipoInmuebleDeseado; }
    public void setTipoInmuebleDeseado(String tipoInmuebleDeseado) { this.tipoInmuebleDeseado = tipoInmuebleDeseado; }
    
    public String getZonasInteres() { return zonasInteres; }
    public void setZonasInteres(String zonasInteres) { this.zonasInteres = zonasInteres; }
    
    public int getCantMinHabitaciones() { return cantMinHabitaciones; }
    public void setCantMinHabitaciones(int cantMinHabitaciones) { this.cantMinHabitaciones = cantMinHabitaciones; }

    @Override
    public String toString() {
        return nombre + " (ID: " + identificacion + ") | Correo: " + email + " | Presupuesto: $" + presupuestoMaximo + "M";
    }
}