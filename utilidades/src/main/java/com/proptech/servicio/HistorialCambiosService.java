package com.proptech.servicio;

import com.proptech.modelo.Inmueble;
import com.proptech.utilidades.estructuras.Pila;

/**
 * Servicio encargado de auditar y permitir la reversión (Ctrl+Z) de las 
 * modificaciones realizadas a los inmuebles usando una Pila (Stack).
 */
public class HistorialCambiosService {

    // Clase interna para empaquetar el estado de un cambio
    private static class RegistroCambio {
        Inmueble inmuebleModificado;
        double precioAnterior;
        String estadoAnterior;

        public RegistroCambio(Inmueble inmueble, double precioAnt, String estadoAnt) {
            this.inmuebleModificado = inmueble;
            this.precioAnterior = precioAnt;
            this.estadoAnterior = estadoAnt;
        }
    }

    // Pila que guardará el historial de cambios
    private Pila<RegistroCambio> pilaDeshacer;

    public HistorialCambiosService() {
        this.pilaDeshacer = new Pila<>();
    }

    /**
     * Modifica el precio o estado de un inmueble, guardando un respaldo del estado anterior.
     */
    public void actualizarInmueble(Inmueble inmueble, double nuevoPrecio, String nuevoEstado) {
        // 1. Guardamos una "foto" de cómo estaba el inmueble ANTES del cambio
        RegistroCambio respaldo = new RegistroCambio(inmueble, inmueble.getPrecio(), inmueble.getEstado());
        
        // 2. Apilamos ese respaldo (Lo ponemos en la cima de la Pila)
        pilaDeshacer.apilar(respaldo);
        
        // 3. Aplicamos los nuevos cambios al inmueble real
        inmueble.setPrecio(nuevoPrecio);
        inmueble.setEstado(nuevoEstado);
        
        System.out.println("Actualización guardada: " + inmueble.getCodigo() + " -> " + nuevoEstado + " / $" + nuevoPrecio + "M");
    }

    /**
     * Revierte (Deshace) la última modificación realizada en el sistema.
     * @return true si se pudo deshacer, false si no hay historial.
     */
    public boolean deshacerUltimoCambio() {
        if (pilaDeshacer.estaVacia()) {
            System.out.println("No hay cambios recientes para deshacer.");
            return false;
        }

        // 1. Desapilamos (Sacamos) el último cambio registrado
        RegistroCambio ultimoCambio = pilaDeshacer.desapilar();

        // 2. Restauramos los valores originales al inmueble
        Inmueble inmueble = ultimoCambio.inmuebleModificado;
        inmueble.setPrecio(ultimoCambio.precioAnterior);
        inmueble.setEstado(ultimoCambio.estadoAnterior);

        System.out.println("Cambio deshecho. " + inmueble.getCodigo() + " ha vuelto a su estado anterior.");
        return true;
    }
}