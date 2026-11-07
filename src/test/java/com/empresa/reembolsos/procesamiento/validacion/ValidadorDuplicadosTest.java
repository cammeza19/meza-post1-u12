package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorDuplicadosTest {

    private final ValidadorDuplicados validador = new ValidadorDuplicados();

    @Test
    void rechazaSiExisteSolicitudMismaCategoriaYMismoPeriodo() {
        var fecha = LocalDate.of(2026, 3, 10);
        var solicitudExistente = new SolicitudReembolso(1L, "EMP01", CategoriaGasto.REPRESENTACION, 
                new BigDecimal("500000"), fecha, 0, true);
        var nuevaSolicitud = new SolicitudReembolso(2L, "EMP01", CategoriaGasto.REPRESENTACION, 
                new BigDecimal("300000"), fecha, 0, true);

        var resultado = validador.validar(nuevaSolicitud, List.of(solicitudExistente));

        assertFalse(resultado.valida());
        assertTrue(resultado.motivoRechazo().contains("Ya existe una solicitud pendiente"));
    }

    @Test
    void apruebaSiNoHayDuplicados() {
        var fecha = LocalDate.of(2026, 3, 10);
        var solicitudExistente = new SolicitudReembolso(1L, "EMP01", CategoriaGasto.VIAJE, 
                new BigDecimal("500000"), fecha, 1, true);
        var nuevaSolicitud = new SolicitudReembolso(2L, "EMP01", CategoriaGasto.REPRESENTACION, 
                new BigDecimal("300000"), fecha, 0, true);

        var resultado = validador.validar(nuevaSolicitud, List.of(solicitudExistente));

        assertTrue(resultado.valida());
    }
}