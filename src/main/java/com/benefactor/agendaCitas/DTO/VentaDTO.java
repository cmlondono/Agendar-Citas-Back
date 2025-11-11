package com.benefactor.agendaCitas.DTO;

import com.benefactor.agendaCitas.model.Venta;
import com.benefactor.agendaCitas.model.DetalleVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO (Data Transfer Object) para la entidad Venta
 * Se utiliza para transferir datos entre el backend y frontend
 * Evita problemas de serialización JSON con relaciones lazy loading de Hibernate
 */
public class VentaDTO {
    private Long id;
    private String numeroFactura;
    private LocalDateTime fechaVenta;
    private BigDecimal total;
    private String estado;
    private String metodoPago;
    private String observaciones;
    private String usuarioCreacion;
    private String documentoCliente;
    private String nombreCliente;
    private String telefonoCliente;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private List<DetalleVentaDTO> detalles;

    // Constructor vacío (requerido para Jackson)
    public VentaDTO() {}

    /**
     * Constructor desde entidad Venta
     * Convierte una entidad Venta en un DTO para transferencia de datos
     */
    public VentaDTO(Venta venta) {
        this.id = venta.getId();
        this.numeroFactura = venta.getNumeroFactura();
        this.fechaVenta = venta.getFechaVenta();
        this.total = venta.getTotal();
        this.estado = venta.getEstado();
        this.metodoPago = venta.getMetodoPago();
        this.observaciones = venta.getObservaciones();
        this.usuarioCreacion = venta.getUsuarioCreacion();
        this.documentoCliente = venta.getDocumentoCliente();
        this.nombreCliente = venta.getNombreCliente();
        this.telefonoCliente = venta.getTelefonoCliente();
        this.fechaCreacion = venta.getFechaCreacion();
        this.fechaActualizacion = venta.getFechaActualizacion();

        // Convertir detalles a DTOs si existen
        if (venta.getDetalles() != null && !venta.getDetalles().isEmpty()) {
            this.detalles = venta.getDetalles().stream()
                    .map(DetalleVentaDTO::new)
                    .collect(Collectors.toList());
        }
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(String usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    public String getDocumentoCliente() {
        return documentoCliente;
    }

    public void setDocumentoCliente(String documentoCliente) {
        this.documentoCliente = documentoCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public List<DetalleVentaDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaDTO> detalles) {
        this.detalles = detalles;
    }

    @Override
    public String toString() {
        return "VentaDTO{" +
                "id=" + id +
                ", numeroFactura='" + numeroFactura + '\'' +
                ", fechaVenta=" + fechaVenta +
                ", total=" + total +
                ", estado='" + estado + '\'' +
                ", metodoPago='" + metodoPago + '\'' +
                ", observaciones='" + observaciones + '\'' +
                ", usuarioCreacion='" + usuarioCreacion + '\'' +
                ", documentoCliente='" + documentoCliente + '\'' +
                ", nombreCliente='" + nombreCliente + '\'' +
                ", telefonoCliente='" + telefonoCliente + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaActualizacion=" + fechaActualizacion +
                ", detalles=" + detalles +
                '}';
    }
}