package com.proptech.modelo;

/**
 * Representa una propiedad (casa, apartamento, local) en la plataforma PropTech.
 */
public class Inmueble {
    private String codigo;      // Ej: "A-001"
    private String tipo;        // Ej: "Apartamento", "Casa", "Oficina"
    private String direccion;
    private double precio;
    private double area;        // En metros cuadrados
    private String estado;      // Ej: "Disponible", "Vendido", "Arrendado"

    public Inmueble(String codigo, String tipo, String direccion, double precio, double area) {
        this.codigo = codigo;
        this.tipo = tipo; 
        this.direccion = direccion;
        this.precio = precio;
        this.area = area;
        this.estado = "Disponible"; // Por defecto, todo nuevo inmueble está disponible
    }

    // --- Getters y Setters ---
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // Método para imprimir el inmueble de forma legible
    @Override
    public String toString() {
        return tipo + " en " + direccion + " | Precio: $" + precio + "M | Área: " + area + "m2 (" + estado + ")";
    }
}