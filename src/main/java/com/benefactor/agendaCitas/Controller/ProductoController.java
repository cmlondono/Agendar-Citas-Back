package com.benefactor.agendaCitas.Controller;

import com.benefactor.agendaCitas.model.Producto;
import com.benefactor.agendaCitas.Servicios.ProductoService;
import com.benefactor.agendaCitas.Servicios.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para gestión de productos/inventario
 * Integra validación de sesión en todos los endpoints
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private AuthService authService;

    /**
     * Verifica la sesión del usuario antes de procesar la solicitud
     */
    private ResponseEntity<?> verificarSesion(String sessionId) {
        if (sessionId == null || !authService.validarSesion(sessionId)) {
            return ResponseEntity.status(401).body(Map.of("error", "Sesión inválida o expirada"));
        }
        return null;
    }

    /**
     * Obtener todos los productos activos
     */
    @GetMapping
    public ResponseEntity<?> obtenerTodosProductos(
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            List<Producto> productos = productoService.obtenerTodosProductosActivos();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener productos: " + e.getMessage()));
        }
    }

    /**
     * Obtener producto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProductoPorId(
            @PathVariable Long id,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            Optional<Producto> producto = productoService.obtenerProductoPorId(id);
            return producto.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener producto: " + e.getMessage()));
        }
    }

    /**
     * Crear nuevo producto
     */
    @PostMapping
    public ResponseEntity<?> crearProducto(
            @RequestBody Producto producto,
            @CookieValue(value = "sessionId", required = false) String sessionId,
            HttpServletRequest request) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            // Obtener usuario de la sesión para auditoría
            String usuario = authService.getUsuarioDeSesion(sessionId);

            Producto nuevoProducto = productoService.crearProducto(producto);
            return ResponseEntity.ok(nuevoProducto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al crear producto: " + e.getMessage()));
        }
    }

    /**
     * Actualizar producto existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Long id,
            @RequestBody Producto productoActualizado,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            Producto producto = productoService.actualizarProducto(id, productoActualizado);
            return ResponseEntity.ok(producto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al actualizar producto: " + e.getMessage()));
        }
    }

    /**
     * Eliminar producto (eliminación lógica)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(
            @PathVariable Long id,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al eliminar producto: " + e.getMessage()));
        }
    }

    /**
     * Actualizar stock de producto
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> actualizarStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> stockData,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            Integer nuevaCantidad = stockData.get("cantidad");
            if (nuevaCantidad == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Campo 'cantidad' es requerido"));
            }

            Producto producto = productoService.actualizarStock(id, nuevaCantidad);
            return ResponseEntity.ok(producto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al actualizar stock: " + e.getMessage()));
        }
    }

    /**
     * Obtener productos por categoría
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<?> obtenerProductosPorCategoria(
            @PathVariable String categoria,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            List<Producto> productos = productoService.obtenerProductosPorCategoria(categoria);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener productos por categoría: " + e.getMessage()));
        }
    }

    /**
     * Obtener productos con stock bajo
     */
    @GetMapping("/stock-bajo")
    public ResponseEntity<?> obtenerProductosStockBajo(
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            List<Producto> productos = productoService.obtenerProductosStockBajo();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener productos con stock bajo: " + e.getMessage()));
        }
    }

    /**
     * Buscar productos por nombre
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarProductosPorNombre(
            @RequestParam String nombre,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            List<Producto> productos = productoService.buscarProductosPorNombre(nombre);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al buscar productos: " + e.getMessage()));
        }
    }

    /**
     * Obtener estadísticas básicas de inventario
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasInventario(
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            List<Producto> todosProductos = productoService.obtenerTodosProductosActivos();
            List<Producto> stockBajo = productoService.obtenerProductosStockBajo();

            BigDecimal valorTotalInventario = todosProductos.stream()
                    .map(p -> p.getPrecio().multiply(BigDecimal.valueOf(p.getStock())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> estadisticas = Map.of(
                    "totalProductos", todosProductos.size(),
                    "productosStockBajo", stockBajo.size(),
                    "valorTotalInventario", valorTotalInventario,
                    "productosActivos", todosProductos.stream().filter(Producto::getActivo).count()
            );

            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }
}