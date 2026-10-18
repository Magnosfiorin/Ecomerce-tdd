package br.com.ecommerce.model;

import java.math.BigDecimal;

public class Produto {
    private Integer id;
    private String nome;
    private BigDecimal preco;
    private int estoque;

    public Produto(String nome, BigDecimal preco, int estoque) {
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public BigDecimal getPreco() { return preco; }
    public int getEstoque() { return estoque; }
}