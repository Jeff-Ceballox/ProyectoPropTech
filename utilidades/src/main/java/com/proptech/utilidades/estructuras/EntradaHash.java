package com.proptech.utilidades.estructuras;

/**
 * Eslabón especial para la Tabla Hash.
 * Almacena un par de Clave (K) y Valor (V).
 */
public class EntradaHash<K, V> {
    private K clave;      // Ej: "CC123456" o "Apto-001"
    private V valor;      // Ej: El objeto Cliente o el texto con sus datos
    private EntradaHash<K, V> siguiente; // Para manejar colisiones (Encadenamiento)

    public EntradaHash(K clave, V valor) {
        this.clave = clave;
        this.valor = valor;
        this.siguiente = null;
    }

    public K getClave() { return clave; }
    public V getValor() { return valor; }
    public void setValor(V valor) { this.valor = valor; }
    
    public EntradaHash<K, V> getSiguiente() { return siguiente; }
    public void setSiguiente(EntradaHash<K, V> siguiente) { this.siguiente = siguiente; }
}