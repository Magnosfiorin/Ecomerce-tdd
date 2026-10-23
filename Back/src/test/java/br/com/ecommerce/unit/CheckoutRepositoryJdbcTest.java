package br.com.ecommerce.unit;

import br.com.ecommerce.config.DatabaseConfig;
import br.com.ecommerce.model.ItemCheckout;
import br.com.ecommerce.repository.CheckoutRepositoryJdbc;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckoutRepositoryJdbcTest {

    @Test
    void deveReverterTransacaoQuandoFalharAoSalvarItemDoPedido() throws Exception {
        boolean[] transacao = new boolean[2];
        ResultSet usuarioResult = resultSet(Map.of("tipo_usuario", 1));
        ResultSet produtoResult = resultSet(Map.of(
                "ativo", true,
                "estoque", 5,
                "preco", new BigDecimal("12.50")));
        ResultSet pedidoResult = resultSet(Map.of("id", 88));

        Connection connection = proxy(Connection.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "prepareStatement" -> {
                    String sql = (String) args[0];
                    if (sql.contains("SELECT id, tipo_usuario FROM usuarios")) {
                        return preparedStatement(usuarioResult, false);
                    }
                    if (sql.contains("FOR UPDATE")) {
                        return preparedStatement(produtoResult, false);
                    }
                    if (sql.contains("INSERT INTO pedidos")) {
                        return preparedStatement(pedidoResult, false);
                    }
                    if (sql.contains("INSERT INTO itens_pedido")) {
                        return preparedStatement(null, true);
                    }
                    throw new SQLException("SQL inesperado: " + sql);
                }
                case "rollback" -> {
                    transacao[0] = true;
                    return null;
                }
                case "commit" -> {
                    transacao[1] = true;
                    return null;
                }
                default -> {
                    return null;
                }
            }
        });
        DatabaseConfig databaseConfig = new DatabaseConfig(
                "jdbc:postgresql://localhost/test", "test", "test") {
            @Override
            public Connection getConnection() {
                return connection;
            }
        };

        CheckoutRepositoryJdbc repository = new CheckoutRepositoryJdbc(databaseConfig);

        assertThrows(IllegalStateException.class, () -> repository.finalizarPedido(
                7,
                List.of(new ItemCheckout(3, 2)),
                "Cliente Teste",
                "Rua A, 10",
                "PIX"));

        org.junit.jupiter.api.Assertions.assertTrue(transacao[0]);
        org.junit.jupiter.api.Assertions.assertFalse(transacao[1]);
    }

    private static PreparedStatement preparedStatement(ResultSet resultSet, boolean failOnUpdate) {
        return proxy(PreparedStatement.class, (proxy, method, args) -> switch (method.getName()) {
            case "executeQuery" -> resultSet;
            case "executeUpdate" -> {
                if (failOnUpdate) {
                    throw new SQLException("falha simulada");
                }
                yield 1;
            }
            default -> null;
        });
    }

    private static ResultSet resultSet(Map<String, Object> values) {
        boolean[] firstRow = {true};
        return proxy(ResultSet.class, (proxy, method, args) -> switch (method.getName()) {
            case "next" -> {
                boolean hasRow = firstRow[0];
                firstRow[0] = false;
                yield hasRow;
            }
            case "getInt", "getBoolean", "getBigDecimal" -> values.get((String) args[0]);
            default -> null;
        });
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                handler));
    }
}
