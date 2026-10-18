package br.com.ecommerce.unit;

import br.com.ecommerce.controller.ProdutoController;
import br.com.ecommerce.service.ProdutoService;
import br.com.ecommerce.unit.fake.ProdutoRepositoryEmMemoria;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProdutoControllerTest {

    @Test
    void deveCadastrarProdutoRecebidoEmJson() throws Exception {
        ProdutoRepositoryEmMemoria repository = new ProdutoRepositoryEmMemoria();
        ProdutoController controller = new ProdutoController(new ProdutoService(repository));
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/produtos", exchange -> {
            try {
                controller.cadastrar(exchange);
            } catch (Exception e) {
                throw new IOException(e);
            }
        });
        server.start();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/produtos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{\"nome\":\"Teclado\",\"preco\":199.90,\"estoque\":10}"))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(201, response.statusCode());
            assertEquals(1, repository.listar().size());
            assertTrue(response.body().contains("\"nome\": \"Teclado\""));
        } finally {
            server.stop(0);
        }
    }
}
