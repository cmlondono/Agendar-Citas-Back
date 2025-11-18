package com.benefactor.agendaCitas.Servicios;
import com.benefactor.agendaCitas.DTO.VentaDTO;
import com.benefactor.agendaCitas.model.Venta;
import com.benefactor.agendaCitas.Repository.VentaRepository;
import com.benefactor.agendaCitas.Repository.DetalleVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteVentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    // Generar reporte de ventas por día
    public Map<String, Object> generarReporteVentasDiarias(LocalDate fecha) {
        LocalDateTime fechaInicio = fecha.atStartOfDay();
        LocalDateTime fechaFin = fecha.atTime(LocalTime.MAX);

        List<Venta> ventas = ventaRepository.findVentasConfirmadasByFecha(fechaInicio, fechaFin);

        // Convertir a DTOs
        List<VentaDTO> ventasDTO = ventas.stream()
                .map(VentaDTO::new)
                .collect(Collectors.toList());

        BigDecimal totalIngresos = ventasDTO.stream()
                .map(VentaDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("fecha", fecha);
        reporte.put("totalVentas", ventasDTO.size());
        reporte.put("totalIngresos", totalIngresos);
        reporte.put("detalle", ventasDTO); // Usar DTOs

        return reporte;
    }
    // Generar reporte de ventas por mes
    public Map<String, Object> generarReporteVentasMensuales(YearMonth yearMonth) {
        LocalDateTime fechaInicio = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime fechaFin = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        List<Venta> ventas = ventaRepository.findVentasConfirmadasByFecha(fechaInicio, fechaFin);
        List<Object[]> estadisticas = ventaRepository.findEstadisticasVentasPorMes(fechaInicio, fechaFin);

        // Convertir a DTOs
        List<VentaDTO> ventasDTO = ventas.stream()
                .map(VentaDTO::new)
                .collect(Collectors.toList());

        BigDecimal totalIngresos = ventasDTO.stream()
                .map(VentaDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular estadísticas adicionales desde los DTOs
        Map<String, Long> ventasPorEstado = ventasDTO.stream()
                .collect(Collectors.groupingBy(VentaDTO::getEstado, Collectors.counting()));

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("mes", yearMonth);
        reporte.put("totalVentas", ventasDTO.size());
        reporte.put("totalIngresos", totalIngresos);
        reporte.put("ventasPorEstado", ventasPorEstado);
        reporte.put("detalle", ventasDTO);
        reporte.put("estadisticas", procesarEstadisticasMensuales(estadisticas));

        return reporte;
    }

    // Generar reporte de ventas por rango de fechas
    public Map<String, Object> generarReporteVentasPorRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            List<Venta> ventas = ventaRepository.findVentasConfirmadasByFecha(fechaInicio, fechaFin);
            List<Object[]> productosMasVendidos = detalleVentaRepository.findProductosMasVendidos(fechaInicio, fechaFin);

            // Convertir ventas a DTOs para evitar problemas de serialización
            List<VentaDTO> ventasDTO = ventas.stream()
                    .map(VentaDTO::new)
                    .collect(Collectors.toList());

            BigDecimal totalIngresos = ventasDTO.stream()
                    .map(VentaDTO::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calcular estadísticas por estado
            Map<String, Long> ventasPorEstado = ventasDTO.stream()
                    .collect(Collectors.groupingBy(VentaDTO::getEstado, Collectors.counting()));

            // Calcular ingresos por día
            Map<String, Map<String, Object>> ingresosPorDia = ventasDTO.stream()
                    .collect(Collectors.groupingBy(
                            v -> v.getFechaVenta().toLocalDate().toString(),
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    list -> {
                                        BigDecimal ingresosDia = list.stream()
                                                .map(VentaDTO::getTotal)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        return Map.of(
                                                "ingresos", ingresosDia,
                                                "ventas", list.size()
                                        );
                                    }
                            )
                    ));

            Map<String, Object> reporte = new HashMap<>();
            reporte.put("fechaInicio", fechaInicio);
            reporte.put("fechaFin", fechaFin);
            reporte.put("totalVentas", ventasDTO.size());
            reporte.put("totalIngresos", totalIngresos);
            reporte.put("ventasPorEstado", ventasPorEstado);
            reporte.put("ingresosPorDia", ingresosPorDia);
            reporte.put("detalle", ventasDTO); // Usar DTOs en lugar de entidades
            reporte.put("productosMasVendidos", procesarProductosMasVendidos(productosMasVendidos));

            return reporte;

        } catch (Exception e) {
            System.err.println("❌ Error en generarReporteVentasPorRango: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error generando reporte: " + e.getMessage(), e);
        }
    }

    // Procesar estadísticas mensuales - CORREGIDO
    private List<Map<String, Object>> procesarEstadisticasMensuales(List<Object[]> estadisticas) {
        List<Map<String, Object>> resultado = new ArrayList<>();

        if (estadisticas != null) {
            for (Object[] estadistica : estadisticas) {
                Map<String, Object> item = new HashMap<>();
                item.put("anio", estadistica[0]);
                item.put("mes", estadistica[1]);
                item.put("totalVentas", estadistica[2]);
                item.put("totalIngresos", estadistica[3]);
                resultado.add(item);
            }
        }

        return resultado;
    }

    // Procesar productos más vendidos - CORREGIDO
    private List<Map<String, Object>> procesarProductosMasVendidos(List<Object[]> productosData) {
        List<Map<String, Object>> resultado = new ArrayList<>();

        if (productosData != null) {
            for (Object[] productoData : productosData) {
                Map<String, Object> item = new HashMap<>();
                item.put("productoId", productoData[0]);
                item.put("productoNombre", productoData[1]);
                item.put("totalVendido", productoData[2]);
                resultado.add(item);
            }
        }

        return resultado;
    }

    // Obtener resumen para dashboard - VERSIÓN MEJORADA
    public Map<String, Object> obtenerResumenVentasDashboard() {
        Map<String, Object> resumen = new HashMap<>();

        try {
            // Fechas para hoy
            LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
            LocalDateTime hoyFin = LocalDateTime.now();

            // Ventas de hoy - usar consulta directa
            Long ventasHoyCount = ventaRepository.countByFechaVentaBetweenAndEstado(hoyInicio, hoyFin, "CONFIRMADA");
            BigDecimal ingresosHoy = ventaRepository.sumTotalByFechaVentaBetweenAndEstado(hoyInicio, hoyFin, "CONFIRMADA");

            // Si no hay datos de hoy, intentar cálculo alternativo
            if (ventasHoyCount == null || ventasHoyCount == 0) {
                List<Venta> ventasHoyList = ventaRepository.findByFechaVentaBetween(hoyInicio, hoyFin);
                ventasHoyCount = ventasHoyList.stream()
                        .filter(v -> "CONFIRMADA".equals(v.getEstado()))
                        .count();
                ingresosHoy = ventasHoyList.stream()
                        .filter(v -> "CONFIRMADA".equals(v.getEstado()))
                        .map(Venta::getTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            // Ventas pendientes
            Long ventasPendientes = ventaRepository.countByEstado("PENDIENTE");

            // Total de ventas
            Long totalVentas = ventaRepository.count();

            resumen.put("ventasHoy", ventasHoyCount != null ? ventasHoyCount : 0);
            resumen.put("ingresosHoy", ingresosHoy != null ? ingresosHoy : BigDecimal.ZERO);
            resumen.put("ventasPendientes", ventasPendientes != null ? ventasPendientes : 0);
            resumen.put("totalVentas", totalVentas != null ? totalVentas : 0);

            System.out.println("📊 Resumen Dashboard - Ventas Hoy: " + ventasHoyCount + ", Ingresos: " + ingresosHoy);

        } catch (Exception e) {
            System.err.println("❌ Error en obtenerResumenVentasDashboard: " + e.getMessage());
            // En caso de error, retornar valores por defecto
            resumen.put("ventasHoy", 0);
            resumen.put("ingresosHoy", BigDecimal.ZERO);
            resumen.put("ventasPendientes", 0);
            resumen.put("totalVentas", 0);
        }

        return resumen;
    }
}