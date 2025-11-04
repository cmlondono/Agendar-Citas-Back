package com.benefactor.agendaCitas.DTO;

/**
 * DTO (Data Transfer Object) para solicitudes de autenticación de usuarios
 * Representa las credenciales necesarias para iniciar sesión en el sistema
 * Contiene los datos mínimos requeridos para el proceso de autenticación
 *
 * Se utiliza exclusivamente en el endpoint de login del controlador de autenticación
 * para recibir de forma segura las credenciales del usuario desde el frontend
 */
public class LoginRequest {

    /**
     * Nombre de usuario o identificador único para el inicio de sesión
     * Campo requerido que identifica al usuario en el sistema
     * Normalmente corresponde al username o email del usuario
     */
    private String usuario;

    /**
     * Contraseña del usuario para verificación de identidad
     * Campo requerido que valida la autenticidad del usuario
     * Debe ser manejado de forma segura y nunca almacenado en texto plano
     */
    private String contrasena;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor por defecto requerido para la deserialización JSON
     * Framework como Jackson utilizan este constructor para crear instancias
     * automáticamente desde el cuerpo de las solicitudes HTTP POST
     */
    public LoginRequest() {}

    /**
     * Constructor completo para crear instancias con todas las credenciales
     * Útil para testing, creación programática de objetos y escenarios específicos
     *
     * @param usuario Nombre de usuario o identificador único
     * @param contrasena Contraseña del usuario para autenticación
     */
    public LoginRequest(String usuario, String contrasena) {
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    // ========== GETTERS Y SETTERS ==========

    /**
     * Obtiene el nombre de usuario para autenticación
     *
     * @return Nombre de usuario o identificador único
     */
    public String getUsuario() { return usuario; }

    /**
     * Establece el nombre de usuario para autenticación
     *
     * @param usuario Nombre de usuario o identificador único
     */
    public void setUsuario(String usuario) { this.usuario = usuario; }

    /**
     * Obtiene la contraseña del usuario para verificación
     *
     * @return Contraseña del usuario en texto plano (debe ser hasheada después)
     */
    public String getContrasena() { return contrasena; }

    /**
     * Establece la contraseña del usuario para verificación
     *
     * @param contrasena Contraseña del usuario en texto plano
     */
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}