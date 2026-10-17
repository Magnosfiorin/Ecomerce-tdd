package br.com.ecommerce.unit;

import br.com.ecommerce.config.DatabaseConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - Configuração de Banco de Dados Externo")
public class DatabaseConfigTest {

    @Test
    @DisplayName("Deve carregar parâmetros padrão de conexão quando não informados")
    void deveCarregarParametrosPadraoDeConexao() {
        DatabaseConfig config = new DatabaseConfig();

        assertNotNull(config.getUrl(), "A URL do banco não deve ser nula");
        assertNotNull(config.getUser(), "O usuário do banco não deve ser nulo");
        assertNotNull(config.getPassword(), "A senha do banco não deve ser nula");
    }

    @Test
    @DisplayName("Deve permitir configurar conexão customizada para PostgreSQL")
    void devePermitirConfigurarConexaoCustomizada() {
        String urlCustomizada = "jdbc:postgresql://meu-banco-externo.supabase.co:5432/postgres";
        String usuarioCustomizado = "admin_ecommerce";
        String senhaCustomizada = "senhaSegura123";

        DatabaseConfig config = new DatabaseConfig(urlCustomizada, usuarioCustomizado, senhaCustomizada);

        assertEquals(urlCustomizada, config.getUrl());
        assertEquals(usuarioCustomizado, config.getUser());
        assertEquals(senhaCustomizada, config.getPassword());
    }

    @Test
    @DisplayName("Deve retornar false graciosamente se o banco externo estiver inacessível")
    void deveRetornarFalseSeBancoEstiverInacessivel() {
        DatabaseConfig configInvalido = new DatabaseConfig(
                "jdbc:postgresql://localhost:9999/banco_inexistente",
                "usuario_errado",
                "senha_errada"
        );

        assertFalse(configInvalido.testConnection(), "Não deve conectar em porta ou host inválido");
    }
}
