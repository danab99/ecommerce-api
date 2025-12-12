package com.techlab.ecommerce.exception;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String recurso, int id) {
        super(recurso + " con ID " + id + " no encontrado");
    }

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}
