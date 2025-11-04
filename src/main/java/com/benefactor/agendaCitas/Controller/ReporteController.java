package com.benefactor.agendaCitas.Controller;

import com.benefactor.agendaCitas.DTO.ReporteRequest;
import com.benefactor.agendaCitas.Servicios.AuthService;
import com.benefactor.agendaCitas.Servicios.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para operaciones relacionadas con generación de reportes
 * Maneja reportes de citas, resúmenes estadísticos y análisis de datos
 * Requiere autenticación mediante cookie de sesión para todos los endpoints
 *
 * @RestController Indica que esta clase es un controlador REST
 * @RequestMapping("/api/reportes") Define la ruta base para endpoints de reportes
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private AuthService authService;

    /**
     * Endpoint para generar reportes detallados de citas
     * Procesa datos basados en fechas, tipo de reporte y filtros opcionales
     * Requiere autenticación mediante cookie de sesión
     *
     * @param reporteRequest Objeto con parámetros del reporte (fechas, tipo, empleadoId)
     * @param sessionId Cookie de sesión para validar autenticación
     * @return ResponseEntity con el reporte generado o error de validación
     */
    @PostMapping("/citas")
    public ResponseEntity<?> generarReporteCitas(
            @RequestBody ReporteRequest reporteRequest,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        try {
            // Validar sesión del usuario
            if (!authService.validarSesion(sessionId)) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            // Validar parámetros requeridos
            if (reporteRequest.getFechaInicio() == null || reporteRequest.getFechaFin() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Fechas de inicio y fin son requeridas"));
            }

            if (reporteRequest.getTipoReporte() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tipo de reporte es requerido"));
            }

            // Generar reporte utilizando el servicio
            Map<String, Object> reporte = reporteService.generarReporteCitas(
                    reporteRequest.getFechaInicio(),
                    reporteRequest.getFechaFin(),
                    reporteRequest.getTipoReporte(),
                    reporteRequest.getEmpleadoId()
            );

            return ResponseEntity.ok(reporte);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al generar reporte: " + e.getMessage()));
        }
    }

    /**
     * Endpoint principal para obtener resumen completo del sistema
     * Proporciona estadísticas generales como citas del día, ingresos, etc.
     * Requiere autenticación mediante cookie de sesión
     *
     * @param sessionId Cookie de sesión para validar autenticación
     * @return ResponseEntity con resumen completo del sistema o error
     */
    @GetMapping("/resumen-completo")
    public ResponseEntity<?> obtenerResumenCompleto(
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        try {
            // Validar sesión del usuario
            if (!authService.validarSesion(sessionId)) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            Map<String, Object> resumen = reporteService.obtenerResumenCompleto();
            return ResponseEntity.ok(resumen);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al obtener resumen: " + e.getMessage()));
        }
    }

    /**
     * Endpoint de respaldo para obtener resumen del sistema
     * Funciona como alternativa al endpoint principal en caso de problemas
     * Requiere autenticación mediante cookie de sesión
     *
     * @param sessionId Cookie de sesión para validar autenticación
     * @return ResponseEntity con resumen del sistema o error
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumen(
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        try {
            // Validar sesión del usuario
            if (!authService.validarSesion(sessionId)) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            // Utiliza el mismo servicio que el endpoint principal
            Map<String, Object> resumen = reporteService.obtenerResumenCompleto();
            return ResponseEntity.ok(resumen);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al obtener resumen: " + e.getMessage()));
        }
    }
}