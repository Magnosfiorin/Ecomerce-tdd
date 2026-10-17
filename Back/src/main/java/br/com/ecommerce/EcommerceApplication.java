package br.com.ecommerce;

import br.com.ecommerce.config.DatabaseConfig;
import br.com.ecommerce.config.DotenvLoader;
import br.com.ecommerce.config.Router;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * Ponto de entrada e inicialização do Backend E-Commerce TDD.
 * Inicializa o servidor HTTP, configura rotas centralizadas e conecta ao banco PostgreSQL externo.
 */
public class EcommerceApplication {

    private final int port;
    private HttpServer server;
    private final DatabaseConfig databaseConfig;
    private final Router router;

    public EcommerceApplication() {
        this(resolvePort(), new DatabaseConfig());
    }

    public EcommerceApplication(int port, DatabaseConfig databaseConfig) {
        this.port = port;
        this.databaseConfig = databaseConfig;
        this.router = new Router(databaseConfig);
    }

    /**
     * Resolve a porta do servidor a partir de variável de ambiente PORT,
     * application.properties ou valor padrão 8080.
     */
    private static int resolvePort() {
        DotenvLoader.load();
        String envPort = DotenvLoader.get("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                return Integer.parseInt(envPort);
            } catch (NumberFormatException ignored) {}
        }

        Properties properties = new Properties();
        try (InputStream in = EcommerceApplication.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                properties.load(in);
                String propPort = properties.getProperty("server.port");
                if (propPort != null && !propPort.isBlank()) {
                    return Integer.parseInt(propPort);
                }
            }
        } catch (Exception ignored) {}

        return 8080;
    }

    /**
     * Inicia o servidor HTTP e inicializa a conexão com o banco de dados.
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", router);
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        printStartupBanner();

        // Verificação do Banco de Dados Externo
        if (databaseConfig.testConnection()) {
            System.out.println("[PostgreSQL] Conexão com banco externo estabelecida com sucesso!");
            databaseConfig.initializeTables();
        } else {
            System.out.println("[PostgreSQL] Banco externo offline ou não configurado.");
            System.out.println("             Configure as variáveis DB_URL, DB_USER e DB_PASSWORD caso vá persistir dados.");
        }
    }

    /**
     * Encerra a execução do servidor HTTP.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[Backend] Servidor encerrado.");
        }
    }

    public int getPort() {
        return (server != null) ? server.getAddress().getPort() : port;
    }

    public Router getRouter() {
        return router;
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    private void printStartupBanner() {
        System.out.println("""
            =============================================================
               ____   ____                            ____             
              |  _ \\ / ___|___  _ __ ___   ___ _ __  / ___|___         
              | |_) | |   / _ \\| '_ ` _ \\ / _ \\ '__|| |   / _ \\        
              |  __/| |__| (_) | | | | | |  __/ |   | |__|  __/        
              |_|    \\____\\___/|_| |_| |_|\\___|_|    \\____\\___|        
                           E-COMMERCE TDD BACKEND                      
            =============================================================
            [Status]   Servidor rodando em: http://localhost:%d/
            [Health]   http://localhost:%d/api/health
            [Rotas Centralizadas]:
              • POST http://localhost:%d/api/auth/login    (Dev 1: Login)
              • POST http://localhost:%d/api/produtos      (Dev 2: Cadastro)
              • GET  http://localhost:%d/api/produtos      (Dev 3: Catálogo)
              • POST http://localhost:%d/api/carrinho      (Dev 4: Carrinho)
              • POST http://localhost:%d/api/checkout      (Dev 5: Checkout)
            =============================================================
            """.formatted(port, port, port, port, port, port, port));
    }

    public static void main(String[] args) {
        try {
            EcommerceApplication app = new EcommerceApplication();
            app.start();

            // Hook para encerramento gracioso (Ctrl + C)
            Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
        } catch (IOException e) {
            System.err.println("[Erro Fatal] Não foi possível iniciar o backend: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
