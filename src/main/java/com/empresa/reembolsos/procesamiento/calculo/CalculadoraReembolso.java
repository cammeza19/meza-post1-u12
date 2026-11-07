package com.empresa.reembolsos.procesamiento.calculo;

import java.math.BigDecimal;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

/**
 * Contrato del patrón <b>Strategy</b> para el cálculo del monto aprobado por categoría (RF2).
 * <p>
 * Cada implementación encapsula la fórmula de una sola {@link CategoriaGasto}. Las implementaciones
 * anotadas con {@code @Component} son descubiertas por Spring e indexadas por
 * {@link SelectorCalculadora}, de modo que una categoría nueva (p. ej. BIENESTAR) se agrega creando
 * una clase nueva, sin modificar las existentes ni el selector (Open/Closed).
 */
public interface CalculadoraReembolso {

    /**
     * @return la categoría de gasto que esta estrategia sabe calcular; debe ser única entre
     *         todas las implementaciones registradas
     */
    CategoriaGasto getCategoria();

    /**
     * Calcula el monto aprobado aplicando la fórmula propia de la categoría.
     *
     * @param solicitud solicitud ya validada por la cadena de validadores
     * @return monto aprobado, nunca mayor que el monto solicitado
     */
    BigDecimal calcular(SolicitudReembolso solicitud);
}
