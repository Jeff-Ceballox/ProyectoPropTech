package com.proptech.servicio;

import com.proptech.modelo.Cliente;
import com.proptech.modelo.Inmueble;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import com.proptech.servicio.InventarioInmueblesService;
import com.proptech.servicio.ClientesService;
import com.proptech.servicio.HistorialYFavoritosService;

/**
 * Servicio encargado de generar recomendaciones de inmuebles a los clientes.
 * Utiliza múltiples criterios como presupuesto, zona, tipo de inmueble, 
 * historial de consultas y propiedades similares visitadas.
 */
public class RecomendacionService {

    private InventarioInmueblesService inventarioService;
    private ClientesService clientesService;
    private HistorialYFavoritosService historialService;

    public RecomendacionService() {
        this.inventarioService = new InventarioInmueblesService();
        this.clientesService = new ClientesService();
        this.historialService = new HistorialYFavoritosService();
    }

    /**
     * Genera recomendaciones de inmuebles para un cliente específico.
     * 
     * @param identificacionCliente Identificación del cliente
     * @return Lista de inmuebles recomendados ordenados por relevancia
     */
    public ListaEnlazada<Inmueble> generarRecomendaciones(String identificacionCliente) {
        // Obtener el cliente
        Cliente cliente = clientesService.buscarPorIdentificacion(identificacionCliente);
        if (cliente == null) {
            System.out.println("Cliente no encontrado: " + identificacionCliente);
            return new ListaEnlazada<>();
        }

        // Obtener todos los inmuebles disponibles
        ListaEnlazada<Inmueble> todosInmuebles = inventarioService.obtenerTodos();
        
        // Filtrar por disponibilidad básica
        ListaEnlazada<Inmueble> inmueblesDisponibles = filtrarPorDisponibilidad(todosInmuebles);
        
        // Aplicar criterios de recomendación y puntuar cada inmueble
        ListaEnlazada<InmuebleConPuntuacion> inmueblesPuntuados = new ListaEnlazada<>();
        
        for (int i = 0; i < inmueblesDisponibles.getTamaño(); i++) {
            Inmueble inmueble = inmueblesDisponibles.obtener(i);
            int puntuacion = calcularPuntuacionRecomendacion(cliente, inmueble);
            inmueblesPuntuados.agregar(new InmuebleConPuntuacion(inmueble, puntuacion));
        }
        
        // Ordenar por puntuación descendente (mayor relevancia primero)
        ListaEnlazada<Inmueble> recomendaciones = ordenarPorPuntuacion(inmueblesPuntuados);
        
        // Limitar a las top 10 recomendaciones
        return limitarResultados(recomendaciones, 10);
    }

    /**
     * Filtra inmuebles por disponibilidad básica (no vendidos ni arrendados definitivamente).
     */
    private ListaEnlazada<Inmueble> filtrarPorDisponibilidad(ListaEnlazada<Inmueble> inmuebles) {
        ListaEnlazada<Inmueble> resultado = new ListaEnlazada<>();
        for (int i = 0; i < inmuebles.getTamaño(); i++) {
            Inmueble inmueble = inmuebles.obtener(i);
            String estado = inmueble.getEstado().toLowerCase();
            // Consideramos disponibles aquellos que no están definitivamente vendidos/arrendados
            if (!estado.equals("vendido") && !estado.equals("arrendado")) {
                resultado.agregar(inmueble);
            }
        }
        return resultado;
    }

    /**
     * Calcula una puntuación de recomendación para un inmueble basado en las preferencias del cliente.
     * Mayor puntuación = mayor relevancia.
     */
    private int calcularPuntuacionRecomendacion(Cliente cliente, Inmueble inmueble) {
        int puntuacion = 0;
        
        // 1. Presupuesto (30 puntos máximo)
        if (cliente.getPresupuestoMaximo() >= inmueble.getPrecio()) {
            // Cuanto más ajuste al presupuesto, mejor puntuación
            double ratio = inmueble.getPrecio() / cliente.getPresupuestoMaximo();
            if (ratio <= 0.5) {
                puntuacion += 30; // Excelente ajuste (menos de la mitad del presupuesto)
            } else if (ratio <= 0.8) {
                puntuacion += 25; // Buen ajuste
            } else if (ratio <= 1.0) {
                puntuacion += 20; // Ajuste justo
            } else {
                puntuacion += 0; // Sobre presupuesto (ya filtrado anteriormente, pero por si acaso)
            }
        }
        
        // 2. Tipo de inmueble deseado (25 puntos máximo)
        if (cliente.getTipoInmuebleDeseado() != null && 
            !cliente.getTipoInmuebleDeseado().isEmpty() &&
            cliente.getTipoInmuebleDeseado().equalsIgnoreCase(inmueble.getTipo())) {
            puntuacion += 25;
        }
        
        // 3. Zona de interés (20 puntos máximo)
        if (cliente.getZonasInteres() != null && 
            !cliente.getZonasInteres().isEmpty()) {
            // Asumiendo que zonasInteres es una lista o string separado por comas
            // Por simplicidad, verificamos si la dirección contiene alguna zona de interés
            String direccionLower = inmueble.getDireccion().toLowerCase();
            String[] zonas = cliente.getZonasInteres().split(",");
            boolean zonaMatch = false;
            for (String zona : zonas) {
                if (direccionLower.contains(zona.trim().toLowerCase())) {
                    zonaMatch = true;
                    break;
                }
            }
            if (zonaMatch) {
                puntuacion += 20;
            }
        }
        
        // 4. Número mínimo de habitaciones (15 puntos máximo)
        if (inmueble.getHabitaciones() >= cliente.getCantMinHabitaciones()) {
            // Puntos basados en cuánto supera el mínimo
            int exceso = inmueble.getHabitaciones() - cliente.getCantMinHabitaciones();
            puntuacion += Math.min(15, 10 + (exceso * 5)); // 10 puntos base + 5 por cada extra, máximo 15
        }
        
        // 5. Historial de consultas (10 puntos máximo)
        ListaEnlazada<Inmueble> historial = historialService.obtenerHistorialConsultas(cliente.getIdentificacion());
        for (int j = 0; j < historial.getTamaño(); j++) {
            Inmueble consultado = historial.obtener(j);
            if (sonInmueblesSimilares(inmueble, consultado)) {
                puntuacion += 10;
                break; // Solo dar puntos una vez por similitud en historial
            }
        }
        
        // 6. Favoritos (bonus adicional si es similar a un favorito)
        ListaEnlazada<Inmueble> favoritos = historialService.obtenerFavoritos(cliente.getIdentificacion());
        for (int j = 0; j < favoritos.getTamaño(); j++) {
            Inmueble favorito = favoritos.obtener(j);
            if (sonInmueblesSimilares(inmueble, favorito)) {
                puntuacion += 5; // Bonus adicional por similitud con favoritos
                break;
            }
        }
        
        return puntuacion;
    }

