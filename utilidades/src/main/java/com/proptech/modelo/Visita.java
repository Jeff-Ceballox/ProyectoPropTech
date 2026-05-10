package com.proptech.modelo;

/**
 * Representa la programación de un encuentro físico entre un cliente, un asesor y un inmueble.
 */
public class Visita {
    private String idVisita;
    private Cliente cliente;   // ¡Vinculamos el objeto Cliente completo!
    private Inmueble inmueble; // ¡Vinculamos el objeto Inmueble completo!
    private Asesor asesor;     // ¡Vinculamos el objeto Asesor completo!
    private String fechaHora;  // Ej: "2026-05-15 14:30"
    private String estado;     // Ej: "Pendiente", "Realizada", "Cancelada"

    public Visita(String idVisita, Cliente cliente, Inmueble inmueble, Asesor asesor, String fechaHora) {
        this.idVisita = idVisita;
        this.cliente = cliente;
        this.inmueble = inmueble;
        this.asesor = asesor;
        this.fechaHora = fechaHora;
        this.estado = "Pendiente"; // Al programarse, siempre nace como Pendiente
    }

    // --- Getters y Setters ---
    public String getIdVisita() { return idVisita; }
    
    public Cliente getCliente() { return cliente; }
    
    public Inmueble getInmueble() { return inmueble; }
    
    public Asesor getAsesor() { return asesor; }
    
    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "Visita [" + idVisita + "] " + fechaHora + " | " + estado + 
               "\n   -> Cliente: " + cliente.getNombre() + 
               "\n   -> Inmueble: " + inmueble.getCodigo() + 
               "\n   -> Asesor: " + asesor.getNombre();
    }
}