package com.proptech.utilidades.estructuras;

/**
 * Estructura dinámica donde los elementos con mayor prioridad (menor número) 
 * se ubican más cerca del frente para ser atendidos primero.
 */
public class ColaPrioridad<T> {
    private NodoPrioridad<T> frente;
    private int tamaño;

    public ColaPrioridad() {
        this.frente = null;
        this.tamaño = 0;
    }

    /**
     * Inserta un elemento en la posición correcta según su prioridad.
     * @param dato El valor a guardar.
     * @param prioridad Nivel de urgencia (1 es más urgente que 2).
     */
    public void encolar(T dato, int prioridad) {
        NodoPrioridad<T> nuevoNodo = new NodoPrioridad<>(dato, prioridad);

        // Caso 1: La fila está vacía, o el nuevo es más urgente que el primero
        if (estaVacia() || prioridad < frente.getPrioridad()) {
            nuevoNodo.setSiguiente(frente);
            frente = nuevoNodo;
        } else {
            // Caso 2: Buscar el lugar correcto en la fila
            NodoPrioridad<T> actual = frente;
            
            // Avanzamos mientras haya un siguiente Y ese siguiente sea más o igual de importante
            while (actual.getSiguiente() != null && actual.getSiguiente().getPrioridad() <= prioridad) {
                actual = actual.getSiguiente();
            }
            
            // Insertamos el nuevo nodo en el hueco encontrado
            nuevoNodo.setSiguiente(actual.getSiguiente());
            actual.setSiguiente(nuevoNodo);
        }
        tamaño++;
    }

    /**
     * Retira y devuelve el elemento más urgente (el que está al frente).
     */
    public T desencolar() {
        if (estaVacia()) {
            return null;
        }
        T datoAtendido = frente.getDato();
        frente = frente.getSiguiente();
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