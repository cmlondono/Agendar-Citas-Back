package com.benefactor.agendaCitas.DTO;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO (Data Transfer Object) para solicitudes de creación y actualización de citas
 * Representa los datos necesarios para agendar o modificar una cita en el sistema
 * Incluye validación de formato de fecha/hora mediante anotaciones JSON
 *
 * Se utiliza en los endpoints del controlador de citas para recibir datos del frontend
 */
public class CitaRequest {

    /**
     * Nombre completo del cliente que solicita la cita
     * Campo requerido para identificar al cliente
     */
    private String clienteNombre;



    private String clienteDocumento;

    /**
     * Número de teléfono del cliente para contactos y recordatorios
     * Campo requerido para comunicación con el cliente
     */
    private String clienteCelular;

    /**
     * ID del empleado asignado para la cita
     * Referencia al profesional que realizará el servicio
     */
    private Long empleadoId;

    /**
     * ID del servicio a realizar en la cita
     * Referencia al tipo de servicio solicitado por el cliente
     */
    private Long servicioId;

    /**
     * Fecha y hora de inicio de la cita
     * Formateado en ISO para compatibilidad con el frontend
     * El patrón define el formato esperado en las solicitudes JSON
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime fechaHoraInicio;

    /**
     * Estado actual de la cita
     * Valores típicos: programada, cumplida, cancelada, no_presento
     * Permite gestionar el ciclo de vida de la cita
     */
    private String estado;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto requerido para la deserialización JSON
     * Framework como Jackson utilizan este constructor para crear instancias
     */
    public CitaRequest() {}

    /**
     * Constructor completo para crear instancias con todos los campos
     * Útil para testing y creación programática de objetos
     *
     * @param clienteNombre Nombre del cliente
     * @param clienteCelular Teléfono del cliente
     * @param empleadoId ID del empleado asignado
     * @param servicioId ID del servicio a realizar
     * @param fechaHoraInicio Fecha y hora de inicio de la cita
     * @param estado Estado inicial de la cita
     */
    public CitaRequest(String clienteNombre, String clienteCelular, Long empleadoId,
                       Long servicioId, LocalDateTime fechaHoraInicio, String estado) {
        this.clienteNombre = clienteNombre;
        this.clienteDocumento = clienteDocumento;
        this.clienteCelular = clienteCelular;
        this.empleadoId = empleadoId;
        this.servicioId = servicioId;
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = estado;
    }

    // ========== GETTERS Y SETTERS ==========

    /**
     * Obtiene el nombre del cliente
     *
     * @return Nombre completo del cliente
     */
    public String getClienteNombre() { return clienteNombre; }

    /**
     * Establece el nombre del cliente
     *
     * @param clienteNombre Nombre completo del cliente
     */
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }



    public String getClienteDocumento() { return clienteDocumento; } // NUEVO GETTER
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; } // NUEVO SETTER

    /**
     * Obtiene el número de teléfono del cliente
     *
     * @return Número de teléfono del cliente
     */
    public String getClienteCelular() { return clienteCelular; }

    /**
     * Establece el número de teléfono del cliente
     *
     * @param clienteCelular Número de teléfono del cliente
     */
    public void setClienteCelular(String clienteCelular) { this.clienteCelular = clienteCelular; }

    /**
     * Obtiene el ID del empleado asignado
     *
     * @return ID del empleado
     */
    public Long getEmpleadoId() { return empleadoId; }

    /**
     * Establece el ID del empleado asignado
     *
     * @param empleadoId ID del empleado
     */
    public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }

    /**
     * Obtiene el ID del servicio a realizar
     *
     * @return ID del servicio
     */
    public Long getServicioId() { return servicioId; }

    /**
     * Establece el ID del servicio a realizar
     *
     * @param servicioId ID del servicio
     */
    public void setServicioId(Long servicioId) { this.servicioId = servicioId; }

    /**
     * Obtiene la fecha y hora de inicio de la cita
     *
     * @return Fecha y hora de inicio en formato LocalDateTime
     */
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }

    /**
     * Establece la fecha y hora de inicio de la cita
     *
     * @param fechaHoraInicio Fecha y hora de inicio
     */
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }

    /**
     * Obtiene el estado actual de la cita
     *
     * @return Estado de la cita (programada, cumplida, cancelada, etc.)
     */
    public String getEstado() { return estado; }

    /**
     * Establece el estado de la cita
     *
     * @param estado Nuevo estado de la cita
     */
    public void setEstado(String estado) { this.estado = estado; }
}