package br.com.ecommerce.integration;

import br.com.ecommerce.controller.CheckoutController;
import br.com.ecommerce.model.ItemCheckout;
import br.com.ecommerce.model.PedidoCheckout;
import br.com.ecommerce.repository.CheckoutRepository;
import br.com.ecommerce.service.CheckoutService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckoutControllerIntegrationTest {

    @Test
    void checkoutHttpDevePersistirPedidoEDevolverStatusConcluido() throws Exception {
        CheckoutRepository repository = new CheckoutRepository() {
            @Override
            public PedidoCheckout finalizarPedido(
                    int usuarioId,
                    List<ItemCheckout> itens,
                    String nomeDestinatario,
                    String enderecoEntrega,
                    String metodoPagamento) {
                assertEquals(7, usuarioId);
                assertEquals(List.of(new ItemCheckout(3, 2)), itens);
                assertEquals("Cliente Teste", nomeDestinatario);
                assertEquals("Rua A, 10", enderecoEntrega);
                assertEquals("PIX", metodoPagamento);
                return new PedidoCheckout(91, new BigDecimal("25.00"), "CONCLUÍDO");
            }
        };
        CheckoutController controller = new CheckoutController(new CheckoutService(repository));
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/checkout", exchange -> {
            try {
                controller.finalizar(exchange, 7);
            } catch (Exception e) {
                throw new java.io.IOException(e);
            }
        });
        server.start();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/checkout"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("""
                            {
                              "nome": "Cliente Teste",
                              "endereco": "Rua A, 10",
                              "metodoPagamento": "PIX",
                              "itens": [
                                {"produtoId": 3, "quantidade": 1},
                                {"produtoId": 3, "quantidade": 1}
                              ]
                            }
                            """))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(201, response.statusCode());
            assertTrue(response.body().contains("\"sucesso\": true"));
            assertTrue(response.body().contains("\"status\": \"CONCLUÍDO\""));
            assertTrue(response.body().contains("\"idPedido\": 91"));
            assertTrue(response.body().contains("\"total\": 25.00"));
        } finally {
            server.stop(0);
        }
    }
}
