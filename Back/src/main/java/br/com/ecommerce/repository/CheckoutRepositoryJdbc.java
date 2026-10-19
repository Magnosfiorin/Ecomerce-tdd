package br.com.ecommerce.repository;

import br.com.ecommerce.config.DatabaseConfig;
import br.com.ecommerce.model.ItemCheckout;
import br.com.ecommerce.model.PedidoCheckout;
import br.com.ecommerce.service.CheckoutException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CheckoutRepositoryJdbc implements CheckoutRepository {

    private static final String STATUS_CONCLUIDO = "CONCLUÍDO";
    private final DatabaseConfig databaseConfig;

    public CheckoutRepositoryJdbc(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public PedidoCheckout finalizarPedido(
            int usuarioId,
            List<ItemCheckout> itens,
            String nomeDestinatario,
            String enderecoEntrega,
            String metodoPagamento) {
        try (Connection connection = databaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                validarUsuario(connection, usuarioId);
                List<ItemValidado> itensValidados = validarECarregarProdutos(connection, itens);
                BigDecimal total = calcularTotal(itensValidados);
                int pedidoId = inserirPedido(
                        connection, usuarioId, total, nomeDestinatario, enderecoEntrega, metodoPagamento);

                for (ItemValidado item : itensValidados) {
                    inserirItemPedido(connection, pedidoId, item);
                    baixarEstoque(connection, item);
                }

                connection.commit();
                return new PedidoCheckout(pedidoId, total, STATUS_CONCLUIDO);
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                if (e instanceof SQLException sqlException) {
                    throw new IllegalStateException("Falha ao persistir o pedido.", sqlException);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha na conexão com o banco ao finalizar o pedido.", e);
        }
    }

    private void validarUsuario(Connection connection, int usuarioId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, tipo_usuario FROM usuarios WHERE id = ?")) {
            statement.setInt(1, usuarioId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new CheckoutException("Usuário não encontrado.");
                }
                int tipoUsuario = resultSet.getInt("tipo_usuario");
                if (tipoUsuario != 1 && tipoUsuario != 2) {
                    throw new CheckoutException("Apenas clientes podem finalizar compras.");
                }
            }
        }
    }

    private List<ItemValidado> validarECarregarProdutos(
            Connection connection, List<ItemCheckout> itens) throws SQLException {
        List<ItemValidado> resultado = new ArrayList<>();
        String sql = "SELECT id, preco, estoque, ativo FROM produtos WHERE id = ? FOR UPDATE";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (ItemCheckout item : itens) {
                statement.setInt(1, item.getProdutoId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new CheckoutException(
                                "Produto não encontrado: " + item.getProdutoId() + ".");
                    }
                    if (!resultSet.getBoolean("ativo")) {
                        throw new CheckoutException(
                                "Produto indisponível: " + item.getProdutoId() + ".");
                    }

                    int estoque = resultSet.getInt("estoque");
                    if (estoque < item.getQuantidade()) {
                        throw new CheckoutException(
                                "Estoque insuficiente para o produto " + item.getProdutoId() + ".");
                    }

                    resultado.add(new ItemValidado(
                            item.getProdutoId(),
                            item.getQuantidade(),
                            resultSet.getBigDecimal("preco")));
                }
            }
        }
        return resultado;
    }

    private BigDecimal calcularTotal(List<ItemValidado> itens) {
        return itens.stream()
                .map(item -> item.precoUnitario().multiply(BigDecimal.valueOf(item.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int inserirPedido(
            Connection connection,
            int usuarioId,
            BigDecimal total,
            String nomeDestinatario,
            String enderecoEntrega,
            String metodoPagamento) throws SQLException {
        String sql = """
                INSERT INTO pedidos
                    (usuario_id, total, status, nome_destinatario, endereco_entrega, metodo_pagamento)
                VALUES (?, ?, ?, ?, ?, ?) RETURNING id
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioId);
            statement.setBigDecimal(2, total);
            statement.setString(3, STATUS_CONCLUIDO);
            statement.setString(4, nomeDestinatario);
            statement.setString(5, enderecoEntrega);
            statement.setString(6, metodoPagamento);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("O banco não retornou o identificador do pedido.");
                }
                return resultSet.getInt("id");
            }
        }
    }

    private void inserirItemPedido(
            Connection connection, int pedidoId, ItemValidado item) throws SQLException {
        String sql = """
                INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, preco_unitario)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, pedidoId);
            statement.setInt(2, item.produtoId());
            statement.setInt(3, item.quantidade());
            statement.setBigDecimal(4, item.precoUnitario());
            statement.executeUpdate();
        }
    }

    private void baixarEstoque(Connection connection, ItemValidado item) throws SQLException {
        String sql = """
                UPDATE produtos SET estoque = estoque - ?
                WHERE id = ? AND estoque >= ? AND ativo = TRUE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, item.quantidade());
            statement.setInt(2, item.produtoId());
            statement.setInt(3, item.quantidade());
            if (statement.executeUpdate() != 1) {
                throw new CheckoutException(
                        "Estoque insuficiente para o produto " + item.produtoId() + ".");
            }
        }
    }

    private void rollback(Connection connection, Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }

    private record ItemValidado(int produtoId, int quantidade, BigDecimal precoUnitario) {}
}
