package com.empresa.reembolsos.adaptadores.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.empresa.reembolsos.aplicacion.SolicitudInvalidaException;

/**
 * Traduce las excepciones de la capa de aplicación a respuestas HTTP con un mensaje claro
 * (checkpoint del Paso 5: "rechaza una solicitud sin documentos soporte con un mensaje claro").
 * Vive en adaptadores.rest y solo usa aplicacion + org.springframework.web/http (cumple la Regla 2).
 */
@RestControllerAdvice
public class ManejadorErroresRest {

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ProblemDetail solicitudInvalida(SolicitudInvalidaException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problema.setTitle("Solicitud de reembolso rechazada");
        return problema;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail argumentoInvalido(IllegalArgumentException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Recurso no encontrado o categoria no soportada");
        return problema;
    }
}
