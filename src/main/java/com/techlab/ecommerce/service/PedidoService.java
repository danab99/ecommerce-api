package com.techlab.ecommerce.service;


import com.techlab.ecommerce.exception.*;
import com.techlab.ecommerce.model.LineaPedido;
import com.techlab.ecommerce.model.Pedido;
import com.techlab.ecommerce.model.Producto;
import com.techlab.ecommerce.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {
    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoService productoService;

    public ArrayList<Pedido> listarTodos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        return new ArrayList<>(pedidos);
    }

    public Pedido obtenerPorId(int id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    public ArrayList<Pedido> listarPorEstado(String estado) {
        List<Pedido> pedidos = pedidoRepository.findByEstado(estado);
        return new ArrayList<>(pedidos);
    }

    public Pedido crearPedidoVacio(String clienteNombre) {
        Pedido pedido = new Pedido();
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado("pendiente");
        pedido.setClienteNombre(clienteNombre);

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido agregarProductoAlPedido(int pedidoId, int productoId, int cantidad){
        if (cantidad <= 0) {
            throw new DatosInvalidosException("cantidad", "debe ser mayor a 0");
        }

        Pedido pedido = obtenerPorId(pedidoId);
        if (pedido == null) {
            throw new ResourceNotFoundException("Pedido", pedidoId);
        }

        Producto producto = productoService.obtenerPorId(productoId);
        if (producto == null) {
            throw new ResourceNotFoundException("Producto", productoId);
        }

        if (!producto.getDisponible()) {
            throw new ProductoNoDisponibleException(producto.getNombre());
        }

        if (producto.getStock() < cantidad) {
            throw new StockInsuficienteException(
                    producto.getNombre(),
                    producto.getStock(),
                    cantidad
            );
        }

        LineaPedido linea = new LineaPedido(producto, cantidad);
        pedido.agregarLinea(linea);

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido crearPedido(String clienteNombre, ArrayList<LineaPedido> lineasPedido) {
        if (lineasPedido == null || lineasPedido.isEmpty()) {
            throw new PedidoVacioException();
        }

        Pedido pedido = new Pedido();
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado("pendiente");
        pedido.setClienteNombre(clienteNombre);

        for (LineaPedido linea : lineasPedido) {
            validarLineaPedido(linea);
        }

        for (LineaPedido linea : lineasPedido) {
            pedido.agregarLinea(linea);
        }

        return pedidoRepository.save(pedido);

    }

    private void validarLineaPedido(LineaPedido linea) {
        if (linea == null) {
            throw new DatosInvalidosException("La línea de pedido no puede ser nula");
        }

        if (linea.getProducto() == null) {
            throw new DatosInvalidosException("La línea debe tener un producto");
        }

        if (linea.getCantidad() <= 0) {
            throw new DatosInvalidosException("cantidad", "debe ser mayor a 0");
        }

        Producto producto = linea.getProducto();

        if (!producto.getDisponible()) {
            throw new ProductoNoDisponibleException(producto.getNombre());
        }

        if (producto.getStock() < linea.getCantidad()) {
            throw new StockInsuficienteException(
                    producto.getNombre(),
                    producto.getStock(),
                    linea.getCantidad()
            );
        }
    }

    @Transactional
    public Pedido confirmarPedido(int pedidoId) {
        Pedido pedido = obtenerPorId(pedidoId);

        if (pedido == null) {
            throw new ResourceNotFoundException("Pedido", pedidoId);
        }

        if (!pedido.getEstado().equals("pendiente")) {
            throw new EstadoPedidoInvalidoException(pedido.getEstado(), "confirmar");
        }

        if (pedido.estaVacio()) {
            throw new PedidoVacioException();
        }

        for (LineaPedido linea : pedido.getLineas()) {
            Producto producto = linea.getProducto();
            if (producto.getStock() < linea.getCantidad()) {
                throw new StockInsuficienteException(
                        producto.getNombre(),
                        producto.getStock(),
                        linea.getCantidad()
                );
            }
        }

        for (LineaPedido linea : pedido.getLineas()) {
            productoService.reducirStock(
                    linea.getProducto().getId(),
                    linea.getCantidad()
            );
        }

        pedido.setEstado("confirmado");

        return pedidoRepository.save(pedido);
    }

    public double calcularTotal(int pedidoId) {
        Pedido pedido = obtenerPorId(pedidoId);

        if (pedido == null) {
            throw new ResourceNotFoundException("Pedido", pedidoId);
        }

        return pedido.calcularTotal();
    }

    public String obtenerResumenPedido(int pedidoId) {
        Pedido pedido = obtenerPorId(pedidoId);

        if (pedido == null) {
            return "Pedido no encontrado";
        }

        StringBuilder resumen = new StringBuilder();
        resumen.append("PEDIDO #").append(pedido.getId()).append("\n");
        resumen.append("Cliente: ").append(pedido.getClienteNombre()).append("\n");
        resumen.append("Fecha: ").append(pedido.getFecha()).append("\n");
        resumen.append("Estado: ").append(pedido.getEstado()).append("\n\n");
        resumen.append("Productos:\n");

        int numeroLinea = 1;
        for (LineaPedido linea : pedido.getLineas()) {
            resumen.append(numeroLinea).append(". ")
                    .append(linea.getProducto().getNombre())
                    .append(" x ").append(linea.getCantidad())
                    .append(" = $").append(linea.calcularSubtotal())
                    .append("\n");
            numeroLinea = numeroLinea + 1;
        }

        resumen.append("\nTOTAL: $").append(pedido.calcularTotal());

        return resumen.toString();
    }

    public Pedido cancelarPedido(int pedidoId) {
        Pedido pedido = obtenerPorId(pedidoId);

        if (pedido == null) {
            throw new ResourceNotFoundException("Pedido", pedidoId);
        }

        if (pedido.getEstado().equals("confirmado") || pedido.getEstado().equals("enviado")) {
            throw new OperacionNoPermitidaException(
                    "No se puede cancelar un pedido en estado '" + pedido.getEstado() + "'"
            );
        }

        pedido.setEstado("cancelado");
        return pedidoRepository.save(pedido);
    }

    public boolean eliminarPedido(int id) {
        Pedido pedido = obtenerPorId(id);

        if (pedido == null) {
            return false;
        }

        pedidoRepository.deleteById(id);
        return true;
    }

    public double calcularTotalVentas() {
        ArrayList<Pedido> pedidosConfirmados = listarPorEstado("confirmado");
        double totalVentas = 0.0;

        for (Pedido pedido : pedidosConfirmados) {
            totalVentas = totalVentas + pedido.calcularTotal();
        }

        return totalVentas;
    }

    public int contarPorEstado(String estado) {
        ArrayList<Pedido> pedidos = listarPorEstado(estado);
        return pedidos.size();
    }

}
