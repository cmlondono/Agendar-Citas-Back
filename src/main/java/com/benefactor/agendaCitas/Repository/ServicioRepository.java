package com.benefactor.agendaCitas.Repository;

import com.benefactor.agendaCitas.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository para operaciones de acceso a datos de la entidad Servicio
 * Extiende JpaRepository para obtener operaciones CRUD básicas automáticamente
 * Maneja la persistencia de servicios ofrecidos por el establecimiento
 *
 * @Repository Indica que esta interfaz es un componente de repositorio de Spring
 * Proporciona una capa de abstracción sobre la base de datos para la entidad Servicio
 */
@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    /**
     * Encuentra todos los servicios activos del sistema
     * Consulta derivada automática generada por Spring Data JPA basada en el nombre del método
     * Filtra los servicios donde el campo 'activo' es true
     *
     * Este método es fundamental para:
     * - Mostrar solo servicios disponibles para agendar citas
     * - Filtrar servicios en interfaces de usuario y formularios
     * - Generar catálogos de servicios activos para clientes
     * - Asignar servicios válidos a nuevas citas
     * - Reportes y estadísticas de servicios ofrecidos
     *
     * @return Lista de servicios activos ordenados por ID o según la implementación por defecto
     *         Retorna una lista vacía si no hay servicios activos en el sistema
     */
    List<Servicio> findByActivoTrue();
}