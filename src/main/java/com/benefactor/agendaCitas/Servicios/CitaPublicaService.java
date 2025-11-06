// Servicios/CitaPublicaService.java
package com.benefactor.agendaCitas.Servicios;

import com.benefactor.agendaCitas.model.Cita;
import com.benefactor.agendaCitas.DTO.CitaResponseDTO;
import com.benefactor.agendaCitas.Repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class CitaPublicaService {

    @Autowired
    private CitaRepository citaRepository;

    /**
     * Obtiene todas las citas programadas de un usuario por documento y celular
     */
    public List<CitaResponseDTO> obtenerCitasProgramadas(String documento, String celular) {
        List<Cita> citas = citaRepository.findByClienteDocumentoAndClienteCelularAndEstado(
                documento, celular, "programada");

        return citas.stream().map(cita -> new CitaResponseDTO(
                cita.getId(),
                cita.getClienteNombre(),
                cita.getEmpleado().getNombre(),
                cita.getServicio().getNombre(),
                cita.getFechaHoraInicio(),
                cita.getFechaHoraFin(),
                cita.getCostoTotal(),
                cita.getEstado()
        )).collect(Collectors.toList());
    }

    /**
     * Cancela una cita específica verificando que pertenezca al usuario
     */
    public Map<String, String> cancelarCita(Long citaId, String documento, String celular) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        // Verificar que la cita pertenece al usuario
        if (!cita.getClienteDocumento().equals(documento) ||
                !cita.getClienteCelular().equals(celular)) {
            throw new RuntimeException("No tienes permisos para cancelar esta cita");
        }

        // Verificar que la cita está programada
        if (!"programada".equals(cita.getEstado())) {
            throw new RuntimeException("Solo se pueden cancelar citas programadas");
        }

        // Cancelar la cita
        cita.setEstado("cancelada");
        citaRepository.save(cita);

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Cita cancelada exitosamente");
        respuesta.put("citaId", citaId.toString());
        respuesta.put("cliente", cita.getClienteNombre());

        return respuesta;
    }

    /**
     * Verifica si un usuario existe (tiene citas programadas)
     */
    public boolean usuarioExiste(String documento, String celular) {
        return citaRepository.existsByClienteDocumentoAndClienteCelularAndEstado(
                documento, celular, "programada");
    }
}