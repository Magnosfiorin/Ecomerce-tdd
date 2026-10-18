package br.com.ecommerce.model;

import java.math.BigDecimal;

/**
 * Entidade Produto (tabela "produtos").
 * Nome, preco e estoque sao imutaveis; apenas o id e atribuido depois da persistencia.
 */
public class Produto {

    private Integer id;
    private final String nome;
    private final BigDecimal preco;
    private final int estoque;

    public Produto(String nome, BigDecimal preco, int estoque) {
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public int getEstoque() {
        return estoque;
    }

    @Override
    public String toString() {
        return "Produto{id=" + id + ", nome='" + nome + "', preco=" + preco + ", estoque=" + estoque + "}";
    }
}