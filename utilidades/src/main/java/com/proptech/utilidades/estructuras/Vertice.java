package com.proptech.utilidades.estructuras;

/**
 * Representa un punto (nodo) dentro del Grafo.
 */
public class Vertice<T> {
    private T dato; // Puede ser un Cliente, un Inmueble, una Zona, etc.
    
    // Usamos la lista que creamos en la Fase 1 para guardar las conexiones
    private ListaEnlazada<T> adyacentes; 

    public Vertice(T dato) {
        this.dato = dato;
        this.adyacentes = new ListaEnlazada<>();
    }

    public T getDato() { return dato; }
    public ListaEnlazada<T> getAdyacentes() { return adyacentes; }
}