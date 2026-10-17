package br.com.ecommerce.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import br.com.ecommerce.service.UsuarioService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralizador de Rotas HTTP da aplicação E-Commerce.
 * Gerencia mapeamento de endpoints, suporte a CORS, serialização/deserialização JSON
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

    private final UsuarioService usuarioService;

    public Router(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        this.usuarioService = new UsuarioService(databaseConfig);
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

        // =========================================================================
        // Dev 1: Autenticação e Cadastro (Cliente / Vendedor / Lojas)
        // =========================================================================

        // 1. Login
        register("POST", "/api/auth/login", exchange -> {
            Map<?, ?> body = readJsonBody(exchange, Map.class);
            if (body == null) {
                sendError(exchange, 400, "Corpo da requisição inválido.");
                return;
            }

            String email = body.get("email") != null ? String.valueOf(body.get("email")) : null;
            String senha = body.get("senha") != null ? String.valueOf(body.get("senha")) : null;

            try {
                br.com.ecommerce.model.Usuario usuario = usuarioService.autenticar(email, senha);
                if (usuario != null) {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("sucesso", true);
                    resp.put("usuario", usuario);
                    br.com.ecommerce.model.Loja loja = usuarioService.buscarLojaPorUsuario(usuario.getId());
                    if (loja != null) {
                        resp.put("loja", loja);
                    }
                    sendJsonResponse(exchange, 200, resp);
                } else {
                    sendError(exchange, 401, "E-mail ou senha incorretos.");
                }
            } catch (IllegalArgumentException e) {
                sendError(exchange, 400, e.getMessage());
            }
        });

        // 2. Cadastro de Cliente (tipo 1)
        register("POST", "/api/auth/cadastro/cliente", exchange -> {
            br.com.ecommerce.model.Usuario usuario = readJsonBody(exchange, br.com.ecommerce.model.Usuario.class);
            if (usuario == null) {
                sendError(exchange, 400, "Dados do cliente não informados.");
                return;
            }

            try {
                br.com.ecommerce.model.Usuario salvo = usuarioService.cadastrarCliente(usuario);
                Map<String, Object> resp = new HashMap<>();
                resp.put("sucesso", true);
                resp.put("mensagem", "Cliente cadastrado com sucesso!");
                resp.put("usuario", salvo);
                sendJsonResponse(exchange, 201, resp);
            } catch (IllegalArgumentException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "Erro ao cadastrar cliente: " + e.getMessage());
            }
        });

        // 3. Cadastro de Vendedor em 2 etapas (tipo 0 + Loja)
        register("POST", "/api/auth/cadastro/vendedor", exchange -> {
            Map<?, ?> body = readJsonBody(exchange, Map.class);
            if (body == null) {
                sendError(exchange, 400, "Dados incompletos.");
                return;
            }

            try {
                String usuarioJson = gson.toJson(body.get("usuario"));
                String lojaJson = gson.toJson(body.get("loja"));

                br.com.ecommerce.model.Usuario usuario = gson.fromJson(usuarioJson, br.com.ecommerce.model.Usuario.class);
                br.com.ecommerce.model.Loja loja = gson.fromJson(lojaJson, br.com.ecommerce.model.Loja.class);

                br.com.ecommerce.model.Usuario vendedorSalvo = usuarioService.cadastrarVendedorComLoja(usuario, loja);

                Map<String, Object> resp = new HashMap<>();
                resp.put("sucesso", true);
                resp.put("mensagem", "Vendedor e loja cadastrados com sucesso!");
                resp.put("usuario", vendedorSalvo);
                resp.put("loja", loja);
                sendJsonResponse(exchange, 201, resp);
            } catch (IllegalArgumentException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "Erro ao cadastrar vendedor: " + e.getMessage());
            }
        });

        // 4. Criação de Loja para Cliente Existente (Promove para tipo 2)
        register("POST", "/api/lojas", exchange -> {
            Map<?, ?> body = readJsonBody(exchange, Map.class);
            if (body == null || !body.containsKey("usuarioId")) {
                sendError(exchange, 400, "Identificação do usuário obrigatória.");
                return;
            }

            try {
                int usuarioId = ((Number) body.get("usuarioId")).intValue();
                String lojaJson = gson.toJson(body);
                br.com.ecommerce.model.Loja loja = gson.fromJson(lojaJson, br.com.ecommerce.model.Loja.class);

                br.com.ecommerce.model.Loja lojaSalva = usuarioService.criarLojaParaCliente(usuarioId, loja);

                Map<String, Object> resp = new HashMap<>();
                resp.put("sucesso", true);
                resp.put("mensagem", "Loja criada com sucesso! Seu perfil agora é Vendedor e Cliente.");
                resp.put("loja", lojaSalva);
                resp.put("novoTipoUsuario", br.com.ecommerce.model.Usuario.TIPO_VENDEDOR_CLIENTE);
                sendJsonResponse(exchange, 201, resp);
            } catch (IllegalArgumentException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "Erro ao criar loja: " + e.getMessage());
            }
        });

        // Dev 2: Módulo Cadastro de Produtos (Vendedor)
        register("POST", "/api/produtos", exchange -> {
            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Endpoint de Cadastro de Produtos pronto para implementação do Dev 2");
            response.put("modulo", "Cadastro de Produtos");
            sendJsonResponse(exchange, 201, response);
        });

        // Dev 3: Módulo Catálogo (Cliente)
        register("GET", "/api/produtos", exchange -> {
            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Endpoint de Catálogo pronto para implementação do Dev 3");
            response.put("produtos", new Object[]{});
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
        if (path == null || path.isEmpty()) return "/";
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
        try {
            byte[] bytes = exchange.getRequestBody().readAllBytes();
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            String json = new String(bytes, StandardCharsets.UTF_8);
            return gson.fromJson(json, clazz);
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
