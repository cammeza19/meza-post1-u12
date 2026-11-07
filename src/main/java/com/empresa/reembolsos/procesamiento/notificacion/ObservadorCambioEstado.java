package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

/**
 * Suscriptor del patrón <b>Observer</b> para los cambios de estado de una solicitud (RF3).
 * <p>
 * {@code SolicitudReembolsoService} notifica a todas las implementaciones registradas como
 * {@code @Component} sin conocer su identidad ni su reacción concreta. Cada suscriptor decide si
 * actúa (p. ej. Contabilidad solo reacciona ante APROBADA). Esta interfaz es además el
 * <i>seam</i> documentado en ADR-001 para la futura integración con la API externa de Contabilidad.
 */
public interface ObservadorCambioEstado {

    /**
     * Se invoca después de persistir el nuevo estado de la solicitud.
     *
     * @param solicitud      solicitud con el estado ya actualizado ({@code solicitud.getEstado()})
     * @param estadoAnterior estado que tenía la solicitud antes del cambio
     */
    void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior);
}
