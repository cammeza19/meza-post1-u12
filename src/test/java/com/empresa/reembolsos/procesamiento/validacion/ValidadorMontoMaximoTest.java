package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorMontoMaximoTest {

    private final ValidadorMontoMaximo validador = new ValidadorMontoMaximo();

    @Test
    void rechazaSiExcedeTopeDeCategoria() {
        var solicitud = new SolicitudReembolso(1L, "EMP01", CategoriaGasto.VIAJE, 
                new BigDecimal("200000"), LocalDate.now(), 1, true);

        var resultado = validador.validar(solicitud, Collections.emptyList());

        assertFalse(resultado.valida());
        assertTrue(resultado.motivoRechazo().contains("Monto excede el tope"));
    }

    @Test
    void apruebaSiMontoDentroDelTope() {
        var solicitud = new SolicitudReembolso(1L, "EMP01", CategoriaGasto.VIAJE, 
                new BigDecimal("100000"), LocalDate.now(), 1, true);

        var resultado = validador.validar(solicitud, Collections.emptyList());

        assertTrue(resultado.valida());
    }
}