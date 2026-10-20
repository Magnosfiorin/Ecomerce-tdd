package br.com.ecommerce.model;

import java.util.Objects;

public class ItemCheckout {

    private Integer produtoId;
    private Integer quantidade;

    public ItemCheckout() {}

    public ItemCheckout(Integer produtoId, Integer quantidade) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
    }

    public Integer getProdutoId() {
        return produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ItemCheckout item)) {
            return false;
        }
        return Objects.equals(produtoId, item.produtoId)
                && Objects.equals(quantidade, item.quantidade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(produtoId, quantidade);
    }
}
