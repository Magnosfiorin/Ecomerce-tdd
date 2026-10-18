package br.com.ecommerce.unit;

import br.com.ecommerce.model.Produto;
import br.com.ecommerce.service.ProdutoInvalidoException;
import br.com.ecommerce.service.ProdutoService;
import br.com.ecommerce.unit.fake.ProdutoRepositoryEmMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UC02 - Gestao de Produtos (Servico de Cadastro)")
public class ProdutoServiceTest {

    private ProdutoRepositoryEmMemoria repository;
    private ProdutoService service;

    @BeforeEach
    void setUp() {
        repository = new ProdutoRepositoryEmMemoria();
        service = new ProdutoService(repository);
    }

    @Test
    @DisplayName("Deve cadastrar um produto valido")
    void deveCadastrarProdutoValido() {
        Produto p = service.cadastrar("Teclado", new BigDecimal("199.90"), new BigDecimal("10"));

        assertNotNull(p.getId());
        assertEquals("Teclado", p.getNome());
        assertEquals(new BigDecimal("199.90"), p.getPreco());
        assertEquals(10, p.getEstoque());
        assertEquals(1, repository.listar().size());
    }

    @Test
    @DisplayName("A1 - Rejeita preco <= 0")
    void deveRejeitarPrecoMenorOuIgualAZero() {
        assertThrows(ProdutoInvalidoException.class,
                () -> service.cadastrar("Mouse", BigDecimal.ZERO, new BigDecimal("5")));
        assertThrows(ProdutoInvalidoException.class,
                () -> service.cadastrar("Mouse", new BigDecimal("-10"), new BigDecimal("5")));
        assertTrue(repository.listar().isEmpty());
    }

    @Test
    @DisplayName("A2 - Teste de limite: estoque inteiro >= 0")
    void estoqueDeveSerInteiroMaiorOuIgualAZero() {
        BigDecimal preco = new BigDecimal("50.00");

        assertEquals(0, service.cadastrar("Brinde", preco, BigDecimal.ZERO).getEstoque()); // limite valido
        assertThrows(ProdutoInvalidoException.class,
                () -> service.cadastrar("Brinde", preco, new BigDecimal("-1")));          // abaixo do limite
        assertThrows(ProdutoInvalidoException.class,
                () -> service.cadastrar("Brinde", preco, new BigDecimal("2.5")));         // fracionario

        assertEquals(1, repository.listar().size());
    }
}