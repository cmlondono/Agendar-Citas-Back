package com.benefactor.agendaCitas.Repository;

import com.benefactor.agendaCitas.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Buscar por número de factura
    Optional<Venta> findByNumeroFactura(String numeroFactura);

    // Buscar ventas por estado
    List<Venta> findByEstado(String estado);

    // Buscar ventas por rango de fechas
    @Query("SELECT v FROM Venta v WHERE v.fechaVenta BETWEEN :fechaInicio AND :fechaFin ORDER BY v.fechaVenta DESC")
    List<Venta> findByFechaVentaBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                        @Param("fechaFin") LocalDateTime fechaFin);

    // Buscar ventas confirmadas por rango de fechas
    @Query("SELECT v FROM Venta v WHERE v.estado = 'CONFIRMADA' AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin ORDER BY v.fechaVenta DESC")
    List<Venta> findVentasConfirmadasByFecha(@Param("fechaInicio") LocalDateTime fechaInicio,
                                             @Param("fechaFin") LocalDateTime fechaFin);

    // Obtener total de ventas por fecha específica - CORREGIDO
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.estado = 'CONFIRMADA' AND CAST(v.fechaVenta AS localdate) = :fecha")
    BigDecimal findTotalVentasByFecha(@Param("fecha") LocalDate fecha);

    // Obtener última factura para generar número consecutivo - CORREGIDO (consulta nativa)
    @Query(value = "SELECT numero_factura FROM ventas ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<String> findUltimoNumeroFactura();

    // Estadísticas de ventas por mes - CORREGIDO
    @Query("SELECT YEAR(v.fechaVenta), MONTH(v.fechaVenta), " +
            "COUNT(v), SUM(v.total) " +
            "FROM Venta v " +
            "WHERE v.estado = 'CONFIRMADA' AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin " +
            "GROUP BY YEAR(v.fechaVenta), MONTH(v.fechaVenta) " +
            "ORDER BY YEAR(v.fechaVenta) DESC, MONTH(v.fechaVenta) DESC")
    List<Object[]> findEstadisticasVentasPorMes(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                @Param("fechaFin") LocalDateTime fechaFin);

    // Método auxiliar para obtener ventas de hoy
    default List<Venta> findVentasConfirmadasHoy() {
        LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hoyFin = LocalDate.now().atTime(23, 59, 59);
        return findVentasConfirmadasByFecha(hoyInicio, hoyFin);
    }

    // Método auxiliar para obtener total de ventas hoy
    default BigDecimal findTotalVentasHoy() {
        return findTotalVentasByFecha(LocalDate.now());
    }


    Long countByFechaVentaBetweenAndEstado(LocalDateTime fechaInicio, LocalDateTime fechaFin, String estado);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fechaVenta BETWEEN :fechaInicio AND :fechaFin AND v.estado = :estado")
    BigDecimal sumTotalByFechaVentaBetweenAndEstado(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                    @Param("fechaFin") LocalDateTime fechaFin,
                                                    @Param("estado") String estado);

    Long countByEstado(String estado);
}
