package br.com.ecommerce.service;

import br.com.ecommerce.model.ItemCheckout;
import br.com.ecommerce.model.Produto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servico de Carrinho (UC04): valida se os itens do carrinho cabem no estoque atual.
 * Usa o CatalogoService para consultar os produtos ativos (banco ou memoria nos testes).
 */
public class CarrinhoService {

    private final CatalogoService catalogo;

    public CarrinhoService(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    /** Lanca CarrinhoException se algum item nao puder ser atendido pelo estoque. */
    public void validarItens(List<ItemCheckout> itens) {
        if (itens == null || itens.isEmpty()) {
            return;
        }

        Map<Integer, Integer> totais = new HashMap<>();
        for (ItemCheckout item : itens) {
            if (item == null || item.getProdutoId() == null || item.getProdutoId() <= 0) {
                throw new CarrinhoException("Identificação de produto inválida.");
            }
            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                throw new CarrinhoException("A quantidade de cada produto deve ser maior que zero.");
            }
            totais.merge(item.getProdutoId(), item.getQuantidade(), Integer::sum);
        }

        Map<Integer, Produto> ativos = catalogo.listarAtivos().stream()
                .collect(Collectors.toMap(Produto::getId, p -> p));

        for (Map.Entry<Integer, Integer> entrada : totais.entrySet()) {
            Produto produto = ativos.get(entrada.getKey());
            if (produto == null) {
                throw new CarrinhoException("O produto " + entrada.getKey() + " não está mais disponível.");
            }
            if (!produto.isDisponivel()) {
                throw new CarrinhoException("O produto \"" + produto.getNome() + "\" está esgotado.");
            }
            if (entrada.getValue() > produto.getEstoque()) {
                throw new CarrinhoException("O produto \"" + produto.getNome() + "\" possui apenas "
                        + produto.getEstoque() + " unidade(s) em estoque.");
            }
        }
    }
}
