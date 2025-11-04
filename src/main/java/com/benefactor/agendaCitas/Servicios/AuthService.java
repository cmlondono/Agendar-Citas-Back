package com.benefactor.agendaCitas.Servicios;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para gestión de autenticación y sesiones de usuario
 * Maneja el login, logout, validación y mantenimiento de sesiones activas
 * Implementa un sistema de sesiones en memoria con expiración automática
 *
 * @Service Indica que esta clase es un componente de servicio de Spring
 * Proporciona lógica de negocio para la autenticación del sistema
 */
@Service
public class AuthService {

    /**
     * Credenciales de administrador para acceso al sistema
     * En una implementación real, estas credenciales deberían estar en la base de datos
     */
    private static final String USUARIO_ADMIN = "admin";
    private static final String CONTRASENA_ADMIN = "admin123";

    /**
     * Tiempo de expiración de sesión en minutos (24 horas)
     * Define el período de inactividad después del cual una sesión se considera expirada
     */
    private static final int TIEMPO_EXPIRACION_SESION_MINUTOS = 24 * 60;

    /**
     * Mapa thread-safe para almacenar sesiones activas
     * Clave: sessionId (UUID)
     * Valor: Objeto SesionUsuario con información de la sesión
     * ConcurrentHashMap asegura operaciones seguras en entorno multi-hilo
     */
    private final Map<String, SesionUsuario> sesionesActivas = new ConcurrentHashMap<>();

    /**
     * Clase interna para almacenar información de la sesión de usuario
     * Mantiene track del usuario, fecha de creación y último acceso
     * Implementa lógica de expiración basada en tiempo de inactividad
     */
    private static class SesionUsuario {
        private final String usuario;
        private final LocalDateTime fechaCreacion;
        private LocalDateTime fechaUltimoAcceso;

        /**
         * Constructor que inicializa una nueva sesión
         *
         * @param usuario Nombre del usuario autenticado
         */
        public SesionUsuario(String usuario) {
            this.usuario = usuario;
            this.fechaCreacion = LocalDateTime.now();
            this.fechaUltimoAcceso = LocalDateTime.now();
        }

        /**
         * Actualiza la fecha del último acceso a la hora actual
         * Se llama cada vez que se valida la sesión para extender su vida útil
         */
        public void actualizarAcceso() {
            this.fechaUltimoAcceso = LocalDateTime.now();
        }

        /**
         * Verifica si la sesión ha expirado basado en el tiempo de inactividad
         *
         * @return true si la sesión ha expirado, false si aún es válida
         */
        public boolean haExpirado() {
            return fechaUltimoAcceso.plusMinutes(TIEMPO_EXPIRACION_SESION_MINUTOS)
                    .isBefore(LocalDateTime.now());
        }

        // ========== GETTERS ==========

        public String getUsuario() { return usuario; }
        public LocalDateTime getFechaCreacion() { return fechaCreacion; }
        public LocalDateTime getFechaUltimoAcceso() { return fechaUltimoAcceso; }
    }

    /**
     * Autentica un usuario y crea una nueva sesión si las credenciales son válidas
     *
     * @param usuario Nombre de usuario para autenticar
     * @param contrasena Contraseña del usuario
     * @return sessionId único generado para la sesión
     * @throws RuntimeException si las credenciales son inválidas
     */
    public String login(String usuario, String contrasena) {
        if (USUARIO_ADMIN.equals(usuario) && CONTRASENA_ADMIN.equals(contrasena)) {
            String sessionId = generarSessionId();
            sesionesActivas.put(sessionId, new SesionUsuario(usuario));
            return sessionId;
        }

        throw new RuntimeException("Credenciales inválidas");
    }

    /**
     * Valida si una sesión es activa y no ha expirado
     * Actualiza la fecha de último acceso si la sesión es válida
     *
     * @param sessionId Identificador de la sesión a validar
     * @return true si la sesión es válida, false en caso contrario
     */
    public boolean validarSesion(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) return false;

        limpiarSesionesExpiradas();
        SesionUsuario sesion = sesionesActivas.get(sessionId);

        if (sesion == null || sesion.haExpirado()) {
            sesionesActivas.remove(sessionId);
            return false;
        }

        sesion.actualizarAcceso();
        return true;
    }

    /**
     * Cierra una sesión removiéndola del mapa de sesiones activas
     *
     * @param sessionId Identificador de la sesión a cerrar
     */
    public void logout(String sessionId) {
        if (sessionId != null) sesionesActivas.remove(sessionId);
    }

    /**
     * Obtiene el nombre de usuario asociado a una sesión válida
     * Actualiza la fecha de último acceso si la sesión es válida
     *
     * @param sessionId Identificador de la sesión
     * @return Nombre del usuario o null si la sesión no es válida
     */
    public String getUsuarioDeSesion(String sessionId) {
        SesionUsuario sesion = sesionesActivas.get(sessionId);
        if (sesion != null && !sesion.haExpirado()) {
            sesion.actualizarAcceso();
            return sesion.getUsuario();
        }
        return null;
    }

    /**
     * Proporciona información detallada del estado de una sesión
     * Útil para debugging y monitoreo de sesiones activas
     *
     * @param sessionId Identificador de la sesión a verificar
     * @return Mapa con información del estado de la sesión
     */
    public Map<String, Object> verificarEstadoSesion(String sessionId) {
        Map<String, Object> estado = new HashMap<>();
        SesionUsuario sesion = sesionesActivas.get(sessionId);

        if (sesion == null) {
            estado.put("valido", false);
            estado.put("mensaje", "Sesión no encontrada o expirada");
        } else {
            estado.put("valido", true);
            estado.put("usuario", sesion.getUsuario());
            estado.put("fechaCreacion", sesion.getFechaCreacion());
            estado.put("ultimoAcceso", sesion.getFechaUltimoAcceso());
        }
        return estado;
    }

    /**
     * Genera un identificador único para una nueva sesión
     *
     * @return UUID único sin guiones
     */
    private String generarSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Limpia automáticamente las sesiones que han expirado
     * Se ejecuta antes de cada validación para mantener el mapa limpio
     */
    private void limpiarSesionesExpiradas() {
        sesionesActivas.entrySet().removeIf(entry -> entry.getValue().haExpirado());
    }
}