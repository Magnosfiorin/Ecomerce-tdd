package br.com.ecommerce.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Centraliza a configuração e conexão com o banco de dados PostgreSQL externo.
 * Permite configurar via variáveis de ambiente (DB_URL, DB_USER, DB_PASSWORD)
 * ou arquivo application.properties.
 */
public class DatabaseConfig {

    private String url;
    private String user;
    private String password;

    public DatabaseConfig() {
        loadConfiguration();
    }

    public DatabaseConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Carrega as configurações priorizando Variáveis de Ambiente,
     * depois System Properties, e por fim application.properties.
     */
    private void loadConfiguration() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            System.err.println("[DatabaseConfig] Aviso: Não foi possível ler application.properties: " + e.getMessage());
        }

        // 1. Variáveis de Ambiente
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");

        // 2. System Properties (-Ddb.url=...)
        String propUrl = System.getProperty("db.url", properties.getProperty("db.url", "jdbc:postgresql://localhost:5432/ecommerce"));
        String propUser = System.getProperty("db.user", properties.getProperty("db.user", "postgres"));
        String propPass = System.getProperty("db.password", properties.getProperty("db.password", "postgres"));

        this.url = (envUrl != null && !envUrl.isBlank()) ? envUrl : propUrl;
        this.user = (envUser != null && !envUser.isBlank()) ? envUser : propUser;
        this.password = (envPass != null && !envPass.isBlank()) ? envPass : propPass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obtém uma conexão direta com o banco de dados PostgreSQL.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.url, this.user, this.password);
    }

    /**
     * Testa se o banco de dados externo está acessível.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DatabaseConfig] Não foi possível conectar ao PostgreSQL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cria as tabelas iniciais necessárias para o e-commerce caso não existam.
     */
    public void initializeTables() {
        String schemaSql = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id SERIAL PRIMARY KEY,
                nome VARCHAR(100) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                senha VARCHAR(100) NOT NULL,
                perfil VARCHAR(20) NOT NULL -- 'CLIENTE' ou 'VENDEDOR'
            );

            CREATE TABLE IF NOT EXISTS produtos (
                id SERIAL PRIMARY KEY,
                nome VARCHAR(150) NOT NULL,
                preco NUMERIC(10, 2) NOT NULL,
                estoque INTEGER NOT NULL DEFAULT 0
            );

            CREATE TABLE IF NOT EXISTS pedidos (
                id SERIAL PRIMARY KEY,
                usuario_id INTEGER REFERENCES usuarios(id),
                data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                total NUMERIC(10, 2) NOT NULL,
                status VARCHAR(30) NOT NULL DEFAULT 'PENDENTE'
            );

            CREATE TABLE IF NOT EXISTS itens_pedido (
                id SERIAL PRIMARY KEY,
                pedido_id INTEGER REFERENCES pedidos(id) ON DELETE CASCADE,
                produto_id INTEGER REFERENCES produtos(id),
                quantidade INTEGER NOT NULL,
                preco_unitario NUMERIC(10, 2) NOT NULL
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(schemaSql);
            System.out.println("[DatabaseConfig] Tabelas do PostgreSQL inicializadas com sucesso!");
        } catch (SQLException e) {
            System.err.println("[DatabaseConfig] Falha ao inicializar tabelas no banco: " + e.getMessage());
        }
    }
}
