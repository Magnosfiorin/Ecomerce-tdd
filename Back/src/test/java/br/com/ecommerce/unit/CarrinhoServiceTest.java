package br.com.ecommerce.unit;

import br.com.ecommerce.model.ItemCheckout;
import br.com.ecommerce.model.Produto;
import br.com.ecommerce.service.CarrinhoException;
import br.com.ecommerce.service.CarrinhoService;
import br.com.ecommerce.service.CatalogoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - Validação de estoque do Carrinho (Dev 4)")
class CarrinhoServiceTest {

    private CarrinhoService carrinho;

    @BeforeEach
    void setUp() {
        CatalogoService catalogo = new CatalogoService(null, List.of(
                new Produto(1, "Mouse", 150.00, 3, true),
                new Produto(2, "Headset", 280.00, 0, true),
                new Produto(3, "Tênis Antigo", 199.00, 5, false)));
        carrinho = new CarrinhoService(catalogo);
    }

    @Test
    @DisplayName("Aceita quantidade dentro do estoque")
    void deveAceitarQuantidadeDentroDoEstoque() {
        assertDoesNotThrow(() -> carrinho.validarItens(List.of(new ItemCheckout(1, 3))));
    }

    @Test
    @DisplayName("Rejeita quantidade acima do estoque")
    void deveRejeitarQuantidadeAcimaDoEstoque() {
        CarrinhoException e = assertThrows(CarrinhoException.class,
                () -> carrinho.validarItens(List.of(new ItemCheckout(1, 4))));
        assertTrue(e.getMessage().contains("3"));
    }

    @Test
    @DisplayName("Rejeita produto esgotado")
    void deveRejeitarProdutoEsgotado() {
        assertThrows(CarrinhoException.class,
                () -> carrinho.validarItens(List.of(new ItemCheckout(2, 1))));
    }

    @Test
    @DisplayName("Soma linhas repetidas do mesmo produto")
    void deveSomarLinhasDoMesmoProduto() {
        assertThrows(CarrinhoException.class,
                () -> carrinho.validarItens(List.of(new ItemCheckout(1, 2), new ItemCheckout(1, 2))));
    }

    @Test
    @DisplayName("Rejeita produto inativo")
    void deveRejeitarProdutoInativo() {
        assertThrows(CarrinhoException.class,
                () -> carrinho.validarItens(List.of(new ItemCheckout(3, 1))));
    }
}
