package br.com.ecommerce.config;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Centraliza a configuração e conexão com o banco de dados PostgreSQL.
 * Lê prioritariamente as credenciais a partir do arquivo .env na raiz do projeto,
 * variáveis de ambiente do sistema, ou application.properties.
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
     * Carrega primeiro as variáveis reconhecidas pela integração Neon/Vercel,
     * mantendo DB_* como opção para configuração manual.
     */
    private void loadConfiguration() {
        DotenvLoader.load();

        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            System.err.println("[DatabaseConfig] Aviso: Não foi possível ler application.properties: " + e.getMessage());
        }

        String envUrl = firstNonBlank(
                DotenvLoader.get("DB_URL"),
                DotenvLoader.get("DATABASE_URL"),
                DotenvLoader.get("DATABASE_URL_UNPOOLED"),
                DotenvLoader.get("POSTGRES_URL"),
                DotenvLoader.get("POSTGRES_URL_NON_POOLING"));
        String envUser = firstNonBlank(
                DotenvLoader.get("DB_USER"),
                DotenvLoader.get("PGUSER"),
                DotenvLoader.get("POSTGRES_USER"));
        String envPass = firstNonBlank(
                DotenvLoader.get("DB_PASSWORD"),
                DotenvLoader.get("PGPASSWORD"),
                DotenvLoader.get("POSTGRES_PASSWORD"));

        if (envUrl == null) {
            String host = firstNonBlank(DotenvLoader.get("PGHOST"), DotenvLoader.get("POSTGRES_HOST"));
            String database = firstNonBlank(DotenvLoader.get("PGDATABASE"), DotenvLoader.get("POSTGRES_DATABASE"));
            if (host != null && database != null) {
                String port = firstNonBlank(DotenvLoader.get("PGPORT"), "5432");
                envUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";
            }
        }

        String vercelDeployment = DotenvLoader.get("VERCEL");
        boolean runningOnVercel = vercelDeployment != null
                && !vercelDeployment.isBlank()
                && !"0".equals(vercelDeployment)
                && !"false".equalsIgnoreCase(vercelDeployment);
        String propUrl = System.getProperty("db.url",
                properties.getProperty("db.url", "jdbc:postgresql://localhost:5432/ecommerce"));
        String propUser = System.getProperty("db.user", properties.getProperty("db.user", "postgres"));
        String propPass = System.getProperty("db.password", properties.getProperty("db.password", "postgres"));

        if (runningOnVercel && envUrl == null) {
            System.err.println(
                    "[DatabaseConfig] Configuração do Neon ausente. Configure DATABASE_URL ou DB_URL no projeto Backend da Vercel.");
            this.url = null;
            this.user = null;
            this.password = null;
            return;
        }

        ConnectionSettings connection = envUrl == null
                ? new ConnectionSettings(propUrl, null, null)
                : parseConnectionString(envUrl);
        String resolvedUser = firstNonBlank(envUser, connection.user(), runningOnVercel ? null : propUser);
        String resolvedPassword = firstNonBlank(envPass, connection.password(), runningOnVercel ? null : propPass);

        if (runningOnVercel && (resolvedUser == null || resolvedPassword == null)) {
            System.err.println(
                    "[DatabaseConfig] Credenciais do Neon ausentes. Configure uma URL com usuário e senha ou as variáveis PGUSER e PGPASSWORD no projeto Backend da Vercel.");
            this.url = null;
            this.user = null;
            this.password = null;
            return;
        }

        this.url = connection.jdbcUrl();
        this.user = firstNonBlank(resolvedUser, propUser);
        this.password = firstNonBlank(resolvedPassword, propPass);
    }

    static ConnectionSettings parseConnectionString(String connectionString) {
        if (connectionString.startsWith("jdbc:postgresql:")) {
            return new ConnectionSettings(connectionString, null, null);
        }

        try {
            URI uri = new URI(connectionString);
            String scheme = uri.getScheme();
            if (!"postgres".equalsIgnoreCase(scheme) && !"postgresql".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException("Esquema de conexão PostgreSQL inválido.");
            }
            if (uri.getHost() == null || uri.getPath() == null || uri.getPath().length() < 2) {
                throw new IllegalArgumentException("A URL do PostgreSQL deve incluir host e nome do banco.");
            }

            String jdbcUrl = "jdbc:postgresql://" + uri.getRawAuthority().substring(
                    uri.getRawUserInfo() == null ? 0 : uri.getRawUserInfo().length() + 1)
                    + uri.getRawPath()
                    + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
            String user = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                int separator = uri.getUserInfo().indexOf(':');
                user = separator < 0 ? uri.getUserInfo() : uri.getUserInfo().substring(0, separator);
                password = separator < 0 ? null : uri.getUserInfo().substring(separator + 1);
            }
            return new ConnectionSettings(jdbcUrl, user, password);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("A URL de conexão PostgreSQL é inválida.", e);
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    record ConnectionSettings(String jdbcUrl, String user, String password) {}

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
     * Testa se o banco de dados local/externo está acessível com as credenciais atuais.
     */
    public boolean testConnection() {
        if (url == null || url.isBlank()) {
            System.err.println(
                    "[DatabaseConfig] URL do PostgreSQL não configurada. Defina DATABASE_URL ou DB_URL no ambiente do backend.");
            return false;
        }

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
                sobrenome VARCHAR(100) NOT NULL DEFAULT '',
                email VARCHAR(150) UNIQUE NOT NULL,
                senha VARCHAR(255) NOT NULL,
                cpf VARCHAR(14) NOT NULL DEFAULT '',
                cep VARCHAR(10) NOT NULL DEFAULT '',
                endereco VARCHAR(200) NOT NULL DEFAULT '',
                numero VARCHAR(20) NOT NULL DEFAULT '',
                bairro VARCHAR(100) NOT NULL DEFAULT '',
                cidade VARCHAR(100) NOT NULL DEFAULT '',
                estado VARCHAR(50) NOT NULL DEFAULT '',
                pais VARCHAR(50) NOT NULL DEFAULT 'Brasil',
                tipo_usuario INT NOT NULL DEFAULT 1,
                data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );

            ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS sobrenome VARCHAR(100) NOT NULL DEFAULT '';
            ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS cpf VARCHAR(14) NOT NULL DEFAULT '';
            ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS cep VARCHAR(10) NOT NULL DEFAULT '';
            ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS endereco VARCHAR(200) NOT NULL DEFAULT '';
            ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS numero VARCHAR(20) NOT NULL DEFAULT '';
            ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS bairro VARCHAR(100) NOT NULL DEFAULT '';
            ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS cidade VARCHAR(100) NOT NULL DEFAULT '';
            ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS estado VARCHAR(50) NOT NULL DEFAULT '';
            ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS pais VARCHAR(50) NOT NULL DEFAULT 'Brasil';
            ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS tipo_usuario INT NOT NULL DEFAULT 1;

            DO $$ 
            BEGIN 
                IF EXISTS (
                    SELECT 1 FROM information_schema.columns 
                    WHERE table_name='usuarios' AND column_name='perfil' AND is_nullable='NO'
                ) THEN 
                    ALTER TABLE usuarios ALTER COLUMN perfil DROP NOT NULL; 
                END IF; 
            END $$;

            CREATE TABLE IF NOT EXISTS lojas (
                id SERIAL PRIMARY KEY,
                usuario_id INT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
                nome_loja VARCHAR(150) NOT NULL,
                email_loja VARCHAR(150) NOT NULL,
                cnpj VARCHAR(18),
                cep_loja VARCHAR(10) NOT NULL DEFAULT '',
                endereco_loja VARCHAR(200) NOT NULL DEFAULT '',
                numero_loja VARCHAR(20) NOT NULL DEFAULT '',
                bairro_loja VARCHAR(100) NOT NULL DEFAULT '',
                cidade_loja VARCHAR(100) NOT NULL DEFAULT '',
                estado_loja VARCHAR(50) NOT NULL DEFAULT '',
                pais_loja VARCHAR(50) NOT NULL DEFAULT 'Brasil',
                data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );

            ALTER TABLE lojas ADD COLUMN IF NOT EXISTS cep_loja VARCHAR(10) NOT NULL DEFAULT '';
            ALTER TABLE lojas ADD COLUMN IF NOT EXISTS endereco_loja VARCHAR(200) NOT NULL DEFAULT '';
            ALTER TABLE lojas ADD COLUMN IF NOT EXISTS numero_loja VARCHAR(20) NOT NULL DEFAULT '';
            ALTER TABLE lojas ADD COLUMN IF NOT EXISTS bairro_loja VARCHAR(100) NOT NULL DEFAULT '';
            ALTER TABLE lojas ADD COLUMN IF NOT EXISTS cidade_loja VARCHAR(100) NOT NULL DEFAULT '';
            ALTER TABLE lojas ADD COLUMN IF NOT EXISTS estado_loja VARCHAR(50) NOT NULL DEFAULT '';
            ALTER TABLE lojas ADD COLUMN IF NOT EXISTS pais_loja VARCHAR(50) NOT NULL DEFAULT 'Brasil';

            CREATE TABLE IF NOT EXISTS produtos (
                id SERIAL PRIMARY KEY,
                loja_id INT REFERENCES lojas(id) ON DELETE SET NULL,
                usuario_id INT REFERENCES usuarios(id) ON DELETE CASCADE,
                nome VARCHAR(150) NOT NULL,
                preco NUMERIC(10, 2) NOT NULL,
                estoque INTEGER NOT NULL DEFAULT 0,
                ativo BOOLEAN NOT NULL DEFAULT TRUE,
                data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );

            ALTER TABLE produtos ADD COLUMN IF NOT EXISTS loja_id INT REFERENCES lojas(id) ON DELETE SET NULL;
            ALTER TABLE produtos ADD COLUMN IF NOT EXISTS usuario_id INT REFERENCES usuarios(id) ON DELETE CASCADE;
            ALTER TABLE produtos ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;

            CREATE TABLE IF NOT EXISTS pedidos (
                id SERIAL PRIMARY KEY,
                usuario_id INT REFERENCES usuarios(id),
                data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                total NUMERIC(10, 2) NOT NULL,
                status VARCHAR(30) NOT NULL DEFAULT 'PENDENTE'
            );

            CREATE TABLE IF NOT EXISTS itens_pedido (
                id SERIAL PRIMARY KEY,
                pedido_id INT REFERENCES pedidos(id) ON DELETE CASCADE,
                produto_id INT REFERENCES produtos(id),
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
