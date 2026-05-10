package com.proptech.servicio;

import com.proptech.modelo.Cliente;
import com.proptech.modelo.Inmueble;
import com.proptech.utilidades.estructuras.Grafo;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import com.proptech.utilidades.estructuras.Vertice;

/**
 * Servicio de Inteligencia de Negocios (BI).
 * Analiza el comportamiento de los usuarios para generar recomendaciones automáticas.
 */
public class AnalisisRelacionesService {

    // El grafo que unirá las Identificaciones de los Clientes con los Códigos de los Inmuebles
    private Grafo<String> redComercial;

    public AnalisisRelacionesService() {
        this.redComercial = new Grafo<>();
    }

    /**
     * Registra que un cliente mostró interés o visitó un inmueble.
     */
    public void registrarInteres(Cliente cliente, Inmueble inmueble) {
        String idCliente = cliente.getIdentificacion();
        String idInmueble = inmueble.getCodigo();

        // 1. Asegurarnos de que ambos existan en el mapa (nuestro Grafo ignora duplicados)
        redComercial.agregarVertice(idCliente);
        redComercial.agregarVertice(idInmueble);

        // 2. Crear la conexión entre ellos
        redComercial.agregarArista(idCliente, idInmueble);
    }

    /**
     * El algoritmo principal: Encuentra inmuebles visitados por personas con gustos similares.
     */
    public ListaEnlazada<String> obtenerRecomendaciones(String idClienteObjetivo) {
        ListaEnlazada<String> recomendaciones = new ListaEnlazada<>();
        
        // Obtenemos los inmuebles que nuestro cliente ya visitó
        ListaEnlazada<String> inmueblesVisitadosPorMi = obtenerAdyacentes(idClienteObjetivo);
        if (inmueblesVisitadosPorMi == null || inmueblesVisitadosPorMi.estaVacia()) {
            return recomendaciones; // Si no ha visitado nada, no podemos recomendar
        }

        // Paso 1: Por cada inmueble que yo visité...
        for (int i = 0; i < inmueblesVisitadosPorMi.getTamaño(); i++) {
            String idInmueble = inmueblesVisitadosPorMi.obtener(i);
            
            // Paso 2: ...buscamos a OTROS clientes que también lo visitaron
            ListaEnlazada<String> otrosClientes = obtenerAdyacentes(idInmueble);
            
            for (int j = 0; j < otrosClientes.getTamaño(); j++) {
                String otroCliente = otrosClientes.obtener(j);
                
                // Ignorarme a mí mismo
                if (!otroCliente.equals(idClienteObjetivo)) {
                    
                    // Paso 3: ...vemos qué OTROS inmuebles visitaron esos clientes
                    ListaEnlazada<String> inmueblesDeOtros = obtenerAdyacentes(otroCliente);
                    
                    for (int k = 0; k < inmueblesDeOtros.getTamaño(); k++) {
                        String posibleRecomendacion = inmueblesDeOtros.obtener(k);
                        
                        // Si yo NO he visitado ese inmueble, y NO está ya en la lista de recomendaciones, lo agrego
                        if (!contiene(inmueblesVisitadosPorMi, posibleRecomendacion) && 
                            !contiene(recomendaciones, posibleRecomendacion)) {
                            recomendaciones.agregar(posibleRecomendacion);
                        }
                    }
                }
            }
        }
        return recomendaciones;
    }

    // --- Métodos Auxiliares Internos ---

    // Busca un nodo en el grafo y devuelve sus conexiones
    private ListaEnlazada<String> obtenerAdyacentes(String idNodo) {
        // En un caso real, accederíamos a la lista de vértices del grafo.
        // Como encapsulamos bien nuestro Grafo, imprimimos y buscamos (podríamos optimizar el Grafo luego para retornar el Vértice).
        // Para mantenerlo simple, simularemos la búsqueda directa en las adyacencias.
        Grafo<String> tempGrafo = this.redComercial; 
        // Nota: Para acceder a los adyacentes, necesitamos modificar temporalmente el Grafo o usar una técnica de búsqueda.
        // Como creamos el Grafo, vamos a asumir que la lógica interna accede a los adyacentes.
        return buscarAdyacentesEnGrafo(idNodo);
    }

    // Adaptación para interactuar con la estructura de Grafo que construimos
    @SuppressWarnings("unchecked")
    private ListaEnlazada<String> buscarAdyacentesEnGrafo(String dato) {
        try {
            // Accedemos mediante reflexión o asumiendo un método público que deberíamos agregar a Grafo.java
            // Como somos los autores, lo ideal sería ir a Grafo.java y añadir: 
            // public ListaEnlazada<T> obtenerAdyacentes(T dato) { ... }
            // Pero para no hacerte cambiar de archivo, lo simulamos así:
            java.lang.reflect.Field field = Grafo.class.getDeclaredField("vertices");
            field.setAccessible(true);
            ListaEnlazada<Vertice<String>> vertices = (ListaEnlazada<Vertice<String>>) field.get(redComercial);
            
            for (int i = 0; i < vertices.getTamaño(); i++) {
                Vertice<String> v = vertices.obtener(i);
                if (v.getDato().equals(dato)) {
                    return v.getAdyacentes();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ListaEnlazada<>();
    }

    // Verifica si nuestra ListaEnlazada propia ya contiene un texto específico
    private boolean contiene(ListaEnlazada<String> lista, String valorBuscado) {
        for (int i = 0; i < lista.getTamaño(); i++) {
            if (lista.obtener(i).equals(valorBuscado)) {
                return true;
            }
        }
        return false;
    }
}