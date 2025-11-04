package com.benefactor.agendaCitas.Servicios;

import com.benefactor.agendaCitas.model.Cita;
import com.benefactor.agendaCitas.model.Empleado;
import com.benefactor.agendaCitas.model.Servicio;
import com.benefactor.agendaCitas.model.HorarioLaboral;
import com.benefactor.agendaCitas.DTO.CitaRequest;
import com.benefactor.agendaCitas.Repository.CitaRepository;
import com.benefactor.agendaCitas.Repository.EmpleadoRepository;
import com.benefactor.agendaCitas.Repository.ServicioRepository;
import com.benefactor.agendaCitas.Repository.HorarioLaboralRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Servicio para gestión de citas del sistema
 * Maneja la creación, validación, actualización y eliminación de citas
 * Implementa lógica de negocio para validación de disponibilidad y horarios laborales
 *
 * @Service Indica que esta clase es un componente de servicio de Spring
 * Proporciona lógica de negocio central para el sistema de citas
 */
@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private HorarioLaboralRepository horarioLaboralRepository;

    /**
     * Obtiene todas las citas del sistema sin filtros
     *
     * @return Lista de todas las citas existentes
     */
    public List<Cita> obtenerTodasCitas() {
        return citaRepository.findAll();
    }

    /**
     * Obtiene las citas de un empleado específico para una fecha particular
     *
     * @param empleadoId ID del empleado cuyas citas se buscan
     * @param fecha Fecha específica para filtrar las citas
     * @return Lista de citas del empleado en la fecha especificada
     */
    public List<Cita> obtenerCitasPorEmpleadoYFecha(Long empleadoId, LocalDate fecha) {
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(LocalTime.MAX);
        return citaRepository.findByEmpleadoIdAndFechaHoraInicioBetween(empleadoId, inicioDia, finDia);
    }

    /**
     * Crea una nueva cita validando todas las reglas de negocio
     * Realiza validaciones de empleado, servicio, disponibilidad y horario laboral
     *
     * @param citaRequest Objeto con los datos de la cita a crear
     * @return Cita creada y guardada en la base de datos
     * @throws RuntimeException Si alguna validación falla
     */
    public Cita crearCita(CitaRequest citaRequest) {
        // 1. Validar que fechaHoraInicio no sea nulo
        if (citaRequest.getFechaHoraInicio() == null) {
            throw new RuntimeException("La fecha y hora de inicio no pueden ser nulas");
        }

        // 2. Validar que el empleado existe
        Empleado empleado = empleadoRepository.findById(citaRequest.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + citaRequest.getEmpleadoId()));

        // 3. Validar que el servicio existe
        Servicio servicio = servicioRepository.findById(citaRequest.getServicioId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con id: " + citaRequest.getServicioId()));

        // 4. Calcular fecha y hora de fin basado en la duración del servicio
        LocalDateTime fechaHoraInicio = citaRequest.getFechaHoraInicio();
        LocalDateTime fechaHoraFin = fechaHoraInicio.plusMinutes(servicio.getDuracionMinutos());

        // 5. Validar disponibilidad del empleado en ese horario
        if (!validarDisponibilidad(empleado.getId(), fechaHoraInicio, fechaHoraFin)) {
            throw new RuntimeException("El empleado no está disponible en el horario seleccionado. Ya existe una cita programada en ese rango de tiempo.");
        }

        // 6. Validar horario laboral del empleado
        if (!validarHorarioLaboral(empleado.getId(), fechaHoraInicio, fechaHoraFin)) {
            throw new RuntimeException("El horario seleccionado está fuera del horario laboral del empleado.");
        }

        // 7. Crear y guardar la cita
        Cita cita = new Cita();
        cita.setClienteNombre(citaRequest.getClienteNombre());
        cita.setClienteCelular(citaRequest.getClienteCelular());
        cita.setEmpleado(empleado);
        cita.setServicio(servicio);
        cita.setFechaHoraInicio(fechaHoraInicio);
        cita.setFechaHoraFin(fechaHoraFin);
        cita.setCostoTotal(servicio.getCosto());
        cita.setEstado(citaRequest.getEstado() != null ? citaRequest.getEstado() : "programada");
        cita.setRecordatorioEnviado(false);

        return citaRepository.save(cita);
    }

    /**
     * Valida la disponibilidad de un empleado en un rango de tiempo específico
     * Verifica que no existan citas que se solapen con el horario propuesto
     *
     * @param empleadoId ID del empleado a validar
     * @param inicio Fecha y hora de inicio propuesta
     * @param fin Fecha y hora de fin propuesta
     * @return true si el empleado está disponible, false si hay conflictos
     */
    private boolean validarDisponibilidad(Long empleadoId, LocalDateTime inicio, LocalDateTime fin) {
        List<Cita> citasConflictivas = citaRepository.findCitasEnConflicto(empleadoId, inicio, fin);
        return citasConflictivas.isEmpty();
    }

    /**
     * Valida que el horario propuesto esté dentro del horario laboral del empleado
     * Verifica que la cita completa esté contenida dentro de los horarios laborales
     *
     * @param empleadoId ID del empleado a validar
     * @param inicio Fecha y hora de inicio propuesta
     * @param fin Fecha y hora de fin propuesta
     * @return true si el horario está dentro del horario laboral, false en caso contrario
     */
    private boolean validarHorarioLaboral(Long empleadoId, LocalDateTime inicio, LocalDateTime fin) {
        int diaSemana = inicio.getDayOfWeek().getValue();
        List<HorarioLaboral> horarios = horarioLaboralRepository.findByEmpleadoIdAndDiaSemanaAndActivoTrue(empleadoId, diaSemana);

        // Si no tiene horario para ese día, no puede trabajar
        if (horarios.isEmpty()) {
            return false;
        }

        LocalTime horaInicioCita = inicio.toLocalTime();
        LocalTime horaFinCita = fin.toLocalTime();

        // Verificar que la cita esté completamente dentro de algún horario laboral
        for (HorarioLaboral horario : horarios) {
            if (!horaInicioCita.isBefore(horario.getHoraInicio()) &&
                    !horaFinCita.isAfter(horario.getHoraFin())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene los horarios disponibles de un empleado para una fecha y servicio específicos
     * Calcula intervalos de 30 minutos dentro del horario laboral que estén disponibles
     *
     * @param empleadoId ID del empleado
     * @param fecha Fecha para la cual se buscan horarios disponibles
     * @param servicioId ID del servicio que determina la duración de la cita
     * @return Lista de horarios disponibles para agendar
     */
    public List<LocalTime> obtenerHorariosDisponibles(Long empleadoId, LocalDate fecha, Long servicioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        List<HorarioLaboral> horariosLaborales = horarioLaboralRepository
                .findByEmpleadoIdAndDiaSemanaAndActivoTrue(empleadoId, fecha.getDayOfWeek().getValue());

        List<LocalTime> horariosDisponibles = new ArrayList<>();

        // Para cada horario laboral del empleado en ese día
        for (HorarioLaboral horario : horariosLaborales) {
            LocalTime horaActual = horario.getHoraInicio();

            // Generar horarios cada 30 minutos mientras quepa el servicio completo
            while (horaActual.plusMinutes(servicio.getDuracionMinutos()).isBefore(horario.getHoraFin()) ||
                    horaActual.plusMinutes(servicio.getDuracionMinutos()).equals(horario.getHoraFin())) {

                LocalDateTime inicioPropuesto = fecha.atTime(horaActual);
                LocalDateTime finPropuesto = inicioPropuesto.plusMinutes(servicio.getDuracionMinutos());

                // Verificar disponibilidad en este horario específico
                if (validarDisponibilidad(empleadoId, inicioPropuesto, finPropuesto)) {
                    horariosDisponibles.add(horaActual);
                }

                horaActual = horaActual.plusMinutes(30); // Intervalos de 30 minutos
            }
        }

        return horariosDisponibles;
    }

    /**
     * Actualiza el estado de una cita existente
     * Permite cambiar estados como: programada, cumplida, cancelada
     *
     * @param citaId ID de la cita a actualizar
     * @param nuevoEstado Nuevo estado a asignar a la cita
     * @return Cita actualizada
     * @throws RuntimeException Si la cita no existe
     */
    public Cita actualizarEstadoCita(Long citaId, String nuevoEstado) {
        return citaRepository.findById(citaId)
                .map(cita -> {
                    cita.setEstado(nuevoEstado);
                    return citaRepository.save(cita);
                })
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
    }

    /**
     * Elimina una cita del sistema
     *
     * @param id ID de la cita a eliminar
     * @throws RuntimeException Si la cita no existe
     */
    public void eliminarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        citaRepository.delete(cita);
    }
}