package br.com.ecommerce.unit;

import br.com.ecommerce.model.ItemCheckout;
import br.com.ecommerce.model.PedidoCheckout;
import br.com.ecommerce.repository.CheckoutRepository;
import br.com.ecommerce.service.CheckoutException;
import br.com.ecommerce.service.CheckoutService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutServiceTest {

    @Test
    void deveRetornarPedidoConcluidoAposConfirmacaoDoRepositorio() {
        CheckoutRepository repository = mock(CheckoutRepository.class);
        CheckoutService service = new CheckoutService(repository);
        PedidoCheckout pedido = new PedidoCheckout(42, new BigDecimal("25.00"), "CONCLUÍDO");
        when(repository.finalizarPedido(
                7,
                List.of(new ItemCheckout(3, 2)),
                "Cliente Teste",
                "Rua A, 10",
                "PIX")).thenReturn(pedido);

        PedidoCheckout resultado = service.finalizar(
                7,
                List.of(new ItemCheckout(3, 1), new ItemCheckout(3, 1)),
                " Cliente Teste ",
                " Rua A, 10 ",
                "PIX");

        assertEquals(42, resultado.getIdPedido());
        assertEquals("CONCLUÍDO", resultado.getStatus());
        verify(repository).finalizarPedido(
                7,
                List.of(new ItemCheckout(3, 2)),
                "Cliente Teste",
                "Rua A, 10",
                "PIX");
    }

    @Test
    void naoDeveConfirmarPedidoQuandoClienteNaoEstaAutenticado() {
        CheckoutRepository repository = mock(CheckoutRepository.class);
        CheckoutService service = new CheckoutService(repository);

        assertThrows(CheckoutException.class, () -> service.finalizar(
                null,
                List.of(new ItemCheckout(3, 1)),
                "Cliente Teste",
                "Rua A, 10",
                "PIX"));

        verify(repository, never()).finalizarPedido(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }
}
