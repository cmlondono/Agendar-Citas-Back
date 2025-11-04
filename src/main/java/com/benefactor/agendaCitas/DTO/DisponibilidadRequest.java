package com.benefactor.agendaCitas.DTO;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) para solicitudes de verificación de disponibilidad
 * Representa los datos necesarios para consultar la disponibilidad de un empleado
 * en una fecha específica del sistema de citas
 *
 * Se utiliza en los endpoints del controlador de citas para verificar horarios disponibles
 * antes de agendar una nueva cita
 */
public class DisponibilidadRequest {

    /**
     * ID del empleado cuya disponibilidad se desea verificar
     * Referencia al profesional para el cual se consultan horarios disponibles
     * Este campo es requerido para identificar al empleado específico
     */
    private Long empleadoId;

    /**
     * Fecha específica para la cual se verifica la disponibilidad
     * Representa el día particular que el cliente desea agendar
     * Utiliza LocalDate para manejar solo la fecha sin hora específica
     */
    private LocalDate fecha;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto requerido para la deserialización JSON
     * Framework como Jackson utilizan este constructor para crear instancias
     * desde solicitudes HTTP entrantes
     */
    public DisponibilidadRequest() {}

    /**
     * Constructor completo para crear instancias con todos los campos
     * Útil para testing y creación programática de objetos
     *
     * @param empleadoId ID del empleado cuya disponibilidad se verifica
     * @param fecha Fecha específica para la verificación de disponibilidad
     */
    public DisponibilidadRequest(Long empleadoId, LocalDate fecha) {
        this.empleadoId = empleadoId;
        this.fecha = fecha;
    }

    // ========== GETTERS Y SETTERS ==========

    /**
     * Obtiene el ID del empleado para la verificación de disponibilidad
     *
     * @return ID del empleado cuya disponibilidad se consulta
     */
    public Long getEmpleadoId() { return empleadoId; }

    /**
     * Establece el ID del empleado para la verificación de disponibilidad
     *
     * @param empleadoId ID del empleado cuya disponibilidad se consulta
     */
    public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }

    /**
     * Obtiene la fecha para la cual se verifica la disponibilidad
     *
     * @return Fecha específica de consulta de disponibilidad
     */
    public LocalDate getFecha() { return fecha; }

    /**
     * Establece la fecha para la cual se verifica la disponibilidad
     *
     * @param fecha Fecha específica de consulta de disponibilidad
     */
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
}