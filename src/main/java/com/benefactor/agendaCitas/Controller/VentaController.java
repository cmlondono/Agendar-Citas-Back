package com.benefactor.agendaCitas.Controller;

import com.benefactor.agendaCitas.DTO.VentaDTO;
import com.benefactor.agendaCitas.model.Venta;
import com.benefactor.agendaCitas.Servicios.VentaService;
import com.benefactor.agendaCitas.Servicios.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.benefactor.agendaCitas.DTO.VentaDTO;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para gestión de ventas
 * Maneja el proceso completo de ventas con validación de sesión
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private AuthService authService;

    /**
     * Verifica la sesión del usuario y obtiene el nombre de usuario
     */
    /**
     * Verifica la sesión del usuario y obtiene el nombre de usuario
     */
    private ResponseEntity<?> verificarSesionYUsuario(String sessionId) {
        System.out.println("🔍 DEBUG VentaController - SessionId recibido: " + sessionId);

        if (sessionId == null) {
            System.out.println("❌ DEBUG VentaController - SessionId es NULL");
            return ResponseEntity.status(401).body(Map.of("error", "Sesión inválida o expirada", "debug", "sessionId es null"));
        }

        if (sessionId.trim().isEmpty()) {
            System.out.println("❌ DEBUG VentaController - SessionId está vacío");
            return ResponseEntity.status(401).body(Map.of("error", "Sesión inválida o expirada", "debug", "sessionId está vacío"));
        }

        System.out.println("🔍 DEBUG VentaController - Validando sesión con authService...");
        boolean sesionValida = authService.validarSesion(sessionId);
        System.out.println("🔍 DEBUG VentaController - authService.validarSesion retornó: " + sesionValida);

        if (!sesionValida) {
            System.out.println("❌ DEBUG VentaController - Sesión NO válida según authService");
            return ResponseEntity.status(401).body(Map.of("error", "Sesión inválida o expirada", "debug", "authService.validarSesion retornó false"));
        }

        String usuario = authService.getUsuarioDeSesion(sessionId);
        System.out.println("🔍 DEBUG VentaController - authService.getUsuarioDeSesion retornó: " + usuario);

        if (usuario == null) {
            System.out.println("❌ DEBUG VentaController - No se pudo obtener usuario de sesión");
            return ResponseEntity.status(401).body(Map.of("error", "No se pudo obtener información del usuario", "debug", "authService.getUsuarioDeSesion retornó null"));
        }

        System.out.println("✅ DEBUG VentaController - Sesión válida para usuario: " + usuario);
        return null;
    }

    /**
     * Crear nueva venta
     */
    /**
     * Crear nueva venta
     */
    /**
     * Crear nueva venta
     */
    @PostMapping
    public ResponseEntity<?> crearVenta(
            @RequestBody Map<String, Object> ventaData,
            @CookieValue(value = "sessionId", required = false) String sessionId,
            HttpServletRequest request) {

        ResponseEntity<?> errorSesion = verificarSesionYUsuario(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            String usuario = authService.getUsuarioDeSesion(sessionId);
            Venta nuevaVenta = ventaService.crearVenta(ventaData, usuario);

            // Asegurarse de devolver la venta completa con sus detalles
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Venta creada exitosamente",
                    "venta", nuevaVenta,  // Esto debe incluir los datos del cliente
                    "numeroFactura", nuevaVenta.getNumeroFactura()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al crear venta: " + e.getMessage()));
        }
    }

    /**
     * Confirmar venta (proceso de pago completado)
     */
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarVenta(
            @PathVariable Long id,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesionYUsuario(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            Venta ventaConfirmada = ventaService.confirmarVenta(id);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Venta confirmada exitosamente",
                    "venta", ventaConfirmada,
                    "numeroFactura", ventaConfirmada.getNumeroFactura()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al confirmar venta: " + e.getMessage()));
        }
    }

    /**
     * Cancelar venta
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarVenta(
            @PathVariable Long id,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesionYUsuario(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            Venta ventaCancelada = ventaService.cancelarVenta(id);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Venta cancelada exitosamente",
                    "venta", ventaCancelada
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al cancelar venta: " + e.getMessage()));
        }
    }

    /**
     * Obtener todas las ventas
     */
    // En VentaController.java
    @GetMapping
    public ResponseEntity<?> obtenerTodasLasVentas(
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesionYUsuario(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            List<VentaDTO> ventas = ventaService.obtenerTodasLasVentas();
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener ventas: " + e.getMessage()));
        }
    }

    /**
     * Obtener venta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerVentaPorId(
            @PathVariable Long id,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesionYUsuario(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            Optional<Venta> venta = ventaService.obtenerVentaPorId(id);
            return venta.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener venta: " + e.getMessage()));
        }
    }

    /**
     * Obtener venta por número de factura
     */
    @GetMapping("/factura/{numeroFactura}")
    public ResponseEntity<?> obtenerVentaPorFactura(
            @PathVariable String numeroFactura,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesionYUsuario(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            Optional<Venta> venta = ventaService.obtenerVentaPorFactura(numeroFactura);
            return venta.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener venta por factura: " + e.getMessage()));
        }
    }

    /**
     * Obtener ventas por rango de fechas
     */
    @GetMapping("/rango-fechas")
    public ResponseEntity<?> obtenerVentasPorRangoFechas(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesionYUsuario(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio);
            LocalDateTime fin = LocalDateTime.parse(fechaFin);

            List<Venta> ventas = ventaService.obtenerVentasPorRangoFechas(inicio, fin);
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Formato de fecha inválido. Use formato ISO: " + e.getMessage()));
        }
    }

    /**
     * Obtener ventas confirmadas por rango de fechas
     */
    @GetMapping("/confirmadas/rango-fechas")
    public ResponseEntity<?> obtenerVentasConfirmadasPorRango(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesionYUsuario(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio);
            LocalDateTime fin = LocalDateTime.parse(fechaFin);

            List<Venta> ventas = ventaService.obtenerVentasConfirmadasPorRango(inicio, fin);
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Formato de fecha inválido. Use formato ISO: " + e.getMessage()));
        }
    }

    /**
     * Obtener total de ventas del día actual
     */
    @GetMapping("/estadisticas/hoy")
    public ResponseEntity<?> obtenerEstadisticasVentasHoy(
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesionYUsuario(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            // Obtener ventas de hoy
            LocalDateTime hoyInicio = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime hoyFin = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

            List<Venta> ventasHoy = ventaService.obtenerVentasConfirmadasPorRango(hoyInicio, hoyFin);
            var totalVentasHoy = ventaService.obtenerTotalVentasHoy();

            Map<String, Object> estadisticas = Map.of(
                    "totalVentas", ventasHoy.size(),
                    "totalIngresos", totalVentasHoy,
                    "fecha", LocalDateTime.now().toLocalDate().toString()
            );

            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * Obtener resumen de ventas para dashboard
     */
    @GetMapping("/resumen-dashboard")
    public ResponseEntity<?> obtenerResumenDashboard(
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        ResponseEntity<?> errorSesion = verificarSesionYUsuario(sessionId);
        if (errorSesion != null) return errorSesion;

        try {
            // Obtener ventas de hoy
            LocalDateTime hoyInicio = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime hoyFin = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

            List<Venta> ventasHoy = ventaService.obtenerVentasConfirmadasPorRango(hoyInicio, hoyFin);
            var totalVentasHoy = ventaService.obtenerTotalVentasHoy();

            // Obtener ventas pendientes
            List<VentaDTO> todasVentas = ventaService.obtenerTodasLasVentas();
            long ventasPendientes = todasVentas.stream()
                    .filter(v -> "PENDIENTE".equals(v.getEstado()))
                    .count();

            Map<String, Object> resumen = Map.of(
                    "ventasHoy", ventasHoy.size(),
                    "ingresosHoy", totalVentasHoy,
                    "ventasPendientes", ventasPendientes,
                    "totalVentas", todasVentas.size()
            );

            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener resumen: " + e.getMessage()));
        }
    }
}