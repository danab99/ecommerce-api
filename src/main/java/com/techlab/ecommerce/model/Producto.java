package com.techlab.ecommerce.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(length = 500)
    private String imagen;

    @Column(nullable = false)
    private Integer  stock;

    @Column(nullable = false)
    private Boolean disponible = true;

    public boolean hayStockDisponible(int cantidad) {
        return this.stock >= cantidad && this.disponible;
    }

   public double calcularPrecioTotal(int cantidad) {
        return this.precio * cantidad;
   }

   public boolean reducirStock(int cantidad) {
        if(cantidad > 0 && this.stock >= cantidad) {
            this.stock = this.stock - cantidad;
            return true;
        }
        return false;
   }

    public void aumentarStock(int cantidad) {
        if (cantidad > 0) {
            this.stock = this.stock + cantidad;
        }
    }

    public boolean precioValido() {
        return this.precio > 0 && this.precio < 1000000;
    }
}
