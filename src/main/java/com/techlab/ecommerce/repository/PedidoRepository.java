package com.techlab.ecommerce.repository;

import com.techlab.ecommerce.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByEstado(String estado);

    List<Pedido> findByClienteNombreContainingIgnoreCase(String nombre);

    List<Pedido> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

}
