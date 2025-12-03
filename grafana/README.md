# Dashboard de Grafana - Microservicio Inventario EP3

## 📊 Descripción

Este dashboard proporciona observabilidad completa del microservicio, cumpliendo con **IE3 (10%)** de la evaluación.

## 🎯 Métricas Incluidas

### Métricas de Rendimiento
- **HTTP Requests por Segundo**: Monitoreo de tráfico en tiempo real
- **Tiempo de Respuesta (P95)**: Latencia del 95% de las peticiones
- **Uso de CPU**: CPU del sistema y del proceso Java
- **Uso de Memoria**: Heap memory utilizada y máxima

### Métricas de Calidad
- **Errores HTTP (5xx)**: Tasa de errores del servidor
- **Errores Registrados**: Logs de nivel ERROR
- **Health Status**: Estado de salud de la aplicación

### Métricas del Pipeline CI/CD
- **Tiempo de Despliegue**: Duración del último deployment
- **Cobertura de Pruebas**: Porcentaje de código cubierto por tests

### Métricas de JVM
- **Threads Activos**: Threads vivos y daemon
- **Garbage Collection**: Tiempo dedicado a GC

## 🚀 Cómo Usar

### Opción 1: Grafana Cloud (Recomendado para demo)

1. Crear cuenta gratuita en https://grafana.com/
2. Ir a **Connections → Add new connection → Prometheus**
3. Configurar Prometheus endpoint (del cluster Kubernetes)
4. Ir a **Dashboards → Import**
5. Cargar el archivo `dashboard-microservicio.json`

### Opción 2: Grafana en Kubernetes
```bash
# Instalar Grafana con Helm
helm repo add grafana https://grafana.github.io/helm-charts
helm install grafana grafana/grafana

# Obtener password de admin
kubectl get secret grafana -o jsonpath="{.data.admin-password}" | base64 --decode

# Port forward
kubectl port-forward svc/grafana 3000:80
```

Acceder a http://localhost:3000

### Opción 3: Docker Compose Local
```yaml
version: '3.8'
services:
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
  
  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
```

## 📈 Interpretación de Métricas

### Umbrales Recomendados
- **Tiempo de Respuesta P95**: < 200ms (óptimo), < 500ms (aceptable)
- **Errores 5xx**: < 1% del total de requests
- **Uso de CPU**: < 70% (normal), > 85% (investigar)
- **Heap Memory**: < 80% del máximo

### Alertas Sugeridas
- Error rate > 5% durante 5 minutos
- Response time P95 > 1 segundo
- Memory usage > 90%
- Application down (health check failed)

## 🔗 Integración con Pipeline CI/CD

El dashboard muestra métricas del pipeline a través de:
1. **Prometheus** scrapeando `/actuator/prometheus`
2. **GitHub Actions** exponiendo métricas de build/deploy
3. **SonarCloud** para métricas de cobertura

## 📸 Screenshots Recomendados para la Entrega

Captura pantallas de:
1. Dashboard completo con todas las métricas
2. Gráfico de HTTP Requests mostrando actividad
3. Métricas de CPU/Memoria durante carga
4. Health status en verde (aplicación funcionando)

## ✅ Cumplimiento de IE3

Este dashboard cumple con el indicador de evaluación:

> **IE3 (10%)**: Crea dashboards con métricas como tiempo de despliegue, cobertura de pruebas, uso de CPU/memoria, y errores registrados, integrados en el proceso CI/CD.

**Métricas implementadas:**
- ✅ Tiempo de despliegue
- ✅ Cobertura de pruebas
- ✅ Uso de CPU
- ✅ Uso de memoria
- ✅ Errores registrados
- ✅ Integración con CI/CD (via Prometheus)