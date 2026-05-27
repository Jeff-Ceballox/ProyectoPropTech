package com.proptech.utilidades.estructuras;

/**
 * Implementación propia de una Tabla Hash genérica (Reemplaza a HashMap de Java).
 * Búsqueda, inserción y eliminación casi instantáneas.
 */
public class TablaHash<K, V> {
    
    // Un arreglo estático que servirá como nuestras "cajas" o "buckets"
    private EntradaHash<K, V>[] tabla;
    private int capacidad; // Cantidad de cajas disponibles
    private int tamaño;    // Cantidad de elementos reales guardados

    @SuppressWarnings("unchecked")
    public TablaHash(int capacidadInicial) {
        this.capacidad = capacidadInicial;
        // En Java, crear arreglos de genéricos requiere este casteo
        this.tabla = new EntradaHash[capacidad]; 
        this.tamaño = 0;
    }

    /**
     * Función Hash: Convierte la clave en un número (índice) válido dentro de nuestra tabla.
     */
    private int calcularHash(K clave) {
        // Math.abs asegura que no haya números negativos
        // % capacidad asegura que el número no sea mayor que nuestras cajas
        return Math.abs(clave.hashCode()) % capacidad;
    }

    /**
     * Inserta un nuevo par Clave-Valor en la tabla.
     */
    public void insertar(K clave, V valor) {
        int indice = calcularHash(clave);
        EntradaHash<K, V> nuevaEntrada = new EntradaHash<>(clave, valor);

        if (tabla[indice] == null) {
            // La caja estaba vacía, insertamos directamente
            tabla[indice] = nuevaEntrada;
            tamaño++;
        } else {
            // COLISIÓN: Ya hay algo en la caja. Usamos una lista enlazada interna.
            EntradaHash<K, V> actual = tabla[indice];
            while (actual != null) {
                // Si la clave ya existe, solo actualizamos el valor
                if (actual.getClave().equals(clave)) {
                    actual.setValor(valor);
                    return;
                }
                if (actual.getSiguiente() == null) {
                    break;
                }
                actual = actual.getSiguiente();
            }
            // Añadimos al final de la lista en esa caja
            actual.setSiguiente(nuevaEntrada);
            tamaño++;
        }
    }

    /**
     * Busca un valor usando su clave al instante.
     */
    public V obtener(K clave) {
        int indice = calcularHash(clave);
        EntradaHash<K, V> actual = tabla[indice];

        // Revisamos la caja (y la lista si hubo colisión)
        while (actual != null) {
            if (actual.getClave().equals(clave)) {
                return actual.getValor(); // ¡Encontrado!
            }
            actual = actual.getSiguiente();
        }
        return null; // No existe esa clave
    }

    /**
     * Recorre TODOS los buckets de la tabla y devuelve una lista con todos los valores almacenados.
     * Útil para listar todos los elementos (ej: GET /api/clientes).
     */
    public ListaEnlazada<V> valores() {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        for (int i = 0; i < capacidad; i++) {
            EntradaHash<K, V> actual = tabla[i];
            while (actual != null) {
                resultado.agregar(actual.getValor());
                actual = actual.getSiguiente();
            }
        }
        return resultado;
    }

    /**
     * Elimina una entrada por su clave.
     * @return true si se eliminó, false si no existía.
     */
    public boolean eliminar(K clave) {
        int indice = calcularHash(clave);
        EntradaHash<K, V> actual = tabla[indice];
        EntradaHash<K, V> anterior = null;

        while (actual != null) {
            if (actual.getClave().equals(clave)) {
                if (anterior == null) {
                    tabla[indice] = actual.getSiguiente();
                } else {
                    anterior.setSiguiente(actual.getSiguiente());
                }
                tamaño--;
                return true;
            }
            anterior = actual;
            actual = actual.getSiguiente();
        }
        return false;
    }

    public int getTamaño() { return tamaño; }
}