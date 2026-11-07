package com.empresa.reembolsos.procesamiento.validacion;

import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Component;

import com.empresa.reembolsos.dominio.SolicitudReembolso;

/**
 * Rechaza la solicitud si el empleado ya tiene otra PENDIENTE de la misma categoría en el mismo
 * período (mes y año). Es el eslabón más costoso (recorre la lista), por eso va al final de la cadena.
 */
@Component
public class ValidadorDuplicados extends ValidadorSolicitud {

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        YearMonth periodo = YearMonth.from(solicitud.getFechaGasto());
        boolean existeDuplicado = pendientes.stream()
                .anyMatch(p -> p.getCategoria() == solicitud.getCategoria()
                        && YearMonth.from(p.getFechaGasto()).equals(periodo));
        return existeDuplicado
                ? ResultadoValidacion.rechazar("Ya existe una solicitud pendiente de la misma categoria en el periodo")
                : ResultadoValidacion.ok();
    }
}
