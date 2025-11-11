package com.benefactor.agendaCitas.Controller;

import com.benefactor.agendaCitas.Servicios.ReporteVentaService;
import com.benefactor.agendaCitas.Servicios.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Controlador REST para generación de reportes de ventas
 * Proporciona endpoints para reportes diarios, mensuales y por rangos
 */
@RestController
@RequestMapping("/api/reportes/ventas")
public class ReporteVentaController {

    @Autowired
    private ReporteVentaService reporteVentaService;

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
     * Generar reporte de ventas diarias
     */
    @GetMapping("/diario")
    public ResponseEntity<?> generarReporteDiario(
            @RequestParam(required = false) String fecha,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            LocalDate fechaReporte;
            if (fecha != null && !fecha.isEmpty()) {
                fechaReporte = LocalDate.parse(fecha);
            } else {
                fechaReporte = LocalDate.now();
            }

            Map<String, Object> reporte = reporteVentaService.generarReporteVentasDiarias(fechaReporte);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Formato de fecha inválido. Use formato YYYY-MM-DD: " + e.getMessage()));
        }
    }

    /**
     * Generar reporte de ventas mensuales
     */
    @GetMapping("/mensual")
    public ResponseEntity<?> generarReporteMensual(
            @RequestParam(required = false) String mes,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            YearMonth yearMonth;
            if (mes != null && !mes.isEmpty()) {
                yearMonth = YearMonth.parse(mes);
            } else {
                yearMonth = YearMonth.now();
            }

            Map<String, Object> reporte = reporteVentaService.generarReporteVentasMensuales(yearMonth);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Formato de mes inválido. Use formato YYYY-MM: " + e.getMessage()));
        }
    }

    /**
     * Generar reporte de ventas por rango de fechas
     */
    @GetMapping("/rango")
    public ResponseEntity<?> generarReportePorRango(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio);
            LocalDateTime fin = LocalDateTime.parse(fechaFin);

            // Validar que el rango no sea mayor a 1 año
            if (inicio.plusYears(1).isBefore(fin)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El rango de fechas no puede ser mayor a 1 año"));
            }

            Map<String, Object> reporte = reporteVentaService.generarReporteVentasPorRango(inicio, fin);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Formato de fecha inválido. Use formato ISO: " + e.getMessage()));
        }
    }

    /**
     * Obtener resumen de ventas para dashboard
     */
    @GetMapping("/resumen-dashboard")
    public ResponseEntity<?> obtenerResumenDashboard(
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            Map<String, Object> resumen = reporteVentaService.obtenerResumenVentasDashboard();
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al generar resumen: " + e.getMessage()));
        }
    }

    /**
     * Generar reporte de productos más vendidos
     */
    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<?> generarReporteProductosMasVendidos(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio);
            LocalDateTime fin = LocalDateTime.parse(fechaFin);

            Map<String, Object> reporte = reporteVentaService.generarReporteVentasPorRango(inicio, fin);

            // Extraer solo la información de productos más vendidos
            Map<String, Object> reporteProductos = Map.of(
                    "fechaInicio", inicio,
                    "fechaFin", fin,
                    "productosMasVendidos", reporte.get("productosMasVendidos")
            );

            return ResponseEntity.ok(reporteProductos);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Formato de fecha inválido. Use formato ISO: " + e.getMessage()));
        }
    }

    /**
     * Exportar reporte a formato Excel (endpoint preparado)
     */
    @GetMapping("/exportar-excel")
    public ResponseEntity<?> exportarReporteExcel(
            @RequestParam String tipoReporte,
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesion(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            // Este endpoint prepara la estructura para la exportación a Excel
            // La generación real del Excel se hará en el frontend con los datos
            Map<String, Object> datosExportacion = Map.of(
                    "tipoReporte", tipoReporte,
                    "fecha", fecha != null ? fecha : LocalDate.now().toString(),
                    "fechaInicio", fechaInicio,
                    "fechaFin", fechaFin,
                    "mensaje", "Datos preparados para exportación. Use los endpoints de reportes para obtener los datos."
            );

            return ResponseEntity.ok(datosExportacion);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al preparar exportación: " + e.getMessage()));
        }
    }
}