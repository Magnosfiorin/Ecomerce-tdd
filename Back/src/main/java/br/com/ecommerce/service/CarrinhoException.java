package br.com.ecommerce.service;

/** Erro de validação do carrinho (ex.: quantidade acima do estoque). */
public class CarrinhoException extends IllegalArgumentException {

    public CarrinhoException(String message) {
        super(message);
    }
}
