package br.com.ecommerce.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de interpretação da URL de conexão")
class DatabaseConfigConnectionStringTest {

    @Test
    @DisplayName("Deve converter URL da integração Neon para JDBC e extrair credenciais")
    void deveConverterUrlNeonParaJdbc() {
        DatabaseConfig.ConnectionSettings connection = DatabaseConfig.parseConnectionString(
                "postgresql://neon_user:p%40ssword@ep-example.neon.tech/ecommerce"
                        + "?sslmode=require&channel_binding=require");

        assertEquals("jdbc:postgresql://ep-example.neon.tech/ecommerce"
                + "?sslmode=require&channel_binding=require", connection.jdbcUrl());
        assertEquals("neon_user", connection.user());
        assertEquals("p@ssword", connection.password());
    }

    @Test
    @DisplayName("Deve manter URL JDBC já configurada")
    void deveManterUrlJdbc() {
        String jdbcUrl = "jdbc:postgresql://ep-example.neon.tech/ecommerce?sslmode=require";

        DatabaseConfig.ConnectionSettings connection = DatabaseConfig.parseConnectionString(jdbcUrl);

        assertEquals(jdbcUrl, connection.jdbcUrl());
        assertNull(connection.user());
        assertNull(connection.password());
    }

    @Test
    @DisplayName("Deve rejeitar URL de banco com esquema incompatível")
    void deveRejeitarUrlComEsquemaIncompativel() {
        assertThrows(IllegalArgumentException.class,
                () -> DatabaseConfig.parseConnectionString("mysql://host/database"));
    }
}
