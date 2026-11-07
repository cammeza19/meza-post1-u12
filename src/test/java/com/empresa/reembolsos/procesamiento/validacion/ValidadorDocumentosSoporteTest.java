package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorDocumentosSoporteTest {

    private final ValidadorDocumentosSoporte validador = new ValidadorDocumentosSoporte();

    @Test
    void rechazaSiNoTieneDocumentosSoporte() {
        var solicitud = new SolicitudReembolso(1L, "EMP01", CategoriaGasto.VIAJE, 
                new BigDecimal("50000"), LocalDate.now(), 2, false);

        var resultado = validador.validar(solicitud, Collections.emptyList());

        assertFalse(resultado.valida());
        assertEquals("Faltan documentos soporte", resultado.motivoRechazo());
    }

    @Test
    void apruebaSiTieneDocumentosSoporte() {
        var solicitud = new SolicitudReembolso(1L, "EMP01", CategoriaGasto.VIAJE, 
                new BigDecimal("50000"), LocalDate.now(), 2, true);

        var resultado = validador.validar(solicitud, Collections.emptyList());

        assertTrue(resultado.valida());
        assertNull(resultado.motivoRechazo());
    }
}