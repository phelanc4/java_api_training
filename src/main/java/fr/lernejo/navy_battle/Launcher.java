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
    // private final ObjectMapper objectMapper = new ObjectMapper();
    public static void main(String[] args) {
    	if (!isValidArgsLength(args)) {
        	displayUsageAndExit();
    	}

   	 int port = Integer.parseInt(args[0]);

    	try {
        	startServer(port, args);
    	} catch (IOException e) {
        	handleServerError(e);
    	}
    }

    private static void startServer(int port, String[] args) throws IOException {
    	createAndStartHttpServer(port);
    	System.out.println("Server started on port " + port);

    	if (args.length == 2) {
        	makePostRequest(port, args[1]);
    	}
    }

    private static boolean isValidArgsLength(String[] args) {
        return args.length == 1 || args.length == 2;
    }

    private static void displayUsageAndExit() {
        System.err.println("Usage: java fr.lernejo.navy_battle.Launcher <port> [adversaryUrl]");
        System.exit(1);
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
                processGameStartRequest(exchange);
            } catch (Exception e) {
                sendErrorResponse(exchange, 400);
            }
        }

        private void processGameStartRequest(HttpExchange exchange) throws IOException {
	    ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            JsonNode jsonNode = objectMapper.readTree(requestBody);

            int adversaryPort = getAdversaryPort(jsonNode);

            System.out.println("Received start request from adversary on port " + adversaryPort);

            JsonNode responseJson = createResponseJson();

            sendJsonResponse(exchange, responseJson);

            makePostRequest(this.port, jsonNode.get("url").asText());
        }

        private int getAdversaryPort(JsonNode jsonNode) {
            String adversaryUrl = jsonNode.get("url").asText();
            return adversaryUrl.endsWith("/") ? URI.create(adversaryUrl).getPort() : URI.create(adversaryUrl + "/").getPort();
        }

        private JsonNode createResponseJson() {
	    ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.createObjectNode()
                    .put("id", "2aca7611-0ae4-49f3-bf63-75bef4769028")
                    .put("url", "http://localhost:" + this.port)
                    .put("message", "May the best code win");
        }

        private void sendJsonResponse(HttpExchange exchange, JsonNode responseJson) throws IOException {
            exchange.sendResponseHeaders(202, responseJson.toString().length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseJson.toString().getBytes());
            }
        }
    }

    private static void makePostRequest(int myPort, String adversaryUrl) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = createPostRequest(myPort, adversaryUrl);

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static HttpRequest createPostRequest(int myPort, String adversaryUrl) {
        return HttpRequest.newBuilder()
                .uri(URI.create(adversaryUrl + "/api/game/start"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString("{\"id\":\"1\", \"url\":\"http://localhost:" + myPort + "\", \"message\":\"hello\"}"))
                .build();
    }

    private static void handleServerError(IOException e) {
        System.err.println("Error starting the server: " + e.getMessage());
        System.exit(1);
    }

    private static void sendErrorResponse(HttpExchange exchange, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, 0);
    }
}
