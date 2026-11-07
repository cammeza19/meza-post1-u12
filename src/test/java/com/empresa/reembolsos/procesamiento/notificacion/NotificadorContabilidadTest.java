package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificadorContabilidadTest {

    private final NotificadorContabilidad notificador = new NotificadorContabilidad();
    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    private PrintStream salidaOriginal;

    @BeforeEach
    void capturarSalida() {
        salidaOriginal = System.out;
        System.setOut(new PrintStream(salida));
    }

    @AfterEach
    void restaurarSalida() {
        System.setOut(salidaOriginal);
    }

    @ParameterizedTest
    @EnumSource(EstadoSolicitud.class)
    void soloProgramaPagoCuandoLaSolicitudEsAprobada(EstadoSolicitud nuevoEstado) {
        var solicitud = new SolicitudReembolso(1L, "EMP01", CategoriaGasto.VIAJE,
                new BigDecimal("100000"), LocalDate.now(), 1, true);
        solicitud.setMontoAprobado(new BigDecimal("100000"));
        solicitud.setEstado(nuevoEstado);

        notificador.onCambioEstado(solicitud, EstadoSolicitud.PENDIENTE);

        boolean programoPago = salida.toString().contains("Contabilidad: programar pago");
        assertEquals(nuevoEstado == EstadoSolicitud.APROBADA, programoPago,
                "Contabilidad solo debe reaccionar ante APROBADA, estado recibido: " + nuevoEstado);
    }
}
