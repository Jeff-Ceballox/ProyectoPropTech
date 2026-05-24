package com.proptech.servicio;

import com.proptech.modelo.Asesor;
import com.proptech.modelo.Cliente;
import com.proptech.modelo.Inmueble;
import com.proptech.modelo.Operacion;
import com.proptech.utilidades.estructuras.ListaEnlazada;

public class OperacionesService {
    // Usamos ListaEnlazada como se solicitó para gestionar el historial de contratos
    private ListaEnlazada<Operacion> contratos;

    public OperacionesService() {
        this.contratos = new ListaEnlazada<>();
    }

    public void registrarOperacion(Operacion operacion) {
        this.contratos.agregar(operacion);
        
        // Actualizamos estado del inmueble
        Inmueble inmueble = operacion.getInmueble();
        if (inmueble != null) {
            if ("Venta".equalsIgnoreCase(operacion.getTipo())) {
                inmueble.setEstado("Vendido");
            } else if ("Arriendo".equalsIgnoreCase(operacion.getTipo())) {
                inmueble.setEstado("Arrendado");
            }
        }

        // Incrementamos métricas del asesor
        Asesor asesor = operacion.getAsesor();
        if (asesor != null) {
            asesor.registrarNegocioExitoso();
        }
        
        System.out.println("Operación registrada exitosamente: " + operacion.toString());
    }

    public ListaEnlazada<Operacion> obtenerTodas() {
        return contratos;
    }
    
    public ListaEnlazada<Operacion> obtenerPorAsesor(String idAsesor) {
        ListaEnlazada<Operacion> resultado = new ListaEnlazada<>();
        for (int i = 0; i < contratos.getTamaño(); i++) {
            Operacion op = contratos.obtener(i);
            if (op.getAsesor() != null && op.getAsesor().getIdAsesor().equals(idAsesor)) {
                resultado.agregar(op);
            }
        }
        return resultado;
    }
}
