package com.techlab.ecommerce.controller;

import com.techlab.ecommerce.model.Producto;
import com.techlab.ecommerce.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    /**
     * GET /api/productos
     */
    @GetMapping
    public ResponseEntity<ArrayList<Producto>> listarTodos() {
        ArrayList<Producto> productos = productoService.listarTodos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable int id) {
        Producto producto = productoService.obtenerPorId(id);

        if (producto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(producto);
    }

    @GetMapping("/buscar")
    public ResponseEntity<ArrayList<Producto>> buscarPorNombre(
            @RequestParam String nombre) {
        ArrayList<Producto> productos = productoService.buscarPorNombre(nombre);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/buscar/{criterio}")
    public ResponseEntity<Producto> buscarProducto(@PathVariable String criterio) {
        Producto producto = productoService.buscarProducto(criterio);

        if (producto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(producto);
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<ArrayList<Producto>> filtrarPorCategoria(
            @PathVariable String categoria) {
        ArrayList<Producto> productos = productoService.filtrarPorCategoria(categoria);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/precio")
    public ResponseEntity<?> filtrarPorPrecio(
            @RequestParam double min,
            @RequestParam double max) {
        try {
            ArrayList<Producto> productos = productoService.filtrarPorRangoPrecio(min, max);
            return ResponseEntity.ok(productos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/productos
    */
    @PostMapping
    public ResponseEntity<Producto> agregarProducto(@RequestBody Producto producto) {
        Producto nuevoProducto = productoService.agregarProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    /**
     * PUT /api/productos
     */
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable int id,
            @RequestBody Producto producto) {
        Producto productoActualizado = productoService.actualizarProducto(id, producto);
        return ResponseEntity.ok(productoActualizado);
    }

    /**
     * PATCH /api/productos
     */

    @PatchMapping("/{id}/precio")
    public ResponseEntity<Producto> actualizarPrecio(
            @PathVariable int id,
            @RequestBody Map<String, Double> body) {
        double precio = body.get("precio");
        Producto producto = productoService.actualizarPrecio(id, precio);
        return ResponseEntity.ok(producto);
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Producto> actualizarStock(
            @PathVariable int id,
            @RequestBody Map<String, Integer> body) {
        int stock = body.get("stock");
        Producto producto = productoService.actualizarStock(id, stock);
        return ResponseEntity.ok(producto);
    }

    /**
     * DELETE /api/productos
     */

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable int id) {
        boolean eliminado = productoService.eliminarProducto(id);

        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado exitosamente"));
    }

    @DeleteMapping("/{id}/confirmar")
    public ResponseEntity<Map<String, Object>> eliminarConConfirmacion(@PathVariable int id) {
        Producto productoEliminado = productoService.eliminarConConfirmacion(id);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Producto eliminado exitosamente");
        respuesta.put("producto", productoEliminado);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * GET /api/productos
     * UTILIDADES
     */

    @GetMapping("/stock-bajo")
    public ResponseEntity<ArrayList<Producto>> obtenerStockBajo(
            @RequestParam(defaultValue = "10") int umbral) {
        ArrayList<Producto> productos = productoService.obtenerStockBajo(umbral);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("totalProductos", productoService.listarTodos().size());
        estadisticas.put("productosDisponibles", productoService.listarDisponibles().size());
        estadisticas.put("valorInventario", productoService.calcularValorInventario());
        estadisticas.put("productosPorCategoria", productoService.contarPorCategoria());
        estadisticas.put("stockBajo", productoService.obtenerStockBajo(10).size());

        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/{id}/verificar-stock")
    public ResponseEntity<Map<String, Object>> verificarStock(
            @PathVariable int id,
            @RequestParam int cantidad) {

        Producto producto = productoService.obtenerPorId(id);

        if (producto == null) {
            return ResponseEntity.notFound().build();
        }

        boolean hayStock = productoService.verificarStock(id, cantidad);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("productoId", id);
        respuesta.put("productoNombre", producto.getNombre());
        respuesta.put("stockActual", producto.getStock());
        respuesta.put("cantidadSolicitada", cantidad);
        respuesta.put("hayStockSuficiente", hayStock);

        return ResponseEntity.ok(respuesta);
    }

}
