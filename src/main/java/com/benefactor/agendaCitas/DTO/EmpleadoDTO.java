package com.benefactor.agendaCitas.DTO;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para representación simplificada de empleados
 * Proporciona una vista limitada y segura de los datos del empleado para el frontend
 * Excluye información sensible y relaciones complejas que no son necesarias en las vistas
 *
 * Se utiliza en endpoints públicos donde se necesita mostrar información básica de empleados
 * sin exponer detalles internos de la entidad completa
 */
public class EmpleadoDTO {

    /**
     * Identificador único del empleado en el sistema
     * Se mantiene del modelo original para referencias y operaciones
     */
    private Long id;

    /**
     * Nombre completo del empleado para mostrar en interfaces
     * Información básica requerida para identificación del profesional
     */
    private String nombre;

    /**
     * Estado actual del empleado en el sistema
     * true = activo (puede recibir citas), false = inactivo (no disponible)
     * Permite filtrar empleados disponibles sin necesidad de lógica adicional
     */
    private Boolean activo;

    /**
     * Fecha y hora de registro del empleado en el sistema
     * Útil para auditoría y reportes, pero no crítico para operaciones diarias
     * Mantenido por consistencia con el modelo original
     */
    private LocalDateTime fechaCreacion;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto requerido para la deserialización JSON
     * Framework como Jackson utilizan este constructor para crear instancias
     * desde respuestas HTTP entrantes
     */
    public EmpleadoDTO() {}

    /**
     * Constructor completo para crear instancias con todos los campos
     * Utilizado principalmente por el método factory fromEntity
     *
     * @param id Identificador único del empleado
     * @param nombre Nombre completo del empleado
     * @param activo Estado de actividad del empleado
     * @param fechaCreacion Fecha de registro en el sistema
     */
    public EmpleadoDTO(Long id, String nombre, Boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    // ========== MÉTODOS FACTORY ==========

    /**
     * Método factory para crear un EmpleadoDTO desde una entidad Empleado
     * Realiza el mapeo de datos de la entidad completa al DTO simplificado
     * Este patrón centraliza la lógica de transformación y evita código duplicado
     *
     * @param empleado Entidad Empleado completa del modelo de datos
     * @return Instancia de EmpleadoDTO con los datos mapeados
     */
    public static EmpleadoDTO fromEntity(com.benefactor.agendaCitas.model.Empleado empleado) {
        return new EmpleadoDTO(
                empleado.getId(),
                empleado.getNombre(),
                empleado.getActivo(),
                empleado.getFechaCreacion()
        );
    }

    // ========== GETTERS Y SETTERS ==========

    /**
     * Obtiene el identificador único del empleado
     *
     * @return ID del empleado
     */
    public Long getId() { return id; }

    /**
     * Establece el identificador único del empleado
     *
     * @param id ID del empleado
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Obtiene el nombre completo del empleado
     *
     * @return Nombre del empleado
     */
    public String getNombre() { return nombre; }

    /**
     * Establece el nombre completo del empleado
     *
     * @param nombre Nombre del empleado
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene el estado de actividad del empleado
     *
     * @return true si el empleado está activo, false si está inactivo
     */
    public Boolean getActivo() { return activo; }

    /**
     * Establece el estado de actividad del empleado
     *
     * @param activo Estado de actividad del empleado
     */
    public void setActivo(Boolean activo) { this.activo = activo; }

    /**
     * Obtiene la fecha de registro del empleado en el sistema
     *
     * @return Fecha y hora de creación del registro
     */
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    /**
     * Establece la fecha de registro del empleado en el sistema
     *
     * @param fechaCreacion Fecha y hora de creación del registro
     */
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    // ========== MÉTODOS COMUNES ==========

    /**
     * Representación en string del objeto para logging y debugging
     * Proporciona una vista legible de todos los campos del DTO
     *
     * @return String con la representación completa del objeto
     */
    @Override
    public String toString() {
        return "EmpleadoDTO{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}