package com.benefactor.agendaCitas.Controller;

import com.benefactor.agendaCitas.Servicios.AuthService;
import com.benefactor.agendaCitas.Servicios.RecordatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Controlador para operaciones relacionadas con recordatorios de citas
 * Maneja la obtención y cierre de recordatorios activos del sistema
 * Los endpoints son accesibles sin autenticación para facilitar el acceso
 *
 * @RestController Indica que esta clase es un controlador REST
 * @RequestMapping("/api/recordatorios") Define la ruta base para endpoints de recordatorios
 */
@RestController
@RequestMapping("/api/recordatorios")
public class RecordatorioController {

    @Autowired
    private RecordatorioService recordatorioService;

    @Autowired
    private AuthService authService;

    /**
     * Endpoint para obtener todos los recordatorios activos del sistema
     * Proporciona una lista de recordatorios pendientes que requieren atención
     * Este endpoint es accesible sin autenticación para facilitar el acceso
     *
     * @param sessionId Cookie de sesión (opcional, no requerida para este endpoint)
     * @return ResponseEntity con lista de recordatorios activos o error
     */
    @GetMapping
    public ResponseEntity<?> obtenerRecordatoriosActivos(
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        try {
            // Obtener todos los recordatorios activos del sistema
            // No requiere autenticación para permitir acceso público
            var recordatorios = recordatorioService.obtenerRecordatoriosActivos();
            return ResponseEntity.ok(recordatorios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para cerrar/marcar como atendido un recordatorio específico
     * Permite indicar que un recordatorio ha sido revisado y no requiere más atención
     * Este endpoint es accesible sin autenticación para facilitar su uso
     *
     * @param citaId ID de la cita asociada al recordatorio a cerrar
     * @param sessionId Cookie de sesión (opcional, no requerida para este endpoint)
     * @return ResponseEntity confirmando el cierre del recordatorio o error
     */
    @PostMapping("/{citaId}/cerrar")
    public ResponseEntity<?> cerrarRecordatorio(
            @PathVariable Long citaId,
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        try {
            // Cerrar el recordatorio asociado a la cita especificada
            // No requiere autenticación para permitir acceso público
            recordatorioService.cerrarRecordatorio(citaId);
            return ResponseEntity.ok().body(Map.of("mensaje", "Recordatorio cerrado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}