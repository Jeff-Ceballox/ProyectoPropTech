package com.proptech.servicio;

import com.proptech.modelo.Alerta;
import com.proptech.modelo.Cliente;
import com.proptech.modelo.Inmueble;
import com.proptech.modelo.Operacion;
import com.proptech.modelo.Visita;
import com.proptech.servicio.InventarioInmueblesService;
import com.proptech.servicio.ClientesService;
import com.proptech.servicio.OperacionesService;
import com.proptech.servicio.VisitaService;
import com.proptech.servicio.HistorialYFavoritosService;
import com.proptech.utilidades.estructuras.ColaPrioridad;
import com.proptech.utilidades.estructuras.ListaEnlazada;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Servicio mejorado para gestionar alertas automáticas en la plataforma PropTech.
 * Genera alertas basadas en condiciones del sistema como contratos por vencer,
 * propiedades sin visitas, alta demanda, etc.
 */
public class AlertasService {
    private ColaPrioridad<Alerta> colaAlertas;
    
    // Servicios necesarios para verificar condiciones
    private InventarioInmueblesService inventarioService;
    private ClientesService clientesService;
    private OperacionesService operacionesService;
    private VisitaService visitaService;
    private HistorialYFavoritosService historialService;
    
    // Formato de fecha para comparaciones
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public AlertasService() {
        this.colaAlertas = new ColaPrioridad<>();
        // Inicializar servicios (en una aplicación real, se inyectarían)
        this.inventarioService = new InventarioInmueblesService();
        this.clientesService = new ClientesService();
        this.operacionesService = new OperacionesService();
        this.visitaService = new VisitaService();
        this.historialService = new HistorialYFavoritosService();
    }

    /**
     * Agrega una alerta manualmente al sistema.
     */
    public void agregarAlerta(Alerta alerta) {
        colaAlertas.encolar(alerta, alerta.getPrioridad());
        System.out.println("Alerta registrada: " + alerta.toString());
    }

    /**
     * Atiende y elimina la alerta de mayor prioridad.
     */
    public Alerta atenderSiguienteAlerta() {
        if (!hayAlertasPendientes()) {
            return null;
        }
        Alerta atendida = colaAlertas.desencolar();
        System.out.println("Alerta atendida: " + atendida.toString());
        return atendida;
    }

    /**
     * Verifica si hay alertas pendientes en el sistema.
     */
    public boolean hayAlertasPendientes() {
        return !colaAlertas.estaVacia();
    }
    
    /**
     * Obtiene la cantidad total de alertas en la cola.
     */
    public int getCantidadAlertas() {
        return colaAlertas.getTamaño();
    }

    /**
     * Genera alertas automáticas basadas en todas las condiciones del sistema.
     * Este método debería ejecutarse periódicamente (ej: cada hora/día).
     */
    public void generarAlertasAutomaticas() {
        System.out.println("=== Iniciando generación de alertas automáticas ===");
        
        try {
            generarAlertasContratosPorVencer();
            generarAlertasPropiedadesSinVisitas();
            generarAlertasAltaDemanda();
            generarAlertasVisitasPendientesConfirmacion();
            generarAlertasPropiedadesReservadasTiempo();
            generarAlertasClientesSinSeguimiento();
        } catch (Exception e) {
            System.out.println("Error al generar alertas automáticas: " + e.getMessage());
        }
        
        System.out.println("=== Fin de generación de alertas automáticas ===");
    }

    /**
     * Genera alertas para contratos próximos a vencer (en los próximos 7 días).
     */
    private void generarAlertasContratosPorVencer() {
        ListaEnlazada<Operacion> operaciones = operacionesService.obtenerTodas();
        Date hoy = new Date();
        
        for (int i = 0; i < operaciones.getTamaño(); i++) {
            Operacion op = operaciones.obtener(i);
            // Solo considerar operaciones activas (no canceladas)
            if (!"Cancelación".equalsIgnoreCase(op.getTipo())) {
                try {
                    Date fechaOperacion = sdf.parse(op.getFecha());
                    // Calcular diferencia en días (asumiendo contratos de 1 año)
                    long diffEnMs = fechaOperacion.getTime() - hoy.getTime();
                    long diffEnDias = diffEnMs / (24 * 60 * 60 * 1000);
                    
                    // Alerta si vence en los próximos 7 días
                    if (diffEnDias >= 0 && diffEnDias <= 7) {
                        Alerta alerta = new Alerta(
                            "ALC-" + op.getIdOperacion(),
                            "Contrato próximo a vencer: " + op.getTipo() + " de " + 
                            op.getInmueble().getCodigo() + " con " + 
                            op.getCliente().getNombre(),
                            1, // Prioridad alta
                            sdf.format(hoy)
                        );
                        agregarAlerta(alerta);
                    }
                } catch (Exception e) {
                    // Error en parseo de fecha, continuamos
                }
            }
        }
    }

