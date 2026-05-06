package com.proptech;

import com.proptech.utilidades.estructuras.ListaEnlazada;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Iniciando Pruebas de Lista Enlazada Propia ===");
        
        // Creamos una lista específicamente para textos (simulando un historial)
        ListaEnlazada<String> historialFavoritos = new ListaEnlazada<>();
        
        // 1. Agregamos datos
        historialFavoritos.agregar("Apartamento Norte (Cod: A001)");
        historialFavoritos.agregar("Casa Centro (Cod: C045)");
        historialFavoritos.agregar("Local Comercial Sur (Cod: L012)");
        
        // 2. Verificamos el tamaño
        System.out.println("Total de inmuebles favoritos: " + historialFavoritos.getTamaño());
        
        // 3. Recorremos e imprimimos nuestra estructura
        System.out.println("\nListado de favoritos:");
        for (int i = 0; i < historialFavoritos.getTamaño(); i++) {
            System.out.println((i + 1) + ". " + historialFavoritos.obtener(i));
        }
    }
}