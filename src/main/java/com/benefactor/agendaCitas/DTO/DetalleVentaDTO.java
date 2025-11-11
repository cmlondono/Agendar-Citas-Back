package com.benefactor.agendaCitas.DTO;

import com.benefactor.agendaCitas.model.DetalleVenta;

import java.math.BigDecimal;

/**
 * DTO para la entidad DetalleVenta
 * Contiene información del detalle de una venta sin las relaciones complejas de Hibernate
 */
public class DetalleVentaDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private String productoCategoria;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    // Constructor vacío (requerido para Jackson)
    public DetalleVentaDTO() {}

    /**
     * Constructor desde entidad DetalleVenta
     * Extrae solo la información necesaria para el frontend
     */
    public DetalleVentaDTO(DetalleVenta detalle) {
        this.id = detalle.getId();
        this.productoId = detalle.getProducto().getId();
        this.productoNombre = detalle.getProducto().getNombre();
        this.productoCategoria = detalle.getProducto().getCategoria();
        this.cantidad = detalle.getCantidad();
        this.precioUnitario = detalle.getPrecioUnitario();
        this.subtotal = detalle.getSubtotal();
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public String getProductoCategoria() {
        return productoCategoria;
    }

    public void setProductoCategoria(String productoCategoria) {
        this.productoCategoria = productoCategoria;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "DetalleVentaDTO{" +
                "id=" + id +
                ", productoId=" + productoId +
                ", productoNombre='" + productoNombre + '\'' +
                ", productoCategoria='" + productoCategoria + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + subtotal +
                '}';
    }
}