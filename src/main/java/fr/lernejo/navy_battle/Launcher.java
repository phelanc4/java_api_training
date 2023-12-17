package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

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
}
