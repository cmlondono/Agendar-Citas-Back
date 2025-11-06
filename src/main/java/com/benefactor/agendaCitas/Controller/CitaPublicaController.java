// Controller/CitaPublicaController.java
package com.benefactor.agendaCitas.Controller;

import com.benefactor.agendaCitas.DTO.ConsultarCitasRequest;
import com.benefactor.agendaCitas.Servicios.CitaPublicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/citas")
public class CitaPublicaController {

    @Autowired
    private CitaPublicaService citaPublicaService;

    @PostMapping("/verificar-usuario")
    public ResponseEntity<?> verificarUsuario(@RequestBody ConsultarCitasRequest request) {
        try {
            boolean existe = citaPublicaService.usuarioExiste(
                    request.getDocumento(),
                    request.getCelular()
            );
            return ResponseEntity.ok(Map.of("usuarioExiste", existe));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/mis-citas")
    public ResponseEntity<?> obtenerMisCitas(@RequestBody ConsultarCitasRequest request) {
        try {
            var citas = citaPublicaService.obtenerCitasProgramadas(
                    request.getDocumento(),
                    request.getCelular()
            );
            return ResponseEntity.ok(Map.of("citas", citas));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{citaId}/cancelar")
    public ResponseEntity<?> cancelarCita(
            @PathVariable Long citaId,
            @RequestBody ConsultarCitasRequest request) {
        try {
            var resultado = citaPublicaService.cancelarCita(
                    citaId,
                    request.getDocumento(),
                    request.getCelular()
            );
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}