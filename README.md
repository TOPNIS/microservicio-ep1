# Microservicio de Inventario - Ingeniería DevOps

Repositorio del microservicio desarrollado para las evaluaciones parciales del curso **Ingeniería DevOps (DOY0101)**.

---

## 🎯 Proyecto

Microservicio REST para gestión de inventario de productos, implementado con Spring Boot 3.5 y Oracle Database, con observabilidad completa, pipeline CI/CD automatizado y despliegue en Kubernetes.

---

## 📊 Evaluaciones Completadas

### ✅ EP1 - Fundamentos de DevOps (30%)
- [x] Repositorio en GitHub con estructura del microservicio
- [x] Estrategia de ramificación (`main` y ramas de trabajo)
- [x] Flujo CI/CD básico con GitHub Actions
- [x] Validaciones en entidad Producto
- [x] Documentación de endpoints
- [x] Release v0.1.0 publicado

### ✅ EP2 - Pipeline CI/CD Avanzado (30%)
- [x] Build automático con Maven
- [x] Tests unitarios con H2 en memoria
- [x] Reportes de cobertura con JaCoCo
- [x] Análisis de código con SonarCloud
- [x] Quality Gate configurado

### ✅ EP3 - Observabilidad y Entornos Reales (30%)
- [x] **IE1 (20%)**: Monitoreo con Actuator y Prometheus
- [x] **IE2 (20%)**: Despliegue en Kubernetes
- [x] **IE3 (10%)**: Dashboard de Grafana con métricas clave
- [x] **IE4 (10%)**: Documentación completa
- [x] **IE5 (20%)**: Branch Protection Rules y SonarCloud
- [x] **IE6 (20%)**: Pipeline gates de seguridad (se detiene ante fallas)

---

## 🚀 Características Técnicas

### Stack Tecnológico
- **Backend**: Java 21, Spring Boot 3.5.5
- **Base de Datos**: Oracle 19c (producción), H2 (tests)
- **Migraciones**: Flyway
- **Observabilidad**: Actuator, Prometheus, Grafana
- **Orquestación**: Kubernetes
- **CI/CD**: GitHub Actions
- **Calidad de Código**: SonarCloud, JaCoCo, OWASP Dependency Check

### Arquitectura
```
┌─────────────────────┐
│   GitHub Actions    │  ← CI/CD Pipeline
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│    SonarCloud       │  ← Quality Gate
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│   Docker Image      │  ← ghcr.io/topnis/microservicio-inventario
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│    Kubernetes       │  ← Deployment en cluster
│  - Deployment       │
│  - Service          │
│  - ConfigMap        │
│  - Secret           │
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│    Prometheus       │  ← Scraping de métricas
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│      Grafana        │  ← Visualización
└─────────────────────┘
```

---

## 📋 Endpoints Principales

### Health & Metrics
```
GET /actuator/health          - Estado de la aplicación
GET /actuator/info            - Información de la app
GET /actuator/prometheus      - Métricas en formato Prometheus
GET /actuator/metrics         - Métricas detalladas
```

### API REST
```
GET    /api/productos         - Listar productos
GET    /api/productos/{id}    - Obtener producto
POST   /api/productos         - Crear producto
PUT    /api/productos/{id}    - Actualizar producto
DELETE /api/productos/{id}    - Eliminar producto
```

---

## 🔧 Ejecución Local

### Prerequisitos
- Java 21
- Maven 3.9+
- Oracle 19c (o Docker con imagen de Oracle)

### Configuración
```bash
# Clonar repositorio
git clone https://github.com/TOPNIS/microservicio-ep1.git
cd microservicio-ep1

# Configurar base de datos en application.properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/XEPDB1
spring.datasource.username=INV_USER
spring.datasource.password=InvUser_123

# Ejecutar
mvn spring-boot:run
```

La aplicación estará disponible en: http://localhost:8081

---

## 🐳 Despliegue con Docker

### Build de imagen
```bash
docker build -t microservicio-inventario:latest .
```

### Ejecutar contenedor
```bash
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:oracle:thin:@//host.docker.internal:1521/XEPDB1 \
  -e SPRING_DATASOURCE_USERNAME=INV_USER \
  -e SPRING_DATASOURCE_PASSWORD=InvUser_123 \
  microservicio-inventario:latest
```

---

## ☸️ Despliegue en Kubernetes

### Aplicar manifiestos
```bash
# Opción 1: Aplicar todos los archivos
kubectl apply -k k8s/

# Opción 2: Aplicar individualmente
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/servicemonitor.yaml
```

### Verificar despliegue
```bash
kubectl get pods
kubectl get svc
kubectl logs -f deployment/microservicio-inventario
```

---

## 📊 Observabilidad

### Dashboard de Grafana
Ver documentación completa en [`grafana/README.md`](grafana/README.md)

**Métricas incluidas:**
- HTTP Requests por segundo
- Tiempo de respuesta (P95)
- Uso de CPU y Memoria
- Errores HTTP 5xx
- Threads JVM y Garbage Collection
- Tiempo de despliegue del pipeline
- Cobertura de pruebas

### Acceder a métricas localmente
```bash
# Métricas Prometheus
curl http://localhost:8081/actuator/prometheus

# Health check
curl http://localhost:8081/actuator/health
```

---

## 🔄 Pipeline CI/CD

### Flujo Automatizado
1. **Build and Test** → Compilación y tests unitarios
2. **Security Analysis** → SonarCloud Quality Gate
3. **Dependency Check** → OWASP para vulnerabilidades
4. **Docker Build** → Construcción de imagen (solo en main)
5. **Notification** → Resumen de estado

### Gates de Seguridad
- ❌ Pipeline se **detiene** si fallan los tests
- ❌ Pipeline se **detiene** si falla el Quality Gate
- ❌ Pipeline se **detiene** si hay vulnerabilidades críticas (CVSS ≥ 7)
- ✅ Build de Docker **solo** si pasan todas las validaciones

---

## 📈 Métricas de Calidad

### SonarCloud
- **Quality Gate**: ✅ Passed
- **Issues**: 0 nuevos
- **Security Hotspots**: 0
- **Coverage**: Medida con JaCoCo

Ver análisis completo: https://sonarcloud.io/project/overview?id=TOPNIS_microservicio-ep1

### Branch Protection
- Requiere Pull Request para merge a `main`
- Requiere 1 aprobación
- Requiere status checks pasando
- No permite bypass de las reglas

---

## 📚 Documentación Adicional

- [`grafana/README.md`](grafana/README.md) - Configuración de Grafana y dashboards
- [`k8s/`](k8s/) - Manifiestos de Kubernetes
- [`.github/workflows/`](.github/workflows/) - Configuración del pipeline CI/CD

---

## 🔗 Enlaces Importantes

- **Repositorio**: https://github.com/TOPNIS/microservicio-ep1
- **Pipeline**: https://github.com/TOPNIS/microservicio-ep1/actions
- **SonarCloud**: https://sonarcloud.io/project/overview?id=TOPNIS_microservicio-ep1

---

## 👥 Autor

**Junior Altidor**  
Estudiante de Ingeniería DevOps - DuocUC

---

## 📄 Licencia

Este proyecto es parte de una evaluación académica del curso DOY0101 - Ingeniería DevOps.