package br.com.ecommerce.service;

import br.com.ecommerce.model.Produto;
import br.com.ecommerce.repository.ProdutoRepository;
import java.math.BigDecimal;

public class ProdutoService {
    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public Produto cadastrar(String nome, BigDecimal preco, BigDecimal estoque) {
        if (nome == null || nome.isBlank())
            throw new ProdutoInvalidoException("O nome do produto e obrigatorio.");
        if (preco == null || preco.signum() <= 0)
            throw new ProdutoInvalidoException("O preco deve ser maior que zero.");
        if (estoque == null || estoque.signum() < 0 || estoque.stripTrailingZeros().scale() > 0)
            throw new ProdutoInvalidoException("O estoque deve ser um numero inteiro maior ou igual a zero.");

        return repository.salvar(new Produto(nome.trim(), preco, estoque.intValueExact()));
    }
}