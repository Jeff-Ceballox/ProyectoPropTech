package com.proptech.modelo;

public class Operacion {
    private String idOperacion;
    private String tipo; // "Venta", "Arriendo"
    private Inmueble inmueble;
    private Cliente cliente;
    private Asesor asesor;
    private double monto;
    private String fecha;

    public Operacion(String idOperacion, String tipo, Inmueble inmueble, Cliente cliente, Asesor asesor, double monto, String fecha) {
        this.idOperacion = idOperacion;
        this.tipo = tipo;
        this.inmueble = inmueble;
        this.cliente = cliente;
        this.asesor = asesor;
        this.monto = monto;
        this.fecha = fecha;
    }

    // Getters and Setters...
    public String getIdOperacion() { return idOperacion; }
    public void setIdOperacion(String idOperacion) { this.idOperacion = idOperacion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Inmueble getInmueble() { return inmueble; }
    public void setInmueble(Inmueble inmueble) { this.inmueble = inmueble; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Asesor getAsesor() { return asesor; }
    public void setAsesor(Asesor asesor) { this.asesor = asesor; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    @Override
    public String toString() {
        return tipo + " | " + inmueble.getCodigo() + " | Cliente: " + cliente.getNombre() + " | Asesor: " + asesor.getNombre() + " | $" + monto + "M";
    }
}
