package com.techlab.ecommerce.exception;

public class OperacionNoPermitidaException extends ApiException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
