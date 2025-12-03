# Reflexión Personal y Declaración de Uso de IA

**Estudiante:** Junior Altidor  
**RUT:** 26330161-7 
**Asignatura:** Ingeniería DevOps (DOY0101)  
**Evaluación:** Parcial N°3 - Observabilidad y Entornos Reales  
**Fecha:** Diciembre 2025

---

## 📋 Declaración de Uso de Inteligencia Artificial

Según lo requerido en la rúbrica de evaluación, declaro el uso de herramientas de Inteligencia Artificial en este proyecto:

### Herramientas de IA Utilizadas

**1. Claude (Anthropic)**
- **Propósito:** Asistencia en la generación de código, configuración de manifiestos de Kubernetes, y estructuración de documentación técnica.
- **Uso específico:**
  - Generación de manifiestos YAML para Kubernetes (deployment, service, configmap, secret, servicemonitor, kustomization)
  - Configuración del Dockerfile multi-stage optimizado
  - Estructuración del dashboard JSON de Grafana
  - Redacción y formato de documentación técnica (README.md, EVIDENCIAS_EP3.md)
  - Resolución de errores en el pipeline CI/CD
  - Sugerencias de mejores prácticas en DevOps

**2. GitHub Copilot** (si aplica)
- **Propósito:** Autocompletado de código durante el desarrollo
- **Uso específico:** Sugerencias de código Java y configuraciones YAML

### Validación de Contenidos

**Todo el contenido generado con IA fue:**
- ✅ Revisado línea por línea por mí
- ✅ Validado contra la documentación oficial (Spring Boot, Kubernetes, Prometheus, Grafana)
- ✅ Probado en el entorno de desarrollo
- ✅ Adaptado a las necesidades específicas del proyecto
- ✅ Comprendido en su totalidad antes de ser implementado

### Limitaciones del Uso de IA

**NO se utilizó IA para:**
- ❌ Redactar esta reflexión personal
- ❌ Tomar decisiones arquitectónicas críticas
- ❌ Justificar elecciones técnicas (esas son mías)
- ❌ Generar conclusiones o análisis crítico

### Referencias de Citación

Según las guías de la biblioteca DuocUC para citar IA:
- Anthropic. (2025). Claude [Modelo de lenguaje grande]. https://claude.ai/

---

## 💭 Reflexión Personal

### Mi Aprendizaje en EP3

Esta evaluación ha sido un punto de inflexión en mi comprensión de DevOps. Antes de EP3, veía el pipeline CI/CD como una serie de pasos automatizados, pero ahora comprendo que es un **sistema de observabilidad completo** que permite tomar decisiones técnicas informadas.

**Los conceptos más importantes que aprendí:**

1. **Observabilidad vs Monitoreo:** 
   Aprendí que monitoreo es "saber qué está mal", pero observabilidad es "entender por qué está mal". Los endpoints de Actuator y las métricas de Prometheus me permiten no solo detectar errores, sino rastrear su causa raíz a través de métricas de JVM, memoria, threads y tiempos de respuesta.

2. **Infraestructura como Código (IaC):**
   Antes pensaba que Kubernetes era complicado, pero al crear los manifiestos YAML comprendí el poder de declarar el estado deseado del sistema. Los archivos YAML no son solo configuración, son **documentación ejecutable** que garantiza reproducibilidad.

3. **Gates de Seguridad:**
   Implementar los gates que detienen el pipeline ante fallas críticas me hizo entender que DevOps no es solo velocidad, es **velocidad con control**. El pipeline que se detiene ante un Quality Gate fallido o una vulnerabilidad crítica protege la producción de código defectuoso.

4. **Métricas que Importan:**
   Crear el dashboard de Grafana me enseñó a distinguir entre métricas "interesantes" y métricas "accionables". No basta con tener datos, hay que saber qué umbrales definen un problema real (ej: P95 > 500ms, CPU > 85%, errores 5xx > 1%).

### Desafíos Superados

**Desafío 1: Branch Protection Rules**
Al principio no entendía por qué el push a `main` fallaba. Aprendí que las Branch Protection Rules no son un obstáculo, sino una **salvaguarda profesional** que fuerza el code review y validación antes del merge.

**Desafío 2: SonarCloud Quality Gate**
El pipeline falló varias veces por el Quality Gate. En lugar de "desactivarlo", investigué qué métrica estaba fallando y por qué. Esto me enseñó que las herramientas de calidad son aliadas, no enemigas.

**Desafío 3: Dependency Check con OWASP**
Ver el Dependency Check tardar 27 minutos fue frustrante al inicio, pero comprendí que es el costo necesario para garantizar seguridad. En producción real, este tiempo es insignificante comparado con el riesgo de desplegar vulnerabilidades conocidas.

