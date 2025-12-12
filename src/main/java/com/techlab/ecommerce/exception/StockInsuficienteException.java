package com.techlab.ecommerce.exception;

public class StockInsuficienteException extends ApiException {
    private int stockDisponible;
    private int stockSolicitado;

    public StockInsuficienteException(String producto, int disponible, int solicitado) {
        super("Stock insuficiente para '" + producto + "'. " +
                "Disponible: " + disponible + ", Solicitado: " + solicitado);
        this.stockDisponible = disponible;
        this.stockSolicitado = solicitado;
    }

    public int getStockDisponible() {
        return stockDisponible;
    }

    public int getStockSolicitado() {
        return stockSolicitado;
    }
}
