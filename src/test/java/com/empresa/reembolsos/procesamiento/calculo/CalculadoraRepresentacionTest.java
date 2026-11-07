package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraRepresentacionTest {

    private final CalculadoraRepresentacion calculadora = new CalculadoraRepresentacion();

    @Test
    void calculaHastaTopeMensual() {
        // Tope 2.000.000
        var solicitudExcedida = new SolicitudReembolso(1L, "EMP01", CategoriaGasto.REPRESENTACION, 
                new BigDecimal("2500000"), LocalDate.now(), 0, true);

        assertEquals(new BigDecimal("2000000"), calculadora.calcular(solicitudExcedida));
    }
}
