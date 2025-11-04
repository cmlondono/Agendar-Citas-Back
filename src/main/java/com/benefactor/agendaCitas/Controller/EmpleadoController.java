package com.benefactor.agendaCitas.Controller;

import com.benefactor.agendaCitas.DTO.EmpleadoDTO;
import com.benefactor.agendaCitas.Repository.CitaRepository;
import com.benefactor.agendaCitas.Repository.HorarioLaboralRepository;
import com.benefactor.agendaCitas.model.Cita;
import com.benefactor.agendaCitas.model.Empleado;
import com.benefactor.agendaCitas.model.HorarioLaboral;
import com.benefactor.agendaCitas.Servicios.AuthService;
import com.benefactor.agendaCitas.Servicios.EmpleadoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para operaciones relacionadas con empleados
 * Maneja CRUD de empleados, horarios laborales y verificación de disponibilidad
 * Incluye endpoints públicos y endpoints administrativos con autenticación
 *
 * @RestController Indica que esta clase es un controlador REST
 * @RequestMapping("/api/empleados") Define la ruta base para endpoints de empleados
 */
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private AuthService authService;

    @Autowired
    private HorarioLaboralRepository horarioLaboralRepository;

    @Autowired
    private CitaRepository citaRepository;

    // ========== ENDPOINTS PÚBLICOS (SIN AUTENTICACIÓN) ==========

    /**
     * Endpoint para obtener todos los empleados activos en formato DTO
     * Proporciona información básica de empleados para selección en el frontend
     *
     * @return ResponseEntity con lista de empleados activos o error
     */
    @GetMapping
    public ResponseEntity<List<EmpleadoDTO>> obtenerEmpleadosActivos() {
        try {
            List<EmpleadoDTO> empleados = empleadoService.obtenerTodosEmpleadosActivosDTO();
            return ResponseEntity.ok(empleados);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint para obtener un empleado específico por ID en formato DTO
     *
     * @param id ID del empleado a buscar
     * @return ResponseEntity con datos del empleado o error si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerEmpleadoPorId(@PathVariable Long id) {
        try {
            EmpleadoDTO empleado = empleadoService.obtenerEmpleadoPorIdDTO(id)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
            return ResponseEntity.ok(empleado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener los horarios laborales de un empleado
     * Retorna la configuración de días y horarios de trabajo
     *
     * @param id ID del empleado
     * @return ResponseEntity con lista de horarios laborales o error
     */
    @GetMapping("/{id}/horarios")
    public ResponseEntity<List<HorarioLaboral>> obtenerHorariosEmpleado(@PathVariable Long id) {
        try {
            List<HorarioLaboral> horarios = empleadoService.obtenerHorariosLaborales(id);
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint simplificado para verificar disponibilidad básica de un empleado
     * Versión básica que siempre retorna disponible para demostración
     *
     * @param id ID del empleado
     * @param fecha Fecha a verificar en formato string
     * @return ResponseEntity indicando disponibilidad básica
     */
    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<?> verificarDisponibilidadEmpleado(
            @PathVariable Long id,
            @RequestParam String fecha) {
        try {
            // Lógica simplificada - siempre disponible para demostración
            return ResponseEntity.ok().body(Map.of("disponible", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint detallado para obtener disponibilidad horaria de un empleado
     * Calcula franjas horarias disponibles basado en horario laboral y citas existentes
     *
     * @param empleadoId ID del empleado
     * @param fecha Fecha específica para verificar disponibilidad
     * @return ResponseEntity con franjas horarias disponibles o error
     */
    @GetMapping("/{empleadoId}/disponibilidad-horaria")
    public ResponseEntity<?> obtenerDisponibilidadHoraria(
            @PathVariable Long empleadoId,
            @RequestParam LocalDate fecha) {
        try {
            // Obtener horario laboral del empleado para el día específico
            int diaSemana = fecha.getDayOfWeek().getValue();
            List<HorarioLaboral> horariosLaborales = horarioLaboralRepository
                    .findByEmpleadoIdAndDiaSemanaAndActivoTrue(empleadoId, diaSemana);

            // Verificar si el empleado trabaja ese día
            if (horariosLaborales.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "empleadoId", empleadoId,
                        "fecha", fecha,
                        "disponible", false,
                        "mensaje", "El empleado no trabaja este día"
                ));
            }

            // Obtener citas existentes para el día especificado
            LocalDateTime inicioDia = fecha.atStartOfDay();
            LocalDateTime finDia = fecha.atTime(LocalTime.MAX);
            List<Cita> citasDelDia = citaRepository.findByEmpleadoIdAndFechaHoraInicioBetween(
                    empleadoId, inicioDia, finDia);

            // Generar franjas horarias de 30 minutos
            List<Map<String, Object>> franjasHorarias = new ArrayList<>();
            LocalTime horaActual = horariosLaborales.get(0).getHoraInicio();
            LocalTime horaFin = horariosLaborales.get(0).getHoraFin();

            while (horaActual.isBefore(horaFin)) {
                LocalDateTime inicioFranja = fecha.atTime(horaActual);
                LocalDateTime finFranja = inicioFranja.plusMinutes(30);

                // Verificar si la franja horaria está disponible (sin citas conflictivas)
                boolean disponible = citasDelDia.stream().noneMatch(cita ->
                        !(finFranja.isBefore(cita.getFechaHoraInicio()) ||
                                inicioFranja.isAfter(cita.getFechaHoraFin()))
                );

                franjasHorarias.add(Map.of(
                        "hora", horaActual.toString(),
                        "disponible", disponible,
                        "horaFin", horaActual.plusMinutes(30).toString()
                ));

                horaActual = horaActual.plusMinutes(30);
            }

            return ResponseEntity.ok(Map.of(
                    "empleadoId", empleadoId,
                    "fecha", fecha.toString(),
                    "horarioLaboral", Map.of(
                            "horaInicio", horariosLaborales.get(0).getHoraInicio().toString(),
                            "horaFin", horariosLaborales.get(0).getHoraFin().toString()
                    ),
                    "franjasHorarias", franjasHorarias
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== ENDPOINTS DE ADMINISTRADOR (REQUIEREN AUTENTICACIÓN) ==========

    /**
     * Endpoint para crear un nuevo empleado
     * Requiere autenticación mediante cookie de sesión
     *
     * @param empleado Objeto empleado con datos a crear
     * @param sessionId Cookie de sesión para validar autenticación
     * @param request Objeto HttpServletRequest para información de la solicitud
     * @return ResponseEntity con el empleado creado o error de autenticación
     */
    @PostMapping
    public ResponseEntity<?> crearEmpleado(
            @RequestBody Empleado empleado,
            @CookieValue(value = "sessionId", required = false) String sessionId,
            HttpServletRequest request) {

        try {
            // Validar sesión del usuario
            if (!authService.validarSesion(sessionId)) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            EmpleadoDTO nuevoEmpleado = empleadoService.crearEmpleadoDTO(empleado);
            return ResponseEntity.ok(nuevoEmpleado);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para actualizar datos de un empleado existente
     * Requiere autenticación mediante cookie de sesión
     *
     * @param id ID del empleado a actualizar
     * @param empleadoActualizado Objeto con datos actualizados
     * @param sessionId Cookie de sesión para validar autenticación
     * @param request Objeto HttpServletRequest para información de la solicitud
     * @return ResponseEntity con el empleado actualizado o error
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarEmpleado(
            @PathVariable Long id,
            @RequestBody Empleado empleadoActualizado,
            @CookieValue(value = "sessionId", required = false) String sessionId,
            HttpServletRequest request) {

        try {
            // Validar sesión del usuario
            if (!authService.validarSesion(sessionId)) {
                return ResponseEntity.status(401).body(Map.of("error", "Sesión expirada o no válida"));
            }

            EmpleadoDTO empleado = empleadoService.actualizarEmpleadoDTO(id, empleadoActualizado);
            return ResponseEntity.ok(empleado);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para eliminar un empleado del sistema
     * Requiere autenticación mediante cookie de sesión
     *
     * @param id ID del empleado a eliminar
     * @param sessionId Cookie de sesión para validar autenticación
     * @return ResponseEntity confirmando eliminación o error
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarEmpleado(
            @PathVariable Long id,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        try {
            // Validar sesión del usuario
            if (!authService.validarSesion(sessionId)) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            empleadoService.eliminarEmpleadoDTO(id);
            return ResponseEntity.ok().body(Map.of("mensaje", "Empleado eliminado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para agregar un horario laboral a un empleado
     * Valida que el campo diaSemana esté presente y en rango válido (1-7)
     * Requiere autenticación mediante cookie de sesión
     *
     * @param id ID del empleado
     * @param horario Objeto horario laboral a agregar
     * @param sessionId Cookie de sesión para validar autenticación
     * @return ResponseEntity con el horario creado o error de validación
     */
    @PostMapping("/{id}/horarios")
    public ResponseEntity<?> agregarHorarioLaboral(
            @PathVariable Long id,
            @RequestBody HorarioLaboral horario,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        try {
            // Validar sesión del usuario
            if (!authService.validarSesion(sessionId)) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            // Validar que el horario tenga diaSemana
            if (horario.getDiaSemana() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El campo 'diaSemana' es requerido (1=Lunes, 2=Martes, ..., 7=Domingo)"));
            }

            // Validar rango de diaSemana
            if (horario.getDiaSemana() < 1 || horario.getDiaSemana() > 7) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El campo 'diaSemana' debe ser un valor entre 1 y 7"));
            }

            HorarioLaboral nuevoHorario = empleadoService.agregarHorarioLaboral(id, horario);
            return ResponseEntity.ok(nuevoHorario);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para agregar múltiples horarios laborales a un empleado
     * Valida cada horario individualmente antes de guardar
     * Requiere autenticación mediante cookie de sesión
     *
     * @param id ID del empleado
     * @param horarios Lista de horarios laborales a agregar
     * @param sessionId Cookie de sesión para validar autenticación
     * @return ResponseEntity confirmando la operación o error de validación
     */
    @PostMapping("/{id}/horarios-multiples")
    public ResponseEntity<?> agregarHorariosLaborales(
            @PathVariable Long id,
            @RequestBody List<HorarioLaboral> horarios,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        try {
            // Validar sesión del usuario
            if (!authService.validarSesion(sessionId)) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            // Validar cada horario individualmente
            for (int i = 0; i < horarios.size(); i++) {
                HorarioLaboral horario = horarios.get(i);
                if (horario.getDiaSemana() == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "El campo 'diaSemana' es requerido para el horario en posición " + i));
                }
                if (horario.getDiaSemana() < 1 || horario.getDiaSemana() > 7) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "El campo 'diaSemana' debe ser entre 1 y 7 para el horario en posición " + i));
                }
            }

            empleadoService.agregarHorariosLaborales(id, horarios);
            return ResponseEntity.ok().body(Map.of("mensaje", horarios.size() + " horarios agregados correctamente"));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para eliminar un horario laboral específico
     * Requiere autenticación mediante cookie de sesión
     *
     * @param horarioId ID del horario laboral a eliminar
     * @param sessionId Cookie de sesión para validar autenticación
     * @return ResponseEntity confirmando eliminación o error
     */
    @DeleteMapping("/horarios/{horarioId}")
    public ResponseEntity<?> eliminarHorarioLaboral(
            @PathVariable Long horarioId,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        try {
            // Validar sesión del usuario
            if (!authService.validarSesion(sessionId)) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            empleadoService.eliminarHorarioLaboral(horarioId);
            return ResponseEntity.ok().body(Map.of("mensaje", "Horario eliminado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para eliminar todos los horarios laborales de un empleado
     * Requiere autenticación mediante cookie de sesión
     *
     * @param id ID del empleado
     * @param sessionId Cookie de sesión para validar autenticación
     * @return ResponseEntity confirmando eliminación o error
     */
    @DeleteMapping("/{id}/horarios")
    public ResponseEntity<?> eliminarTodosHorariosLaborales(
            @PathVariable Long id,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        try {
            // Validar sesión del usuario
            if (!authService.validarSesion(sessionId)) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            empleadoService.eliminarTodosHorariosLaborales(id);
            return ResponseEntity.ok().body(Map.of("mensaje", "Todos los horarios eliminados correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para verificar si un empleado trabaja en un día específico de la semana
     *
     * @param id ID del empleado
     * @param diaSemana Día de la semana (1=Lunes, 7=Domingo)
     * @return ResponseEntity con información sobre el día laboral
     */
    @GetMapping("/{id}/trabaja-dia/{diaSemana}")
    public ResponseEntity<?> verificarTrabajaDia(
            @PathVariable Long id,
            @PathVariable Integer diaSemana) {

        try {
            boolean trabaja = empleadoService.trabajaElDia(id, diaSemana);
            String nombreDia = obtenerNombreDia(diaSemana);

            return ResponseEntity.ok().body(Map.of(
                    "empleadoId", id,
                    "diaSemana", diaSemana,
                    "nombreDia", nombreDia,
                    "trabaja", trabaja
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Método auxiliar para obtener el nombre del día basado en el número
     *
     * @param diaSemana Número del día (1-7)
     * @return Nombre del día en español
     */
    private String obtenerNombreDia(Integer diaSemana) {
        switch (diaSemana) {
            case 1: return "Lunes";
            case 2: return "Martes";
            case 3: return "Miércoles";
            case 4: return "Jueves";
            case 5: return "Viernes";
            case 6: return "Sábado";
            case 7: return "Domingo";
            default: return "Desconocido";
        }
    }
}