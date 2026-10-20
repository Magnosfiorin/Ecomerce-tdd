package br.com.ecommerce.controller;

import br.com.ecommerce.config.Router;
import br.com.ecommerce.model.ItemCheckout;
import br.com.ecommerce.model.PedidoCheckout;
import br.com.ecommerce.service.CheckoutException;
import br.com.ecommerce.service.CheckoutService;
import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutController {

    private final CheckoutService service;

    public CheckoutController(CheckoutService service) {
        this.service = service;
    }

    public void finalizar(HttpExchange exchange, int usuarioId) throws Exception {
        CheckoutRequest request = Router.readJsonBody(exchange, CheckoutRequest.class);
        if (request == null) {
            Router.sendError(exchange, 400, "Corpo da requisição inválido.");
            return;
        }

        try {
            PedidoCheckout pedido = service.finalizar(
                    usuarioId,
                    request.itens,
                    request.nome,
                    request.endereco,
                    request.metodoPagamento);

            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("idPedido", pedido.getIdPedido());
            response.put("status", pedido.getStatus());
            response.put("total", pedido.getTotal());
            response.put("mensagem", "Pedido processado com sucesso.");
            Router.sendJsonResponse(exchange, 201, response);
        } catch (CheckoutException e) {
            Router.sendError(exchange, 400, e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("[CheckoutController] Falha ao persistir pedido: " + e.getMessage());
            Router.sendError(exchange, 500, "Não foi possível finalizar o pedido.");
        }
    }

    private static class CheckoutRequest {
        private List<ItemCheckout> itens;
        private String nome;
        private String endereco;
        private String metodoPagamento;
    }
}
