package br.com.ecommerce.service;

import br.com.ecommerce.model.Produto;
import br.com.ecommerce.repository.ProdutoRepository;

import java.math.BigDecimal;

/**
 * UC02 - Cadastro de produtos.
 * Regras: preco estritamente positivo (> 0) e estoque inteiro maior ou igual a zero (>= 0).
 */
public class ProdutoService {

    static final String MSG_NOME_OBRIGATORIO = "O nome do produto e obrigatorio.";
    static final String MSG_PRECO_INVALIDO = "O preco deve ser maior que zero.";
    static final String MSG_ESTOQUE_INVALIDO = "O estoque deve ser um numero inteiro maior ou igual a zero.";

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public Produto cadastrar(String nome, BigDecimal preco, BigDecimal estoque) {
        return cadastrar(nome, preco, estoque, null);
    }

    public Produto cadastrar(String nome, BigDecimal preco, BigDecimal estoque, Integer usuarioId) {
        validarNome(nome);
        validarPreco(preco);
        validarEstoque(estoque);

        Produto produto = new Produto(nome.trim(), preco, estoque.intValueExact());
        return repository.salvar(produto, usuarioId);
    }

    public boolean excluir(int produtoId, int usuarioId) {
        if (produtoId <= 0 || usuarioId <= 0) {
            throw new ProdutoInvalidoException("Identificação inválida.");
        }
        return repository.desativar(produtoId, usuarioId);
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ProdutoInvalidoException(MSG_NOME_OBRIGATORIO);
        }
    }

    private void validarPreco(BigDecimal preco) {
        if (preco == null || preco.signum() <= 0) {
            throw new ProdutoInvalidoException(MSG_PRECO_INVALIDO);
        }
    }

    private void validarEstoque(BigDecimal estoque) {
        boolean negativo = estoque == null || estoque.signum() < 0;
        // 2.0 e aceito como 2; 2.5 e fracionario e invalido
        boolean fracionario = !negativo && estoque.stripTrailingZeros().scale() > 0;
        if (negativo || fracionario) {
            throw new ProdutoInvalidoException(MSG_ESTOQUE_INVALIDO);
        }
    }
}