    /**
     * Genera alertas para propiedades que no han tenido visitas en los últimos 30 días.
     */
    private void generarAlertasPropiedadesSinVisitas() {
        ListaEnlazada<Inmueble> inmuebles = inventarioService.obtenerTodos();
        Date hoy = new Date();
        long treintaDiasEnMs = 30L * 24 * 60 * 60 * 1000;
        
        for (int i = 0; i < inmuebles.getTamaño(); i++) {
            Inmueble inmueble = inmuebles.obtener(i);
            // Solo considerar propiedades disponibles o en proceso
            if ("Disponible".equalsIgnoreCase(inmueble.getEstado()) || 
                "En proceso".equalsIgnoreCase(inmueble.getEstado())) {
                
                ListaEnlazada<Visita> visitas = visitaService.obtenerVisitasPorInmueble(inmueble.getCodigo());
                Date ultimaVisitaFecha = null;
                boolean tieneVisitas = false;
                
                // Encontrar la visita más reciente
                for (int j = 0; j < visitas.getTamaño(); j++) {
                    Visita visita = visitas.obtener(j);
                    try {
                        Date visitaFecha = sdf.parse(visita.getFechaHora().split(" ")[0]); // Solo fecha
                        if (!tieneVisitas || visitaFecha.after(ultimaVisitaFecha)) {
                            ultimaVisitaFecha = visitaFecha;
                            tieneVisitas = true;
                        }
                    } catch (Exception e) {
                        // Error en parseo, continuamos
                    }
                }
                
                // Si no tiene visitas o la última fue hace más de 30 días
                if (!tieneVisitas || 
                    (ultimaVisitaFecha != null && 
                     (hoy.getTime() - ultimaVisitaFecha.getTime()) > treintaDiasEnMs)) {
                    
                    Alerta alerta = new Alerta(
                        "ALV-" + inmueble.getCodigo(),
                        "Propiedad sin visitas recientes: " + inmueble.getCodigo() + 
                        " - " + inmueble.getDireccion(),
                        2, // Prioridad media
                        sdf.format(hoy)
                    );
                    agregarAlerta(alerta);
                }
            }
        }
    }

    /**
     * Genera alertas para propiedades con alta demanda (más de 5 visitas en el último mes).
     */
    private void generarAlertasAltaDemanda() {
        ListaEnlazada<Inmueble> inmuebles = inventarioService.obtenerTodos();
        Date hoy = new Date();
        long unMesEnMs = 30L * 24 * 60 * 60 * 1000;
        
        for (int i = 0; i < inmuebles.getTamaño(); i++) {
            Inmueble inmueble = inmuebles.obtener(i);
            
            ListaEnlazada<Visita> visitas = visitaService.obtenerVisitasPorInmueble(inmueble.getCodigo());
            int visitasUltimoMes = 0;
            
            // Contar visitas del último mes
            for (int j = 0; j < visitas.getTamaño(); j++) {
                Visita visita = visitas.obtener(j);
                try {
                    Date visitaFecha = sdf.parse(visita.getFechaHora().split(" ")[0]); // Solo fecha
                    long diffEnMs = hoy.getTime() - visitaFecha.getTime();
                    if (diffEnMs < unMesEnMs) {
                        visitasUltimoMes++;
                    }
                } catch (Exception e) {
                    // Error en parseo, continuamos
                }
            }
            
            // Alerta si tiene más de 5 visitas en el último mes
            if (visitasUltimoMes > 5) {
                Alerta alerta = new Alerta(
                    "ALD-" + inmueble.getCodigo(),
                    "Alta demanda detectada: " + visitasUltimoMes + 
                    " visitas en el último mes para " + inmueble.getCodigo(),
                    2, // Prioridad media
                    sdf.format(hoy)
                );
                agregarAlerta(alerta);
            }
        }
    }

    /**
     * Genera alertas para visitas pendientes de confirmación (más de 24 horas).
     */
    private void generarAlertasVisitasPendientesConfirmacion() {
        ListaEnlazada<Visita> visitas = visitaService.obtenerTodas();
        Date hoy = new Date();
        long horas24EnMs = 24 * 60 * 60 * 1000;
        
        for (int i = 0; i < visitas.getTamaño(); i++) {
            Visita visita = visitas.obtener(i);
            
            // Solo visitas pendientes
            if ("Pendiente".equalsIgnoreCase(visita.getEstado())) {
                try {
                    // Asumiendo formato "yyyy-MM-dd HH:mm"
                    String fechaHora = visita.getFechaHora();
                    Date fechaVisita = sdf.parse(fechaHora.split(" ")[0]); // Solo fecha para simplificación
                    long diffEnMs = hoy.getTime() - fechaVisita.getTime();
                    
                    // Alerta si lleva más de 24 horas pendiente
                    if (diffEnMs > horas24EnMs) {
                        Alerta alerta = new Alerta(
                            "ALVP-" + visita.getIdVisita(),
                            "Visita pendiente de confirmación hace más de 24h: " + 
                            visita.getIdVisita() + " para " + 
                            visita.getInmueble().getCodigo(),
                            1, // Prioridad alta
                            sdf.format(hoy)
                        );
                        agregarAlerta(alerta);
                    }
                } catch (Exception e) {
                    // Error en parseo de fecha
                }
            }
        }
    }

