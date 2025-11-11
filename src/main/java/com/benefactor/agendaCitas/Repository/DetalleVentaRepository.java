package com.benefactor.agendaCitas.Repository;

import com.benefactor.agendaCitas.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    // Buscar detalles por venta
    List<DetalleVenta> findByVentaId(Long ventaId);

    // Obtener productos más vendidos - CORREGIDO
    @Query("SELECT dv.producto.id, dv.producto.nombre, SUM(dv.cantidad) as totalVendido " +
            "FROM DetalleVenta dv " +
            "JOIN dv.venta v " +
            "WHERE v.estado = 'CONFIRMADA' AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin " +
            "GROUP BY dv.producto.id, dv.producto.nombre " +
            "ORDER BY totalVendido DESC")
    List<Object[]> findProductosMasVendidos(@Param("fechaInicio") java.time.LocalDateTime fechaInicio,
                                            @Param("fechaFin") java.time.LocalDateTime fechaFin);
}