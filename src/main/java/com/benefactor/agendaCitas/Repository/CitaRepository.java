package com.benefactor.agendaCitas.Repository;

import com.benefactor.agendaCitas.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para operaciones de acceso a datos de la entidad Cita
 * Extiende JpaRepository para obtener operaciones CRUD básicas automáticamente
 * Define consultas personalizadas para casos de uso específicos del sistema de citas
 *
 * @Repository Indica que esta interfaz es un componente de repositorio de Spring
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {



        List<Cita> findByClienteDocumentoAndClienteCelularAndEstado(
                String documento, String celular, String estado);

        boolean existsByClienteDocumentoAndClienteCelularAndEstado(
                String documento, String celular, String estado);

    /**
     * Encuentra todas las citas de un empleado específico dentro de un rango de fechas
     * Consulta derivada automática generada por Spring Data JPA basada en el nombre del método
     *
     * @param empleadoId ID del empleado cuyas citas se buscan
     * @param inicio Fecha y hora de inicio del rango (inclusive)
     * @param fin Fecha y hora de fin del rango (inclusive)
     * @return Lista de citas del empleado en el rango de fechas especificado
     */
    List<Cita> findByEmpleadoIdAndFechaHoraInicioBetween(Long empleadoId, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Encuentra citas que podrían estar en conflicto de horario con una nueva cita propuesta
     * Consulta personalizada que verifica solapamientos en tres escenarios diferentes:
     * 1. Inicio de cita existente dentro del rango propuesto
     * 2. Fin de cita existente dentro del rango propuesto
     * 3. Cita existente que engloba completamente el rango propuesto
     *
     * @param empleadoId ID del empleado para verificar disponibilidad
     * @param inicio Fecha y hora de inicio de la cita propuesta
     * @param fin Fecha y hora de fin de la cita propuesta
     * @return Lista de citas existentes que entran en conflicto con el horario propuesto
     */
    @Query("SELECT c FROM Cita c WHERE c.empleado.id = :empleadoId AND " +
            "((c.fechaHoraInicio BETWEEN :inicio AND :fin) OR " +
            "(c.fechaHoraFin BETWEEN :inicio AND :fin) OR " +
            "(c.fechaHoraInicio <= :inicio AND c.fechaHoraFin >= :fin))")
    List<Cita> findCitasEnConflicto(Long empleadoId, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Encuentra citas por estado específico dentro de un rango de fechas
     * Consulta derivada automática útil para reportes y filtros por estado
     *
     * @param estado Estado de la cita a filtrar (programada, cumplida, cancelada, etc.)
     * @param inicio Fecha y hora de inicio del rango (inclusive)
     * @param fin Fecha y hora de fin del rango (inclusive)
     * @return Lista de citas que coinciden con el estado y rango de fechas
     */
    List<Cita> findByEstadoAndFechaHoraInicioBetween(String estado, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Encuentra citas pendientes de recordatorio dentro de un rango de fechas
     * Consulta personalizada para el sistema de notificaciones y recordatorios
     * Busca citas programadas que no han recibido recordatorio y están dentro del rango
     *
     * @param inicio Fecha y hora de inicio del rango para recordatorios
     * @param fin Fecha y hora de fin del rango para recordatorios
     * @return Lista de citas que requieren envío de recordatorio
     */
    @Query("SELECT c FROM Cita c WHERE c.recordatorioEnviado = false AND " +
            "c.estado = 'programada' AND " +
            "c.fechaHoraInicio BETWEEN :inicio AND :fin")
    List<Cita> findCitasParaRecordatorio(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Encuentra todas las citas dentro de un rango de fechas específico
     * Consulta derivada automática para obtener citas por período temporal
     * Útil para reportes, dashboards y análisis temporales
     *
     * @param inicio Fecha y hora de inicio del rango (inclusive)
     * @param fin Fecha y hora de fin del rango (inclusive)
     * @return Lista de todas las citas dentro del rango de fechas especificado
     */
    List<Cita> findByFechaHoraInicioBetween(LocalDateTime inicio, LocalDateTime fin);
}