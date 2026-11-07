package com.empresa.reembolsos;

import com.empresa.reembolsos.adaptadores.rest.SolicitudReembolsoController;
import com.empresa.reembolsos.aplicacion.SolicitudInvalidaException;
import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.procesamiento.notificacion.NotificadorAuditoria;
import com.empresa.reembolsos.procesamiento.notificacion.NotificadorContabilidad;
import com.empresa.reembolsos.procesamiento.notificacion.NotificadorEmpleado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
class SolicitudReembolsoIntegrationTest {

    @Autowired
    private SolicitudReembolsoController controller;

    @MockitoSpyBean
    private NotificadorEmpleado notificadorEmpleado;

    @MockitoSpyBean
    private NotificadorContabilidad notificadorContabilidad;

    @MockitoSpyBean
    private NotificadorAuditoria notificadorAuditoria;

    @ParameterizedTest(name = "{0}: solicita {1}, {2} dias -> aprueba {3}")
    @CsvSource({
            "VIAJE,          120000,  2, 120000",
            "REPRESENTACION, 1500000, 0, 1500000",
            "CAPACITACION,   2000000, 0, 1600000"
    })
    void creaSolicitudYCalculaMontoSegunCategoria(CategoriaGasto categoria, String solicitado,
                                                  int dias, String esperado) {
        var nueva = new SolicitudReembolso(null, "EMP-" + categoria, categoria,
                new BigDecimal(solicitado), LocalDate.now(), dias, true);

        var respuesta = controller.crear(nueva);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        var creada = respuesta.getBody();
        assertNotNull(creada);
        assertNotNull(creada.getId());
        assertEquals(EstadoSolicitud.PENDIENTE, creada.getEstado());
        assertEquals(0, new BigDecimal(esperado).compareTo(creada.getMontoAprobado()),
                "Monto aprobado incorrecto para " + categoria);
    }

    @Test
    void cambioDeEstadoNotificaALosTresObservadores() {
        var creada = controller.crear(new SolicitudReembolso(null, "EMP007", CategoriaGasto.VIAJE,
                new BigDecimal("120000"), LocalDate.now(), 2, true)).getBody();
        assertNotNull(creada);

        var respuesta = controller.cambiarEstado(creada.getId(), EstadoSolicitud.APROBADA);

        assertEquals(EstadoSolicitud.APROBADA, respuesta.getBody().getEstado());
        verify(notificadorEmpleado).onCambioEstado(argThat(s -> s.getId().equals(creada.getId())), eq(EstadoSolicitud.PENDIENTE));
        verify(notificadorContabilidad).onCambioEstado(argThat(s -> s.getId().equals(creada.getId())), eq(EstadoSolicitud.PENDIENTE));
        verify(notificadorAuditoria).onCambioEstado(argThat(s -> s.getId().equals(creada.getId())), eq(EstadoSolicitud.PENDIENTE));
    }

    @Test
    void rechazaSolicitudSinDocumentosSoporte() {
        var sinDocumentos = new SolicitudReembolso(null, "EMP008", CategoriaGasto.VIAJE,
                new BigDecimal("80000"), LocalDate.now(), 1, false);

        var excepcion = assertThrows(SolicitudInvalidaException.class, () -> controller.crear(sinDocumentos));
        assertEquals("Faltan documentos soporte", excepcion.getMessage());
    }

    @Test
    void rechazaSolicitudQueExcedeElTopeDeCategoria() {
        var excedida = new SolicitudReembolso(null, "EMP009", CategoriaGasto.REPRESENTACION,
                new BigDecimal("2500000"), LocalDate.now(), 0, true);

        var excepcion = assertThrows(SolicitudInvalidaException.class, () -> controller.crear(excedida));
        assertTrue(excepcion.getMessage().contains("Monto excede el tope"));
    }

    @Test
    void rechazaSolicitudDuplicadaEnElMismoPeriodo() {
        var fecha = LocalDate.now();
        controller.crear(new SolicitudReembolso(null, "EMP010", CategoriaGasto.CAPACITACION,
                new BigDecimal("500000"), fecha, 0, true));

        var duplicada = new SolicitudReembolso(null, "EMP010", CategoriaGasto.CAPACITACION,
                new BigDecimal("300000"), fecha, 0, true);

        var excepcion = assertThrows(SolicitudInvalidaException.class, () -> controller.crear(duplicada));
        assertTrue(excepcion.getMessage().contains("Ya existe una solicitud pendiente"));
    }
}