### Decisiones Técnicas Importantes

**1. Por qué elegí Prometheus sobre CloudWatch:**
Aunque CloudWatch es más simple para AWS, Prometheus es:
- Open source y vendor-neutral
- Mejor integración con Spring Boot Actuator
- Más control sobre las queries (PromQL)
- Ecosistema maduro con Grafana

**2. Por qué usé multi-stage Dockerfile:**
El Dockerfile multi-stage reduce la imagen final de ~400MB a ~200MB al separar las herramientas de build (Maven) del runtime (JRE). Esto mejora:
- Tiempo de pull en Kubernetes
- Seguridad (menos superficie de ataque)
- Costos de storage

**3. Por qué 2 réplicas en Kubernetes:**
Decidí 2 réplicas (no 1, no 3) porque:
- 1 réplica = no hay alta disponibilidad
- 2 réplicas = mínimo para zero-downtime deployments
- 3+ réplicas = overhead innecesario para un microservicio de bajo tráfico

### Conexión con la Industria Real

Este proyecto me hizo investigar cómo se hace DevOps en empresas reales:

- **Netflix:** Usa Spinnaker para deployments en Kubernetes, con gates similares a los que implementé
- **Spotify:** Pioneró el concepto de "observability-driven development"
- **Amazon:** Su regla de "two-pizza teams" se refleja en la arquitectura de microservicios que practicamos

### Habilidades Desarrolladas

**Técnicas:**
- Configuración avanzada de pipelines CI/CD
- Escritura de manifiestos de Kubernetes
- Query de métricas con PromQL
- Análisis de logs y trazabilidad
- Dockerización optimizada

**Blandas:**
- Debugging sistemático (no "trial and error")
- Lectura de documentación técnica oficial
- Toma de decisiones basada en trade-offs
- Persistencia ante errores del pipeline

### Mi Contribución al Proyecto

Aunque usé IA como herramienta, **todas las decisiones técnicas fueron mías:**

- ✅ Decidí la estructura de los manifiestos de Kubernetes
- ✅ Elegí qué métricas incluir en el dashboard
- ✅ Definí los umbrales de alertas
- ✅ Configuré los gates del pipeline
- ✅ Resolví errores iterando sobre las fallas
- ✅ Validé que cada componente funcionara correctamente

La IA fue mi "par de programación", pero yo fui el arquitecto y tomador de decisiones.

### Aprendizaje Más Valioso

**El sistema se detiene cuando debe detenerse.**

En desarrollo tradicional, un error en producción es costoso. En DevOps moderno con observabilidad y gates, el sistema **se auto-protege**. El pipeline que implementé:
- Detecta vulnerabilidades antes del deploy
- Rechaza código que no cumple quality standards
- Expone métricas que permiten decisiones proactivas

Esta filosofía de "fail fast, fail safe" es el corazón de DevOps.

### Próximos Pasos

**Qué me gustaría aprender:**
1. Service Mesh (Istio) para observabilidad avanzada entre microservicios
2. Distributed tracing con Jaeger o Zipkin
3. GitOps con ArgoCD para deployments declarativos
4. Chaos Engineering con Chaos Monkey
5. FinOps para optimización de costos en la nube

### Conclusión

EP3 transformó mi entendimiento de DevOps de "automatizar deployments" a "crear sistemas observables, seguros y auto-validados". Cada línea de YAML, cada métrica en Grafana, y cada gate en el pipeline tiene un propósito: **garantizar que lo que llega a producción es confiable**.

Agradezco especialmente los errores del pipeline, porque cada fallo fue una oportunidad de aprender cómo se comporta el sistema bajo estrés. En producción real, estos errores costarían dinero y reputación. En este entorno académico, fueron invaluables lecciones.

---

**Firma:**  
Junior Altidor  


---

## 📚 Referencias Consultadas

1. Spring Boot Actuator Documentation. (2025). Spring Framework. https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html

2. Prometheus Documentation. (2025). Prometheus. https://prometheus.io/docs/

3. Kubernetes Documentation. (2025). Kubernetes. https://kubernetes.io/docs/

4. Grafana Dashboards. (2025). Grafana Labs. https://grafana.com/docs/

5. OWASP Dependency Check. (2025). OWASP Foundation. https://owasp.org/www-project-dependency-check/

6. SonarCloud Documentation. (2025). SonarSource. https://docs.sonarcloud.io/

7. Burns, B., Beda, J., & Hightower, K. (2019). *Kubernetes: Up and Running* (2nd ed.). O'Reilly Media.

8. Kim, G., Debois, P., Willis, J., & Humble, J. (2016). *The DevOps Handbook*. IT Revolution Press.