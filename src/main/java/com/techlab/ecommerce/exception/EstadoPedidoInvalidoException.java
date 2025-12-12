package com.techlab.ecommerce.exception;

public class EstadoPedidoInvalidoException extends ApiException {

    private String estadoActual;
    private String estadoRequerido;

    public EstadoPedidoInvalidoException(String estadoActual, String operacion) {
        super("No se puede " + operacion + " un pedido en estado '" + estadoActual + "'");
        this.estadoActual = estadoActual;
    }

    public EstadoPedidoInvalidoException(String estadoActual, String estadoRequerido, String operacion) {
        super("No se puede " + operacion + ". Estado actual: '" + estadoActual +
                "', se requiere: '" + estadoRequerido + "'");
        this.estadoActual = estadoActual;
        this.estadoRequerido = estadoRequerido;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public String getEstadoRequerido() {
        return estadoRequerido;
    }
}