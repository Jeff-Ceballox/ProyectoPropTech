package com.proptech.utilidades.estructuras;

/**
 * Eslabón especial para la Cola de Prioridad.
 * Almacena el dato y un nivel de urgencia.
 */
public class NodoPrioridad<T> {
    private T dato;
    private int prioridad; // Ejemplo: 1 = Urgente (VIP), 5 = Normal
    private NodoPrioridad<T> siguiente;

    public NodoPrioridad(T dato, int prioridad) {
        this.dato = dato;
        this.prioridad = prioridad;
        this.siguiente = null;
    }

    public T getDato() { return dato; }
    public int getPrioridad() { return prioridad; }
    public NodoPrioridad<T> getSiguiente() { return siguiente; }
    public void setSiguiente(NodoPrioridad<T> siguiente) { this.siguiente = siguiente; }
}