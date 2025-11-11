package com.benefactor.agendaCitas.Servicios;

import com.benefactor.agendaCitas.model.Producto;
import com.benefactor.agendaCitas.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // Obtener todos los productos activos
    public List<Producto> obtenerTodosProductosActivos() {
        return productoRepository.findByActivoTrue();
    }

    // Obtener producto por ID
    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id)
                .filter(Producto::getActivo);
    }

    // Crear nuevo producto
    @Transactional
    public Producto crearProducto(Producto producto) {
        // Validaciones básicas
        if (producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El precio no puede ser negativo");
        }
        if (producto.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        // Verificar si ya existe un producto con el mismo nombre
        Optional<Producto> productoExistente = productoRepository.findByNombreAndActivoTrue(producto.getNombre());
        if (productoExistente.isPresent()) {
            throw new RuntimeException("Ya existe un producto con el nombre: " + producto.getNombre());
        }

        producto.setActivo(true);
        return productoRepository.save(producto);
    }

    // Actualizar producto
    @Transactional
    public Producto actualizarProducto(Long id, Producto productoActualizado) {
        return productoRepository.findById(id)
                .map(productoExistente -> {
                    // Validar que el producto esté activo
                    if (!productoExistente.getActivo()) {
                        throw new RuntimeException("No se puede actualizar un producto inactivo");
                    }

                    // Validaciones de precio y stock
                    if (productoActualizado.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("El precio no puede ser negativo");
                    }
                    if (productoActualizado.getStock() < 0) {
                        throw new RuntimeException("El stock no puede ser negativo");
                    }

                    // Actualizar campos permitidos
                    productoExistente.setNombre(productoActualizado.getNombre());
                    productoExistente.setDescripcion(productoActualizado.getDescripcion());
                    productoExistente.setCategoria(productoActualizado.getCategoria());
                    productoExistente.setPrecio(productoActualizado.getPrecio());
                    productoExistente.setStock(productoActualizado.getStock());
                    productoExistente.setStockMinimo(productoActualizado.getStockMinimo());

                    return productoRepository.save(productoExistente);
                })
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    // Eliminar producto (eliminación lógica)
    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        producto.setActivo(false);
        productoRepository.save(producto);
    }

    // Actualizar stock
    @Transactional
    public Producto actualizarStock(Long productoId, Integer nuevaCantidad) {
        if (nuevaCantidad < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        Producto producto = productoRepository.findById(productoId)
                .filter(Producto::getActivo)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado o inactivo"));

        producto.setStock(nuevaCantidad);
        return productoRepository.save(producto);
    }

    // Disminuir stock (para ventas)
    @Transactional
    public void disminuirStock(Long productoId, Integer cantidad) {
        Producto producto = productoRepository.findById(productoId)
                .filter(Producto::getActivo)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado o inactivo"));

        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre() +
                    ". Stock disponible: " + producto.getStock());
        }

        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
    }

    // Aumentar stock
    @Transactional
    public void aumentarStock(Long productoId, Integer cantidad) {
        if (cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }

        Producto producto = productoRepository.findById(productoId)
                .filter(Producto::getActivo)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado o inactivo"));

        producto.setStock(producto.getStock() + cantidad);
        productoRepository.save(producto);
    }

    // Obtener productos por categoría
    public List<Producto> obtenerProductosPorCategoria(String categoria) {
        return productoRepository.findByCategoriaAndActivoTrue(categoria);
    }

    // Obtener productos con stock bajo
    public List<Producto> obtenerProductosStockBajo() {
        return productoRepository.findProductosStockBajo();
    }

    // Buscar productos por nombre
    public List<Producto> buscarProductosPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }
}