package br.com.ecommerce.service;

import br.com.ecommerce.model.Produto;
import br.com.ecommerce.repository.ProdutoRepository;
import java.math.BigDecimal;

public class ProdutoService {
    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) { this.repository = repository; }

    public Produto cadastrar(String nome, BigDecimal preco, BigDecimal estoque) {
        throw new UnsupportedOperationException("Ainda nao implementado");
    }
}