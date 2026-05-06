package com.proptech.utilidades.estructuras;

/**
 * Estructura de datos Cola (Queue) basada en Nodos.
 * Sigue el principio FIFO (Primero en entrar, primero en salir).
 */
public class Cola<T> {
    
    private Nodo<T> frente;    // Por donde salen los elementos (el primero en ser atendido)
    private Nodo<T> finalCola; // Por donde entran los nuevos elementos
    private int tamaño;

    public Cola() {
        this.frente = null;
        this.finalCola = null;
        this.tamaño = 0;
    }

    /**
     * Agrega un elemento al final de la fila.
     * @param dato El valor a guardar.
     */
    public void encolar(T dato) {
        Nodo<T> nuevoNodo = new Nodo<>(dato);
        
        if (estaVacia()) {
            // Si no hay nadie en la fila, el nuevo es tanto el frente como el final
            frente = nuevoNodo;
            finalCola = nuevoNodo;
        } else {
            // Si ya hay gente, el último actual apunta al nuevo, y el nuevo se vuelve el último
            finalCola.setSiguiente(nuevoNodo);
            finalCola = nuevoNodo;
        }
        tamaño++;
    }

    /**
     * Retira y devuelve el elemento al frente de la fila.
     * @return El dato de tipo T, o null si la cola está vacía.
     */
    public T desencolar() {
        if (estaVacia()) {
            return null;
        }
        T datoAtendido = frente.getDato(); // Extraemos el dato del frente
        frente = frente.getSiguiente();    // El frente avanza al siguiente en la fila
        
        // Si al desencolar la fila quedó vacía, el final también debe ser null
        if (frente == null) {
            finalCola = null;
        }
        
        tamaño--;
        return datoAtendido;
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int getTamaño() {
        return tamaño;
    }
}