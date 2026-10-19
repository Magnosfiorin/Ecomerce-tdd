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

    @Test
    @DisplayName("Integração HTTP: Sucesso no cadastro de cliente, login e identificação do perfil")
    void deveCadastrarELogarClienteComSucessoViaHttp() throws Exception {
        String emailUnico = "cliente_http_" + System.currentTimeMillis() + "@teste.com";
        String cadastroJson = """
            {
                "nome": "Rodrigo",
                "sobrenome": "Moraes",
                "email": "%s",
                "senha": "senhaSegura123",
                "cpf": "123.456.789-01",
                "cep": "01001-000",
                "endereco": "Av Paulista",
                "numero": "1000",
                "bairro": "Bela Vista",
                "cidade": "São Paulo",
                "estado": "SP",
                "pais": "Brasil",
                "tipoUsuario": 1
            }
        """.formatted(emailUnico);

        // 1. Cadastra o Cliente
        HttpRequest postCadastro = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/cadastro/cliente"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cadastroJson))
                .build();

        HttpResponse<String> respCadastro = httpClient.send(postCadastro, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, respCadastro.statusCode());
        assertTrue(respCadastro.body().contains("Cliente cadastrado com sucesso!"));
        assertFalse(respCadastro.body().contains("\"senha\""));
        assertFalse(respCadastro.body().contains("senhaSegura123"));

        // 2. Realiza Login com credenciais válidas e verifica identificação do perfil (tipo 1)
        String loginJson = """
            {
                "email": "%s",
                "senha": "senhaSegura123"
            }
        """.formatted(emailUnico);

        HttpRequest postLogin = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .build();

        HttpResponse<String> respLogin = httpClient.send(postLogin, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respLogin.statusCode());
        assertTrue(respLogin.body().contains("\"sucesso\": true"));
        assertTrue(respLogin.body().contains("\"tipoUsuario\": 1"));
        assertTrue(respLogin.body().contains("\"token\":"));
        assertTrue(respLogin.body().contains("\"nome\": \"Rodrigo\""));
        assertFalse(respLogin.body().contains("\"senha\""));
        assertFalse(respLogin.body().contains("senhaSegura123"));
    }

    @Test
    @DisplayName("Integração HTTP: Retornar 401 Unauthorized para credenciais incorretas")
    void deveRetornar401ParaCredenciaisIncorretasViaHttp() throws Exception {
        String loginIncorreto = """
            {
                "email": "usuario.inexistente@dominio.com",
                "senha": "senhaErrada123"
            }
        """;

        HttpRequest postLogin = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginIncorreto))
                .build();

        HttpResponse<String> response = httpClient.send(postLogin, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("E-mail ou senha incorretos."));
    }

    @Test
    @DisplayName("Checkout HTTP: rejeitar requisição sem token de sessão")
    void deveRejeitarCheckoutSemAutenticacaoViaHttp() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/checkout"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                          "nome": "Cliente",
                          "endereco": "Rua A, 10",
                          "metodoPagamento": "PIX",
                          "itens": [{"produtoId": 1, "quantidade": 1}]
                        }
                        """))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("Sessão inválida ou expirada"));
    }

    @Test
    @DisplayName("Integração HTTP (Baseado em Requisitos): Rejeição de login caso e-mail seja inválido ou senha < 4 caracteres")
    void deveRejeitarLoginComEmailInvalidoOuSenhaCurtaViaHttp() throws Exception {
        // Cenário 1: E-mail em formato inválido
        String emailInvalidoJson = """
            {
                "email": "email_sem_arroba.com",
                "senha": "senhaValida123"
            }
        """;

        HttpRequest reqEmailInvalido = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(emailInvalidoJson))
                .build();

        HttpResponse<String> respEmail = httpClient.send(reqEmailInvalido, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, respEmail.statusCode());
        assertTrue(respEmail.body().contains("E-mail em formato inválido"));

        // Cenário 2: Senha com menos de 4 caracteres
        String senhaCurtaJson = """
            {
                "email": "cliente.valido@dominio.com",
                "senha": "123"
            }
        """;

        HttpRequest reqSenhaCurta = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(senhaCurtaJson))
                .build();

        HttpResponse<String> respSenha = httpClient.send(reqSenhaCurta, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, respSenha.statusCode());
        assertTrue(respSenha.body().contains("no mínimo 4 caracteres"));
    }
}
