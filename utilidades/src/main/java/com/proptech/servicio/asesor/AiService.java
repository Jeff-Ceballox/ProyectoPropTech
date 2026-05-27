package com.proptech.servicio.asesor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AiService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODELO_DEFAULT = "nvidia/nemotron-3-nano-30b-a3b:free";
    private static final int TIMEOUT_SEGUNDOS = 30;

    private final HttpClient client;
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;
    private boolean configurado;

    public AiService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SEGUNDOS))
                .build();
        this.mapper = new ObjectMapper();
        this.apiKey = System.getenv("OPENROUTER_API_KEY");
        this.model = MODELO_DEFAULT;
        this.configurado = (apiKey != null && !apiKey.isBlank());

        if (!configurado) {
            System.err.println("⚠️  OPENROUTER_API_KEY no configurada. El Asesor IA no funcionará.");
            System.err.println("   Para usarlo, ejecuta en PowerShell:");
            System.err.println("   $env:OPENROUTER_API_KEY=\"sk-or-v1-tu-clave\"");
        } else {
            System.out.println("✅ Asesor IA configurado con modelo: " + model);
        }
    }

    public boolean isConfigurado() {
        return configurado;
    }

    public String getModelo() {
        return model;
    }

    public String preguntar(String systemPrompt, String userMessage) throws Exception {
        if (!configurado) {
            return "⚠️ El asesor IA no está configurado. Debes definir la variable de entorno OPENROUTER_API_KEY.";
        }

        String json = construirPayload(systemPrompt, userMessage);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost:7070")
                .header("X-Title", "Insignia Inmo")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(TIMEOUT_SEGUNDOS))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            String errorBody = response.body();
            System.err.println("Error OpenRouter (" + response.statusCode() + "): " + errorBody);
            try {
                JsonNode errorJson = mapper.readTree(errorBody);
                String msg = errorJson.path("error").path("message").asText("Error desconocido");
                return "Lo siento, el servicio de IA no está disponible: " + msg;
            } catch (Exception e) {
                return "Lo siento, el servicio de IA respondió con un error (código " + response.statusCode() + "). Intenta de nuevo más tarde.";
            }
        }

        return extraerRespuesta(response.body());
    }

    private String construirPayload(String systemPrompt, String userMessage) {
        ObjectNode root = mapper.createObjectNode();
        root.put("model", model);
        root.put("temperature", 0.7);
        root.put("max_tokens", 512);

        ArrayNode messages = root.putArray("messages");

        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        try {
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            System.err.println("Error creando payload JSON: " + e.getMessage());
            return "{}";
        }
    }

    private String extraerRespuesta(String jsonRespuesta) {
        try {
            JsonNode root = mapper.readTree(jsonRespuesta);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message");
                return message.path("content").asText("No se pudo obtener respuesta.");
            }
            return "No se pudo obtener respuesta.";
        } catch (Exception e) {
            System.err.println("Error parseando respuesta JSON: " + e.getMessage());
            System.err.println("Respuesta raw (primeros 500 chars): " + jsonRespuesta.substring(0, Math.min(500, jsonRespuesta.length())));
            return "Error al procesar la respuesta de la IA.";
        }
    }
}
