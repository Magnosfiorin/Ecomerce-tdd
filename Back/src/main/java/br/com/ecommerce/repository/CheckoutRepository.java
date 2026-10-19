package br.com.ecommerce.repository;

import br.com.ecommerce.model.ItemCheckout;
import br.com.ecommerce.model.PedidoCheckout;

import java.util.List;

public interface CheckoutRepository {
    PedidoCheckout finalizarPedido(
            int usuarioId,
            List<ItemCheckout> itens,
            String nomeDestinatario,
            String enderecoEntrega,
            String metodoPagamento);
}
