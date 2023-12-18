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

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            handleError("Usage: java fr.lernejo.navy_battle.Launcher <port> [adversaryUrl]");
        }

        int port = Integer.parseInt(args[0]);

        NavyBattleServer navyBattleServer = new NavyBattleServer(port, args);

	try {
	    navyBattleServer.start();
        } catch (IOException e) {
            handleError("Error starting the server: " + e.getMessage());
        }
    }

    private static void handleError(String message) {
        System.err.println(message);
        System.exit(1);
    }
}
