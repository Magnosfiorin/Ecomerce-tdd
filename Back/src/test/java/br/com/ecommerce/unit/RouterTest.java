package br.com.ecommerce.unit;

import br.com.ecommerce.config.DatabaseConfig;
import br.com.ecommerce.config.Router;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - Centralização de Rotas (Router)")
public class RouterTest {

    private Router router;

    @BeforeEach
    void setUp() {
        DatabaseConfig databaseConfig = new DatabaseConfig();
        router = new Router(databaseConfig);
    }

    @Test
    @DisplayName("Deve inicializar com as rotas padrão dos 5 módulos registradas")
    void deveInicializarComRotasPadraoRegistradas() {
        var rotas = router.getRoutes();

        assertTrue(rotas.containsKey("GET:/api/health"), "Rota de Health Check deve estar registrada");
        assertTrue(rotas.containsKey("POST:/api/auth/login"), "Rota de Login (Dev 1) deve estar registrada");
        assertTrue(rotas.containsKey("POST:/api/produtos"), "Rota de Cadastro (Dev 2) deve estar registrada");
        assertTrue(rotas.containsKey("GET:/api/produtos"), "Rota de Catálogo (Dev 3) deve estar registrada");
        assertTrue(rotas.containsKey("POST:/api/carrinho"), "Rota de Carrinho (Dev 4) deve estar registrada");
        assertTrue(rotas.containsKey("POST:/api/checkout"), "Rota de Checkout (Dev 5) deve estar registrada");
    }

    @Test
    @DisplayName("Deve permitir registrar uma nova rota dinamicamente")
    void devePermitirRegistrarNovaRota() {
        router.register("GET", "/api/teste", exchange -> {});

        assertTrue(router.getRoutes().containsKey("GET:/api/teste"));
    }

    @Test
    @DisplayName("Deve normalizar caminhos com barra extra no final")
    void deveNormalizarCaminhosComBarraNoFinal() {
        router.register("GET", "/api/itens/", exchange -> {});

        assertTrue(router.getRoutes().containsKey("GET:/api/itens"));
    }
}
