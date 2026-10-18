package br.com.ecommerce.model;

/**
 * Entidade de Produto vendido na loja.
 * Um produto "inativo" nao aparece no catalogo do cliente.
 */
public class Produto {

    private Integer id;
    private String nome;
    private double preco;
    private int estoque;
    private boolean ativo = true;

    public Produto() {
    }

    public Produto(Integer id, String nome, double preco, int estoque, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
        this.ativo = ativo;
    }

    /** Disponivel = tem pelo menos 1 unidade em estoque. */
    public boolean isDisponivel() {
        return estoque > 0;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}