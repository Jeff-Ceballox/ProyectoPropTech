package com.proptech.servicio;

import com.proptech.modelo.Inmueble;
import com.proptech.modelo.Visita;
import com.proptech.modelo.Operacion;
import com.proptech.modelo.Cliente;
import com.proptech.modelo.Asesor;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import com.proptech.dao.InmuebleDAO;
import com.proptech.dao.VisitaDAO;
import com.proptech.dao.OperacionDAO;
import com.proptech.dao.ClienteDAO;
import com.proptech.dao.AsesorDAO;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio encargado de generar reportes y análisis avanzados de la plataforma.
 */
public class ReporteService {

    private InmuebleDAO inmuebleDAO;
    private VisitaDAO visitaDAO;
    private OperacionDAO operacionDAO;
    private ClienteDAO clienteDAO;
    private AsesorDAO asesorDAO;

    public ReporteService() {
        this.inmuebleDAO = new InmuebleDAO();
        this.visitaDAO = new VisitaDAO();
        this.operacionDAO = new OperacionDAO();
        this.clienteDAO = new ClienteDAO();
        this.asesorDAO = new AsesorDAO();
    }

    /**
     * Genera un reporte de rendimiento de inmuebles.
     */
    public Map<String, Object> generarReporteRendimientoInmuebles() {
        Map<String, Object> reporte = new HashMap<>();
        
        ListaEnlazada<Inmueble> inmuebles = inmuebleDAO.obtenerTodos();
        ListaEnlazada<Visita> visitas = visitaDAO.obtenerTodas();
        ListaEnlazada<Operacion> operaciones = operacionDAO.obtenerTodas();
        
        int totalInmuebles = inmuebles.getTamaño();
        int totalVisitas = visitas.getTamaño();
        int totalOperaciones = operaciones.getTamaño();
        
        double tasaConversion = totalInmuebles > 0 ? (double) totalOperaciones / totalInmuebles * 100 : 0;
        
        reporte.put("totalInmuebles", totalInmuebles);
        reporte.put("totalVisitas", totalVisitas);
        reporte.put("totalOperaciones", totalOperaciones);
        reporte.put("tasaConversion", String.format("%.2f%%", tasaConversion));
        
        return reporte;
    }

    /**
     * Genera un reporte de desempeño de asesores.
     */
    public ListaEnlazada<Map<String, Object>> generarReporteAsesores() {
        ListaEnlazada<Map<String, Object>> reporte = new ListaEnlazada<>();
        
        ListaEnlazada<Asesor> asesores = asesorDAO.obtenerTodos();
        ListaEnlazada<Operacion> operaciones = operacionDAO.obtenerTodas();
        
        for (int i = 0; i < asesores.getTamaño(); i++) {
            Asesor asesor = asesores.obtener(i);
            int operacionesCerradas = 0;
            double totalVentas = 0;
            
            for (int j = 0; j < operaciones.getTamaño(); j++) {
                Operacion op = operaciones.obtener(j);
                if (op.getAsesor().getIdAsesor().equals(asesor.getIdAsesor())) {
                    operacionesCerradas++;
                    totalVentas += op.getMonto();
                }
            }
            
            Map<String, Object> datosAsesor = new HashMap<>();
            datosAsesor.put("idAsesor", asesor.getIdAsesor());
            datosAsesor.put("nombre", asesor.getNombre());
            datosAsesor.put("operacionesCerradas", operacionesCerradas);
            datosAsesor.put("totalVentas", String.format("$%.2fM", totalVentas));
            datosAsesor.put("calificacion", asesor.getCalificacion());
            
            reporte.agregar(datosAsesor);
        }
        
        return reporte;
    }

    /**
     * Genera un reporte de tendencias de precios por zona.
     */
    public Map<String, Double> generarReportePreciosPorZona() {
        Map<String, Double> preciosPorZona = new HashMap<>();
        
        ListaEnlazada<Inmueble> inmuebles = inmuebleDAO.obtenerTodos();
        Map<String, Double> sumasPorZona = new HashMap<>();
        Map<String, Integer> conteoPorZona = new HashMap<>();
        
        for (int i = 0; i < inmuebles.getTamaño(); i++) {
            Inmueble inmueble = inmuebles.obtener(i);
            String zona = extraerZona(inmueble.getDireccion());
            
            sumasPorZona.put(zona, sumasPorZona.getOrDefault(zona, 0.0) + inmueble.getPrecio());
            conteoPorZona.put(zona, conteoPorZona.getOrDefault(zona, 0) + 1);
        }
        
        for (Map.Entry<String, Double> entry : sumasPorZona.entrySet()) {
            String zona = entry.getKey();
            int conteo = conteoPorZona.get(zona);
            preciosPorZona.put(zona, entry.getValue() / conteo);
        }
        
        return preciosPorZona;
    }

    /**
     * Genera un reporte de clientes activos.
     */
    public Map<String, Object> generarReporteClientesActivos() {
        Map<String, Object> reporte = new HashMap<>();
        
        ListaEnlazada<Cliente> clientes = clienteDAO.obtenerTodos();
        ListaEnlazada<Visita> visitas = visitaDAO.obtenerTodas();
        ListaEnlazada<Operacion> operaciones = operacionDAO.obtenerTodas();
        
        int clientesConVisitas = 0;
        int clientesConOperaciones = 0;
        
        for (int i = 0; i < clientes.getTamaño(); i++) {
            String idCliente = clientes.obtener(i).getIdentificacion();
            
            for (int j = 0; j < visitas.getTamaño(); j++) {
                if (visitas.obtener(j).getCliente().getIdentificacion().equals(idCliente)) {
                    clientesConVisitas++;
                    break;
                }
            }
            
            for (int j = 0; j < operaciones.getTamaño(); j++) {
                if (operaciones.obtener(j).getCliente().getIdentificacion().equals(idCliente)) {
                    clientesConOperaciones++;
                    break;
                }
            }
        }
        
        reporte.put("totalClientes", clientes.getTamaño());
        reporte.put("clientesConVisitas", clientesConVisitas);
        reporte.put("clientesConOperaciones", clientesConOperaciones);
        reporte.put("tasaActividad", String.format("%.2f%%", 
            (double) clientesConVisitas / clientes.getTamaño() * 100));
        
        return reporte;
    }

    /**
     * Genera un reporte de tipos de operación.
     */
    public Map<String, Integer> generarReporteTiposOperacion() {
        Map<String, Integer> tipos = new HashMap<>();
        
        ListaEnlazada<Operacion> operaciones = operacionDAO.obtenerTodas();
        
        for (int i = 0; i < operaciones.getTamaño(); i++) {
            String tipo = operaciones.obtener(i).getTipo();
            tipos.put(tipo, tipos.getOrDefault(tipo, 0) + 1);
        }
        
        return tipos;
    }

    /**
     * Helper para extraer zona de una dirección.
     */
    private String extraerZona(String direccion) {
        String[] palabras = direccion.split("\\s+");
        if (palabras.length > 0) {
            return palabras[0];
        }
        return direccion;
    }
}