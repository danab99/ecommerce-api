package com.techlab.ecommerce.exception;

public class ProductoNoDisponibleException extends ApiException {
    public ProductoNoDisponibleException(String nombreProducto) {
        super("El producto '" + nombreProducto + "' no está disponible");
    }
}
