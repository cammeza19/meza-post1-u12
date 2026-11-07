package com.empresa.reembolsos;

import com.empresa.reembolsos.adaptadores.rest.SolicitudReembolsoController;
import com.empresa.reembolsos.aplicacion.SolicitudInvalidaException;
import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SolicitudReembolsoIntegrationTest {

    @Autowired
    private SolicitudReembolsoController controller;

    @Test
    void flujoCompletoCrearYAprobarSolicitud() {
        // 1. Crear solicitud de viaje válida (2 días a 120.000 -> tope 300.000, aprueba 120.000)
        var nueva = new SolicitudReembolso(null, "EMP007", CategoriaGasto.VIAJE, 
                new BigDecimal("120000"), LocalDate.now(), 2, true);

        var respuestaCrear = controller.crear(nueva);

        assertEquals(HttpStatus.OK, respuestaCrear.getStatusCode());
        assertNotNull(respuestaCrear.getBody());
        assertNotNull(respuestaCrear.getBody().getId());
        assertEquals(EstadoSolicitud.PENDIENTE, respuestaCrear.getBody().getEstado());
        assertEquals(new BigDecimal("120000"), respuestaCrear.getBody().getMontoAprobado());

        // 2. Cambiar estado a APROBADA (dispara notificaciones a los 3 observadores)
        Long id = respuestaCrear.getBody().getId();
        var respuestaEstado = controller.cambiarEstado(id, EstadoSolicitud.APROBADA);

        assertEquals(HttpStatus.OK, respuestaEstado.getStatusCode());
        assertNotNull(respuestaEstado.getBody());
        assertEquals(EstadoSolicitud.APROBADA, respuestaEstado.getBody().getEstado());
    }

    @Test
    void rechazaSolicitudSinDocumentosSoporte() {
        var sinDocumentos = new SolicitudReembolso(null, "EMP008", CategoriaGasto.VIAJE, 
                new BigDecimal("80000"), LocalDate.now(), 1, false);

        // Al no tener documentos de soporte, la cadena de validación lanza SolicitudInvalidaException
        var excepcion = assertThrows(SolicitudInvalidaException.class, () -> controller.crear(sinDocumentos));
        assertEquals("Faltan documentos soporte", excepcion.getMessage());
    }
}