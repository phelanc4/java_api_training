package fr.lernejo.navy_battle;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class NavyBattleServer {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final int port;

    public NavyBattleServer(int port, String[] args) {
        this.port = port;
        createAndStartHttpServer();
        System.out.println("Server started on port " + port);

        if (args.length == 2) {
            String adversaryUrl = args[1];
            makePostRequest(adversaryUrl);
        }
    }

    public void start() throws IOException {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newFixedThreadPool(1));
            server.createContext("/ping", new PingHandler());
            server.createContext("/api/game/start", new GameStartHandler(port));
            server.start();
        } catch (IOException e) {
            handleError("Error creating and starting the server: " + e.getMessage());
        }
    }


    private void createAndStartHttpServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newFixedThreadPool(1));
            server.createContext("/ping", new PingHandler());
            server.createContext("/api/game/start", new GameStartHandler(port));
            server.start();
        } catch (IOException e) {
            handleError("Error creating and starting the server: " + e.getMessage());
        }
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

    private static void handleError(String message) {
        System.err.println(message);
        System.exit(1);
    }
}
