package com.benefactor.agendaCitas.Servicios;

import com.benefactor.agendaCitas.model.Servicio;
import com.benefactor.agendaCitas.Repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de servicios ofrecidos por el establecimiento.
 *
 * <p>Esta clase proporciona operaciones CRUD para la entidad Servicio,
 * incluyendo creación, consulta, actualización y eliminación lógica
 * de los servicios disponibles en el sistema.</p>
 */
@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    /**
     * Obtiene todos los servicios registrados en el sistema.
     *
     * <p>Este método retorna tanto servicios activos como inactivos.
     * Para obtener solo servicios activos, usar {@link #obtenerServiciosActivos()}.</p>
     *
     * @return Lista de todos los servicios existentes en la base de datos
     */
    public List<Servicio> obtenerTodosServicios() {
        return servicioRepository.findAll();
    }

    /**
     * Obtiene solo los servicios activos del sistema.
     *
     * <p>Este método filtra los servicios que están marcados como activos
     * y están disponibles para ser asignados a nuevas citas.</p>
     *
     * @return Lista de servicios activos
     */
    public List<Servicio> obtenerServiciosActivos() {
        return servicioRepository.findByActivoTrue();
    }

    /**
     * Busca un servicio por su identificador único.
     *
     * <p>Si el servicio no existe, retorna un Optional vacío.
     * Este método puede retornar servicios tanto activos como inactivos.</p>
     *
     * @param id Identificador único del servicio
     * @return Optional con el servicio encontrado o vacío si no existe
     */
    public Optional<Servicio> obtenerServicioPorId(Long id) {
        return servicioRepository.findById(id);
    }

    /**
     * Guarda un nuevo servicio en el sistema.
     *
     * <p>Antes de guardar, establece el servicio como activo automáticamente.
     * El método incluye logs informativos sobre los datos del servicio que se está guardando.</p>
     *
     * @param servicio El objeto Servicio a guardar
     * @return El servicio guardado con su ID generado
     */
    public Servicio guardarServicio(Servicio servicio) {
        System.out.println("💾 Guardando servicio: " + servicio.getNombre());
        System.out.println("💰 Costo: " + servicio.getCosto());
        System.out.println("⏰ Duración minutos: " + servicio.getDuracionMinutos());

        servicio.setActivo(true);
        Servicio servicioGuardado = servicioRepository.save(servicio);

        System.out.println("✅ Servicio guardado con ID: " + servicioGuardado.getId());
        return servicioGuardado;
    }

    /**
     * Actualiza un servicio existente con nueva información.
     *
     * <p>Busca el servicio por ID y actualiza todos sus campos con los valores
     * proporcionados en el objeto servicioActualizado. Si el servicio no existe,
     * lanza una excepción RuntimeException.</p>
     *
     * @param id ID del servicio a actualizar
     * @param servicioActualizado Objeto con los nuevos datos del servicio
     * @return El servicio actualizado
     * @throws RuntimeException Si no se encuentra el servicio con el ID especificado
     */
    public Servicio actualizarServicio(Long id, Servicio servicioActualizado) {
        System.out.println("🔄 Actualizando servicio ID: " + id);
        System.out.println("📝 Nuevos datos - Nombre: " + servicioActualizado.getNombre());
        System.out.println("💰 Nuevo costo: " + servicioActualizado.getCosto());
        System.out.println("⏰ Nueva duración: " + servicioActualizado.getDuracionMinutos());

        return servicioRepository.findById(id)
                .map(servicio -> {
                    servicio.setNombre(servicioActualizado.getNombre());
                    servicio.setDescripcion(servicioActualizado.getDescripcion());
                    servicio.setDuracionMinutos(servicioActualizado.getDuracionMinutos());
                    servicio.setCosto(servicioActualizado.getCosto());
                    servicio.setActivo(servicioActualizado.getActivo());

                    Servicio servicioActualizadoEntity = servicioRepository.save(servicio);
                    System.out.println("✅ Servicio actualizado: " + servicioActualizadoEntity.getId());
                    return servicioActualizadoEntity;
                })
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con id: " + id));
    }

    /**
     * Realiza una eliminación lógica de un servicio.
     *
     * <p>En lugar de eliminar físicamente el servicio de la base de datos,
     * este método marca el servicio como inactivo (activo = false), lo que
     * permite mantener el historial pero evita que sea asignado a nuevas citas.</p>
     *
     * @param id ID del servicio a eliminar
     * @throws RuntimeException Si no se encuentra el servicio con el ID especificado
     */
    public void eliminarServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con id: " + id));
        servicio.setActivo(false);
        servicioRepository.save(servicio);
        System.out.println("🗑️ Servicio eliminado (inactivado): " + id);
    }
}