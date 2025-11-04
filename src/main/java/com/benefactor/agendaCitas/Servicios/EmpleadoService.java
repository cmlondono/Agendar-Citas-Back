package com.benefactor.agendaCitas.Servicios;

import com.benefactor.agendaCitas.DTO.EmpleadoDTO;
import com.benefactor.agendaCitas.model.Empleado;
import com.benefactor.agendaCitas.model.HorarioLaboral;
import com.benefactor.agendaCitas.Repository.EmpleadoRepository;
import com.benefactor.agendaCitas.Repository.HorarioLaboralRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de empleados y sus horarios laborales
 * Maneja operaciones CRUD de empleados y la administración de sus horarios de trabajo
 * Proporciona métodos tanto con DTOs para la API como con entidades para uso interno
 *
 * @Service Indica que esta clase es un componente de servicio de Spring
 * Proporciona lógica de negocio para la gestión de empleados del sistema
 */
@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private HorarioLaboralRepository horarioLaboralRepository;

    // ========== MÉTODOS CON DTOs (PARA LA API) ==========

    /**
     * Obtiene todos los empleados activos en formato DTO
     * Utilizado por la API para proporcionar datos seguros al frontend
     *
     * @return Lista de empleados activos convertidos a DTO
     */
    public List<EmpleadoDTO> obtenerTodosEmpleadosActivosDTO() {
        return empleadoRepository.findByActivoTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un empleado específico por ID en formato DTO
     *
     * @param id ID del empleado a buscar
     * @return Optional con el empleado convertido a DTO si existe
     */
    public Optional<EmpleadoDTO> obtenerEmpleadoPorIdDTO(Long id) {
        return empleadoRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Crea un nuevo empleado y retorna el resultado en formato DTO
     *
     * @param empleado Entidad empleado con los datos a crear
     * @return Empleado creado convertido a DTO
     */
    public EmpleadoDTO crearEmpleadoDTO(Empleado empleado) {
        Empleado empleadoGuardado = empleadoRepository.save(empleado);
        return convertToDTO(empleadoGuardado);
    }

    /**
     * Actualiza un empleado existente y retorna el resultado en formato DTO
     *
     * @param id ID del empleado a actualizar
     * @param empleadoActualizado Entidad con los datos actualizados
     * @return Empleado actualizado convertido a DTO
     * @throws RuntimeException Si el empleado no existe
     */
    public EmpleadoDTO actualizarEmpleadoDTO(Long id, Empleado empleadoActualizado) {
        return empleadoRepository.findById(id)
                .map(empleado -> {
                    empleado.setNombre(empleadoActualizado.getNombre());
                    empleado.setActivo(empleadoActualizado.getActivo());
                    Empleado empleadoActualizadoEntity = empleadoRepository.save(empleado);
                    return convertToDTO(empleadoActualizadoEntity);
                })
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
    }

    /**
     * Realiza una eliminación lógica de un empleado (cambia estado a inactivo)
     *
     * @param id ID del empleado a eliminar
     * @return Empleado eliminado convertido a DTO
     * @throws RuntimeException Si el empleado no existe
     */
    public EmpleadoDTO eliminarEmpleadoDTO(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
        empleado.setActivo(false);
        Empleado empleadoEliminado = empleadoRepository.save(empleado);
        return convertToDTO(empleadoEliminado);
    }

    // ========== MÉTODOS CON ENTIDADES (PARA USO INTERNO) ==========

    /**
     * Obtiene todos los empleados del sistema (incluyendo inactivos)
     *
     * @return Lista completa de empleados
     */
    public List<Empleado> obtenerTodosEmpleados() {
        return empleadoRepository.findAll();
    }

    /**
     * Obtiene solo los empleados activos del sistema
     *
     * @return Lista de empleados con estado activo
     */
    public List<Empleado> obtenerEmpleadosActivos() {
        return empleadoRepository.findByActivoTrue();
    }

    /**
     * Obtiene un empleado específico por ID
     *
     * @param id ID del empleado a buscar
     * @return Optional con el empleado si existe
     */
    public Optional<Empleado> obtenerEmpleadoPorId(Long id) {
        return empleadoRepository.findById(id);
    }

    /**
     * Guarda un empleado en la base de datos
     *
     * @param empleado Entidad empleado a guardar
     * @return Empleado guardado
     */
    public Empleado guardarEmpleado(Empleado empleado) {
        return empleadoRepository.save(empleado);
    }

    /**
     * Actualiza un empleado existente
     *
     * @param id ID del empleado a actualizar
     * @param empleadoActualizado Entidad con los datos actualizados
     * @return Empleado actualizado
     * @throws RuntimeException Si el empleado no existe
     */
    public Empleado actualizarEmpleado(Long id, Empleado empleadoActualizado) {
        return empleadoRepository.findById(id)
                .map(empleado -> {
                    empleado.setNombre(empleadoActualizado.getNombre());
                    empleado.setActivo(empleadoActualizado.getActivo());
                    return empleadoRepository.save(empleado);
                })
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
    }

    /**
     * Realiza eliminación lógica de un empleado (cambia estado a inactivo)
     *
     * @param id ID del empleado a eliminar
     * @throws RuntimeException Si el empleado no existe
     */
    public void eliminarEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
        empleado.setActivo(false);
        empleadoRepository.save(empleado);
    }

    // ========== MÉTODOS DE HORARIOS LABORALES ==========

    /**
     * Agrega un horario laboral para un empleado
     * Realiza validaciones de día de la semana y rango de horas
     *
     * @param empleadoId ID del empleado
     * @param horario Objeto horario laboral a agregar
     * @return Horario laboral guardado
     * @throws RuntimeException Si las validaciones fallan o el empleado no existe
     */
    public HorarioLaboral agregarHorarioLaboral(Long empleadoId, HorarioLaboral horario) {
        // Validaciones básicas del día de la semana
        if (horario.getDiaSemana() == null || horario.getDiaSemana() < 1 || horario.getDiaSemana() > 7) {
            throw new RuntimeException("El campo 'diaSemana' debe ser un valor entre 1 y 7");
        }

        // Validaciones de horas requeridas
        if (horario.getHoraInicio() == null || horario.getHoraFin() == null) {
            throw new RuntimeException("Las horas de inicio y fin son requeridas");
        }

        // Validación de rango de horas
        if (horario.getHoraFin().isBefore(horario.getHoraInicio()) ||
                horario.getHoraFin().equals(horario.getHoraInicio())) {
            throw new RuntimeException("La hora de fin debe ser mayor que la hora de inicio");
        }

        // Verificar existencia del empleado
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        horario.setEmpleado(empleado);
        horario.setActivo(true);

        return horarioLaboralRepository.save(horario);
    }

    /**
     * Obtiene todos los horarios laborales activos de un empleado
     *
     * @param empleadoId ID del empleado
     * @return Lista de horarios laborales activos
     */
    public List<HorarioLaboral> obtenerHorariosLaborales(Long empleadoId) {
        return horarioLaboralRepository.findByEmpleadoIdAndActivoTrue(empleadoId);
    }

    /**
     * Elimina un horario laboral específico (eliminación lógica)
     *
     * @param horarioId ID del horario a eliminar
     * @throws RuntimeException Si el horario no existe
     */
    public void eliminarHorarioLaboral(Long horarioId) {
        HorarioLaboral horario = horarioLaboralRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        horario.setActivo(false);
        horarioLaboralRepository.save(horario);
    }

    /**
     * Agrega múltiples horarios laborales para diferentes días
     * Valida cada horario individualmente antes de guardar
     *
     * @param empleadoId ID del empleado
     * @param horarios Lista de horarios laborales a agregar
     * @throws RuntimeException Si las validaciones fallan o el empleado no existe
     */
    public void agregarHorariosLaborales(Long empleadoId, List<HorarioLaboral> horarios) {
        // Verificar existencia del empleado
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        // Validar y guardar cada horario
        for (HorarioLaboral horario : horarios) {
            // Validaciones básicas del día de la semana
            if (horario.getDiaSemana() == null || horario.getDiaSemana() < 1 || horario.getDiaSemana() > 7) {
                throw new RuntimeException("El campo 'diaSemana' debe ser un valor entre 1 y 7");
            }

            horario.setEmpleado(empleado);
            horario.setActivo(true);
            horarioLaboralRepository.save(horario);
        }
    }

    /**
     * Elimina todos los horarios laborales de un empleado (eliminación lógica)
     *
     * @param empleadoId ID del empleado
     */
    public void eliminarTodosHorariosLaborales(Long empleadoId) {
        List<HorarioLaboral> horarios = horarioLaboralRepository.findByEmpleadoIdAndActivoTrue(empleadoId);

        for (HorarioLaboral horario : horarios) {
            horario.setActivo(false);
            horarioLaboralRepository.save(horario);
        }
    }

    /**
     * Obtiene el horario laboral de un empleado para un día específico
     *
     * @param empleadoId ID del empleado
     * @param diaSemana Día de la semana (1=Lunes, 7=Domingo)
     * @return Optional con el horario laboral si existe
     */
    public Optional<HorarioLaboral> obtenerHorarioPorDia(Long empleadoId, Integer diaSemana) {
        return horarioLaboralRepository.findByEmpleadoIdAndDiaSemanaAndActivoTrue(empleadoId, diaSemana)
                .stream()
                .findFirst();
    }

    // ========== MÉTODOS DE VERIFICACIÓN DE DISPONIBILIDAD ==========

    /**
     * Verifica la disponibilidad básica de un empleado (placeholder)
     *
     * @param empleadoId ID del empleado
     * @param fecha Fecha a verificar
     * @return true siempre (implementación básica)
     */
    public boolean verificarDisponibilidad(Long empleadoId, String fecha) {
        return true; // Placeholder para implementación futura
    }

    /**
     * Verifica si un empleado trabaja en un día específico de la semana
     *
     * @param empleadoId ID del empleado
     * @param diaSemana Día de la semana (1=Lunes, 7=Domingo)
     * @return true si el empleado trabaja ese día, false en caso contrario
     */
    public boolean trabajaElDia(Long empleadoId, Integer diaSemana) {
        List<HorarioLaboral> horarios = horarioLaboralRepository
                .findByEmpleadoIdAndDiaSemanaAndActivoTrue(empleadoId, diaSemana);
        return !horarios.isEmpty();
    }

    // ========== MÉTODO DE CONVERSIÓN PRIVADO ==========

    /**
     * Convierte una entidad Empleado a su representación DTO
     *
     * @param empleado Entidad empleado a convertir
     * @return DTO con los datos del empleado
     */
    private EmpleadoDTO convertToDTO(Empleado empleado) {
        return new EmpleadoDTO(
                empleado.getId(),
                empleado.getNombre(),
                empleado.getActivo(),
                empleado.getFechaCreacion()
        );
    }
}