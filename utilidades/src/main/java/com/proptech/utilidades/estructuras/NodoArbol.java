package com.proptech.utilidades.estructuras;

/**
 * Nodo para un Árbol Binario de Búsqueda.
 * @param <K> La clave de ordenamiento (Ej: Double para el precio). Debe ser "Comparable".
 * @param <V> El valor a guardar (Ej: El objeto Inmueble).
 */
public class NodoArbol<K extends Comparable<K>, V> {
    private K clave;      // Lo que usamos para ordenar (Ej: Precio 150000.0)
    private V valor;      // Los datos reales (Ej: "Apto Norte")
    private NodoArbol<K, V> izquierdo;
    private NodoArbol<K, V> derecho;

    public NodoArbol(K clave, V valor) {
        this.clave = clave;
        this.valor = valor;
        this.izquierdo = null;
        this.derecho = null;
    }

    // Getters y Setters
    public K getClave() { return clave; }
    public V getValor() { return valor; }
    public NodoArbol<K, V> getIzquierdo() { return izquierdo; }
    public void setIzquierdo(NodoArbol<K, V> izquierdo) { this.izquierdo = izquierdo; }
    public NodoArbol<K, V> getDerecho() { return derecho; }
    public void setDerecho(NodoArbol<K, V> derecho) { this.derecho = derecho; }
}