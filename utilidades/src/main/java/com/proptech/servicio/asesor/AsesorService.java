package com.proptech.servicio.asesor;

import com.proptech.modelo.Cliente;
import com.proptech.modelo.Inmueble;
import com.proptech.modelo.Operacion;
import com.proptech.servicio.InventarioInmueblesService;
import com.proptech.servicio.ClientesService;
import com.proptech.servicio.OperacionesService;
import com.proptech.utilidades.estructuras.ListaEnlazada;

public class AsesorService {

    private final AiService aiService;
    private final InventarioInmueblesService inventarioService;
    private final ClientesService clientesService;
    private final OperacionesService operacionesService;

    public AsesorService(
            AiService aiService,
            InventarioInmueblesService inventarioService,
            ClientesService clientesService,
            OperacionesService operacionesService) {
        this.aiService = aiService;
        this.inventarioService = inventarioService;
        this.clientesService = clientesService;
        this.operacionesService = operacionesService;
    }

    public ChatResponse procesarPregunta(ChatRequest request) {
        try {
            String pregunta = request.getPregunta();
            if (pregunta == null || pregunta.trim().isEmpty()) {
                return new ChatResponse("Por favor escribe una pregunta.", false);
            }

            String contexto = construirContexto(request.getEmailUsuario());
            String systemPrompt = String.format(
                "Eres un asesor virtual experto en el sistema inmobiliario 'Insignia Inmo'. " +
                "Ayudas a agentes y clientes con información sobre propiedades, " +
                "clientes, operaciones y recomendaciones.\n\n" +
                "Contexto actual del sistema:\n%s\n\n" +
                "Responde SIEMPRE en texto plano, sin usar formato markdown. " +
                "No uses asteriscos, negritas, guiones especiales ni ningún otro caracter de formato. " +
                "Usa texto plano solamente. " +
                "Para enumerar elementos usa numeraci\u00f3n simple (1, 2, 3...). " +
                "Responde de forma breve, clara y amable en español. " +
                "Si no sabes la respuesta, sugiere contactar a un asesor humano. " +
                "Si te preguntan por datos específicos que no tienes en el contexto, " +
                "sé honesto y dilo.",
                contexto);

            String respuesta = aiService.preguntar(systemPrompt, pregunta);
            respuesta = limpiarMarkdown(respuesta);
            return new ChatResponse(respuesta);

        } catch (Exception e) {
            System.err.println("Error en AsesorService: " + e.getMessage());
            e.printStackTrace();
            return new ChatResponse(
                "Lo siento, ocurrió un error al procesar tu pregunta. " +
                "Por favor verifica que el servidor tenga conexión a Internet e intenta de nuevo.",
                false);
        }
    }

    public String getEstado() {
        if (!aiService.isConfigurado()) {
            return "desconfigurado";
        }
        return "listo";
    }

    public String getModelo() {
        return aiService.getModelo();
    }

    private String limpiarMarkdown(String texto) {
        if (texto == null) return "";
        return texto
            .replaceAll("\\*\\*", "")
            .replaceAll("\\*", "")
            .replaceAll("__", "")
            .replaceAll("`", "")
            .replaceAll("^#+\\s*", "")
            .replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1")
            .replaceAll("~~~+", "")
            .replaceAll("^>\\s*", "")
            .trim();
    }

    private String construirContexto(String emailUsuario) {
        StringBuilder sb = new StringBuilder();

        try {
            ListaEnlazada<Inmueble> inmuebles = inventarioService.obtenerTodos();
            int disponibles = 0;
            for (int i = 0; i < inmuebles.getTamaño(); i++) {
                Inmueble inm = inmuebles.obtener(i);
                if ("Disponible".equalsIgnoreCase(inm.getEstado())) disponibles++;
            }
            sb.append("- Total inmuebles: ").append(inmuebles.getTamaño());
            sb.append("\n- Inmuebles disponibles: ").append(disponibles);
        } catch (Exception e) {
            sb.append("- Inmuebles: no disponible");
        }

        try {
            ListaEnlazada<Cliente> clientes = clientesService.obtenerTodos();
            sb.append("\n- Total clientes registrados: ").append(clientes.getTamaño());
        } catch (Exception e) {
            sb.append("\n- Clientes: no disponible");
        }

        try {
            ListaEnlazada<Operacion> operaciones = operacionesService.obtenerTodas();
            int ventas = 0;
            int arriendos = 0;
            for (int i = 0; i < operaciones.getTamaño(); i++) {
                Operacion op = operaciones.obtener(i);
                if ("Venta".equalsIgnoreCase(op.getTipo())) ventas++;
                else if ("Arriendo".equalsIgnoreCase(op.getTipo())) arriendos++;
            }
            sb.append("\n- Total operaciones: ").append(operaciones.getTamaño());
            sb.append("\n  - Ventas: ").append(ventas);
            sb.append("\n  - Arriendos: ").append(arriendos);
        } catch (Exception e) {
            sb.append("\n- Operaciones: no disponible");
        }

        if (emailUsuario != null && !emailUsuario.isEmpty()) {
            sb.append("\n- Usuario que pregunta: ").append(emailUsuario);
        }

        return sb.toString();
    }
}
