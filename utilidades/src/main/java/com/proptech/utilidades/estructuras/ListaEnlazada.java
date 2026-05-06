package com.proptech.utilidades.estructuras;

/**
 * Nuestra propia implementación de una Lista Enlazada Simple y Genérica.
 * 
 * @param <T> El tipo de dato que almacenará la lista (Inmuebles, Clientes, etc.)
 */
public class ListaEnlazada<T> {
    
    // Apunta al primer nodo de la lista. Si es null, la lista está vacía.
    private Nodo<T> cabeza;
    
    // Mantiene un conteo rápido de cuántos elementos hay en total.
    private int tamaño;

    /**
     * Constructor: Inicializa la lista vacía.
     */
    public ListaEnlazada() {
        this.cabeza = null;
        this.tamaño = 0;
    }

    /**
     * Añade un nuevo elemento al final de la lista.
     * @param dato El valor a guardar.
     */
    public void agregar(T dato) {
        Nodo<T> nuevoNodo = new Nodo<>(dato); // Empaquetamos el dato en un Nodo

        if (cabeza == null) {
            // Caso 1: La lista estaba vacía. El nuevo nodo es ahora la cabeza.
            cabeza = nuevoNodo;
        } else {
            // Caso 2: Ya hay elementos. Debemos recorrer la lista hasta el final.
            Nodo<T> actual = cabeza;
            
            // Saltamos de nodo en nodo mientras exista un "siguiente"
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            
            // Enlazamos el último nodo encontrado con el nuevo nodo
            actual.setSiguiente(nuevoNodo);
        }
        tamaño++; // Aumentamos el contador de elementos
    }

    /**
     * Recupera el dato guardado en una posición específica.
     * @param indice La posición (comenzando desde 0).
     * @return El dato de tipo T.
     */
    public T obtener(int indice) {
        // Validamos que el índice solicitado exista realmente en la lista
        if (indice < 0 || indice >= tamaño) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
        
        Nodo<T> actual = cabeza;
        // Avanzamos el puntero 'indice' veces
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }
        
        return actual.getDato(); // Extraemos y devolvemos el dato del nodo encontrado
    }

    // Retorna la cantidad de elementos almacenados
    public int getTamaño() {
        return tamaño;
    }

    // Retorna true si la lista no tiene elementos
    public boolean estaVacia() {
        return cabeza == null;
    }
}