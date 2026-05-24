package com.proptech.servicio;

import com.proptech.modelo.Alerta;
import com.proptech.utilidades.estructuras.ColaPrioridad;

public class AlertasService {
    private ColaPrioridad<Alerta> colaAlertas;

    public AlertasService() {
        this.colaAlertas = new ColaPrioridad<>();
    }

    public void agregarAlerta(Alerta alerta) {
        colaAlertas.encolar(alerta, alerta.getPrioridad());
        System.out.println("Alerta registrada: " + alerta.toString());
    }

    public Alerta atenderSiguienteAlerta() {
        if (!hayAlertasPendientes()) {
            return null;
        }
        Alerta atendida = colaAlertas.desencolar();
        System.out.println("Alerta atendida: " + atendida.toString());
        return atendida;
    }

    public boolean hayAlertasPendientes() {
        return !colaAlertas.estaVacia();
    }
    
    public int getCantidadAlertas() {
        return colaAlertas.getTamaño();
    }
}
