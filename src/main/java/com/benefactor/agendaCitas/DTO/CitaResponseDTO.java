// DTO/CitaResponseDTO.java
package com.benefactor.agendaCitas.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CitaResponseDTO {
    private Long id;
    private String clienteNombre;
    private String empleado;
    private String servicio;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private BigDecimal costoTotal;
    private String estado;

    // Constructores
    public CitaResponseDTO() {}

    public CitaResponseDTO(Long id, String clienteNombre, String empleado, String servicio,
                           LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin,
                           BigDecimal costoTotal, String estado) {
        this.id = id;
        this.clienteNombre = clienteNombre;
        this.empleado = empleado;
        this.servicio = servicio;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.costoTotal = costoTotal;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getEmpleado() { return empleado; }
    public void setEmpleado(String empleado) { this.empleado = empleado; }

    public String getServicio() { return servicio; }
    public void setServicio(String servicio) { this.servicio = servicio; }

    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }

    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }

    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}