package com.benefactor.agendaCitas.Servicios;

import com.benefactor.agendaCitas.DTO.VentaDTO;
import com.benefactor.agendaCitas.model.DetalleVenta;
import com.benefactor.agendaCitas.model.Producto;
import com.benefactor.agendaCitas.model.Venta;
import com.benefactor.agendaCitas.Repository.ProductoRepository;
import com.benefactor.agendaCitas.Repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.benefactor.agendaCitas.DTO.VentaDTO;
import com.benefactor.agendaCitas.DTO.DetalleVentaDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProductoService productoService;

    // Generar número de factura consecutivo
    private String generarNumeroFactura() {
        String prefijo = "FACT-";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String fecha = LocalDateTime.now().format(formatter);

        // Buscar última factura del día
        Optional<String> ultimaFactura = ventaRepository.findUltimoNumeroFactura();

        if (ultimaFactura.isPresent() && ultimaFactura.get().contains(fecha)) {
            // Extraer el consecutivo y incrementar
            String numero = ultimaFactura.get().substring(ultimaFactura.get().lastIndexOf("-") + 1);
            try {
                int consecutivo = Integer.parseInt(numero) + 1;
                return prefijo + fecha + "-" + String.format("%04d", consecutivo);
            } catch (NumberFormatException e) {
                // Si hay error, empezar desde 1
                return prefijo + fecha + "-0001";
            }
        } else {
            // Primera factura del día
            return prefijo + fecha + "-0001";
        }
    }

    // Crear nueva venta
    @Transactional
    public Venta crearVenta(Map<String, Object> ventaData, String usuarioCreacion) {
        try {
            // Validar datos básicos
            if (ventaData == null || !ventaData.containsKey("detalles")) {
                throw new RuntimeException("Datos de venta incompletos");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detallesData = (List<Map<String, Object>>) ventaData.get("detalles");

            if (detallesData == null || detallesData.isEmpty()) {
                throw new RuntimeException("La venta debe tener al menos un producto");
            }

            // Validar que todos los productos existan y tengan stock antes de proceder
            for (Map<String, Object> detalleData : detallesData) {
                Long productoId = Long.valueOf(detalleData.get("productoId").toString());
                Integer cantidad = Integer.valueOf(detalleData.get("cantidad").toString());

                Producto producto = productoRepository.findById(productoId)
                        .filter(Producto::getActivo)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado o inactivo: " + productoId));

                if (producto.getStock() < cantidad) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() +
                            ". Disponible: " + producto.getStock());
                }
            }

            // Crear nueva venta
            Venta venta = new Venta();
            venta.setNumeroFactura(generarNumeroFactura());
            venta.setUsuarioCreacion(usuarioCreacion);
            venta.setMetodoPago((String) ventaData.getOrDefault("metodoPago", "EFECTIVO"));
            venta.setObservaciones((String) ventaData.get("observaciones"));
            venta.setEstado("CONFIRMADA");

            // NUEVO: Agregar datos del cliente
            venta.setDocumentoCliente((String) ventaData.get("documentoCliente"));
            venta.setTelefonoCliente((String) ventaData.get("telefonoCliente"));
            venta.setNombreCliente((String) ventaData.get("nombreCliente"));

            // Procesar detalles de venta
            List<DetalleVenta> detalles = new ArrayList<>();

            for (Map<String, Object> detalleData : detallesData) {
                Long productoId = Long.valueOf(detalleData.get("productoId").toString());
                Integer cantidad = Integer.valueOf(detalleData.get("cantidad").toString());

                Producto producto = productoRepository.findById(productoId).get();

                // Crear detalle
                DetalleVenta detalle = new DetalleVenta();
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(producto.getPrecio());
                detalle.calcularSubtotal();

                detalles.add(detalle);
                venta.agregarDetalle(detalle);

                // Disminuir stock
                productoService.disminuirStock(productoId, cantidad);
            }

            return ventaRepository.save(venta);

        } catch (RuntimeException e) {
            throw e; // Re-lanzar excepciones de negocio
        } catch (Exception e) {
            throw new RuntimeException("Error al crear venta: " + e.getMessage());
        }
    }

    // Confirmar venta
    @Transactional
    public Venta confirmarVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + ventaId));

        if (!"PENDIENTE".equals(venta.getEstado())) {
            throw new RuntimeException("Solo se pueden confirmar ventas en estado PENDIENTE");
        }

        venta.setEstado("CONFIRMADA");
        return ventaRepository.save(venta);
    }

    // Cancelar venta
    @Transactional
    public Venta cancelarVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + ventaId));

        if ("CONFIRMADA".equals(venta.getEstado())) {
            // Devolver stock si la venta estaba confirmada
            for (DetalleVenta detalle : venta.getDetalles()) {
                productoService.aumentarStock(detalle.getProducto().getId(), detalle.getCantidad());
            }
        }

        venta.setEstado("CANCELADA");
        return ventaRepository.save(venta);
    }

    // Obtener venta por ID
    public Optional<Venta> obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id);
    }

    // En VentaService.java
    public List<VentaDTO> obtenerTodasLasVentas() {
        List<Venta> ventas = ventaRepository.findAll();
        return ventas.stream()
                .map(VentaDTO::new)
                .collect(Collectors.toList());
    }

    // Obtener ventas por rango de fechas
    public List<Venta> obtenerVentasPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.findByFechaVentaBetween(fechaInicio, fechaFin);
    }

    // Obtener ventas confirmadas por rango de fechas
    public List<Venta> obtenerVentasConfirmadasPorRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.findVentasConfirmadasByFecha(fechaInicio, fechaFin);
    }

    // Obtener total de ventas del día
    public BigDecimal obtenerTotalVentasHoy() {
        return ventaRepository.findTotalVentasHoy();
    }

    // Obtener venta por número de factura
    public Optional<Venta> obtenerVentaPorFactura(String numeroFactura) {
        return ventaRepository.findByNumeroFactura(numeroFactura);
    }
}