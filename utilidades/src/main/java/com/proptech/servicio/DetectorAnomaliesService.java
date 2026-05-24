package com.proptech.servicio;

import com.proptech.modelo.Cliente;
import com.proptech.modelo.Inmueble;
import com.proptech.modelo.Visita;
import com.proptech.modelo.Asesor;
import com.proptech.modelo.Operacion;
import com.proptech.utilidades.estructuras.ListaEnlazada;
import com.proptech.dao.VisitaDAO;
import com.proptech.dao.ClienteDAO;
import com.proptech.dao.InmuebleDAO;
import com.proptech.dao.AsesorDAO;
import com.proptech.dao.OperacionDAO;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio encargado de detectar comportamientos inusuales en la plataforma.
 * Identifica patrones anómalos que podrían requerir atención especial.
 */
public class DetectorAnomaliesService {

    private VisitaDAO visitaDAO;
    private ClienteDAO clienteDAO;
    private InmuebleDAO inmuebleDAO;
    private AsesorDAO asesorDAO;
    private OperacionDAO operacionDAO;

    private static final int UMBRAL_VISITAS_SIN_CIERRE = 3;
    private static final int UMBRAL_VISITAS_asesor_DIA = 5;
    private static final double UMBRAL_CAMBIO_PRECIO = 0.15;
    private static final int UMBRAL_CONCENTRACION_ZONA = 5;

    public DetectorAnomaliesService() {
        this.visitaDAO = new VisitaDAO();
        this.clienteDAO = new ClienteDAO();
        this.inmuebleDAO = new InmuebleDAO();
        this.asesorDAO = new AsesorDAO();
        this.operacionDAO = new OperacionDAO();
    }

    public ListaEnlazada<Map<String, Object>> detectarAnomalias() {
        ListaEnlazada<Map<String, Object>> anomalias = new ListaEnlazada<>();
        anomalias = combinarListas(anomalias, detectarVisitasSinCierre());
        anomalias = combinarListas(anomalias, detectarSobrecargaAsesores());
        anomalias = combinarListas(anomalias, detectarCambiosPrecioFrecuentes());
        anomalias = combinarListas(anomalias, detectarConcentracionGeografica());
        return anomalias;
    }

    public ListaEnlazada<Map<String, Object>> detectarVisitasSinCierre() {
        ListaEnlazada<Map<String, Object>> anomalias = new ListaEnlazada<>();
        ListaEnlazada<Cliente> clientes = clienteDAO.obtenerTodos();

        for (int i = 0; i < clientes.getTamaño(); i++) {
            Cliente cliente = clientes.obtener(i);
            ListaEnlazada<Visita> visitasCliente = visitaDAO.obtenerVisitasPorCliente(cliente.getIdentificacion());
            
            int visitasRealizadas = 0;
            for (int j = 0; j < visitasCliente.getTamaño(); j++) {
                if (visitasCliente.obtener(j).getEstado().equals("Realizada")) {
                    visitasRealizadas++;
                }
            }

            ListaEnlazada<Operacion> operaciones = operacionDAO.obtenerOperacionesPorCliente(cliente.getIdentificacion());
            
            if (visitasRealizadas >= UMBRAL_VISITAS_SIN_CIERRE && operaciones.getTamaño() == 0) {
                Map<String, Object> anomalia = new HashMap<>();
                anomalia.put("tipo", "VISITAS_SIN_CIERRE");
                anomalia.put("cliente", cliente.getNombre());
                anomalia.put("idCliente", cliente.getIdentificacion());
                anomalia.put("visitas", visitasRealizadas);
                anomalia.put("mensaje", String.format(
                    "Cliente %s (%s) ha realizado %d visitas sin cerrar operación",
                    cliente.getNombre(), cliente.getIdentificacion(), visitasRealizadas
                ));
                anomalias.agregar(anomalia);
            }
        }
        return anomalias;
    }

