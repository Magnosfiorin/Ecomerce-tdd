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

    // =========================================================================
    // 2x Testes Unitários (JUnit) Requeridos:
    // 1. Sucesso na validação de credenciais e identificação do perfil
    // 2. Tratamento de falha para credenciais incorretas
    // =========================================================================

    @Test
    @DisplayName("Teste Unitário 1: Sucesso na validação de credenciais e identificação do perfil")
    void deveValidarCredenciaisComSucessoEIdentificarPerfil() {
        // Cenário 1: Perfil Cliente (Tipo 1)
        Usuario cliente = new Usuario("Carlos", "Silva", "cliente.teste@exemplo.com", "senha123", "Rua A, 10", "11122233344", Usuario.TIPO_CLIENTE);
        service.cadastrarCliente(cliente);

        Usuario authCliente = service.autenticar("cliente.teste@exemplo.com", "senha123");
        assertNotNull(authCliente, "Cliente deve ser autenticado com sucesso");
        assertEquals("Carlos", authCliente.getNome());
        assertEquals("Silva", authCliente.getSobrenome());
        assertEquals(Usuario.TIPO_CLIENTE, authCliente.getTipoUsuario(), "Perfil deve ser identificado como TIPO_CLIENTE (1)");
        assertTrue(authCliente.isCliente(), "Deve possuir privilégios de Cliente");
        assertFalse(authCliente.isVendedor(), "Não deve possuir privilégios de Vendedor");

        // Cenário 2: Perfil Vendedor (Tipo 0)
        Usuario vendedor = new Usuario("Beatriz", "Lopes", "vendedora.loja@exemplo.com", "senha456", "Rua B, 20", "22233344455", Usuario.TIPO_VENDEDOR);
        Loja loja = new Loja(null, "Modas Bia", "contato@modasbia.com", "Rua B, 20", "12.345.678/0001-90");
        service.cadastrarVendedorComLoja(vendedor, loja);

        Usuario authVendedor = service.autenticar("vendedora.loja@exemplo.com", "senha456");
        assertNotNull(authVendedor, "Vendedor deve ser autenticado com sucesso");
        assertEquals("Beatriz", authVendedor.getNome());
        assertEquals(Usuario.TIPO_VENDEDOR, authVendedor.getTipoUsuario(), "Perfil deve ser identificado como TIPO_VENDEDOR (0)");
        assertTrue(authVendedor.isVendedor(), "Deve possuir privilégios de Vendedor");
        assertFalse(authVendedor.isCliente(), "Não deve possuir privilégios de Cliente exclusivo");

        // Cenário 3: Perfil Híbrido Vendedor/Cliente (Tipo 2)
        Usuario clienteParaPromover = new Usuario("Mariana", "Diniz", "mariana.hibrida@exemplo.com", "senha789", "Rua C, 30", "33344455566", Usuario.TIPO_CLIENTE);
        service.cadastrarCliente(clienteParaPromover);
        service.criarLojaParaCliente(clienteParaPromover.getId(), new Loja(null, "Diniz Store", "contato@diniz.com", "Rua C, 30", "98.765.432/0001-10"));

        Usuario authHibrido = service.autenticar("mariana.hibrida@exemplo.com", "senha789");
        assertNotNull(authHibrido, "Usuário híbrido deve ser autenticado com sucesso");
        assertEquals(Usuario.TIPO_VENDEDOR_CLIENTE, authHibrido.getTipoUsuario(), "Perfil deve ser identificado como TIPO_VENDEDOR_CLIENTE (2)");
        assertTrue(authHibrido.isVendedor(), "Deve possuir permissões de Vendedor");
        assertTrue(authHibrido.isCliente(), "Deve possuir permissões de Cliente");
    }

    @Test
    @DisplayName("Teste Unitário 2: Tratamento de falha para credenciais incorretas")
    void deveTratarFalhaParaCredenciaisIncorretas() {
        // Pré-cadastro de usuário para verificação de tentativas incorretas
        Usuario usuario = new Usuario("Gabriel", "Ramos", "gabriel@exemplo.com", "senhaCorreta123", "Rua D, 40", "44455566677", Usuario.TIPO_CLIENTE);
        service.cadastrarCliente(usuario);

        // Cenário A: Senha incorreta para e-mail existente
        Usuario authSenhaIncorreta = service.autenticar("gabriel@exemplo.com", "senhaIncorreta");
        assertNull(authSenhaIncorreta, "A autenticação deve falhar e retornar null quando a senha estiver errada");

        // Cenário B: E-mail inexistente com qualquer senha
        Usuario authEmailInexistente = service.autenticar("nao.cadastrado@exemplo.com", "senhaCorreta123");
        assertNull(authEmailInexistente, "A autenticação deve falhar e retornar null para usuário inexistente");

        // Cenário C: Case-insensitivity no e-mail porém com senha errada
        Usuario authCaseIncorreto = service.autenticar("GABRIEL@EXEMPLO.COM", "senhaTotalmenteErrada");
        assertNull(authCaseIncorreto, "A autenticação deve falhar e retornar null mesmo com variação de caixa");
    }

    // =========================================================================
    // 1x Teste Baseado em Requisitos:
    // Rejeição de login caso o e-mail seja num formato inválido ou a senha tenha < 4 caracteres
    // =========================================================================

    @Test
    @DisplayName("Teste Baseado em Requisitos: Rejeição de login com e-mail em formato inválido ou senha com menos de 4 caracteres")
    void deveRejeitarLoginPorEmailInvalidoOuSenhaMenorQueQuatroCaracteres() {
        // Requisito 1: Rejeição de login se o e-mail estiver em formato inválido
        String[] emailsInvalidos = {
            "usuario_sem_arroba.com",
            "@semusuario.com",
            "usuario@.com",
            "usuario@dominio",
            "usuario@dominio.",
            "usuario espaço@teste.com"
        };

        for (String emailInvalido : emailsInvalidos) {
            IllegalArgumentException exEmail = assertThrows(
                IllegalArgumentException.class,
                () -> service.autenticar(emailInvalido, "senhaValida123"),
                "Deveria rejeitar e lançar IllegalArgumentException para o e-mail: " + emailInvalido
            );
            assertTrue(
                exEmail.getMessage().contains("E-mail em formato inválido") || exEmail.getMessage().contains("E-mail inválido"),
                "Mensagem de rejeição esperada para e-mail inválido (" + emailInvalido + "): " + exEmail.getMessage()
            );
        }

        // Requisito 2: Rejeição de login se a senha tiver menos de 4 caracteres (< 4)
        String[] senhasCurtas = {
            "1",
            "12",
            "123",
            "a",
            "ab",
            "abc"
        };

        for (String senhaCurta : senhasCurtas) {
            IllegalArgumentException exSenha = assertThrows(
                IllegalArgumentException.class,
                () -> service.autenticar("usuario.valido@exemplo.com", senhaCurta),
                "Deveria rejeitar e lançar IllegalArgumentException para a senha com menos de 4 caracteres: '" + senhaCurta + "'"
            );
            assertTrue(
                exSenha.getMessage().contains("no mínimo 4 caracteres"),
                "Mensagem de rejeição esperada para senha curta ('" + senhaCurta + "'): " + exSenha.getMessage()
            );
        }
    }
}
