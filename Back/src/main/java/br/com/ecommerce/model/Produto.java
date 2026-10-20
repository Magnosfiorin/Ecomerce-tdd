package br.com.ecommerce.model;

import java.math.BigDecimal;

/**
 * Entidade Produto (tabela "produtos").
 * Nome, preco e estoque sao imutaveis; apenas o id e atribuido depois da persistencia.
 * Um produto "inativo" nao aparece no catalogo do cliente.
 */
public class Produto {

    private Integer id;
    private final String nome;
    private final BigDecimal preco;
    private final int estoque;
    private final boolean ativo;
    private final boolean disponivel;

    /** Construtor do cadastro (Dev 2): produto nasce ativo. */
    public Produto(String nome, BigDecimal preco, int estoque) {
        this(nome, preco, estoque, true);
    }

    /** Construtor completo (usado pelo catalogo ao ler do banco). */
    public Produto(String nome, BigDecimal preco, int estoque, boolean ativo) {
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
        this.ativo = ativo;
        this.disponivel = estoque > 0;
    }

    /** Compatibilidade com o codigo do catalogo (preco em double). */
    public Produto(Integer id, String nome, double preco, int estoque, boolean ativo) {
        this(nome, BigDecimal.valueOf(preco), estoque, ativo);
        this.id = id;
    }

    /** Disponivel = tem pelo menos 1 unidade em estoque. Tambem e enviado no JSON para a interface. */
    public boolean isDisponivel() {
        return disponivel;
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

    public boolean isAtivo() {
        return ativo;
    }

    @Override
    public String toString() {
        return "Produto{id=" + id + ", nome='" + nome + "', preco=" + preco
                + ", estoque=" + estoque + ", ativo=" + ativo + "}";
    }
}