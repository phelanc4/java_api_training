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

public class Launcher {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java fr.lernejo.navy_battle.Launcher <port> [adversaryUrl]");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        try {
            createAndStartHttpServer(port);
            System.out.println("Server started on port " + port);

            if (args.length == 2) {
                String adversaryUrl = args[1];
                makePostRequest(port, adversaryUrl);
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void createAndStartHttpServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(1));
        server.createContext("/ping", new PingHandler());
        server.createContext("/api/game/start", new GameStartHandler(port));
        server.start();
    }

    static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendPingResponse(exchange);
        }

        private void sendPingResponse(HttpExchange exchange) throws IOException {
            String body = "OK";
            exchange.sendResponseHeaders(200, body.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body.getBytes());
            }
        }
    }

    static class GameStartHandler implements HttpHandler {
        private final int port;

        public GameStartHandler(int port) {
            this.port = port;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                JsonNode jsonNode = objectMapper.readTree(requestBody);

                int adversaryPort = jsonNode.get("url").asText().endsWith("/") ?
                        URI.create(jsonNode.get("url").asText()).getPort() :
                        URI.create(jsonNode.get("url").asText() + "/").getPort();

                System.out.println("Received start request from adversary on port " + adversaryPort);

                JsonNode responseJson = objectMapper.createObjectNode()
                        .put("id", "2aca7611-0ae4-49f3-bf63-75bef4769028")
                        .put("url", "http://localhost:" + this.port)
                        .put("message", "May the best code win");

                exchange.sendResponseHeaders(202, responseJson.toString().length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseJson.toString().getBytes());
                }

                makePostRequest(this.port, jsonNode.get("url").asText());
            } catch (Exception e) {
                exchange.sendResponseHeaders(400, 0);
            }
        }
    }

    private static void makePostRequest(int myPort, String adversaryUrl) {
    try {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(adversaryUrl + "/api/game/start"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString("{\"id\":\"1\", \"url\":\"http://localhost:" + myPort + "\", \"message\":\"hello\"}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }
 }
	
}
