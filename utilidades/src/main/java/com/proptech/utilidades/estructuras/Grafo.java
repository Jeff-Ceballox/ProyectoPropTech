package com.proptech.utilidades.estructuras;

/**
 * Estructura de Grafo basada en Listas de Adyacencia.
 * Ideal para analizar relaciones complejas en la inmobiliaria.
 */
public class Grafo<T> {
    
    // Una lista que contiene todos los vértices del sistema
    private ListaEnlazada<Vertice<T>> vertices;

    public Grafo() {
        this.vertices = new ListaEnlazada<>();
    }

    /**
     * Agrega un nuevo nodo independiente al grafo.
     */
    public void agregarVertice(T dato) {
        // Evitamos duplicados
        if (buscarVertice(dato) == null) {
            vertices.agregar(new Vertice<>(dato));
        }
    }

    /**
     * Crea una conexión (arista) entre dos vértices existentes.
     */
    public void agregarArista(T origen, T destino) {
        Vertice<T> vOrigen = buscarVertice(origen);
        Vertice<T> vDestino = buscarVertice(destino);

        if (vOrigen != null && vDestino != null) {
            // Como es una relación bidireccional, los conectamos mutuamente
            vOrigen.getAdyacentes().agregar(destino);
            vDestino.getAdyacentes().agregar(origen);
        } else {
            System.out.println("Error: Uno de los vértices no existe en el grafo.");
        }
    }

    // Método auxiliar para encontrar un vértice dentro de nuestra lista
    private Vertice<T> buscarVertice(T dato) {
        for (int i = 0; i < vertices.getTamaño(); i++) {
            Vertice<T> actual = vertices.obtener(i);
            if (actual.getDato().equals(dato)) {
                return actual;
            }
        }
        return null;
    }

    /**
     * Imprime el mapa completo de relaciones.
     */
    public void imprimirRelaciones() {
        System.out.println("--- Mapa de Relaciones (Grafo) ---");
        for (int i = 0; i < vertices.getTamaño(); i++) {
            Vertice<T> v = vertices.obtener(i);
            System.out.print("[" + v.getDato() + "] está relacionado con: ");
            
            ListaEnlazada<T> adyacentes = v.getAdyacentes();
            if (adyacentes.estaVacia()) {
                System.out.print("Nadie aún.");
            } else {
                for (int j = 0; j < adyacentes.getTamaño(); j++) {
                    System.out.print(adyacentes.obtener(j) + (j < adyacentes.getTamaño() - 1 ? ", " : ""));
                }
            }
            System.out.println();
        }
        System.out.println("----------------------------------");
    }
}