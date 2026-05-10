package com.proptech.utilidades.estructuras;

/**
 * Estructura de Árbol Binario de Búsqueda (BST) para ordenamiento rápido.
 */
public class ArbolBinarioBusqueda<K extends Comparable<K>, V> {
    private NodoArbol<K, V> raiz; // El nodo principal en la cima del árbol

    public ArbolBinarioBusqueda() {
        this.raiz = null;
    }

    /**
     * Inserta un nuevo elemento en la posición correcta del árbol.
     */
    public void insertar(K clave, V valor) {
        raiz = insertarRecursivo(raiz, clave, valor);
    }

    // Método oculto que hace el trabajo matemático de buscar el lugar correcto
    private NodoArbol<K, V> insertarRecursivo(NodoArbol<K, V> actual, K clave, V valor) {
        if (actual == null) {
            return new NodoArbol<>(clave, valor); // Encontramos un espacio vacío
        }

        // Comparamos la clave nueva con la clave actual
        int comparacion = clave.compareTo(actual.getClave());

        if (comparacion < 0) {
            // Es menor, vamos a la rama izquierda
            actual.setIzquierdo(insertarRecursivo(actual.getIzquierdo(), clave, valor));
        } else if (comparacion > 0) {
            // Es mayor, vamos a la rama derecha
            actual.setDerecho(insertarRecursivo(actual.getDerecho(), clave, valor));
        }
        // Si es igual, podríamos actualizar el valor, pero por simplicidad lo dejamos así
        return actual;
    }

    /**
     * Imprime los elementos ordenados de menor a mayor.
     * Utiliza un recorrido "In-Order" (Izquierda -> Raíz -> Derecha).
     */
    public void imprimirOrdenado() {
        System.out.println("--- Listado Ordenado por Clave ---");
        recorridoEnOrden(raiz);
        System.out.println("----------------------------------");
    }

    private void recorridoEnOrden(NodoArbol<K, V> nodo) {
        if (nodo != null) {
            recorridoEnOrden(nodo.getIzquierdo()); // Visita los menores
            System.out.println("Clave: " + nodo.getClave() + " -> " + nodo.getValor()); // Imprime el actual
            recorridoEnOrden(nodo.getDerecho());   // Visita los mayores
        }
    }
}