package br.com.ecommerce.integration;

import br.com.ecommerce.config.DatabaseConfig;
import br.com.ecommerce.model.ItemCheckout;
import br.com.ecommerce.model.PedidoCheckout;
import br.com.ecommerce.repository.CheckoutRepositoryJdbc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_CHECKOUT_TESTS", matches = "true")
@DisplayName("BDD - Dado um carrinho válido, quando o cliente confirma, pedido e estoque são atualizados no PostgreSQL")
class CheckoutPostgresIntegrationTest {

    @Test
    @DisplayName("Dado um carrinho válido, quando o cliente confirma, então pedido e estoque são atualizados no PostgreSQL")
    void devePersistirPedidoConcluidoEAbaterEstoqueNoPostgres() throws Exception {
        String url = requiredEnvironmentVariable("CHECKOUT_TEST_DB_URL");
        String user = requiredEnvironmentVariable("CHECKOUT_TEST_DB_USER");
        String password = requiredEnvironmentVariable("CHECKOUT_TEST_DB_PASSWORD");
        DatabaseConfig databaseConfig = new DatabaseConfig(url, user, password);
        databaseConfig.initializeTables();

        int usuarioId = -1;
        int produtoId = -1;
        int pedidoId = -1;
        String email = "checkout-" + UUID.randomUUID() + "@integration.test";

        try (Connection connection = databaseConfig.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?) RETURNING id")) {
                statement.setString(1, "Cliente de integração");
                statement.setString(2, email);
                statement.setString(3, "hash-de-teste");
                try (ResultSet resultSet = statement.executeQuery()) {
                    assertTrue(resultSet.next());
                    usuarioId = resultSet.getInt("id");
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO produtos (nome, preco, estoque) VALUES (?, ?, ?) RETURNING id")) {
                statement.setString(1, "Produto de integração");
                statement.setBigDecimal(2, new BigDecimal("12.50"));
                statement.setInt(3, 5);
                try (ResultSet resultSet = statement.executeQuery()) {
                    assertTrue(resultSet.next());
                    produtoId = resultSet.getInt("id");
                }
            }

            PedidoCheckout pedido = new CheckoutRepositoryJdbc(databaseConfig).finalizarPedido(
                    usuarioId,
                    List.of(new ItemCheckout(produtoId, 2)),
                    "Cliente de integração",
                    "Rua de teste, 1",
                    "PIX");
            pedidoId = pedido.getIdPedido();

            assertEquals("CONCLUÍDO", pedido.getStatus());
            assertEquals(new BigDecimal("25.00"), pedido.getTotal());

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT status, total, nome_destinatario, endereco_entrega, metodo_pagamento " +
                            "FROM pedidos WHERE id = ?")) {
                statement.setInt(1, pedidoId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    assertTrue(resultSet.next());
                    assertEquals("CONCLUÍDO", resultSet.getString("status"));
                    assertEquals(new BigDecimal("25.00"), resultSet.getBigDecimal("total"));
                    assertEquals("Cliente de integração", resultSet.getString("nome_destinatario"));
                    assertEquals("Rua de teste, 1", resultSet.getString("endereco_entrega"));
                    assertEquals("PIX", resultSet.getString("metodo_pagamento"));
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT estoque FROM produtos WHERE id = ?")) {
                statement.setInt(1, produtoId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    assertTrue(resultSet.next());
                    assertEquals(3, resultSet.getInt("estoque"));
                }
            }
        } finally {
            try (Connection cleanup = databaseConfig.getConnection()) {
                if (pedidoId > 0) {
                    deleteById(cleanup, "pedidos", pedidoId);
                }
                if (produtoId > 0) {
                    deleteById(cleanup, "produtos", produtoId);
                }
                if (usuarioId > 0) {
                    deleteById(cleanup, "usuarios", usuarioId);
                }
            }
        }
    }

    private static void deleteById(Connection connection, String table, int id) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + table + " WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be set when PostgreSQL checkout tests are enabled.");
        }
        return value;
    }
}
