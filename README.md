# Sistema de Solicitudes de Reembolso de Gastos Corporativos
**Proyecto Integrador — Unidad 12: Integración de Patrones y Arquitecturas**

## Diseño Propuesto

### RF1 — Validación en varias etapas
* **Punto de variabilidad:** La secuencia y cantidad de reglas de validación independientes que deben cumplirse antes de autorizar una solicitud, con previsión de añadir nuevas verificaciones futuras (como topes acumulados anuales) sin alterar las existentes.
* **Patrón de diseño:** **Chain of Responsibility (Cadena de Responsabilidad)**.
* **Justificación:** Permite desacoplar el emisor de la solicitud de sus validadores concretos, encadenando eslabones autónomos que deciden individualmente si aprueban o detienen la evaluación sin que ninguno conozca la estructura global de la cadena.

---

### RF2 — Cálculo distinto por categoría de gasto
* **Punto de variabilidad:** El algoritmo de liquidación del monto aprobado según el tipo de gasto (viáticos, representación, capacitación), el cual variará a medida que Finanzas incorpore nuevas categorías.
* **Patrón de diseño:** **Strategy (Estrategia) + Factory/Selector de estrategias**.
* **Justificación:** Encapsula cada fórmula de liquidación en clases intercambiables que implementan un contrato común, permitiendo incorporar nuevas categorías de gasto simplemente registrando un nuevo componente de Spring sin modificar las clases de cálculo existentes ni introducir estructuras condicionales rígidas.

---

### RF3 — Notificación simultánea a varios interesados
* **Punto de variabilidad:** La multiplicidad y heterogeneidad de receptores (empleado, contabilidad, auditoría) que deben reaccionar ante una transición de estado, con la expectativa de sumar nuevos suscriptores (como paneles de analítica).
* **Patrón de diseño:** **Observer (Observador)**.
* **Justificación:** Establece un mecanismo de suscripción desacoplado de uno a muchos donde el servicio de reembolsos difunde los cambios de estado sin conocer la identidad ni las acciones específicas que ejecuta cada parte interesada.

## Decisiones de Diseño

### Decisión de Diseño 1: Orden de ejecución en la Cadena de Validadores
* **Contexto:** Se deben encadenar los validadores de documentos soporte, monto máximo y solicitudes duplicadas.
* **Elección:** Se ordenan de menor a mayor costo computacional: primero `ValidadorDocumentosSoporte` (verificación de un booleano en memoria O(1)), segundo `ValidadorMontoMaximo` (comparación numérica con un mapa O(1)), y al final `ValidadorDuplicados` (recorrido de colección O(N)).
* **Justificación de eficiencia (Fail-Fast):** Al posicionar las validaciones más económicas y frecuentes al inicio, se descartan solicitudes inválidas de inmediato, evitando iterar sobre la colección de solicitudes pendientes en memoria. Si se ubicara la verificación de duplicados de primera, pagaríamos el costo de recorrer la lista incluso para solicitudes trivialmente rechazadas por carecer de documentos soporte.

### Decisión de Diseño 2: Mecanismo de selección de estrategias (Inyección Spring vs. Switch Manual)
* **Contexto:** Se requiere despachar la solicitud a la estrategia de cálculo correspondiente según `CategoriaGasto`, considerando que Finanzas agregará nuevas categorías en semestres posteriores.
* **Elección:** Se adopta un `Map` resuelto automáticamente por Spring mediante la inyección del listado `List<CalculadoraReembolso>` en el constructor de `SelectorCalculadora`, descartando un condicional `switch` manual.
* **Justificación de extensibilidad (Principio Open/Closed):** Con el mapa inyectado, añadir una nueva categoría (ejemplo: "BIENESTAR") solo requiere crear una clase anotada con `@Component` que implemente `CalculadoraReembolso`; Spring la descubre e inyecta sin necesidad de modificar ni una sola línea del selector. En contraste, un `switch` obligaría a editar código existente cada vez que surja una categoría, violando el OCP y aumentando el riesgo de regresiones.