    public ListaEnlazada<Map<String, Object>> detectarSobrecargaAsesores() {
        ListaEnlazada<Map<String, Object>> anomalias = new ListaEnlazada<>();
        ListaEnlazada<Asesor> asesores = asesorDAO.obtenerTodos();

        for (int i = 0; i < asesores.getTamaño(); i++) {
            Asesor asesor = asesores.obtener(i);
            ListaEnlazada<Visita> visitasAsesor = visitaDAO.obtenerVisitasPorAsesor(asesor.getIdAsesor());
            
            if (visitasAsesor.getTamaño() > UMBRAL_VISITAS_asesor_DIA) {
                Map<String, Object> anomalia = new HashMap<>();
                anomalia.put("tipo", "SOBRECARGA_ASESOR");
                anomalia.put("asesor", asesor.getNombre());
                anomalia.put("idAsesor", asesor.getIdAsesor());
                anomalia.put("visitas", visitasAsesor.getTamaño());
                anomalia.put("mensaje", String.format(
                    "Asesor %s (%s) tiene %d visitas asignadas, posible sobrecarga",
                    asesor.getNombre(), asesor.getIdAsesor(), visitasAsesor.getTamaño()
                ));
                anomalias.agregar(anomalia);
            }
        }
        return anomalias;
    }

    public ListaEnlazada<Map<String, Object>> detectarCambiosPrecioFrecuentes() {
        ListaEnlazada<Map<String, Object>> anomalias = new ListaEnlazada<>();
        ListaEnlazada<Inmueble> inmuebles = inmuebleDAO.obtenerTodos();

        for (int i = 0; i < inmuebles.getTamaño(); i++) {
            Inmueble inmueble = inmuebles.obtener(i);
            if (inmueble.getDescripcion() != null && inmueble.getDescripcion().toLowerCase().contains("descuento")) {
                Map<String, Object> anomalia = new HashMap<>();
                anomalia.put("tipo", "CAMBIO_PRECIO");
                anomalia.put("inmueble", inmueble.getCodigo());
                anomalia.put("tipoInmueble", inmueble.getTipo());
                anomalia.put("mensaje", String.format(
                    "Inmueble %s (%s) tiene posible descuento aplicado",
                    inmueble.getCodigo(), inmueble.getTipo()
                ));
                anomalias.agregar(anomalia);
            }
        }
        return anomalias;
    }

    public ListaEnlazada<Map<String, Object>> detectarConcentracionGeografica() {
        ListaEnlazada<Map<String, Object>> anomalias = new ListaEnlazada<>();
        ListaEnlazada<Inmueble> inmuebles = inmuebleDAO.obtenerTodos();

        java.util.HashMap<String, Integer> contadorZonas = new java.util.HashMap<>();
        
        for (int i = 0; i < inmuebles.getTamaño(); i++) {
            Inmueble inmueble = inmuebles.obtener(i);
            String zona = inmueble.getDireccion().toLowerCase();
            String zonaSimple = extraerZona(zona);
            contadorZonas.put(zonaSimple, contadorZonas.getOrDefault(zonaSimple, 0) + 1);
        }

        for (java.util.Map.Entry<String, Integer> entry : contadorZonas.entrySet()) {
            if (entry.getValue() >= UMBRAL_CONCENTRACION_ZONA) {
                Map<String, Object> anomalia = new HashMap<>();
                anomalia.put("tipo", "CONCENTRACION_GEOGRAFICA");
                anomalia.put("zona", entry.getKey());
                anomalia.put("cantidad", entry.getValue());
                anomalia.put("mensaje", String.format(
                    "Alta concentración de inmuebles en zona '%s': %d propiedades",
                    entry.getKey(), entry.getValue()
                ));
                anomalias.agregar(anomalia);
            }
        }
        return anomalias;
    }

    private String extraerZona(String direccion) {
        String[] palabras = direccion.split("\\s+");
        if (palabras.length > 0) {
            return palabras[0];
        }
        return direccion;
    }

    private ListaEnlazada<Map<String, Object>> combinarListas(ListaEnlazada<Map<String, Object>> lista1, ListaEnlazada<Map<String, Object>> lista2) {
        ListaEnlazada<Map<String, Object>> resultado = new ListaEnlazada<>();
        
        for (int i = 0; i < lista1.getTamaño(); i++) {
            resultado.agregar(lista1.obtener(i));
        }
        
        for (int i = 0; i < lista2.getTamaño(); i++) {
            resultado.agregar(lista2.obtener(i));
        }
        
        return resultado;
    }

    public int obtenerTotalAnomalias() {
        int total = 0;
        total += detectarVisitasSinCierre().getTamaño();
        total += detectarSobrecargaAsesores().getTamaño();
        total += detectarCambiosPrecioFrecuentes().getTamaño();
        total += detectarConcentracionGeografica().getTamaño();
        return total;
    }
}