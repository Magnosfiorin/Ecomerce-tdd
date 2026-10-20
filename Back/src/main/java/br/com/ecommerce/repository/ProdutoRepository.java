package br.com.ecommerce.repository;

import br.com.ecommerce.model.Produto;

public interface ProdutoRepository {
    Produto salvar(Produto produto);

    default Produto salvar(Produto produto, Integer usuarioId) {
        return salvar(produto);
    }

    default boolean desativar(int produtoId, int usuarioId) {
        return false;
    }
}