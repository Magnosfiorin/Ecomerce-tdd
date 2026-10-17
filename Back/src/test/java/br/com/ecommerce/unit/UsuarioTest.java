package br.com.ecommerce.unit;

import br.com.ecommerce.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - Entidade Usuario e Validação de E-mail (Dev 1)")
public class UsuarioTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "cliente@exemplo.com",
            "vendedor.loja@sub.dominio.org",
            "usuario_123@teste.com.br",
            "contato+vendas@empresa.io"
    })
    @DisplayName("Deve aceitar e-mails válidos através do Regex")
    void deveAceitarEmailsValidos(String emailValido) {
        assertTrue(Usuario.isEmailValido(emailValido), "Deveria ser válido: " + emailValido);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "email_sem_arroba.com",
            "@semusuario.com",
            "usuario@.com",
            "usuario@dominio",
            "usuario@dominio.",
            "",
            "   "
    })
    @DisplayName("Deve rejeitar e-mails inválidos através do Regex")
    void deveRejeitarEmailsInvalidos(String emailInvalido) {
        assertFalse(Usuario.isEmailValido(emailInvalido), "Deveria ser inválido: " + emailInvalido);
    }

    @Test
    @DisplayName("Deve identificar corretamente as permissões por tipo de usuário")
    void deveIdentificarPermissoesPorTipoUsuario() {
        Usuario vendedor = new Usuario("João", "Silva", "joao@loja.com", "1234", "Rua 1", "111", Usuario.TIPO_VENDEDOR);
        Usuario cliente = new Usuario("Maria", "Souza", "maria@gmail.com", "1234", "Rua 2", "222", Usuario.TIPO_CLIENTE);
        Usuario hibrido = new Usuario("Carlos", "Lima", "carlos@gmail.com", "1234", "Rua 3", "333", Usuario.TIPO_VENDEDOR_CLIENTE);

        assertTrue(vendedor.isVendedor());
        assertFalse(vendedor.isCliente());

        assertFalse(cliente.isVendedor());
        assertTrue(cliente.isCliente());

        assertTrue(hibrido.isVendedor());
        assertTrue(hibrido.isCliente());
    }
}
