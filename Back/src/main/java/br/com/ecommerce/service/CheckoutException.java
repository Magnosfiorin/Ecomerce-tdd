package br.com.ecommerce.service;

public class CheckoutException extends IllegalArgumentException {

    public CheckoutException(String message) {
        super(message);
    }
}
