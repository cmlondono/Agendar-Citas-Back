package com.benefactor.agendaCitas.Config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

/**
 * Manejador global de excepciones para la aplicación
 * Captura y procesa excepciones no manejadas en los controladores
 * Proporciona respuestas consistentes de error al cliente
 *
 * @RestControllerAdvice Habilita el manejo global de excepciones para todos los controladores REST
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de tipo RuntimeException
     * Incluye la mayoría de excepciones de negocio y validación
     *
     * @param e Excepción RuntimeException capturada
     * @return ResponseEntity con código 400 Bad Request y mensaje de error
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    /**
     * Maneja excepciones generales no capturadas por otros manejadores
     * Actúa como capturador de última instancia para cualquier excepción
     *
     * @param e Excepción general capturada
     * @return ResponseEntity con código 500 Internal Server Error y mensaje genérico
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
    }
}