package com.proptech.modelo;

public class Alerta {
    private String idAlerta;
    private String mensaje;
    private int prioridad; // 1 = Urgente, 2 = Alta, 3 = Normal
    private String fechaCreacion;

    public Alerta(String idAlerta, String mensaje, int prioridad, String fechaCreacion) {
        this.idAlerta = idAlerta;
        this.mensaje = mensaje;
        this.prioridad = prioridad;
        this.fechaCreacion = fechaCreacion;
    }

    public String getIdAlerta() { return idAlerta; }
    public void setIdAlerta(String idAlerta) { this.idAlerta = idAlerta; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public int getPrioridad() { return prioridad; }
    public void setPrioridad(int prioridad) { this.prioridad = prioridad; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public String toString() {
        return "[Prioridad " + prioridad + "] " + mensaje + " (" + fechaCreacion + ")";
    }
}