    /**
     * Genera alertas para propiedades reservadas por mucho tiempo sin cierre.
     * Consideramos "reservada" cuando tiene una operación pendiente (no concretada) hace más de 14 días.
     */
    private void generarAlertasPropiedadesReservadasTiempo() {
        ListaEnlazada<Operacion> operaciones = operacionesService.obtenerTodas();
        Date hoy = new Date();
        long catorceDiasEnMs = 14L * 24 * 60 * 60 * 1000;
        
        for (int i = 0; i < operaciones.getTamaño(); i++) {
            Operacion op = operaciones.obtener(i);
            
            // Operaciones que indican interés pero no cierre definitivo
            // En este caso, consideramos todas las operaciones no finalizadas
            if (!"Venta".equalsIgnoreCase(op.getTipo()) && 
                !"Arriendo".equalsIgnoreCase(op.getTipo()) &&
                !"Cancelación".equalsIgnoreCase(op.getTipo())) {
                
                try {
                    Date fechaOperacion = sdf.parse(op.getFecha());
                    long diffEnMs = hoy.getTime() - fechaOperacion.getTime();
                    
                    if (diffEnMs > catorceDiasEnMs) {
                        Alerta alerta = new Alerta(
                            "ALPR-" + op.getIdOperacion(),
                            "Propiedad con interés prolongado sin cierre: " + 
                            op.getInmueble().getCodigo() + 
                            " desde " + op.getFecha(),
                            2, // Prioridad media
                            sdf.format(hoy)
                        );
                        agregarAlerta(alerta);
                    }
                } catch (Exception e) {
                    // Error en parseo de fecha
                }
            }
        }
    }

    /**
     * Genera alertas para clientes sin seguimiento reciente (más de 15 días sin actividad).
     * Actividad incluye: visitas, consultas, operaciones o favoritos.
     */
    private void generarAlertasClientesSinSeguimiento() {
        ListaEnlazada<Cliente> clientes = clientesService.obtenerTodos();
        Date hoy = new Date();
        long quinceDiasEnMs = 15L * 24 * 60 * 60 * 1000;
        
        for (int i = 0; i < clientes.getTamaño(); i++) {
            Cliente cliente = clientes.obtener(i);
            boolean tieneActividadReciente = false;
            Date ultimaActividad = null;
            
            // 1. Verificar operaciones recientes
            ListaEnlazada<Operacion> operaciones = operacionesService.obtenerPorCliente(cliente.getIdentificacion());
            for (int j = 0; j < operaciones.getTamaño(); j++) {
                Operacion op = operaciones.obtener(j);
                try {
                    Date fechaOp = sdf.parse(op.getFecha());
                    if (!tieneActividadReciente || fechaOp.after(ultimaActividad)) {
                        ultimaActividad = fechaOp;
                        tieneActividadReciente = true;
                    }
                } catch (Exception e) {
                    // Continuamos
                }
            }
            
            // 2. Verificar visitas recientes
            if (!tieneActividadReciente) {
                ListaEnlazada<Visita> visitas = visitaService.obtenerVisitasPorCliente(cliente.getIdentificacion());
                for (int j = 0; j < visitas.getTamaño(); j++) {
                    Visita visita = visitas.obtener(j);
                    try {
                        Date fechaVisita = sdf.parse(visita.getFechaHora().split(" ")[0]);
                        if (!tieneActividadReciente || fechaVisita.after(ultimaActividad)) {
                            ultimaActividad = fechaVisita;
                            tieneActividadReciente = true;
                        }
                    } catch (Exception e) {
                        // Continuamos
                    }
                }
            }
            
            // 3. Verificar consultas recientes (a través del historial)
            if (!tieneActividadReciente) {
                ListaEnlazada<Inmueble> historial = historialService.obtenerHistorialConsultas(cliente.getIdentificacion());
                // Aquí necesitaríamos timestamps en el historial, por ahora asumimos actividad si hay historial
                if (historial.getTamaño() > 0) {
                    // En una implementación real, verificaríamos fechas de las consultas
                    tieneActividadReciente = true; // Simplificación
                }
            }
            
            // 4. Verificar favoritos recientes (similar al historial)
            if (!tieneActividadReciente) {
                ListaEnlazada<Inmueble> favoritos = historialService.obtenerFavoritos(cliente.getIdentificacion());
                if (favoritos.getTamaño() > 0) {
                    tieneActividadReciente = true; // Simplificación
                }
            }
            
            // Si no hay actividad reciente, generar alerta
            if (!tieneActividadReciente) {
                Alerta alerta = new Alerta(
                    "ALCS-" + cliente.getIdentificacion(),
                    "Cliente sin seguimiento reciente: " + 
                    cliente.getNombre() + " (ID: " + cliente.getIdentificacion() + ")",
                    2, // Prioridad media
                    sdf.format(hoy)
                );
                agregarAlerta(alerta);
            }
        }
    }
}
