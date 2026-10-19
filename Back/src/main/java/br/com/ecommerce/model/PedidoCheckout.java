package br.com.ecommerce.model;

import java.math.BigDecimal;

public class PedidoCheckout {

    private final int idPedido;
    private final BigDecimal total;
    private final String status;

    public PedidoCheckout(int idPedido, BigDecimal total, String status) {
        this.idPedido = idPedido;
        this.total = total;
        this.status = status;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }
}