### Decisión de Diseño 4: Distinción entre anotaciones de estereotipo y dependencias técnicas en ArchUnit
* **Contexto:** La Regla 1 prohíbe que el paquete `procesamiento` dependa de `infraestructura`, `adaptadores`, `org.springframework.data..` y `jakarta.persistence..`, pero permite anotaciones de Spring como `@Component`.
* **Justificación técnica:** Las anotaciones de estereotipo como `@Component` son metadatos puramente declarativos para la Inyección de Dependencias (IoC), lo que permite que Spring ensamble automáticamente los validadores, calculadoras y observadores sin acoplarlos a infraestructura pesada. Lo que degrada la arquitectura y viola la separación de responsabilidades no es el contenedor de inyección, sino el acoplamiento a tecnologías de persistencia (JPA, SQL) o protocolos web (HTTP, servlets). Prohibir todo Spring forzaría la creación manual de fábricas boilerplate sin aportar valor arquitectónico real.

## Análisis de Impacto: Qué se descartó por sobreingeniería y por qué

Durante el diseño e implementación del proyecto se evaluaron alternativas más complejas que fueron descartadas conscientemente para evitar la deuda cognitiva y la complejidad accidental:

### 1. Spring ApplicationEventPublisher en vez de una lista de observadores inyectada
* **Qué se consideró:** Publicar un evento de dominio desacoplado (`SolicitudEstadoCambiadoEvent`) utilizando el bus interno de Spring (`ApplicationEventPublisher` y `@EventListener`).
* **Por qué se descartó:** Con exactamente tres suscriptores fijos y conocidos en tiempo de compilación (empleado, contabilidad y auditoría), la indirección de un bus de eventos no aporta desacoplamiento real adicional frente a la interfaz `ObservadorCambioEstado`. Además, dificulta la trazabilidad y la depuración del flujo al no ser evidente quién consume el evento en el código fuente.
* **Señal futura para reconsiderar:** Si en el futuro los suscriptores requieren ejecutarse de forma asíncrona (`@Async`) o procesarse en hilos separados para no bloquear la respuesta HTTP.

---

### 2. Patrón Visitor o Abstract Factory para exportación de reportes
* **Qué se consideró:** Diseñar una jerarquía de exportadores para generar reportes en formatos PDF, Excel y CSV previendo solicitudes de la gerencia de Finanzas.
* **Por qué se descartó:** Hoy en día no existe un requerimiento real de exportación en múltiples formatos; solo se maneja el formato estructurado JSON en la API REST. Construir abstracciones para formatos hipotéticos viola la **Rule of Three** y el principio **YAGNI**.
* **Señal futura para reconsiderar:** Si al menos dos áreas de negocio solicitan formalmente descargas periódicas de solicitudes en formatos ofimáticos concretos.

---

### 3. Jerarquía tipada de excepciones por cada validador vs. Excepción unificada (Elemento propio)
* **Qué se consideró:** Crear una clase de excepción especializada para cada eslabón de la cadena (`DocumentosSoporteFaltantesException`, `TopeMontoExcedidoException`, `SolicitudDuplicadaException`) extendiendo de una clase abstracta común `ValidacionReembolsoException`.
* **Por qué se descartó:** En el estado actual, el cliente HTTP solo necesita conocer el mensaje descriptivo del rechazo para informar al usuario (`SolicitudInvalidaException(motivoRechazo)`). Crear tres subclases adicionales habría añadido indirección y archivos vacíos sin aportar lógica diferenciada en el manejo de errores (principio **KISS** y evitar *shallow modules*).
* **Señal futura para reconsiderar:** Si el cliente frontend o sistemas integradores requieren códigos de error numéricos/alfanuméricos normalizados o si diferentes tipos de rechazo desencadenan flujos de compensación o reintentos automáticos distintos.