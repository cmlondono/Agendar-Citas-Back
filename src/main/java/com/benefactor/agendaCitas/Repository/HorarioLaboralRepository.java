package com.benefactor.agendaCitas.Repository;

import com.benefactor.agendaCitas.model.HorarioLaboral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository para operaciones de acceso a datos de la entidad HorarioLaboral
 * Extiende JpaRepository para obtener operaciones CRUD básicas automáticamente
 * Maneja la persistencia de horarios laborales de empleados y sus relaciones
 *
 * @Repository Indica que esta interfaz es un componente de repositorio de Spring
 * Proporciona una capa de abstracción sobre la base de datos para horarios laborales
 */
@Repository
public interface HorarioLaboralRepository extends JpaRepository<HorarioLaboral, Long> {

    /**
     * Encuentra todos los horarios laborales activos de un empleado específico
     * Consulta derivada automática generada por Spring Data JPA
     * Retorna todos los días de la semana en los que el empleado trabaja
     *
     * @param empleadoId ID del empleado cuyos horarios se buscan
     * @return Lista de horarios laborales activos del empleado
     */
    List<HorarioLaboral> findByEmpleadoIdAndActivoTrue(Long empleadoId);

    /**
     * Encuentra el horario laboral activo de un empleado para un día específico de la semana
     * Consulta derivada automática generada por Spring Data JPA
     * Útil para verificar disponibilidad en días particulares
     *
     * @param empleadoId ID del empleado cuyo horario se busca
     * @param diaSemana Día de la semana (1=Lunes, 2=Martes, ..., 7=Domingo)
     * @return Lista de horarios laborales activos para el empleado en el día especificado
     */
    List<HorarioLaboral> findByEmpleadoIdAndDiaSemanaAndActivoTrue(Long empleadoId, Integer diaSemana);

    /**
     * Verifica si un empleado tiene horario laboral activo para un día específico de la semana
     * Consulta personalizada JPQL que retorna un booleano indicando existencia
     * Optimizada para verificación rápida sin cargar entidades completas
     *
     * @param empleadoId ID del empleado a verificar
     * @param diaSemana Día de la semana (1=Lunes, 2=Martes, ..., 7=Domingo)
     * @return true si el empleado trabaja ese día, false en caso contrario
     */
    @Query("SELECT COUNT(h) > 0 FROM HorarioLaboral h WHERE h.empleado.id = :empleadoId AND h.diaSemana = :diaSemana AND h.activo = true")
    boolean existsByEmpleadoIdAndDiaSemanaAndActivoTrue(
            @Param("empleadoId") Long empleadoId,
            @Param("diaSemana") Integer diaSemana
    );
}