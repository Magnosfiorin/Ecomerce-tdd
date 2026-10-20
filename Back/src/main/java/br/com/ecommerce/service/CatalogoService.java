package br.com.ecommerce.service;

import br.com.ecommerce.config.DatabaseConfig;
import br.com.ecommerce.model.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servico de Catalogo (UC03): lista produtos ativos e busca por nome.
 * Segue o mesmo padrao do UsuarioService: usa o PostgreSQL quando disponivel
 * e cai para uma lista em memoria quando nao ha banco (testes unitarios).
 */
public class CatalogoService {

    private final DatabaseConfig databaseConfig;
    private final List<Produto> produtosMemoria;

    public CatalogoService(DatabaseConfig databaseConfig) {
        this(databaseConfig, new ArrayList<>());
    }

    /** Construtor usado nos testes: injeta os produtos em memoria. */
    public CatalogoService(DatabaseConfig databaseConfig, List<Produto> produtosMemoria) {
        this.databaseConfig = databaseConfig;
        this.produtosMemoria = new ArrayList<>(produtosMemoria);
    }

    /** Retorna apenas os produtos ativos. */
    public List<Produto> listarAtivos() {
        return buscarPorNome(null);
    }

    /** Retorna os produtos ativos cujo nome contem o termo (sem diferenciar maiusculas). */
    public List<Produto> buscarPorNome(String termo) {
        String busca = termo == null ? "" : termo.trim().toLowerCase();

        if (databaseConfig != null) {
            String sql = "SELECT id, nome, preco, estoque, ativo FROM produtos " +
                         "WHERE ativo = TRUE AND LOWER(nome) LIKE ? ORDER BY nome";
            try (Connection conn = databaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + busca + "%");
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Produto> resultado = new ArrayList<>();
                    while (rs.next()) {
                        resultado.add(new Produto(
                                rs.getInt("id"),
                                rs.getString("nome"),
                                rs.getDouble("preco"),
                                rs.getInt("estoque"),
                                rs.getBoolean("ativo")));
                    }
                    return resultado;
                }
            } catch (SQLException e) {
                System.err.println("[CatalogoService] Erro ao consultar banco: " + e.getMessage());
            }
        }

        // Fallback em memoria
        return produtosMemoria.stream()
                .filter(Produto::isAtivo)
                .filter(p -> p.getNome().toLowerCase().contains(busca))
                .collect(Collectors.toList());
    }
}