package com.techlab.ecommerce.exception;

public class PedidoVacioException extends ApiException {
    public PedidoVacioException() {
        super("El pedido no puede estar vacío");
    }
}
