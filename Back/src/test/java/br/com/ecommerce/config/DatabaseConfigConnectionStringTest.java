package br.com.ecommerce.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Testes da URL de conexão do Neon")
class DatabaseConfigConnectionStringTest {

    @Test
    @DisplayName("Converte URL postgresql e extrai credenciais")
    void converteUrlPostgresql() {
        DatabaseConfig.ConnectionSettings settings = DatabaseConfig.parseConnectionString(
                "postgresql://neon_user:p%40ssword@ep-example.neon.tech/ecommerce?sslmode=require");

        assertEquals("jdbc:postgresql://ep-example.neon.tech/ecommerce?sslmode=require", settings.jdbcUrl());
        assertEquals("neon_user", settings.user());
        assertEquals("p@ssword", settings.password());
    }

    @Test
    @DisplayName("Preserva uma URL JDBC")
    void preservaUrlJdbc() {
        String jdbcUrl = "jdbc:postgresql://ep-example.neon.tech/ecommerce?sslmode=require";

        DatabaseConfig.ConnectionSettings settings = DatabaseConfig.parseConnectionString(jdbcUrl);

        assertEquals(jdbcUrl, settings.jdbcUrl());
        assertNull(settings.user());
        assertNull(settings.password());
    }

    @Test
    @DisplayName("Rejeita URL que não seja PostgreSQL")
    void rejeitaOutroEsquema() {
        assertThrows(IllegalArgumentException.class,
                () -> DatabaseConfig.parseConnectionString("mysql://host/database"));
    }
}
