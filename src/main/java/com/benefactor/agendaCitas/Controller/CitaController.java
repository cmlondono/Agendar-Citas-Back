package com.benefactor.agendaCitas.Controller;

import com.benefactor.agendaCitas.model.Cita;
import com.benefactor.agendaCitas.DTO.CitaRequest;
import com.benefactor.agendaCitas.DTO.DisponibilidadRequest;
import com.benefactor.agendaCitas.Servicios.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador para operaciones relacionadas con citas
 * Maneja CRUD de citas, verificación de disponibilidad y gestión de estados
 *
 * @RestController Indica que esta clase es un controlador REST
 * @RequestMapping("/api/citas") Define la ruta base para endpoints de citas
 */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    /**
     * Endpoint para obtener todas las citas del sistema
     *
     * @return Lista de todas las citas existentes
     */
    @GetMapping
    public List<Cita> obtenerTodasCitas() {
        return citaService.obtenerTodasCitas();
    }

    /**
     * Endpoint para verificar disponibilidad de un empleado en una fecha específica
     * Determina si hay horarios disponibles para agendar una cita
     *
     * @param request Objeto con datos de verificación (empleadoId, fecha)
     * @return ResponseEntity indicando disponibilidad o error
     */
    @PostMapping("/verificar-disponibilidad")
    public ResponseEntity<?> verificarDisponibilidad(@RequestBody DisponibilidadRequest request) {
        try {
            // Verificar horarios disponibles para el empleado en la fecha especificada
            // Se utiliza un servicioId temporal (1L) hasta implementar la lógica completa
            List<LocalTime> horariosDisponibles = citaService.obtenerHorariosDisponibles(
                    request.getEmpleadoId(), request.getFecha(), 1L);

            // Retorna true si hay horarios disponibles, false si no hay disponibilidad
            return ResponseEntity.ok().body(Map.of("disponible", !horariosDisponibles.isEmpty()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para crear una nueva cita
     * Valida disponibilidad y crea la cita en el sistema
     *
     * @param citaRequest Objeto con los datos de la cita a crear
     * @return ResponseEntity con la cita creada o error de validación
     */
    @PostMapping
    public ResponseEntity<?> crearCita(@RequestBody CitaRequest citaRequest) {
        try {
            Cita cita = citaService.crearCita(citaRequest);
            return ResponseEntity.ok(cita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para actualizar el estado de una cita existente
     * Permite cambiar estados como: programada, cumplida, cancelada
     *
     * @param id ID de la cita a actualizar
     * @param request Mapa con el nuevo estado a asignar
     * @return ResponseEntity con la cita actualizada o error
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoCita(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String nuevoEstado = request.get("estado");
            Cita cita = citaService.actualizarEstadoCita(id, nuevoEstado);
            return ResponseEntity.ok(cita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para eliminar una cita del sistema
     * Remueve permanentemente la cita especificada
     *
     * @param id ID de la cita a eliminar
     * @return ResponseEntity confirmando la eliminación o error
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCita(@PathVariable Long id) {
        try {
            citaService.eliminarCita(id);
            return ResponseEntity.ok().body(Map.of("mensaje", "Cita eliminada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}