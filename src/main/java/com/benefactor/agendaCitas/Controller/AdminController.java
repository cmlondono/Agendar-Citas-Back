package com.benefactor.agendaCitas.Controller;

import com.benefactor.agendaCitas.Servicios.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Controlador para endpoints administrativos del sistema
 * Maneja operaciones relacionadas con el dashboard y estadísticas generales
 * Requiere autenticación mediante cookie de sesión
 *
 * @RestController Indica que esta clase es un controlador REST
 * @RequestMapping("/api/admin") Define la ruta base para todos los endpoints
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AuthService authService;

    /**
     * Endpoint para obtener datos del dashboard administrativo
     * Proporciona estadísticas generales del sistema para visualización
     *
     * @param sessionId Cookie de sesión para validar autenticación
     * @return ResponseEntity con datos del dashboard o error de autenticación
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> obtenerDashboard(@CookieValue(value = "sessionId", required = false) String sessionId) {
        try {
            // Validar sesión del usuario
            if (!authService.validarSesion(sessionId)) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            // Datos de ejemplo para el dashboard
            // En una implementación real, estos datos vendrían de servicios específicos
            Map<String, Object> dashboard = Map.of(
                    "totalEmpleados", 5,        // Total de empleados en el sistema
                    "totalServicios", 8,        // Total de servicios activos
                    "citasHoy", 3,              // Citas programadas para hoy
                    "gananciasMes", 1250.00     // Ganancias acumuladas del mes
            );

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            // Manejo de errores inesperados
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}