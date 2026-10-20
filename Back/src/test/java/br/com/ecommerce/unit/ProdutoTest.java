package br.com.ecommerce.unit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.ecommerce.model.Produto;

@DisplayName("Testes Unitários - Entidade Produto e Disponibilidade de Estoque (Dev 3)")
public class ProdutoTest {

    private final Gson gson = new Gson();

    @Test
    @DisplayName("Teste de limite: produto com estoque zero deve estar indisponível")
    void deveSerIndisponivelQuandoEstoqueForZero() {
        Produto produto = new Produto(1, "Camiseta Azul", 49.90, 0, true);

        assertFalse(produto.isDisponivel());
    }

    @Test
    @DisplayName("Teste de limite: produto com 1 unidade em estoque deve estar disponível")
    void deveSerDisponivelQuandoHouverUmaUnidade() {
        Produto produto = new Produto(1, "Camiseta Azul", 49.90, 1, true);

        assertTrue(produto.isDisponivel());
    }

    @Test
    @DisplayName("JSON de produto esgotado deve informar disponivel=false para a interface desativar a compra")
    void jsonDeProdutoEsgotadoDeveInformarIndisponivel() {
        Produto produto = new Produto(1, "Camiseta Azul", 49.90, 0, true);

        JsonObject json = JsonParser.parseString(gson.toJson(produto)).getAsJsonObject();

        assertTrue(json.has("disponivel"), "O JSON deve conter o campo 'disponivel'");
        assertFalse(json.get("disponivel").getAsBoolean());
    }

    @Test
    @DisplayName("JSON de produto com estoque deve informar disponivel=true")
    void jsonDeProdutoComEstoqueDeveInformarDisponivel() {
        Produto produto = new Produto(2, "Camiseta Preta", 59.90, 5, true);

        JsonObject json = JsonParser.parseString(gson.toJson(produto)).getAsJsonObject();

        assertTrue(json.has("disponivel"), "O JSON deve conter o campo 'disponivel'");
        assertTrue(json.get("disponivel").getAsBoolean());
    }
}