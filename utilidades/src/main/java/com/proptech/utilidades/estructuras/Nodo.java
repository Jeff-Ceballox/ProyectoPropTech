package com.proptech.utilidades.estructuras;

/**
 * Clase genérica que representa la unidad básica de nuestras estructuras dinámicas.
 * 
 * @param <T> Permite que el nodo almacene cualquier tipo de objeto sin reescribir código.
 */
public class Nodo<T> {
    
    // Almacena el valor real (puede ser un String, un Inmueble, etc.)
    private T dato;
    
    // Puntero que guarda la referencia en memoria del siguiente nodo
    private Nodo<T> siguiente;

    /**
     * Constructor principal. Al crearse, el nodo está aislado y no apunta a nada.
     */
    public Nodo(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    /* === Métodos de Encapsulamiento (Getters y Setters) === */

    public T getDato() {
        return dato;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public Nodo<T> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(Nodo<T> siguiente) {
        this.siguiente = siguiente;
    }
}