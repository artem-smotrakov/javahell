package com.gypsyengineer.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Simple HTTP server.
 */
public class SimpleHttpServer implements HttpHandler, AutoCloseable {

    private final String content;
    private final HttpServer server;

    /**
     * This flag indicates if the server accepted a connection.
     */
    private boolean accepted = false;

    public SimpleHttpServer(String content) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/test", this);
        server.setExecutor(null);
        server.start();
        this.content = content;
    }

    public String url() {
        return String.format("http://localhost:%d/test", server.getAddress().getPort());
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        System.out.println("server: accepted a request");
        synchronized (this) {
            accepted = true;
        }
        byte[] response = content.getBytes();
        t.sendResponseHeaders(200, response.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(response);
        }
    }

    /**
     * @return True if the server accepted a connection, false otherwise.
     */
    public boolean accepted() {
        synchronized (this) {
            return accepted;
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
