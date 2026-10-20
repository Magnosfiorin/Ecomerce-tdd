package br.com.ecommerce.unit;

import br.com.ecommerce.service.PasswordResetService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordResetServiceTest {

    private final PasswordResetService service = new PasswordResetService(null);

    @Test
    void deveRejeitarEmailInvalidoAntesDeAcessarBanco() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.solicitarRecuperacao("email-invalido"));

        assertEquals("Informe um e-mail válido.", exception.getMessage());
    }

    @Test
    void deveRejeitarTokenAusente() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.redefinirSenha(" ", "senha-segura"));

        assertEquals("O link de recuperação é inválido ou expirou.", exception.getMessage());
    }

    @Test
    void deveRejeitarSenhaComMenosDeOitoCaracteres() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.redefinirSenha("token", "curta"));

        assertEquals("A senha deve conter no mínimo 8 caracteres.", exception.getMessage());
    }
}
