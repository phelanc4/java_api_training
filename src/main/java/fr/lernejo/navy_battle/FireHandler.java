import fr.lernejo.navy_battle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class FireHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            processFireRequest(exchange);
        } else {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void processFireRequest(HttpExchange exchange) throws IOException {
        try {
            String cell = exchange.getRequestURI().getQuery();
            JsonNode responseJson = createFireResponse(cell);
            sendJsonResponse(exchange, 200, responseJson);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, "Bad Request");
        }
    }

    private JsonNode createFireResponse(String cell) {
        return objectMapper.createObjectNode()
                .put("consequence", "miss")
                .put("shipLeft", true);
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, JsonNode responseJson) throws IOException {
        exchange.sendResponseHeaders(statusCode, responseJson.toString().length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseJson.toString().getBytes());
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        exchange.sendResponseHeaders(statusCode, 0);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(message.getBytes());
        }
    }
}
