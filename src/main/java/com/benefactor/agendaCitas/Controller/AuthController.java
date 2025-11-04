package com.benefactor.agendaCitas.Controller;

import com.benefactor.agendaCitas.Servicios.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Controlador para operaciones de autenticación y gestión de sesiones
 * Maneja login, logout y verificación de sesiones de usuario
 * Utiliza cookies HTTP-only para mayor seguridad
 *
 * @RestController Indica que esta clase es un controlador REST
 * @RequestMapping("/api/auth") Define la ruta base para endpoints de autenticación
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Endpoint para iniciar sesión de usuario
     * Crea una cookie de sesión segura con configuración dinámica para entornos local/producción
     *
     * @param credentials Mapa con credenciales de usuario (usuario, contrasena)
     * @param request Objeto HttpServletRequest para detectar el entorno
     * @param response Objeto HttpServletResponse para configurar la cookie
     * @return ResponseEntity con resultado del login o error de autenticación
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        try {
            String usuario = credentials.get("usuario");
            String contrasena = credentials.get("contrasena");

            // Validar credenciales proporcionadas
            if (usuario == null || contrasena == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario y contraseña son requeridos"));
            }

            // Autenticar usuario y obtener ID de sesión
            String sessionId = authService.login(usuario, contrasena);

            // Detectar si la solicitud viene de entorno local
            String origin = request.getHeader("Origin");
            boolean isLocal = origin != null && (
                    origin.contains("localhost") ||
                            origin.contains("127.0.0.1") ||
                            origin.contains("0.0.0.0")
            );

            // Configurar atributos de cookie según el entorno
            boolean secure = !isLocal; // Solo seguro en producción
            String sameSite = isLocal ? "Lax" : "None";

            // Crear cookie de sesión segura
            ResponseCookie cookie = ResponseCookie.from("sessionId", sessionId)
                    .httpOnly(true)      // No accesible desde JavaScript
                    .secure(true)        // Solo sobre HTTPS
                    .path("/")           // Disponible en todas las rutas
                    .maxAge(Duration.ofHours(24)) // Expira en 24 horas
                    .sameSite("none")    // Permite cross-site en producción
                    .build();

            // Agregar cookie a la respuesta
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Login exitoso",
                    "usuario", usuario
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para cerrar sesión de usuario
     * Elimina la cookie de sesión del navegador y limpia la sesión del servidor
     *
     * @param sessionId Cookie de sesión actual (opcional)
     * @param request Objeto HttpServletRequest para detectar el entorno
     * @param response Objeto HttpServletResponse para eliminar la cookie
     * @return ResponseEntity confirmando el cierre de sesión
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "sessionId", required = false) String sessionId,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        // Invalidar sesión en el servidor si existe
        if (sessionId != null) authService.logout(sessionId);

        // Detectar entorno para configuración de cookie
        String origin = request.getHeader("Origin");
        boolean isLocal = origin != null && (
                origin.contains("localhost") ||
                        origin.contains("127.0.0.1") ||
                        origin.contains("0.0.0.0")
        );

        boolean secure = !isLocal;
        String sameSite = isLocal ? "Lax" : "None";

        // Crear cookie vacía con tiempo de expiración 0 para eliminación
        ResponseCookie cookie = ResponseCookie.from("sessionId", "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(0) // Expira inmediatamente
                .sameSite(sameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
    }

    /**
     * Endpoint para verificar la validez de una sesión activa
     * Valida si la cookie de sesión existe y es válida en el servidor
     *
     * @param sessionId Cookie de sesión a verificar (opcional)
     * @return ResponseEntity con estado de la sesión o error de autenticación
     */
    @GetMapping("/verificar")
    public ResponseEntity<?> verificarSesion(@CookieValue(value = "sessionId", required = false) String sessionId) {
        // Verificar existencia de cookie
        if (sessionId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No hay sesión activa"));
        }

        // Validar sesión en el servidor
        boolean valida = authService.validarSesion(sessionId);
        if (!valida) {
            return ResponseEntity.status(401).body(Map.of("error", "Sesión expirada o inválida"));
        }

        // Obtener información del usuario de la sesión válida
        String usuario = authService.getUsuarioDeSesion(sessionId);
        return ResponseEntity.ok(Map.of("mensaje", "Sesión válida", "usuario", usuario));
    }
}