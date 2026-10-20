package br.com.ecommerce.controller;

import br.com.ecommerce.config.Router;
import br.com.ecommerce.model.Produto;
import br.com.ecommerce.service.ProdutoInvalidoException;
import br.com.ecommerce.service.ProdutoService;
import com.sun.net.httpserver.HttpExchange;
import java.math.BigDecimal;

public class ProdutoController {

    static class ProdutoRequest {
        String nome;
        BigDecimal preco;
        BigDecimal estoque;
    }

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) { this.service = service; }

    public void cadastrar(HttpExchange exchange) throws Exception {
        cadastrar(exchange, null);
    }

    public void cadastrar(HttpExchange exchange, Integer usuarioId) throws Exception {
        ProdutoRequest req = Router.readJsonBody(exchange, ProdutoRequest.class);
        if (req == null) {
            Router.sendError(exchange, 400, "Corpo da requisicao invalido ou ausente.");
            return;
        }
        try {
            Produto p = service.cadastrar(req.nome, req.preco, req.estoque, usuarioId);
            Router.sendJsonResponse(exchange, 201, p);
        } catch (ProdutoInvalidoException e) {
            Router.sendError(exchange, 400, e.getMessage());
        }
    }

    public void excluir(HttpExchange exchange, int produtoId, int usuarioId) throws Exception {
        try {
            if (!service.excluir(produtoId, usuarioId)) {
                Router.sendError(exchange, 404, "Produto não encontrado nesta loja.");
                return;
            }
            Router.sendJsonResponse(exchange, 200, java.util.Map.of(
                    "sucesso", true, "mensagem", "Produto excluído do catálogo."));
        } catch (ProdutoInvalidoException e) {
            Router.sendError(exchange, 400, e.getMessage());
        }
    }
}