package br.com.ecommerce.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import br.com.ecommerce.controller.ProdutoController;
import br.com.ecommerce.repository.ProdutoRepositoryJdbc;
import br.com.ecommerce.service.ProdutoService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralizador de Rotas HTTP da aplicação E-Commerce.
 * Gerencia mapeamento de endpoints, suporte a CORS, serialização/deserialização
 * JSON
 * e códigos de resposta padronizados.
 */
public class Router implements HttpHandler {

    @FunctionalInterface
    public interface RouteHandler {
        void handle(HttpExchange exchange) throws Exception;
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, RouteHandler> routes = new HashMap<>();
    private final DatabaseConfig databaseConfig;

    public Router(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        registerDefaultRoutes();
    }

    /**
     * Registra uma rota com método HTTP e caminho.
     */
    public void register(String method, String path, RouteHandler handler) {
        String key = buildKey(method, normalizePath(path));
        routes.put(key, handler);
    }

    /**
     * Centralização de todas as rotas do E-Commerce.
     * Serve como ponto único onde cada integrante (Dev 1 a 5) acopla seus
     * controladores.
     */
    private void registerDefaultRoutes() {
        // Rota de Health Check / Status da API e Banco de Dados
        register("GET", "/api/health", exchange -> {
            boolean dbConnected = databaseConfig != null && databaseConfig.testConnection();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "ecommerce-backend");
            response.put("database", dbConnected ? "CONECTADO" : "DESCONECTADO (Verifique as credenciais no .env)");
            response.put("timestamp", System.currentTimeMillis());
            sendJsonResponse(exchange, 200, response);
        });

        // Dev 1: Módulo Login (Vendedor / Cliente)
        register("POST", "/api/auth/login", exchange -> {
            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Endpoint de Login pronto para implementação do Dev 1");
            response.put("modulo", "Autenticação");
            sendJsonResponse(exchange, 200, response);
        });

        // Dev 2: Módulo Cadastro de Produtos (Vendedor)
        ProdutoController produtoController = new ProdutoController(
                new ProdutoService(new ProdutoRepositoryJdbc(databaseConfig)));
        register("POST", "/api/produtos", produtoController::cadastrar);

        // Dev 3: Módulo Catálogo (Cliente)
        register("GET", "/api/produtos", exchange -> {
            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Endpoint de Catálogo pronto para implementação do Dev 3");
            response.put("produtos", new Object[] {});
            sendJsonResponse(exchange, 200, response);
        });

        // Dev 4: Módulo Carrinho de Compras
        register("POST", "/api/carrinho", exchange -> {
            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Endpoint do Carrinho pronto para implementação do Dev 4");
            response.put("modulo", "Carrinho");
            sendJsonResponse(exchange, 200, response);
        });

        // Dev 5: Módulo Checkout
        register("POST", "/api/checkout", exchange -> {
            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Endpoint de Checkout pronto para implementação do Dev 5");
            response.put("modulo", "Checkout");
            sendJsonResponse(exchange, 200, response);
        });
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Adiciona headers de CORS para permitir requisições do frontend Web
        addCorsHeaders(exchange);

        String method = exchange.getRequestMethod();

        // Tratamento de requisições Preflight (OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = normalizePath(exchange.getRequestURI().getPath());
        String key = buildKey(method, path);

        RouteHandler handler = routes.get(key);

        if (handler != null) {
            try {
                handler.handle(exchange);
            } catch (Exception e) {
                System.err.println("[Router] Erro interno na rota " + key + ": " + e.getMessage());
                sendError(exchange, 500, "Erro interno no servidor: " + e.getMessage());
            }
        } else {
            sendError(exchange, 404, "Rota não encontrada: " + method + " " + path);
        }
    }

    private String buildKey(String method, String path) {
        return method.toUpperCase() + ":" + path;
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty())
            return "/";
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    // =========================================================================
    // Métodos Utilitários para os Controladores dos Devs
    // =========================================================================

    /**
     * Envia resposta formatada em JSON com status HTTP.
     */
    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = gson.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Lê o corpo da requisição e desserializa para a classe informada.
     */
    public static <T> T readJsonBody(HttpExchange exchange, Class<T> clazz) {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Envia mensagem de erro padronizada em formato JSON.
     */
    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("status", statusCode);
        error.put("erro", message);
        sendJsonResponse(exchange, statusCode, error);
    }

    public Map<String, RouteHandler> getRoutes() {
        return routes;
    }
}
