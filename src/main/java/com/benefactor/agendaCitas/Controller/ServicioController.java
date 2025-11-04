package com.benefactor.agendaCitas.Controller;

import com.benefactor.agendaCitas.model.Servicio;
import com.benefactor.agendaCitas.Servicios.AuthService;
import com.benefactor.agendaCitas.Servicios.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private AuthService authService;

    // Endpoints públicos (sin autenticación)
    @GetMapping
    public List<Servicio> obtenerServiciosActivos() {
        System.out.println("✅ GET /api/servicios - Obteniendo servicios activos");
        return servicioService.obtenerServiciosActivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerServicioPorId(@PathVariable Long id) {
        try {
            System.out.println("🔍 GET /api/servicios/" + id + " - Buscando servicio");
            Servicio servicio = servicioService.obtenerServicioPorId(id)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
            return ResponseEntity.ok(servicio);
        } catch (RuntimeException e) {
            System.out.println("❌ Error obteniendo servicio: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoints de administrador (requieren autenticación)
    @PostMapping
    public ResponseEntity<?> crearServicio(
            @RequestBody Servicio servicio,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        System.out.println("➕ POST /api/servicios - Creando nuevo servicio");
        System.out.println("📦 Datos recibidos:");
        System.out.println("   Nombre: " + servicio.getNombre());
        System.out.println("   Descripción: " + servicio.getDescripcion());
        System.out.println("   Duración Minutos: " + servicio.getDuracionMinutos());
        System.out.println("   Costo: " + servicio.getCosto());
        System.out.println("   Activo: " + servicio.getActivo());

        try {
            if (!authService.validarSesion(sessionId)) {
                System.out.println("❌ No autorizado - Sesión inválida");
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            Servicio nuevoServicio = servicioService.guardarServicio(servicio);
            return ResponseEntity.ok(nuevoServicio);

        } catch (RuntimeException e) {
            System.out.println("❌ Error creando servicio: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarServicio(
            @PathVariable Long id,
            @RequestBody Servicio servicioActualizado,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        System.out.println("🔄 PUT /api/servicios/" + id + " - Actualizando servicio");
        System.out.println("📝 Datos actualización:");
        System.out.println("   Nombre: " + servicioActualizado.getNombre());
        System.out.println("   Descripción: " + servicioActualizado.getDescripcion());
        System.out.println("   Duración Minutos: " + servicioActualizado.getDuracionMinutos());
        System.out.println("   Costo: " + servicioActualizado.getCosto());
        System.out.println("   Activo: " + servicioActualizado.getActivo());

        try {
            if (!authService.validarSesion(sessionId)) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            Servicio servicio = servicioService.actualizarServicio(id, servicioActualizado);
            return ResponseEntity.ok(servicio);

        } catch (RuntimeException e) {
            System.out.println("❌ Error actualizando servicio: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarServicio(
            @PathVariable Long id,
            @CookieValue(value = "sessionId", required = false) String sessionId) {

        System.out.println("🗑️ DELETE /api/servicios/" + id + " - Eliminando servicio");

        try {
            if (!authService.validarSesion(sessionId)) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            servicioService.eliminarServicio(id);
            return ResponseEntity.ok().body(Map.of("mensaje", "Servicio eliminado correctamente"));

        } catch (RuntimeException e) {
            System.out.println("❌ Error eliminando servicio: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}