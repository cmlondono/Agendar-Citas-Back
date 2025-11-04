package com.benefactor.agendaCitas.Repository;

import com.benefactor.agendaCitas.model.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository para operaciones de acceso a datos de la entidad Configuracion
 * Maneja la persistencia de configuraciones del sistema usando clave-valor
 * Extiende JpaRepository para obtener operaciones CRUD básicas automáticamente
 *
 * @Repository Indica que esta interfaz es un componente de repositorio de Spring
 * Proporciona una capa de abstracción sobre la base de datos para configuraciones
 */
@Repository
public interface ConfiguracionRepository extends JpaRepository<Configuracion, Long> {

    /**
     * Busca una configuración específica por su clave única
     * Consulta derivada automática generada por Spring Data JPA
     * Utiliza Optional para manejar de forma segura configuraciones que no existen
     *
     * Este método es fundamental para el sistema de configuración clave-valor,
     * permitiendo recuperar valores de configuración de manera eficiente
     *
     * @param clave Identificador único de la configuración a buscar
     * @return Optional que contiene la Configuracion si existe, o empty si no se encuentra
     *
     * Ejemplos de uso:
     * - "tiempo_recordatorio_minutos" → "30"
     * - "hora_apertura" → "08:00"
     * - "hora_cierre" → "18:00"
     * - "duracion_cita_default" → "60"
     */
    Optional<Configuracion> findByClave(String clave);
}