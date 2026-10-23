package br.com.ecommerce.unit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.ecommerce.config.DatabaseConfig;
import br.com.ecommerce.model.Produto;
import br.com.ecommerce.service.CatalogoService;

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

        @Test
    @DisplayName("Teste Unitário 3: Produto ativo sem estoque continua listado, mas marcado como indisponível")
    void deveListarProdutoEsgotadoComoIndisponivel() {
        CatalogoService comEsgotado = new CatalogoService(null, List.of(
                new Produto(1, "Camiseta Azul", 49.90, 10, true),
                new Produto(4, "Boné Vermelho", 39.90, 0, true)));

        List<Produto> lista = comEsgotado.listarAtivos();

        assertEquals(2, lista.size());
        Produto esgotado = lista.stream()
                .filter(p -> p.getNome().equals("Boné Vermelho"))
                .findFirst()
                .orElseThrow();
        assertFalse(esgotado.isDisponivel());
        assertTrue(lista.stream().filter(p -> p.getEstoque() > 0).allMatch(Produto::isDisponivel));
    }

    @Test
    @DisplayName("Teste de contrato: resposta JSON de /api/produtos deve trazer 'disponivel' em cada produto")
    void respostaJsonDoCatalogoDeveInformarDisponibilidade() {
        CatalogoService comEsgotado = new CatalogoService(null, List.of(
                new Produto(1, "Camiseta Azul", 49.90, 10, true),
                new Produto(4, "Boné Vermelho", 39.90, 0, true)));
        // Mesma serialização usada pelo Router na rota GET /api/produtos
        Map<String, Object> response = new HashMap<>();
        response.put("produtos", comEsgotado.listarAtivos());

        String corpo = new GsonBuilder().setPrettyPrinting().create().toJson(response);
        JsonArray produtos = JsonParser.parseString(corpo).getAsJsonObject().getAsJsonArray("produtos");

        assertEquals(2, produtos.size());
        for (int i = 0; i < produtos.size(); i++) {
            JsonObject produto = produtos.get(i).getAsJsonObject();
            assertTrue(produto.has("disponivel"), "Todo produto do catálogo deve informar 'disponivel'");
            assertEquals(produto.get("estoque").getAsInt() > 0, produto.get("disponivel").getAsBoolean());
        }
    }
}