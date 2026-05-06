package com.proptech.utilidades.estructuras;

/**
 * Estructura de datos Pila (Stack) basada en Nodos.
 * Sigue el principio LIFO (Último en entrar, primero en salir).
 */
public class Pila<T> {
    
    private Nodo<T> cima; // Apunta al elemento superior de la pila
    private int tamaño;

    public Pila() {
        this.cima = null;
        this.tamaño = 0;
    }

    /**
     * Agrega un nuevo elemento a la cima de la pila.
     * @param dato El valor a guardar.
     */
    public void apilar(T dato) {
        Nodo<T> nuevoNodo = new Nodo<>(dato);
        nuevoNodo.setSiguiente(cima); // El nuevo nodo apunta a la antigua cima
        cima = nuevoNodo;             // La nueva cima es ahora este nodo
        tamaño++;
    }

    /**
     * Retira y devuelve el elemento en la cima de la pila (Deshacer acción).
     * @return El dato de tipo T, o null si la pila está vacía.
     */
    public T desapilar() {
        if (estaVacia()) {
            return null;
        }
        T datoExtraido = cima.getDato(); // Guardamos el dato a devolver
        cima = cima.getSiguiente();      // La nueva cima ahora es el elemento de abajo
        tamaño--;
        return datoExtraido;
    }

    public boolean estaVacia() {
        return cima == null;
    }

    public int getTamaño() {
        return tamaño;
    }
}