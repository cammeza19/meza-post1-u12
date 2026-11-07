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