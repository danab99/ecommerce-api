package com.techlab.ecommerce.repository;

import com.techlab.ecommerce.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByNombreContainingIgnoreCase(String nombreProducto);

    List<Producto> findByCategoria(String categoria);

    List<Producto> findByStockGreaterThan(int stock);

    List<Producto> findByPrecioBetween(double precioMin, double precioMax);

}
