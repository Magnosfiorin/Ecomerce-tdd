package br.com.ecommerce.integration;

import br.com.ecommerce.EcommerceApplication;
import br.com.ecommerce.config.DatabaseConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Integração - Inicialização do Backend e Health Check")
public class EcommerceApplicationTest {

    private EcommerceApplication app;
    private int port;
    private HttpClient httpClient;

    @BeforeEach
    void setUp() throws IOException {
        // Usa porta 0 para alocação dinâmica pelo SO e evitar conflito de portas
        DatabaseConfig dbConfig = new DatabaseConfig();
        app = new EcommerceApplication(0, dbConfig);
        app.start();
        port = app.getPort();
        httpClient = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        if (app != null) {
            app.stop();
        }
    }

    @Test
    @DisplayName("Deve inicializar o servidor e responder 200 OK na rota /api/health com CORS")
    void deveResponder200NoHealthCheck() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/health"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\": \"UP\""));
        assertTrue(response.headers().firstValue("Access-Control-Allow-Origin").isPresent());
        assertEquals("*", response.headers().firstValue("Access-Control-Allow-Origin").get());
    }

    @Test
    @DisplayName("Deve retornar 404 quando requisitar uma rota inexistente")
    void deveRetornar404ParaRotaInexistente() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/rota-desconhecida"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Rota não encontrada"));
    }
}
