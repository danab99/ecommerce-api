package com.techlab.ecommerce.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<LineaPedido> lineas = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(length = 200)
    private String clienteNombre;

    public double calcularTotal() {
        double total = 0.0;
        for (LineaPedido linea : lineas) {
            total = total + linea.calcularSubtotal();
        }
        return total;
    }

    public void agregarLinea(LineaPedido linea) {
        linea.setPedido(this);
        this.lineas.add(linea);
    }

    public boolean eliminarLinea(int indice) {
        if (indice >= 0 && indice < lineas.size()) {
            lineas.remove(indice);
            return true;
        }
        return false;
    }

    public int contarProductos() {
        int total = 0;
        for (LineaPedido linea : lineas) {
            total = total + linea.getCantidad();
        }
        return total;
    }

    public boolean estaVacio() {
        return lineas.isEmpty() || lineas.size() == 0;
    }

    public boolean puedeSerEnviado() {
        return !estaVacio() &&
                estado != null &&
                estado.equals("pagado");
    }

    public int obtenerCantidadLineas() {
        return lineas.size();
    }

    public LineaPedido obtenerLinea(int indice) {
        if (indice >= 0 && indice < lineas.size()) {
            return lineas.get(indice);
        }
        return null;
    }

    public double calcularTotalConDescuento(double porcentajeDescuento) {
        double total = calcularTotal();
        double descuento = total * (porcentajeDescuento / 100.0);
        return total - descuento;
    }

}
