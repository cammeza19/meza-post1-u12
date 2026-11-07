package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraViajeTest {

    private final CalculadoraViaje calculadora = new CalculadoraViaje();

    @Test
    void calculaTopeDiarioMultiplicadoPorDias() {
        // 3 dias * 150000 = 450000 maximo
        var solicitud = new SolicitudReembolso(1L, "EMP01", CategoriaGasto.VIAJE, 
                new BigDecimal("500000"), LocalDate.now(), 3, true);

        var montoAprobado = calculadora.calcular(solicitud);

        assertEquals(new BigDecimal("450000"), montoAprobado);
    }
}
