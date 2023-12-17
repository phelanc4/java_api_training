package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Launcher {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java fr.lernejo.navy_battle.Launcher <port>");
			System.exit(1);
		}

		int port = Integer.parseInt(args[0]);

		try {
			createAndStartHttpServer(port);
			System.out.println("Server started on port " + port);
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
        	private final ObjectMapper objectMapper = new ObjectMapper();
		private final int port;

		public GameStartHandler(int port) {
			this.port = port;
		}
        	@Override
        	public void handle(HttpExchange exchange) throws IOException {
            		try {
                		String requestBody = new String(exchange.getRequestBody().readAllBytes());
                		JsonNode responseJson = objectMapper.createObjectNode()
                        		.put("id", "2aca7611-0ae4-49f3-bf63-75bef4769028")
                        		.put("url", "http://localhost:" + port)
                        		.put("message", "May the best code win");
                		exchange.sendResponseHeaders(202, responseJson.toString().length());
                		try (OutputStream os = exchange.getResponseBody()) {
					os.write(responseJson.toString().getBytes());
                		}
            		} catch (IOException e) {
                		exchange.sendResponseHeaders(400, 0);
            		}
        	}
    	}
}
