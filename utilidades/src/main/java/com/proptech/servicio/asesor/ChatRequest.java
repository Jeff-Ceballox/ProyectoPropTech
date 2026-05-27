package com.proptech.servicio.asesor;

public class ChatRequest {
    private String pregunta;
    private String emailUsuario;

    public ChatRequest() {}

    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }
    public String getEmailUsuario() { return emailUsuario; }
    public void setEmailUsuario(String emailUsuario) { this.emailUsuario = emailUsuario; }
}
