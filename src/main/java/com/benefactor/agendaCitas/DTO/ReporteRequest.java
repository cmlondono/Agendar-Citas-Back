package com.benefactor.agendaCitas.DTO;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) para solicitudes de generación de reportes
 * Representa los parámetros de filtro y configuración necesarios para generar reportes del sistema
 * Permite especificar rangos de fechas, tipos de reporte y filtros opcionales por empleado
 *
 * Se utiliza en los endpoints del controlador de reportes para recibir los criterios
 * de generación de reportes desde el frontend de manera estructurada y tipada
 */
public class ReporteRequest {

    /**
     * Fecha de inicio del período para el reporte
     * Define el límite inferior del rango temporal a analizar
     * Campo requerido para todos los tipos de reporte
     */
    private LocalDate fechaInicio;

    /**
     * Fecha de fin del período para el reporte
     * Define el límite superior del rango temporal a analizar
     * Campo requerido para todos los tipos de reporte
     */
    private LocalDate fechaFin;

    /**
     * Tipo de reporte a generar
     * Determina la estructura y métricas incluidas en el reporte
     * Valores típicos: CITAS, INGRESOS, EMPLEADOS, SERVICIOS
     * Campo requerido para especificar el formato del reporte
     */
    private String tipoReporte;

    /**
     * ID opcional del empleado para filtrar el reporte
     * Permite generar reportes específicos para un empleado en particular
     * Si es null o no se proporciona, el reporte incluye todos los empleados
     * Campo opcional para reportes segmentados
     */
    private Long empleadoId;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto requerido para la deserialización JSON
     * Framework como Jackson utilizan este constructor para crear instancias
     * automáticamente desde el cuerpo de las solicitudes HTTP POST
     */
    public ReporteRequest() {}

    /**
     * Constructor completo para crear instancias con todos los parámetros
     * Útil para testing, creación programática de objetos y escenarios específicos
     *
     * @param fechaInicio Fecha de inicio del período del reporte
     * @param fechaFin Fecha de fin del período del reporte
     * @param tipoReporte Tipo de reporte a generar (CITAS, INGRESOS, etc.)
     * @param empleadoId ID opcional del empleado para filtrar el reporte
     */
    public ReporteRequest(LocalDate fechaInicio, LocalDate fechaFin, String tipoReporte, Long empleadoId) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipoReporte = tipoReporte;
        this.empleadoId = empleadoId;
    }

    // ========== GETTERS Y SETTERS ==========

    /**
     * Obtiene la fecha de inicio del período del reporte
     *
     * @return Fecha de inicio del rango temporal
     */
    public LocalDate getFechaInicio() { return fechaInicio; }

    /**
     * Establece la fecha de inicio del período del reporte
     *
     * @param fechaInicio Fecha de inicio del rango temporal
     */
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    /**
     * Obtiene la fecha de fin del período del reporte
     *
     * @return Fecha de fin del rango temporal
     */
    public LocalDate getFechaFin() { return fechaFin; }

    /**
     * Establece la fecha de fin del período del reporte
     *
     * @param fechaFin Fecha de fin del rango temporal
     */
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    /**
     * Obtiene el tipo de reporte a generar
     *
     * @return Tipo de reporte (CITAS, INGRESOS, EMPLEADOS, SERVICIOS)
     */
    public String getTipoReporte() { return tipoReporte; }

    /**
     * Establece el tipo de reporte a generar
     *
     * @param tipoReporte Tipo de reporte (CITAS, INGRESOS, EMPLEADOS, SERVICIOS)
     */
    public void setTipoReporte(String tipoReporte) { this.tipoReporte = tipoReporte; }

    /**
     * Obtiene el ID del empleado para filtrar el reporte
     *
     * @return ID del empleado o null si no hay filtro por empleado
     */
    public Long getEmpleadoId() { return empleadoId; }

    /**
     * Establece el ID del empleado para filtrar el reporte
     *
     * @param empleadoId ID del empleado o null para incluir todos los empleados
     */
    public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }
}