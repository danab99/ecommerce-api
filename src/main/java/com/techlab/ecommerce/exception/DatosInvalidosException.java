package com.techlab.ecommerce.exception;

public class DatosInvalidosException extends ApiException {

    public DatosInvalidosException(String campo, String razon) {
        super("El campo '" + campo + "' es inválido: " + razon);
    }

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}