package com.techlab.ecommerce.service;

import com.techlab.ecommerce.exception.DatosInvalidosException;
import com.techlab.ecommerce.exception.ResourceNotFoundException;
import com.techlab.ecommerce.exception.StockInsuficienteException;
import com.techlab.ecommerce.model.Producto;
import com.techlab.ecommerce.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public ArrayList<Producto> listarTodos() {
        List<Producto> productos = productoRepository.findAll();
        return new ArrayList<>(productos);
    }

    public ArrayList<Producto> listarDisponibles() {
        List<Producto> productos = productoRepository.findByStockGreaterThan(0);
        return new ArrayList<>(productos);
    }

    public Producto obtenerPorId(int id) {
        return productoRepository.findById(id).orElse(null);
    }

    public ArrayList<Producto> buscarPorNombre(String nombre){
        if(nombre == null || nombre.trim().isEmpty()){
            return new ArrayList<>();
        }
        List<Producto> productos = productoRepository.findByNombreContainingIgnoreCase(nombre);
        return new ArrayList<>(productos);
    }

    public Producto buscarProducto(String criterio) {
        try {
            int id = Integer.parseInt(criterio);
            Producto producto = obtenerPorId(id);
            if (producto != null) {
                return producto;
            }
        } catch (NumberFormatException e) {
            // buscar por nombre
        }

        ArrayList<Producto> productos = buscarPorNombre(criterio);
        if (!productos.isEmpty()) {
            return productos.get(0);
        }

        return null;
    }

    public ArrayList<Producto> filtrarPorCategoria(String categoria) {
        List<Producto> productos = productoRepository.findByCategoria(categoria);
        return new ArrayList<>(productos);
    }

    public ArrayList<Producto> filtrarPorRangoPrecio(double precioMin, double precioMax){
        if (precioMin < 0 || precioMax < precioMin) {
            throw new IllegalArgumentException("Rango de precios inválido");
        }
        List<Producto> productos = productoRepository.findByPrecioBetween(precioMin, precioMax);
        return new ArrayList<>(productos);
    }

    public Producto agregarProducto(Producto producto) {
        if(producto.getNombre() == null || producto.getNombre().trim().isEmpty() ){
            throw new DatosInvalidosException("nombre", "no puede estar vacío");
        }

        if(producto.getPrecio() <= 0){
            throw new DatosInvalidosException("precio", "debe ser mayor a 0");
        }

        if(producto.getStock() < 0){
            throw new DatosInvalidosException("stock", "no puede ser negativo");
        }

        if(producto.getCategoria() == null || producto.getCategoria().trim().isEmpty()){
            throw new DatosInvalidosException("categoria", "no puede estar vacío");
        }

        if(producto.getStock() > 0){
            producto.setDisponible(true);
        }

        return productoRepository.save(producto);
    }

    public Producto actualizarProducto(int id, Producto productoActualizado){
        Producto productoExistente = obtenerPorId(id);

        if (productoExistente == null) {
            throw new ResourceNotFoundException("Producto", id);
        }

        if(productoActualizado.getNombre() != null && !productoActualizado.getNombre().trim().isEmpty()){
            productoExistente.setNombre(productoActualizado.getNombre());
        }

        if(productoActualizado.getDescripcion() != null){
            productoExistente.setDescripcion(productoActualizado.getDescripcion());
        }

        if(productoActualizado.getPrecio() > 0){
            productoExistente.setPrecio(productoActualizado.getPrecio());
        }

        if(productoActualizado.getDescripcion() != null && !productoActualizado.getDescripcion().trim().isEmpty()){
            productoExistente.setDescripcion(productoActualizado.getDescripcion());
        }

        if (productoActualizado.getImagen() != null) {
            productoExistente.setImagen(productoActualizado.getImagen());
        }

        if (productoActualizado.getStock() >= 0) {
            productoExistente.setStock(productoActualizado.getStock());
            productoExistente.setDisponible(productoActualizado.getStock() > 0);
        }

        return productoRepository.save(productoExistente);
    }

    public Producto actualizarPrecio(int id, double nuevoPrecio) {
        if (nuevoPrecio <= 0) {
            throw new DatosInvalidosException("precio", "debe ser mayor a 0");
        }

        Producto producto = obtenerPorId(id);
        if (producto == null) {
            throw new ResourceNotFoundException("Producto", id);
        }

        producto.setPrecio(nuevoPrecio);
        return productoRepository.save(producto);
    }

    public Producto actualizarStock(int id, int nuevoStock) {
        if (nuevoStock < 0) {
            throw new DatosInvalidosException("stock", "no puede ser negativo");
        }

        Producto producto = obtenerPorId(id);
        if (producto == null) {
            throw new ResourceNotFoundException("Producto", id);
        }

        producto.setStock(nuevoStock);
        producto.setDisponible(nuevoStock > 0);
        return productoRepository.save(producto);
    }

    public boolean reducirStock(int id, int cantidad) {
        if (cantidad <= 0) {
            throw new DatosInvalidosException("cantidad", "debe ser mayor a 0");
        }

        Producto producto = obtenerPorId(id);
        if (producto == null) {
            throw new ResourceNotFoundException("Producto", id);
        }

        if (producto.getStock() < cantidad) {
            throw new StockInsuficienteException(
                    producto.getNombre(),
                    producto.getStock(),
                    cantidad
            );
        }
        int nuevoStock = producto.getStock() - cantidad;
        producto.setStock(nuevoStock);
        producto.setDisponible(nuevoStock > 0);

        productoRepository.save(producto);
        return true;
    }

    public boolean eliminarProducto(int id) {
        Producto producto = obtenerPorId(id);

        if (producto == null) {
            return false;
        }

        productoRepository.deleteById(id);
        return true;
    }

    public Producto eliminarConConfirmacion(int id) {
        Producto producto = obtenerPorId(id);

        if (producto == null) {
            throw new ResourceNotFoundException("Producto", id);
        }

        productoRepository.deleteById(id);
        return producto;
    }

    public ArrayList<Producto> obtenerStockBajo(int umbral) {
        ArrayList<Producto> todosLosProductos = listarTodos();
        ArrayList<Producto> stockBajo = new ArrayList<>();

        for (Producto producto : todosLosProductos) {
            if (producto.getStock() <= umbral && producto.getStock() > 0) {
                stockBajo.add(producto);
            }
        }
        return stockBajo;
    }

    public double calcularValorInventario() {
        ArrayList<Producto> productos = listarTodos();
        double valorTotal = 0.0;

        for (Producto producto : productos) {
            double valorProducto = producto.getPrecio() * producto.getStock();
            valorTotal = valorTotal + valorProducto;
        }
        return valorTotal;
    }

    public Map<String, Integer> contarPorCategoria() {
        ArrayList<Producto> productos = listarTodos();
        Map<String, Integer> conteo = new HashMap<>();

        for (Producto producto : productos) {
            String categoria = producto.getCategoria();
            if (conteo.containsKey(categoria)) {
                int cantidadActual = conteo.get(categoria);
                conteo.put(categoria, cantidadActual + 1);
            } else {
                conteo.put(categoria, 1);
            }
        }
        return conteo;
    }

    public boolean verificarStock(int id, int cantidadRequerida) {
        Producto producto = obtenerPorId(id);
        return producto != null && producto.getStock() >= cantidadRequerida;
    }
}
