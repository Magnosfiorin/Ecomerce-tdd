package br.com.ecommerce.unit;

import br.com.ecommerce.model.Loja;
import br.com.ecommerce.model.Usuario;
import br.com.ecommerce.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - Serviço de Usuário e Lojas (Dev 1)")
public class UsuarioServiceTest {

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        // Inicializa sem banco (usa repositório mock/em memória para testes unitários isolados)
        service = new UsuarioService(null);
    }

    @Test
    @DisplayName("Deve cadastrar cliente com tipo 1 e dados válidos")
    void deveCadastrarClienteComSucesso() {
        Usuario cliente = new Usuario("Lucas", "Ferreira", "lucas@email.com", "senha123", "Rua A, 100", "12345678900", 1);
        Usuario salvo = service.cadastrarCliente(cliente);

        assertNotNull(salvo.getId());
        assertEquals(Usuario.TIPO_CLIENTE, salvo.getTipoUsuario());
        assertEquals("Lucas", salvo.getNome());
    }

    @Test
    @DisplayName("Deve rejeitar cadastro com e-mail inválido pelo regex")
    void deveRejeitarCadastroComEmailInvalido() {
        Usuario clienteInvalido = new Usuario("Lucas", "Ferreira", "email-invalido", "senha123", "Rua A", "123", 1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.cadastrarCliente(clienteInvalido);
        });

        assertTrue(ex.getMessage().contains("E-mail inválido"));
    }

    @Test
    @DisplayName("Deve rejeitar cadastro com senha menor que 4 caracteres")
    void deveRejeitarSenhaCurta() {
        Usuario clienteSenhaCurta = new Usuario("Lucas", "Ferreira", "lucas@email.com", "123", "Rua A", "123", 1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.cadastrarCliente(clienteSenhaCurta);
        });

        assertTrue(ex.getMessage().contains("no mínimo 4 caracteres"));
    }

    @Test
    @DisplayName("Deve cadastrar vendedor com tipo 0 e associar loja")
    void deveCadastrarVendedorComLoja() {
        Usuario vendedor = new Usuario("Ana", "Pereira", "ana@loja.com", "senha123", "Rua B, 200", "98765432100", 0);
        Loja loja = new Loja(null, "EletroAna", "contato@eletroana.com", "Av Central, 50", "12.345.678/0001-90");

        Usuario vendedorSalvo = service.cadastrarVendedorComLoja(vendedor, loja);

        assertNotNull(vendedorSalvo.getId());
        assertEquals(Usuario.TIPO_VENDEDOR, vendedorSalvo.getTipoUsuario());

        Loja lojaBuscada = service.buscarLojaPorUsuario(vendedorSalvo.getId());
        assertNotNull(lojaBuscada);
        assertEquals("EletroAna", lojaBuscada.getNomeLoja());
        assertEquals(vendedorSalvo.getId(), lojaBuscada.getUsuarioId());
    }

    @Test
    @DisplayName("Deve permitir a um cliente criar uma loja e ser promovido a tipo 2 (Vendedor/Cliente)")
    void devePromoverClienteParaVendedorClienteAoCriarLoja() {
        Usuario cliente = new Usuario("Marcos", "Rocha", "marcos@email.com", "senha123", "Rua C", "333", 1);
        Usuario salvo = service.cadastrarCliente(cliente);
        assertEquals(Usuario.TIPO_CLIENTE, salvo.getTipoUsuario());

        Loja novaLoja = new Loja(null, "Informática Rocha", "contato@rocha.com", "Rua Comercial, 10", null);
        Loja lojaCriada = service.criarLojaParaCliente(salvo.getId(), novaLoja);

        assertNotNull(lojaCriada.getId());
        assertEquals(salvo.getId(), lojaCriada.getUsuarioId());

        Usuario autenticado = service.autenticar("marcos@email.com", "senha123");
        assertEquals(Usuario.TIPO_VENDEDOR_CLIENTE, autenticado.getTipoUsuario());
        assertTrue(autenticado.isVendedor());
        assertTrue(autenticado.isCliente());
    }

    @Test
    @DisplayName("Deve cadastrar cliente com endereço completo detalhado")
    void deveCadastrarClienteComEnderecoCompleto() {
        Usuario cliente = new Usuario(
                "Fernanda", "Costa", "fernanda@teste.com", "senha123", "11122233344",
                "01001-000", "Praça da Sé", "100", "Sé", "São Paulo", "SP", "Brasil", 1
        );

        Usuario salvo = service.cadastrarCliente(cliente);

        assertEquals("01001-000", salvo.getCep());
        assertEquals("Praça da Sé", salvo.getEndereco());
        assertEquals("100", salvo.getNumero());
        assertEquals("Sé", salvo.getBairro());
        assertEquals("São Paulo", salvo.getCidade());
        assertEquals("SP", salvo.getEstado());
        assertEquals("Brasil", salvo.getPais());
    }
}
