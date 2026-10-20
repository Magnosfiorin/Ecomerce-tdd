package br.com.ecommerce.unit;

import br.com.ecommerce.config.DatabaseConfig;
import br.com.ecommerce.model.Produto;
import br.com.ecommerce.service.CatalogoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - Serviço de Catálogo (Dev 3)")
public class CatalogoServiceTest {

    private CatalogoService catalogo;

    @BeforeEach
    void setUp() {
        List<Produto> base = List.of(
                new Produto(1, "Camiseta Azul", 49.90, 10, true),
                new Produto(2, "Camiseta Preta", 59.90, 5, true),
                new Produto(3, "Tênis Antigo", 199.00, 0, false) // inativo
        );
        // Sem banco: usa a lista em memória (mesmo padrão do UsuarioServiceTest)
        catalogo = new CatalogoService(null, base);
    }

    @Test
    @DisplayName("Teste Unitário 1: Retorno correto da lista contendo apenas produtos ativos")
    void deveListarApenasProdutosAtivos() {
        List<Produto> lista = catalogo.listarAtivos();

        assertEquals(2, lista.size());
        assertTrue(lista.stream().allMatch(Produto::isAtivo));
        assertTrue(lista.stream().noneMatch(p -> p.getNome().equals("Tênis Antigo")));
    }

    @Test
    @DisplayName("Teste Unitário 2: Filtragem e busca de produtos por nome")
    void deveBuscarProdutosPorNome() {
        List<Produto> resultado = catalogo.buscarPorNome("PRETA"); // ignora maiúsculas

        assertEquals(1, resultado.size());
        assertEquals("Camiseta Preta", resultado.get(0).getNome());
        assertTrue(catalogo.buscarPorNome("tênis").isEmpty(), "Produto inativo não pode aparecer na busca");
        assertEquals(2, catalogo.buscarPorNome("camiseta").size());
    }

    @Test
    @DisplayName("Deve consultar o banco diretamente sem abrir uma conexão de teste separada")
    void deveEvitarConexaoDeTesteAntesDaConsulta() {
        DatabaseConfig databaseConfig = new DatabaseConfig("jdbc:test", "user", "password") {
            @Override
            public boolean testConnection() {
                fail("A consulta não deve abrir uma conexão de teste separada.");
                return false;
            }

            @Override
            public Connection getConnection() throws SQLException {
                throw new SQLException("Banco indisponível.");
            }
        };
        CatalogoService catalogoComBancoIndisponivel = new CatalogoService(databaseConfig, List.of(
                new Produto(1, "Camiseta Azul", 49.90, 10, true)));

        List<Produto> resultado = catalogoComBancoIndisponivel.listarAtivos();

        assertEquals(1, resultado.size());
        assertEquals("Camiseta Azul", resultado.get(0).getNome());
    }
}