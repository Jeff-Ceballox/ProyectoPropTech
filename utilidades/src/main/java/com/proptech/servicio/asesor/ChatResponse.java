package com.proptech.servicio.asesor;

public class ChatResponse {
    private String respuesta;
    private boolean exito;

    public ChatResponse() {}

    public ChatResponse(String respuesta) {
        this.respuesta = respuesta;
        this.exito = true;
    }

    public ChatResponse(String respuesta, boolean exito) {
        this.respuesta = respuesta;
        this.exito = exito;
    }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }
}
