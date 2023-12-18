package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpRequest.BodyPublishers;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;

class GameStartHandler implements HttpHandler {
    private final int port;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public GameStartHandler(int port) {
        this.port = port;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            processGameStartRequest(exchange);
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, 0);
        }
    }


    private void processGameStartRequest(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        JsonNode jsonNode = objectMapper.readTree(requestBody);
        int adversaryPort = extractAdversaryPort(jsonNode);

        System.out.println("Received start request from adversary on port " + adversaryPort);

        JsonNode responseJson = objectMapper.createObjectNode()
                .put("id", "2aca7611-0ae4-49f3-bf63-75bef4769028")
                .put("url", "http://localhost:" + this.port)
                .put("message", "May the best code win");

        sendJsonResponse(exchange, responseJson);

        makePostRequest(jsonNode.get("url").asText());
    }

    private void makePostRequest(String adversaryUrl) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(adversaryUrl + "/api/game/start"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"id\":\"1\", \"url\":\"http://localhost:" + port + "\", \"message\":\"hello\"}"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    private int extractAdversaryPort(JsonNode jsonNode) {
        String adversaryUrl = jsonNode.get("url").asText();
        return adversaryUrl.endsWith("/") ?
                URI.create(adversaryUrl).getPort() :
                URI.create(adversaryUrl + "/").getPort();
    }

    private void sendJsonResponse(HttpExchange exchange, JsonNode responseJson) throws IOException {
        exchange.sendResponseHeaders(202, responseJson.toString().length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseJson.toString().getBytes());
        }
    }
}
