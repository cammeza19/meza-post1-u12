package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraCapacitacionTest {

    private final CalculadoraCapacitacion calculadora = new CalculadoraCapacitacion();

    @Test
    void calculaOchentaPorCientoHastaTopeAnual() {
        // 80% de 2.000.000 = 1.600.000 (menor al tope de 3.000.000)
        var solicitud = new SolicitudReembolso(1L, "EMP01", CategoriaGasto.CAPACITACION, 
                new BigDecimal("2000000"), LocalDate.now(), 0, true);

        assertEquals(new BigDecimal("1600000.0"), calculadora.calcular(solicitud));
    }
}
