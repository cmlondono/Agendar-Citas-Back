package com.benefactor.agendaCitas.Repository;

import com.benefactor.agendaCitas.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository para operaciones de acceso a datos de la entidad Empleado
 * Extiende JpaRepository para obtener operaciones CRUD básicas automáticamente
 * Define consultas específicas para la gestión de empleados en el sistema
 *
 * @Repository Indica que esta interfaz es un componente de repositorio de Spring
 * Proporciona una capa de abstracción sobre la base de datos para la entidad Empleado
 */
@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    /**
     * Encuentra todos los empleados activos del sistema
     * Consulta derivada automática generada por Spring Data JPA basada en el nombre del método
     * Filtra los empleados donde el campo 'activo' es true
     *
     * Este método es esencial para:
     * - Mostrar solo empleados disponibles para asignar citas
     * - Filtrar empleados en interfaces de usuario
     * - Generar reportes de empleados activos
     * - Asignar citas solo a empleados habilitados
     *
     * @return Lista de empleados activos ordenados por ID o según la implementación por defecto
     *         Retorna una lista vacía si no hay empleados activos en el sistema
     */
    List<Empleado> findByActivoTrue();
}