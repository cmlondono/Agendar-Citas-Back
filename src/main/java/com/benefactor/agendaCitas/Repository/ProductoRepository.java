package com.benefactor.agendaCitas.Repository;

import com.benefactor.agendaCitas.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Buscar productos activos
    List<Producto> findByActivoTrue();

    // Buscar por categoría
    List<Producto> findByCategoriaAndActivoTrue(String categoria);

    // Buscar productos con stock bajo
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo AND p.activo = true")
    List<Producto> findProductosStockBajo();

    // Buscar por nombre (búsqueda parcial)
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND p.activo = true")
    List<Producto> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    // Verificar si existe un producto con el mismo nombre
    Optional<Producto> findByNombreAndActivoTrue(String nombre);

    // Obtener productos por IDs
    @Query("SELECT p FROM Producto p WHERE p.id IN :ids AND p.activo = true")
    List<Producto> findByIdInAndActivoTrue(@Param("ids") List<Long> ids);
}