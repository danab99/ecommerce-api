package com.techlab.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lineas_pedido")
@Data
@NoArgsConstructor
public class LineaPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonIgnore
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private double precioUnitario;


    public LineaPedido(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
    }

    public double calcularSubtotal() {
        return this.precioUnitario * this.cantidad;
    }

    public void aumentarCantidad(int unidades) {
        if (unidades > 0) {
            this.cantidad = this.cantidad + unidades;
        }
    }

    public boolean disminuirCantidad(int unidades) {
        if (unidades > 0 && this.cantidad > unidades) {
            this.cantidad = this.cantidad - unidades;
            return true;
        }
        return false;
    }

    public boolean hayStockDisponible() {
        return producto.getStock() >= this.cantidad;
    }
}