    /**
     * Determina si dos inmuebles son similares basado en características clave.
     */
    private boolean sonInmueblesSimilares(Inmueble i1, Inmueble i2) {
        // Similares si comparten tipo y rango de precio similar
        boolean mismoTipo = i1.getTipo().equalsIgnoreCase(i2.getTipo());
        
        double precio1 = i1.getPrecio();
        double precio2 = i2.getPrecio();
        double diferenciaPrecio = Math.abs(precio1 - precio2);
        double precioPromedio = (precio1 + precio2) / 2;
        boolean precioSimilar = (precioPromedio > 0) && (diferenciaPrecio / precioPromedio < 0.3); // Dentro del 30%
        
        boolean habitacionesSimilares = Math.abs(i1.getHabitaciones() - i2.getHabitaciones()) <= 1;
        boolean banosSimilares = Math.abs(i1.getBanos() - i2.getBanos()) <= 1;
        
        return mismoTipo && precioSimilar && habitacionesSimilares && banosSimilares;
    }

    /**
     * Ordena una lista de inmuebles con puntuación por puntuación descendente.
     */
    private ListaEnlazada<Inmueble> ordenarPorPuntuacion(ListaEnlazada<InmuebleConPuntuacion> inmueblesPuntuados) {
        // Implementación simple de ordenamiento por inserción para listas pequeñas
        ListaEnlazada<InmuebleConPuntuacion> ordenada = new ListaEnlazada<>();
        
        for (int i = 0; i < inmueblesPuntuados.getTamaño(); i++) {
            InmuebleConPuntuacion actual = inmueblesPuntuados.obtener(i);
            boolean insertado = false;
            
            for (int j = 0; j < ordenada.getTamaño(); j++) {
                if (actual.puntuacion > ordenada.obtener(j).puntuacion) {
                    // Insertar en posición j
                    ListaEnlazada<InmuebleConPuntuacion> temp = new ListaEnlazada<>();
                    for (int k = 0; k < j; k++) {
                        temp.agregar(ordenada.obtener(k));
                    }
                    temp.agregar(actual);
                    for (int k = j; k < ordenada.getTamaño(); k++) {
                        temp.agregar(ordenada.obtener(k));
                    }
                    ordenada = temp;
                    insertado = true;
                    break;
                }
            }
            
            if (!insertado) {
                ordenada.agregar(actual);
            }
        }
        
        // Extraer solo los inmuebles
        ListaEnlazada<Inmueble> resultado = new ListaEnlazada<>();
        for (int i = 0; i < ordenada.getTamaño(); i++) {
            resultado.agregar(ordenada.obtener(i).inmueble);
        }
        return resultado;
    }

    /**
     * Limita los resultados a un número máximo de recomendaciones.
     */
    private ListaEnlazada<Inmueble> limitarResultados(ListaEnlazada<Inmueble> lista, int max) {
        ListaEnlazada<Inmueble> resultado = new ListaEnlazada<>();
        int limite = Math.min(lista.getTamaño(), max);
        for (int i = 0; i < limite; i++) {
            resultado.agregar(lista.obtener(i));
        }
        return resultado;
    }

    /**
     * Clase interna para asociar un inmueble con su puntuación de recomendación.
     */
    private static class InmuebleConPuntuacion {
        Inmueble inmueble;
        int puntuacion;
        
        InmuebleConPuntuacion(Inmueble inmueble, int puntuacion) {
            this.inmueble = inmueble;
            this.puntuacion = puntuacion;
        }
    }
}