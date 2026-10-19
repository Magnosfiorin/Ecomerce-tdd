package br.com.ecommerce.unit;

import br.com.ecommerce.config.DatabaseConfig;
import br.com.ecommerce.model.ItemCheckout;
import br.com.ecommerce.repository.CheckoutRepositoryJdbc;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutRepositoryJdbcTest {

    @Test
    void deveReverterTransacaoQuandoFalharAoSalvarItemDoPedido() throws Exception {
        Connection connection = mock(Connection.class);
        DatabaseConfig databaseConfig = new DatabaseConfig(
                "jdbc:postgresql://localhost/test", "test", "test") {
            @Override
            public Connection getConnection() {
                return connection;
            }
        };
        PreparedStatement usuarioStatement = mock(PreparedStatement.class);
        PreparedStatement produtoStatement = mock(PreparedStatement.class);
        PreparedStatement pedidoStatement = mock(PreparedStatement.class);
        PreparedStatement itemStatement = mock(PreparedStatement.class);
        ResultSet usuarioResult = mock(ResultSet.class);
        ResultSet produtoResult = mock(ResultSet.class);
        ResultSet pedidoResult = mock(ResultSet.class);

        when(connection.prepareStatement(contains("SELECT id, tipo_usuario FROM usuarios")))
                .thenReturn(usuarioStatement);
        when(connection.prepareStatement(contains("FOR UPDATE")))
                .thenReturn(produtoStatement);
        when(connection.prepareStatement(contains("INSERT INTO pedidos")))
                .thenReturn(pedidoStatement);
        when(connection.prepareStatement(contains("INSERT INTO itens_pedido")))
                .thenReturn(itemStatement);
        when(usuarioStatement.executeQuery()).thenReturn(usuarioResult);
        when(usuarioResult.next()).thenReturn(true);
        when(usuarioResult.getInt("tipo_usuario")).thenReturn(1);
        when(produtoStatement.executeQuery()).thenReturn(produtoResult);
        when(produtoResult.next()).thenReturn(true);
        when(produtoResult.getBoolean("ativo")).thenReturn(true);
        when(produtoResult.getInt("estoque")).thenReturn(5);
        when(produtoResult.getBigDecimal("preco")).thenReturn(new java.math.BigDecimal("12.50"));
        when(pedidoStatement.executeQuery()).thenReturn(pedidoResult);
        when(pedidoResult.next()).thenReturn(true);
        when(pedidoResult.getInt("id")).thenReturn(88);
        when(itemStatement.executeUpdate()).thenThrow(new SQLException("falha simulada"));

        CheckoutRepositoryJdbc repository = new CheckoutRepositoryJdbc(databaseConfig);

        assertThrows(IllegalStateException.class, () -> repository.finalizarPedido(
                7,
                List.of(new ItemCheckout(3, 2)),
                "Cliente Teste",
                "Rua A, 10",
                "PIX"));

        verify(connection).rollback();
        verify(connection, never()).commit();
    }
}
