package br.com.ecommerce.service;

import br.com.ecommerce.model.ItemCheckout;
import br.com.ecommerce.model.PedidoCheckout;
import br.com.ecommerce.repository.CheckoutRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CheckoutService {

    private final CheckoutRepository repository;

    public CheckoutService(CheckoutRepository repository) {
        this.repository = repository;
    }

    public PedidoCheckout finalizar(
            Integer usuarioId,
            List<ItemCheckout> itens,
            String nomeDestinatario,
            String enderecoEntrega,
            String metodoPagamento) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new CheckoutException("É necessário estar autenticado para finalizar a compra.");
        }
        if (itens == null || itens.isEmpty()) {
            throw new CheckoutException("O carrinho está vazio.");
        }
        if (nomeDestinatario == null || nomeDestinatario.isBlank() || nomeDestinatario.trim().length() > 200) {
            throw new CheckoutException("Informe um nome de destinatário com até 200 caracteres.");
        }
        if (enderecoEntrega == null || enderecoEntrega.isBlank() || enderecoEntrega.trim().length() > 300) {
            throw new CheckoutException("Informe um endereço de entrega com até 300 caracteres.");
        }
        if (metodoPagamento == null || !List.of("PIX", "CARTAO", "BOLETO").contains(metodoPagamento)) {
            throw new CheckoutException("Forma de pagamento inválida.");
        }

        Map<Integer, Integer> quantidadesPorProduto = new TreeMap<>();
        for (ItemCheckout item : itens) {
            if (item == null || item.getProdutoId() == null || item.getProdutoId() <= 0) {
                throw new CheckoutException("Identificação de produto inválida.");
            }
            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                throw new CheckoutException("A quantidade de cada produto deve ser maior que zero.");
            }
            try {
                quantidadesPorProduto.merge(item.getProdutoId(), item.getQuantidade(), Math::addExact);
            } catch (ArithmeticException e) {
                throw new CheckoutException("Quantidade de produto excede o limite permitido.");
            }
        }

        List<ItemCheckout> itensConsolidados = new ArrayList<>();
        quantidadesPorProduto.forEach((produtoId, quantidade) ->
                itensConsolidados.add(new ItemCheckout(produtoId, quantidade)));

        return repository.finalizarPedido(
                usuarioId,
                itensConsolidados,
                nomeDestinatario.trim(),
                enderecoEntrega.trim(),
                metodoPagamento);
    }
}
