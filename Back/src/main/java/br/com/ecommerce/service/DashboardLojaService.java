package br.com.ecommerce.service;

import br.com.ecommerce.config.DatabaseConfig;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardLojaService {

    private final DatabaseConfig databaseConfig;

    public DashboardLojaService(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public Map<String, Object> carregar(int usuarioId) {
        String produtosSql = """
                SELECT p.id, p.nome, p.preco, p.estoque,
                       COALESCE(SUM(CASE WHEN ped.status = 'CONCLUÍDO' THEN ip.quantidade ELSE 0 END), 0)
                           AS quantidade_vendida
                FROM produtos p
                LEFT JOIN itens_pedido ip ON ip.produto_id = p.id
                LEFT JOIN pedidos ped ON ped.id = ip.pedido_id
                WHERE p.usuario_id = ? AND p.ativo = TRUE
                GROUP BY p.id, p.nome, p.preco, p.estoque
                ORDER BY p.id
                """;
        String vendasSql = """
                SELECT COUNT(DISTINCT ped.id) AS quantidade_pedidos,
                       COALESCE(SUM(ip.quantidade * ip.preco_unitario), 0) AS valor_vendas
                FROM produtos p
                JOIN itens_pedido ip ON ip.produto_id = p.id
                JOIN pedidos ped ON ped.id = ip.pedido_id
                WHERE p.usuario_id = ? AND ped.status = 'CONCLUÍDO'
                """;

        try (Connection connection = databaseConfig.getConnection()) {
            List<Map<String, Object>> produtos = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(produtosSql)) {
                statement.setInt(1, usuarioId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Map<String, Object> produto = new HashMap<>();
                        produto.put("id", resultSet.getInt("id"));
                        produto.put("nome", resultSet.getString("nome"));
                        produto.put("preco", resultSet.getBigDecimal("preco"));
                        produto.put("estoque", resultSet.getInt("estoque"));
                        produto.put("quantidadeVendida", resultSet.getLong("quantidade_vendida"));
                        produtos.add(produto);
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("produtos", produtos);
            try (PreparedStatement statement = connection.prepareStatement(vendasSql)) {
                statement.setInt(1, usuarioId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    response.put("quantidadePedidos", resultSet.getLong("quantidade_pedidos"));
                    BigDecimal valor = resultSet.getBigDecimal("valor_vendas");
                    response.put("valorVendas", valor == null ? BigDecimal.ZERO : valor);
                }
            }
            return response;
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao carregar o dashboard da loja.", e);
        }
    }
}
