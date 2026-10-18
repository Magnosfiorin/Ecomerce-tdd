package br.com.ecommerce.service;

public class ProdutoInvalidoException extends RuntimeException {
    public ProdutoInvalidoException(String mensagem) { super(mensagem); }
}