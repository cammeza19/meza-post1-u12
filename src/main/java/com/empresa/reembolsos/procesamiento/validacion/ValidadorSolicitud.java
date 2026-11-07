package com.empresa.reembolsos.procesamiento.validacion;

import java.util.List;

import com.empresa.reembolsos.dominio.SolicitudReembolso;

/**
 * Eslabón base del patrón <b>Chain of Responsibility</b> para la validación en varias etapas (RF1).
 * <p>
 * Cada subclase implementa una única verificación en {@link #validarPropio}. Si la verificación
 * falla, la cadena se corta (fail-fast) y se devuelve el motivo de rechazo; si pasa, la solicitud
 * se delega al siguiente eslabón sin que el eslabón actual sepa cuántos quedan después de él.
 * Agregar una verificación nueva (p. ej. límite anual acumulado) consiste en crear una subclase
 * y enlazarla en {@link CadenaValidadores}, sin modificar los validadores existentes.
 */
public abstract class ValidadorSolicitud {

    private ValidadorSolicitud siguiente;

    /**
     * Enlaza el siguiente eslabón de la cadena.
     *
     * @param siguiente validador que se ejecutará si este aprueba la solicitud
     * @return el validador recibido, para permitir encadenamiento fluido
     */
    public ValidadorSolicitud siguiente(ValidadorSolicitud siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /**
     * Ejecuta la verificación propia y, si aprueba, delega en el siguiente eslabón.
     *
     * @param solicitud             solicitud a validar
     * @param pendientesDelEmpleado solicitudes PENDIENTES del mismo empleado, ya cargadas por el
     *                              servicio de aplicación (este paquete no accede a persistencia)
     * @return el primer rechazo encontrado, o {@link ResultadoValidacion#ok()} si todos aprueban
     */
    public ResultadoValidacion validar(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado) {
        ResultadoValidacion propio = validarPropio(solicitud, pendientesDelEmpleado);
        if (!propio.valida() || siguiente == null) {
            return propio;
        }
        return siguiente.validar(solicitud, pendientesDelEmpleado);
    }

    /**
     * Verificación concreta de este eslabón.
     *
     * @param solicitud             solicitud a validar
     * @param pendientesDelEmpleado solicitudes pendientes del mismo empleado
     * @return resultado de esta verificación únicamente
     */
    protected abstract ResultadoValidacion validarPropio(
            SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado);
}
