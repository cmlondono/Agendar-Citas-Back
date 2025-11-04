package com.benefactor.agendaCitas.Servicios;

import com.benefactor.agendaCitas.model.Cita;
import com.benefactor.agendaCitas.Repository.CitaRepository;
import com.benefactor.agendaCitas.Repository.ConfiguracionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

/**
 * Servicio para la gestión y verificación de recordatorios de citas.
 *
 * <p>Este servicio se ejecuta periódicamente para verificar las citas próximas
 * y gestionar los recordatorios activos en memoria. Utiliza un mapa concurrente
 * para almacenar los recordatorios activos y proporciona métodos para consultar
 * y cerrar recordatorios.</p>
 */
@Service
public class RecordatorioService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    // Almacenamiento en memoria de recordatorios activos
    private Map<Long, LocalDateTime> recordatoriosActivos = new ConcurrentHashMap<>();

    /**
     * Verifica periódicamente las citas que necesitan recordatorio.
     *
     * <p>Este método se ejecuta automáticamente cada minuto (60000 ms) y realiza
     * las siguientes acciones:
     * - Obtiene las citas que están dentro del rango de tiempo para recordatorio
     * - Registra nuevos recordatorios en el mapa de recordatorios activos
     * - Marca las citas como recordatorio enviado en la base de datos
     * - Limpia los recordatorios de citas pasadas</p>
     *
     * <p>En caso de error, captura la excepción y registra un mensaje de error.</p>
     */
    @Scheduled(fixedRate = 60000) // Ejecutar cada minuto
    public void verificarRecordatorios() {
        try {
            // Obtener minutos de anticipación desde configuración (default: 30 minutos)
            int minutosAnticipacion = 30; // Por defecto

            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime limiteRecordatorio = ahora.plusMinutes(minutosAnticipacion);

            // Buscar citas que necesitan recordatorio
            List<Cita> citasParaRecordar = citaRepository.findCitasParaRecordatorio(ahora, limiteRecordatorio);

            for (Cita cita : citasParaRecordar) {
                if (!recordatoriosActivos.containsKey(cita.getId())) {
                    // Agregar a recordatorios activos
                    recordatoriosActivos.put(cita.getId(), cita.getFechaHoraInicio());

                    // Marcar como recordatorio enviado en la base de datos
                    cita.setRecordatorioEnviado(true);
                    citaRepository.save(cita);

                    System.out.println("📅 RECORDATORIO: Cita para " + cita.getClienteNombre() +
                            " con " + cita.getEmpleado().getNombre() +
                            " a las " + cita.getFechaHoraInicio());
                }
            }

            // Limpiar recordatorios de citas pasadas
            limpiarRecordatoriosAntiguos();

        } catch (Exception e) {
            System.err.println("Error en verificación de recordatorios: " + e.getMessage());
        }
    }

    /**
     * Limpia los recordatorios de citas que ya han pasado.
     *
     * <p>Elimina del mapa de recordatorios activos aquellas citas cuya fecha y hora
     * de inicio sea anterior a 2 horas desde el momento actual. Esto ayuda a mantener
     * el mapa de recordatorios libre de citas antiguas.</p>
     */
    private void limpiarRecordatoriosAntiguos() {
        LocalDateTime ahora = LocalDateTime.now();
        recordatoriosActivos.entrySet().removeIf(entry ->
                entry.getValue().isBefore(ahora.minusHours(2)) // Remover citas pasadas más de 2 horas
        );
    }

    /**
     * Obtiene la lista de recordatorios activos con información detallada.
     *
     * <p>Recupera todos los recordatorios activos del mapa en memoria y para cada uno
     * obtiene la información completa de la cita desde el repositorio. Construye un
     * mapa con la información relevante de cada cita incluyendo el tiempo restante
     * calculado.</p>
     *
     * @return Lista de mapas con información de los recordatorios activos. Cada mapa
     *         contiene: citaId, clienteNombre, empleadoNombre, servicioNombre, fechaHora
     *         y tiempoRestante.
     */
    public List<Map<String, Object>> obtenerRecordatoriosActivos() {
        List<Map<String, Object>> recordatorios = new ArrayList<>();

        for (Map.Entry<Long, LocalDateTime> entry : recordatoriosActivos.entrySet()) {
            Cita cita = citaRepository.findById(entry.getKey()).orElse(null);
            if (cita != null) {
                Map<String, Object> recordatorio = new HashMap<>();
                recordatorio.put("citaId", cita.getId());
                recordatorio.put("clienteNombre", cita.getClienteNombre());
                recordatorio.put("empleadoNombre", cita.getEmpleado().getNombre());
                recordatorio.put("servicioNombre", cita.getServicio().getNombre());
                recordatorio.put("fechaHora", cita.getFechaHoraInicio());
                recordatorio.put("tiempoRestante", calcularTiempoRestante(cita.getFechaHoraInicio()));

                recordatorios.add(recordatorio);
            }
        }

        return recordatorios;
    }

    /**
     * Calcula el tiempo restante hasta la cita en formato legible.
     *
     * <p>Calcula la diferencia entre la fecha/hora actual y la fecha/hora de la cita,
     * retornando el resultado en un formato fácil de leer:
     * - "Ahora" para citas que están por comenzar o ya comenzaron
     * - "X minutos" para menos de 1 hora
     * - "Xh Ym" para 1 hora o más</p>
     *
     * @param fechaHoraCita La fecha y hora de la cita
     * @return String con el tiempo restante formateado
     */
    private String calcularTiempoRestante(LocalDateTime fechaHoraCita) {
        LocalDateTime ahora = LocalDateTime.now();
        long minutosRestantes = java.time.Duration.between(ahora, fechaHoraCita).toMinutes();

        if (minutosRestantes <= 0) {
            return "Ahora";
        } else if (minutosRestantes < 60) {
            return minutosRestantes + " minutos";
        } else {
            long horas = minutosRestantes / 60;
            long minutos = minutosRestantes % 60;
            return horas + "h " + minutos + "m";
        }
    }

    /**
     * Cierra un recordatorio activo removiéndolo del mapa de recordatorios.
     *
     * <p>Este método permite eliminar manualmente un recordatorio activo,
     * útil cuando un usuario descarta un recordatorio o cuando la cita
     * ha sido cancelada o completada.</p>
     *
     * @param citaId El ID de la cita cuyo recordatorio se desea cerrar
     */
    public void cerrarRecordatorio(Long citaId) {
        recordatoriosActivos.remove(citaId);
    }
}