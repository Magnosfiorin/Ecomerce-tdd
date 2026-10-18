package br.com.ecommerce.repository;

import br.com.ecommerce.model.Produto;

public interface ProdutoRepository {
    Produto salvar(Produto produto);
}