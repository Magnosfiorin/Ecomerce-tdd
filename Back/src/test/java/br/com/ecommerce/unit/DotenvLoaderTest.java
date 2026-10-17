package br.com.ecommerce.unit;

import br.com.ecommerce.config.DotenvLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - Carregamento do Arquivo .env")
public class DotenvLoaderTest {

    @Test
    @DisplayName("Deve inicializar DotenvLoader sem erros mesmo se .env não existir")
    void deveInicializarSemErros() {
        assertDoesNotThrow(() -> DotenvLoader.load());
    }

    @Test
    @DisplayName("Deve retornar valor padrão quando chave não existir no .env")
    void deveRetornarValorPadraoQuandoChaveNaoExistir() {
        String resultado = DotenvLoader.get("CHAVE_INEXISTENTE_XYZ", "padrao");
        assertEquals("padrao", resultado);
    }
}
