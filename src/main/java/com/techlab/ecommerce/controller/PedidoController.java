package com.techlab.ecommerce.controller;

import com.techlab.ecommerce.model.LineaPedido;
import com.techlab.ecommerce.model.Pedido;
import com.techlab.ecommerce.model.Producto;
import com.techlab.ecommerce.service.PedidoService;
import com.techlab.ecommerce.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoService productoService;

    /**
     * GET /api/pedidos
     */
    @GetMapping
    public ResponseEntity<ArrayList<Pedido>> listarTodos() {
        ArrayList<Pedido> pedidos = pedidoService.listarTodos();
        return ResponseEntity.ok(pedidos);
    }

    /**
     * GET /api/pedidos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtenerPorId(@PathVariable int id) {
        Pedido pedido = pedidoService.obtenerPorId(id);

        if (pedido == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(pedido);
    }

    /**
     * GET /api/pedidos/estado/{estado}
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<ArrayList<Pedido>> listarPorEstado(@PathVariable String estado) {
        ArrayList<Pedido> pedidos = pedidoService.listarPorEstado(estado);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * POST /api/pedidos/crear
     * Body: { "clienteNombre": "Juan Pérez" }
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearPedidoVacio(@RequestBody Map<String, String> body) {
        String clienteNombre = body.get("clienteNombre");

        if (clienteNombre == null || clienteNombre.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El nombre del cliente es obligatorio"));
        }

        Pedido pedido = pedidoService.crearPedidoVacio(clienteNombre);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    /**
     * POST /api/pedidos/{pedidoId}/agregar-producto
     * Body: { "productoId": 1, "cantidad": 2 }
     */
    @PostMapping("/{pedidoId}/agregar-producto")
    public ResponseEntity<?> agregarProducto(
            @PathVariable int pedidoId,
            @RequestBody Map<String, Integer> body) {
        try {
            int productoId = body.get("productoId");
            int cantidad = body.get("cantidad");

            Pedido pedido = pedidoService.agregarProductoAlPedido(pedidoId, productoId, cantidad);
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/pedidos/crear-completo
     * Body: {
     *   "clienteNombre": "Juan Pérez",
     *   "productos": [
     *     { "productoId": 1, "cantidad": 2 },
     *     { "productoId": 3, "cantidad": 1 }
     *   ]
     * }
     */
    @PostMapping("/crear-completo")
    public ResponseEntity<?> crearPedidoCompleto(@RequestBody Map<String, Object> body) {
        try {
            String clienteNombre = (String) body.get("clienteNombre");

            if (clienteNombre == null || clienteNombre.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El nombre del cliente es obligatorio"));
            }

            @SuppressWarnings("unchecked")
            ArrayList<Map<String, Integer>> productosData =
                    (ArrayList<Map<String, Integer>>) body.get("productos");

            if (productosData == null || productosData.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El pedido debe tener al menos un producto"));
            }

            ArrayList<LineaPedido> lineas = new ArrayList<>();

            for (Map<String, Integer> productoData : productosData) {
                int productoId = productoData.get("productoId");
                int cantidad = productoData.get("cantidad");

                Producto producto = productoService.obtenerPorId(productoId);
                if (producto == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Producto con ID " + productoId + " no encontrado"));
                }

                LineaPedido linea = new LineaPedido(producto, cantidad);
                lineas.add(linea);
            }

            Pedido pedido = pedidoService.crearPedido(clienteNombre, lineas);

            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al crear el pedido: " + e.getMessage()));
        }
    }

    /**
     * POST /api/pedidos/{id}/confirmar
     */
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarPedido(@PathVariable int id) {
        try {
            Pedido pedido = pedidoService.confirmarPedido(id);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Pedido confirmado exitosamente");
            respuesta.put("pedido", pedido);
            respuesta.put("total", pedido.calcularTotal());

            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/pedidos/{id}/total
     */
    @GetMapping("/{id}/total")
    public ResponseEntity<?> calcularTotal(@PathVariable int id) {
        try {
            double total = pedidoService.calcularTotal(id);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("pedidoId", id);
            respuesta.put("total", total);

            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/pedidos/{id}/resumen
     */
    @GetMapping("/{id}/resumen")
    public ResponseEntity<?> obtenerResumen(@PathVariable int id) {
        String resumen = pedidoService.obtenerResumenPedido(id);

        if (resumen.equals("Pedido no encontrado")) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("resumen", resumen));
    }

    /**
     * POST /api/pedidos/{id}/cancelar
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarPedido(@PathVariable int id) {
        try {
            Pedido pedido = pedidoService.cancelarPedido(id);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Pedido cancelado exitosamente",
                    "pedido", pedido
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/pedidos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPedido(@PathVariable int id) {
        boolean eliminado = pedidoService.eliminarPedido(id);

        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("mensaje", "Pedido eliminado exitosamente"));
    }

}
