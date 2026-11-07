package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa; // <-- VIOLACIÓN ARQUITECTÓNICA
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ValidadorDuplicados extends ValidadorSolicitud {

    // Se agrega este campo para forzar la dependencia en el bytecode compilado
    private RepositorioSolicitudesJpa repositorioViolacion;

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        boolean existeDuplicado = pendientes.stream()
                .anyMatch(p -> p.getCategoria() == solicitud.getCategoria()
                        && p.getFechaGasto().getMonth() == solicitud.getFechaGasto().getMonth());
        return existeDuplicado
                ? ResultadoValidacion.rechazar("Ya existe una solicitud pendiente de la misma categoria en el periodo")
                : ResultadoValidacion.ok();
    }
}