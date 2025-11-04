package com.benefactor.agendaCitas.Servicios;

import com.benefactor.agendaCitas.model.Cita;
import com.benefactor.agendaCitas.model.Empleado;
import com.benefactor.agendaCitas.model.Servicio;
import com.benefactor.agendaCitas.Repository.CitaRepository;
import com.benefactor.agendaCitas.Repository.EmpleadoRepository;
import com.benefactor.agendaCitas.Repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para la generación de reportes y estadísticas del sistema.
 *
 * <p>Este servicio proporciona métodos para generar diversos tipos de reportes
 * sobre citas, ingresos, empleados y servicios, así como un resumen general
 * del estado del negocio.</p>
 */
@Service
public class ReporteService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    /**
     * Genera un reporte específico según el tipo solicitado.
     *
     * <p>Este método actúa como punto de entrada principal para la generación de reportes.
     * Dependiendo del tipo de reporte solicitado, delega la generación al método
     * correspondiente.</p>
     *
     * @param fechaInicio Fecha de inicio del período del reporte
     * @param fechaFin Fecha de fin del período del reporte
     * @param tipoReporte Tipo de reporte a generar: "CITAS", "INGRESOS", "EMPLEADOS", "SERVICIOS"
     * @param empleadoId ID opcional del empleado para filtrar el reporte (puede ser null)
     * @return Mapa con los datos del reporte generado
     * @throws IllegalArgumentException Si el tipo de reporte no es válido
     */
    public Map<String, Object> generarReporteCitas(LocalDate fechaInicio, LocalDate fechaFin, String tipoReporte, Long empleadoId) {
        System.out.println("📊 Generando reporte: " + tipoReporte + " desde " + fechaInicio + " hasta " + fechaFin);

        switch (tipoReporte.toUpperCase()) {
            case "CITAS":
                return generarReporteCitasDetallado(fechaInicio, fechaFin, empleadoId);
            case "INGRESOS":
                return generarReporteIngresos(fechaInicio, fechaFin);
            case "EMPLEADOS":
                return generarReporteEmpleados(fechaInicio, fechaFin);
            case "SERVICIOS":
                return generarReporteServicios(fechaInicio, fechaFin);
            default:
                throw new IllegalArgumentException("Tipo de reporte no válido: " + tipoReporte);
        }
    }

    /**
     * Genera un reporte detallado de citas dentro del rango de fechas especificado.
     *
     * <p>El reporte incluye:
     * - Total de citas en el período
     * - Distribución de citas por estado
     * - Detalle completo de cada cita con información del cliente, servicio y empleado</p>
     *
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @param empleadoId ID opcional del empleado para filtrar las citas
     * @return Mapa con el reporte detallado de citas
     */
    private Map<String, Object> generarReporteCitasDetallado(LocalDate fechaInicio, LocalDate fechaFin, Long empleadoId) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);

        List<Cita> citas = obtenerCitasEnRango(inicio, fin, empleadoId);
        System.out.println("📋 Citas encontradas: " + citas.size());

        // Estadísticas por estado
        Map<String, Long> citasPorEstado = citas.stream()
                .collect(Collectors.groupingBy(
                        Cita::getEstado,
                        Collectors.counting()
                ));

        // Detalle de citas para la tabla
        List<Map<String, Object>> detalle = citas.stream()
                .map(cita -> {
                    Map<String, Object> detalleCita = new HashMap<>();
                    detalleCita.put("fechaHora", cita.getFechaHoraInicio());
                    detalleCita.put("nombreCliente", cita.getClienteNombre());
                    detalleCita.put("servicioNombre", cita.getServicio() != null ? cita.getServicio().getNombre() : "N/A");
                    detalleCita.put("empleadoNombre", cita.getEmpleado() != null ? cita.getEmpleado().getNombre() : "N/A");
                    detalleCita.put("estado", cita.getEstado());
                    detalleCita.put("costo", cita.getCostoTotal() != null ? cita.getCostoTotal() : 0.0);
                    return detalleCita;
                })
                .collect(Collectors.toList());

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("totalCitas", citas.size());
        reporte.put("citasPorEstado", citasPorEstado);
        reporte.put("detalle", detalle);
        reporte.put("tipoReporte", "CITAS");

        System.out.println("✅ Reporte CITAS generado: " + citas.size() + " citas");
        return reporte;
    }

    /**
     * Genera un reporte de ingresos basado en citas cumplidas.
     *
     * <p>Este reporte considera únicamente las citas con estado "cumplida" y proporciona:
     * - Ingresos totales del período
     * - Distribución de ingresos por día
     * - Promedio diario de ingresos
     * - Total de citas cumplidas</p>
     *
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return Mapa con el reporte de ingresos
     */
    private Map<String, Object> generarReporteIngresos(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);

        List<Cita> citas = obtenerCitasEnRango(inicio, fin, null);
        System.out.println("💰 Total citas en rango: " + citas.size());

        // Solo citas CUMPLIDAS para ingresos
        List<Cita> citasCumplidas = citas.stream()
                .filter(c -> "cumplida".equalsIgnoreCase(c.getEstado()))
                .collect(Collectors.toList());

        System.out.println("💰 Citas cumplidas: " + citasCumplidas.size());

        // Ingresos totales de citas CUMPLIDAS
        double totalIngresos = citasCumplidas.stream()
                .mapToDouble(c -> c.getCostoTotal() != null ? c.getCostoTotal().doubleValue() : 0.0)
                .sum();

        // Ingresos por día (solo CUMPLIDAS)
        Map<LocalDate, Map<String, Object>> ingresosPorDia = citasCumplidas.stream()
                .collect(Collectors.groupingBy(
                        cita -> cita.getFechaHoraInicio().toLocalDate(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                citasDelDia -> {
                                    double ingresosDia = citasDelDia.stream()
                                            .mapToDouble(c -> c.getCostoTotal() != null ? c.getCostoTotal().doubleValue() : 0.0)
                                            .sum();
                                    Map<String, Object> datosDia = new HashMap<>();
                                    datosDia.put("ingresos", ingresosDia);
                                    datosDia.put("citas", citasDelDia.size());
                                    return datosDia;
                                }
                        )
                ));

        // Promedio diario
        long dias = fechaInicio.datesUntil(fechaFin.plusDays(1)).count();
        double promedioDiario = dias > 0 ? totalIngresos / dias : 0;

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("totalIngresos", totalIngresos);
        reporte.put("ingresosPorDia", ingresosPorDia);
        reporte.put("promedioDiario", promedioDiario);
        reporte.put("totalCitasCumplidas", citasCumplidas.size());
        reporte.put("tipoReporte", "INGRESOS");

        System.out.println("✅ Reporte INGRESOS generado: $" + totalIngresos);
        return reporte;
    }

    /**
     * Genera un reporte de desempeño de empleados.
     *
     * <p>El reporte incluye para cada empleado:
     * - Total de citas asignadas
     * - Citas cumplidas
     * - Ingresos generados
     * - Calificación promedio (actualmente fija en 0.0)</p>
     *
     * <p>Los empleados se ordenan por ingresos generados de mayor a menor y solo
     * se incluyen aquellos con al menos una cita cumplida.</p>
     *
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return Mapa con el reporte de empleados
     */
    private Map<String, Object> generarReporteEmpleados(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);

        List<Empleado> empleados = empleadoRepository.findAll();
        List<Cita> citas = obtenerCitasEnRango(inicio, fin, null);

        List<Map<String, Object>> datosEmpleados = empleados.stream()
                .map(empleado -> {
                    // Todas las citas del empleado
                    List<Cita> citasEmpleado = citas.stream()
                            .filter(c -> c.getEmpleado() != null && c.getEmpleado().getId().equals(empleado.getId()))
                            .collect(Collectors.toList());

                    // Solo citas CUMPLIDAS del empleado
                    List<Cita> citasCumplidas = citasEmpleado.stream()
                            .filter(c -> "cumplida".equalsIgnoreCase(c.getEstado()))
                            .collect(Collectors.toList());

                    // Ingresos solo de citas CUMPLIDAS
                    double ingresos = citasCumplidas.stream()
                            .mapToDouble(c -> c.getCostoTotal() != null ? c.getCostoTotal().doubleValue() : 0.0)
                            .sum();

                    Map<String, Object> datos = new HashMap<>();
                    datos.put("id", empleado.getId());
                    datos.put("nombre", empleado.getNombre());
                    datos.put("totalCitas", citasEmpleado.size());
                    datos.put("citasCumplidas", citasCumplidas.size());
                    datos.put("ingresosGenerados", ingresos);
                    datos.put("calificacionPromedio", 0.0);
                    return datos;
                })
                .filter(emp -> (Integer) emp.get("citasCumplidas") > 0)
                .sorted((e1, e2) -> Double.compare(
                        (Double) e2.get("ingresosGenerados"),
                        (Double) e1.get("ingresosGenerados")
                ))
                .collect(Collectors.toList());

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("empleados", datosEmpleados);
        reporte.put("tipoReporte", "EMPLEADOS");
        reporte.put("totalEmpleados", datosEmpleados.size());

        System.out.println("✅ Reporte EMPLEADOS generado: " + datosEmpleados.size() + " empleados");
        return reporte;
    }

    /**
     * Genera un reporte de desempeño de servicios.
     *
     * <p>El reporte incluye para cada servicio:
     * - Veces solicitado
     * - Citas cumplidas
     * - Ingresos generados
     * - Precio promedio real vs precio base</p>
     *
     * <p>Los servicios se ordenan por número de citas cumplidas de mayor a menor
     * y solo se incluyen aquellos con al menos una cita cumplida.</p>
     *
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return Mapa con el reporte de servicios
     */
    private Map<String, Object> generarReporteServicios(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);

        List<Servicio> servicios = servicioRepository.findAll();
        List<Cita> citas = obtenerCitasEnRango(inicio, fin, null);

        List<Map<String, Object>> datosServicios = servicios.stream()
                .map(servicio -> {
                    // Todas las citas del servicio
                    List<Cita> citasServicio = citas.stream()
                            .filter(c -> c.getServicio() != null && c.getServicio().getId().equals(servicio.getId()))
                            .collect(Collectors.toList());

                    // Solo citas CUMPLIDAS del servicio
                    List<Cita> citasCumplidas = citasServicio.stream()
                            .filter(c -> "cumplida".equalsIgnoreCase(c.getEstado()))
                            .collect(Collectors.toList());

                    // Ingresos solo de citas CUMPLIDAS
                    double ingresos = citasCumplidas.stream()
                            .mapToDouble(c -> c.getCostoTotal() != null ? c.getCostoTotal().doubleValue() : 0.0)
                            .sum();

                    double precioPromedio = citasCumplidas.isEmpty() ? 0.0 :
                            ingresos / citasCumplidas.size();

                    Map<String, Object> datos = new HashMap<>();
                    datos.put("id", servicio.getId());
                    datos.put("nombre", servicio.getNombre());
                    datos.put("vecesSolicitado", citasServicio.size());
                    datos.put("cumplidas", citasCumplidas.size());
                    datos.put("ingresosGenerados", ingresos);
                    datos.put("precioPromedio", precioPromedio);
                    datos.put("precioBase", servicio.getCosto() != null ? servicio.getCosto().doubleValue() : 0.0);
                    return datos;
                })
                .filter(serv -> (Integer) serv.get("cumplidas") > 0)
                .sorted((s1, s2) -> Integer.compare(
                        (Integer) s2.get("cumplidas"),
                        (Integer) s1.get("cumplidas")
                ))
                .collect(Collectors.toList());

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("servicios", datosServicios);
        reporte.put("tipoReporte", "SERVICIOS");
        reporte.put("totalServicios", datosServicios.size());

        System.out.println("✅ Reporte SERVICIOS generado: " + datosServicios.size() + " servicios");
        return reporte;
    }

    /**
     * Obtiene las citas dentro de un rango de fechas y hora.
     *
     * <p>Si se proporciona un ID de empleado, filtra las citas por ese empleado específico.
     * Si no se proporciona, retorna todas las citas en el rango especificado.</p>
     *
     * @param inicio Fecha y hora de inicio del rango
     * @param fin Fecha y hora de fin del rango
     * @param empleadoId ID opcional del empleado para filtrar
     * @return Lista de citas que cumplen con los criterios de búsqueda
     */
    private List<Cita> obtenerCitasEnRango(LocalDateTime inicio, LocalDateTime fin, Long empleadoId) {
        List<Cita> citas;
        if (empleadoId != null) {
            citas = citaRepository.findByEmpleadoIdAndFechaHoraInicioBetween(empleadoId, inicio, fin);
        } else {
            citas = citaRepository.findByFechaHoraInicioBetween(inicio, fin);
        }
        System.out.println("🔍 Citas en rango: " + citas.size() + " citas");
        if (citas.size() > 0) {
            System.out.println("📝 Ejemplo de cita: " + citas.get(0).getEstado() + " - " + citas.get(0).getCostoTotal());
        }
        return citas;
    }

    /**
     * Obtiene un resumen completo del estado actual del negocio.
     *
     * <p>El resumen incluye métricas para el día actual y el mes en curso:
     * - Citas cumplidas hoy
     * - Ingresos generados hoy
     * - Citas cumplidas en el mes
     * - Ingresos generados en el mes</p>
     *
     * @return Mapa con el resumen de métricas del negocio
     */
    public Map<String, Object> obtenerResumenCompleto() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        System.out.println("📈 Obteniendo resumen completo para: " + hoy);

        // Calcular citas CUMPLIDAS de hoy
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(23, 59, 59);
        List<Cita> citasHoy = citaRepository.findByFechaHoraInicioBetween(inicioHoy, finHoy);
        List<Cita> citasHoyCumplidas = citasHoy.stream()
                .filter(c -> "cumplida".equalsIgnoreCase(c.getEstado()))
                .collect(Collectors.toList());

        int citasHoyCount = citasHoyCumplidas.size();
        double ingresosHoy = citasHoyCumplidas.stream()
                .mapToDouble(c -> c.getCostoTotal() != null ? c.getCostoTotal().doubleValue() : 0.0)
                .sum();

        // Calcular citas CUMPLIDAS del mes
        LocalDateTime inicioMesDateTime = inicioMes.atStartOfDay();
        LocalDateTime finHoyDateTime = hoy.atTime(23, 59, 59);
        List<Cita> citasMes = citaRepository.findByFechaHoraInicioBetween(inicioMesDateTime, finHoyDateTime);
        List<Cita> citasMesCumplidas = citasMes.stream()
                .filter(c -> "cumplida".equalsIgnoreCase(c.getEstado()))
                .collect(Collectors.toList());

        int citasMesCount = citasMesCumplidas.size();
        double ingresosMes = citasMesCumplidas.stream()
                .mapToDouble(c -> c.getCostoTotal() != null ? c.getCostoTotal().doubleValue() : 0.0)
                .sum();

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("citasHoy", citasHoyCount);
        resumen.put("ingresosHoy", ingresosHoy);
        resumen.put("citasMes", citasMesCount);
        resumen.put("ingresosMes", ingresosMes);

        System.out.println("📊 Resumen: Hoy=" + citasHoyCount + " citas, Mes=" + citasMesCount + " citas");
        return resumen;
    }
}