package com.empresa.reembolsos;

import com.empresa.reembolsos.procesamiento.calculo.CalculadoraReembolso;
import com.empresa.reembolsos.procesamiento.notificacion.ObservadorCambioEstado;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Reglas que verifican los limites que SI existen en el sistema (coherentes con ADR-001:
 * ninguna regla asume puertos de persistencia).
 */
@AnalyzeClasses(packages = "com.empresa.reembolsos", importOptions = { ImportOption.DoNotIncludeTests.class })
public class ReglasArquitectura {

    // Regla 1: procesamiento no depende de infraestructura, adaptadores, JPA ni de la capa web.
    // Se permiten anotaciones de estereotipo (org.springframework.stereotype) - ver Decision de Diseno 4.
    @ArchTest
    static final ArchRule procesamientoSinPersistenciaNiWeb = noClasses()
            .that().resideInAPackage("..procesamiento..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "..infraestructura..", "..adaptadores..",
                    "org.springframework.data..", "jakarta.persistence..",
                    "org.springframework.web..", "org.springframework.http..", "jakarta.servlet..")
            .because("los patrones de procesamiento deben ser logica pura, testeable sin base de datos ni HTTP");

    // Regla 2: el controlador solo accede al servicio de aplicacion, al dominio y a su propio paquete
    @ArchTest
    static final ArchRule controladorSoloUsaServicio = classes()
            .that().resideInAPackage("..adaptadores.rest..")
            .should().onlyAccessClassesThat()
            .resideInAnyPackage(
                    "..adaptadores.rest..",
                    "..aplicacion..", "..dominio..",
                    "org.springframework.web..", "org.springframework.http..", "java..")
            .because("el adaptador REST no debe saltarse el servicio de aplicacion para llegar a JPA o a los patrones");

    // Regla 3: las calculadoras implementan CalculadoraReembolso
    // (el filtro por nombre excluye a SelectorCalculadora, que es el orquestador y no una estrategia)
    @ArchTest
    static final ArchRule calculadorasImplementanInterfaz = classes()
            .that().resideInAPackage("..procesamiento.calculo..")
            .and().haveSimpleNameStartingWith("Calculadora")
            .and().areNotInterfaces()
            .should().implement(CalculadoraReembolso.class)
            .because("SelectorCalculadora solo descubre estrategias que cumplen el contrato CalculadoraReembolso");

    // Regla 4: los notificadores implementan ObservadorCambioEstado
    @ArchTest
    static final ArchRule notificadoresImplementanInterfaz = classes()
            .that().resideInAPackage("..procesamiento.notificacion..")
            .and().areNotInterfaces()
            .should().implement(ObservadorCambioEstado.class)
            .because("el servicio solo notifica a los beans que implementan ObservadorCambioEstado");
}
