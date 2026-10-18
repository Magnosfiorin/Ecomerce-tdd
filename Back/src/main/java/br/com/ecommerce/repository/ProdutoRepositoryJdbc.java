package br.com.ecommerce.repository;

import br.com.ecommerce.config.DatabaseConfig;
import br.com.ecommerce.model.Produto;
import java.sql.*;

public class ProdutoRepositoryJdbc implements ProdutoRepository {
    private static final String SQL = "INSERT INTO produtos (nome, preco, estoque) VALUES (?, ?, ?) RETURNING id";
    private final DatabaseConfig db;

    public ProdutoRepositoryJdbc(DatabaseConfig db) { this.db = db; }

    @Override
    public Produto salvar(Produto produto) {
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.setString(1, produto.getNome());
            stmt.setBigDecimal(2, produto.getPreco());
            stmt.setInt(3, produto.getEstoque());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) produto.setId(rs.getInt("id"));
            }
            return produto;
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao salvar produto: " + e.getMessage(), e);
        }
    }
}